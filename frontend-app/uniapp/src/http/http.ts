import type { IDoubleTokenRes } from '@/api/types/login'
import type { CustomRequestOptions, IResponse } from '@/http/types'
import { nextTick } from 'vue'
import { useTokenStore } from '@/store/token'
import { isDoubleTokenMode } from '@/utils'
import { toLoginPage } from '@/utils/toLoginPage'
import { ResultEnum } from './tools/enum'

// 刷新 token 状态管理
let refreshing = false // 防止重复刷新 token 标识
let taskQueue: (() => void)[] = [] // 刷新 token 请求队列

/**
 * 正在执行的请求信息接口
 */
interface PendingRequest {
  requestTask?: UniApp.RequestTask // 已发起的请求任务，用于调用 abort() 取消
  resolve: (value: unknown) => void // Promise resolve 回调
  reject: (reason?: unknown) => void // Promise reject 回调
  options: CustomRequestOptions // 请求配置
}

// 请求队列映射表，key为请求唯一标识（method + url + query + data）
const pendingRequests = new Map<string, PendingRequest>()

// 记录请求完成时间，用于防重复提交（即使请求已完成，短时间内也不允许重复请求）
const requestTimestamps = new Map<string, number>()

// 最小请求间隔时间（毫秒），相同请求在此时间内只能发起一次
const MIN_REQUEST_INTERVAL = 300

/**
 * 生成请求唯一标识
 * @param options 请求配置
 * @returns 请求标识字符串
 */
function generateRequestKey(options: CustomRequestOptions): string {
  const { method = 'GET', url, query, data } = options
  // 只使用 method、url、query、data 生成标识，不考虑 header
  const queryStr = query ? JSON.stringify(query) : ''
  const dataStr = data ? JSON.stringify(data) : ''
  return `${method}:${url}:${queryStr}:${dataStr}`
}

/**
 * 执行实际的请求
 * @param options 请求配置
 * @param resolve Promise resolve 回调
 * @param reject Promise reject 回调
 * @param requestKey 请求标识，用于存储 requestTask
 * @returns requestTask 请求任务对象
 */
function executeRequest<T>(options: CustomRequestOptions, resolve: (value: T) => void, reject: (reason?: unknown) => void, requestKey: string): UniApp.RequestTask {
  const requestTask = uni.request({
    ...options,
    dataType: 'json',
    // #ifndef MP-WEIXIN
    responseType: 'json',
    // #endif
    success: async (res) => {
      // 请求完成后从队列中移除，并记录请求时间戳
      pendingRequests.delete(requestKey)
      requestTimestamps.set(requestKey, Date.now())

      const responseData = res.data as IResponse<T>
      const { code } = responseData

      // 检查是否是401错误（包括HTTP状态码401或业务码401）
      const isTokenExpired = res.statusCode === 401 || code === 401

      if (isTokenExpired) {
        const tokenStore = useTokenStore()
        if (!isDoubleTokenMode) {
          // 未启用双token策略，清理用户信息，跳转到登录页
          tokenStore.logout()
          toLoginPage()
          return reject(res)
        }

        /* -------- 无感刷新 token ----------- */
        const { refreshToken } = tokenStore.tokenInfo as IDoubleTokenRes || {}
        // token 失效的，且有刷新 token 的，才放到请求队列里
        if (refreshToken) {
          taskQueue.push(() => {
            executeRequest<T>(options, resolve, reject, requestKey)
          })
        }

        // 如果有 refreshToken 且未在刷新中，发起刷新 token 请求
        if (refreshToken && !refreshing) {
          refreshing = true
          try {
            // 发起刷新 token 请求（使用 store 的 refreshToken 方法）
            await tokenStore.refreshToken()
            // 刷新 token 成功
            refreshing = false
            nextTick(() => {
              // 关闭其他弹窗
              uni.hideToast()
              uni.showToast({
                title: 'token 刷新成功',
                icon: 'none',
              })
            })
            // 将任务队列的所有任务重新请求
            taskQueue.forEach(task => task())
          }
          catch (refreshErr) {
            console.error('刷新 token 失败:', refreshErr)
            refreshing = false
            // 刷新 token 失败，跳转到登录页
            nextTick(() => {
              // 关闭其他弹窗
              uni.hideToast()
              uni.showToast({
                title: '登录已过期，请重新登录',
                icon: 'none',
              })
            })
            // 清除用户信息
            await tokenStore.logout()
            // 跳转到登录页
            setTimeout(() => {
              toLoginPage()
            }, 2000)
          }
          finally {
            // 不管刷新 token 成功与否，都清空任务队列
            taskQueue = []
          }
        }

        return reject(res)
      }

      // 处理其他成功状态（HTTP状态码200-299）
      if (res.statusCode >= 200 && res.statusCode < 300) {
        // 处理业务逻辑错误
        if (code !== ResultEnum.Success0 && code !== ResultEnum.Success200) {
          uni.showToast({
            icon: 'none',
            title: responseData.msg || responseData.message || '请求错误',
          })
          return reject(responseData.data)
        }
        return resolve(responseData.data)
      }

      // 处理其他错误
      !options.hideErrorToast
      && uni.showToast({
        icon: 'none',
        title: (res.data as any).msg || '请求错误',
      })
      reject(res)
    },
    fail(err) {
      // 请求失败后从队列中移除，并记录请求时间戳
      pendingRequests.delete(requestKey)
      requestTimestamps.set(requestKey, Date.now())

      // 如果是主动取消，不显示错误提示
      if ((err as any).errMsg?.includes('abort')) {
        return reject(new Error('请求已被取消'))
      }

      uni.showToast({
        icon: 'none',
        title: '网络错误，换个网络试试',
      })
      reject(err)
    },
  })

  return requestTask
}

/**
 * 封装 uni.request，支持防重复提交
 * 点击立即请求，如果相同请求正在进行中或刚刚完成，后续请求直接返回"请勿重复请求"错误
 * @param options 请求配置
 * @returns Promise 响应数据
 */
export function http<T>(options: CustomRequestOptions) {
  return new Promise<T>((resolve, reject) => {
    const requestKey = generateRequestKey(options)

    // 检查是否在最小间隔时间内重复请求
    const lastRequestTime = requestTimestamps.get(requestKey)
    const now = Date.now()
    if (lastRequestTime && now - lastRequestTime < MIN_REQUEST_INTERVAL) {
      uni.showToast({
        icon: 'none',
        title: '请勿重复请求',
      })
      return reject(new Error('请勿重复请求'))
    }

    // 如果相同请求正在进行中，直接返回"请勿重复请求"错误
    if (pendingRequests.has(requestKey)) {
      uni.showToast({
        icon: 'none',
        title: '请勿重复请求',
      })
      return reject(new Error('请勿重复请求'))
    }

    // 先存储请求信息到队列（标记为正在进行），再执行请求
    pendingRequests.set(requestKey, {
      resolve,
      reject,
      options,
    })

    // 执行请求
    const requestTask = executeRequest<T>(options, resolve, reject, requestKey)

    // 更新请求任务引用
    const pendingInfo = pendingRequests.get(requestKey)
    if (pendingInfo) {
      pendingInfo.requestTask = requestTask
    }
  })
}

/**
 * GET 请求
 * @param url 后台地址
 * @param query 请求query参数
 * @param header 请求头，默认为json格式
 * @returns
 */
export function httpGet<T>(url: string, query?: Record<string, any>, header?: Record<string, any>, options?: Partial<CustomRequestOptions>) {
  return http<T>({
    url,
    query,
    method: 'GET',
    header,
    ...options,
  })
}

/**
 * POST 请求
 * @param url 后台地址
 * @param data 请求body参数
 * @param query 请求query参数，post请求也支持query，很多微信接口都需要
 * @param header 请求头，默认为json格式
 * @returns
 */
export function httpPost<T>(url: string, data?: Record<string, any>, query?: Record<string, any>, header?: Record<string, any>, options?: Partial<CustomRequestOptions>) {
  return http<T>({
    url,
    query,
    data,
    method: 'POST',
    header,
    ...options,
  })
}
/**
 * PUT 请求
 */
export function httpPut<T>(url: string, data?: Record<string, any>, query?: Record<string, any>, header?: Record<string, any>, options?: Partial<CustomRequestOptions>) {
  return http<T>({
    url,
    data,
    query,
    method: 'PUT',
    header,
    ...options,
  })
}

/**
 * DELETE 请求（无请求体，仅 query）
 */
export function httpDelete<T>(url: string, query?: Record<string, any>, header?: Record<string, any>, options?: Partial<CustomRequestOptions>) {
  return http<T>({
    url,
    query,
    method: 'DELETE',
    header,
    ...options,
  })
}

// 支持与 axios 类似的API调用
http.get = httpGet
http.post = httpPost
http.put = httpPut
http.delete = httpDelete

// 支持与 alovaJS 类似的API调用
http.Get = httpGet
http.Post = httpPost
http.Put = httpPut
http.Delete = httpDelete
