import type { ClientMessage, ServerEvent } from './types'
import { ConnectionState } from './types'

/** WebSocket 连接配置 */
export interface WebSocketOptions {
  /** 服务器 WebSocket 地址 */
  url: string
  /** 认证 token，连接时附带 */
  token?: string
  /** 连接成功回调 */
  onOpen?: () => void
  /** 连接关闭回调 */
  onClose?: (code: number, reason: string) => void
  /** 连接错误回调 */
  onError?: (error: Error) => void
  /** 收到服务器消息回调（已解析为 ServerEvent） */
  onMessage?: (event: ServerEvent) => void
  /** 最大重连次数，默认 5 */
  reconnectMaxRetries?: number
  /** 初始重连间隔（毫秒），默认 1000 */
  reconnectInterval?: number
}

/**
 * WebSocket 语音通话连接管理器
 * 使用 uni.connectSocket 实现跨平台 WebSocket 通信
 * 对应安卓示例中 RealtimeViewModel 的 WebSocket 逻辑
 */
export class VoiceWebSocketManager {
  private options: Required<WebSocketOptions>
  private socketTask: UniApp.SocketTask | null = null
  private reconnectCount = 0
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private connectionState: ConnectionState = ConnectionState.IDLE
  private heartbeatTimer: ReturnType<typeof setInterval> | null = null
  /** 最后一次收到消息的时间戳 */
  private lastMessageTime = 0
  private intentionalClose = false
  /** 回调是否已绑定到当前 socketTask */
  private listenersBound = false

  /** 心跳间隔（毫秒） */
  private static readonly HEARTBEAT_INTERVAL = 25000
  /** 连续未收到 pong 的最大次数 */
  private static readonly MAX_HEARTBEAT_MISS = 3
  /** 最大重连次数 */
  private static readonly DEFAULT_MAX_RETRIES = 5
  /** 初始重连间隔（毫秒） */
  private static readonly DEFAULT_RECONNECT_INTERVAL = 1000
  /** 最大重连间隔（毫秒） */
  private static readonly MAX_RECONNECT_INTERVAL = 30000

  constructor(options: WebSocketOptions) {
    this.options = {
      url: options.url,
      token: options.token ?? '',
      onOpen: options.onOpen ?? (() => {}),
      onClose: options.onClose ?? (() => {}),
      onError: options.onError ?? (() => {}),
      onMessage: options.onMessage ?? (() => {}),
      reconnectMaxRetries: options.reconnectMaxRetries ?? VoiceWebSocketManager.DEFAULT_MAX_RETRIES,
      reconnectInterval: options.reconnectInterval ?? VoiceWebSocketManager.DEFAULT_RECONNECT_INTERVAL,
    }
  }

  /** 获取当前连接状态 */
  getState(): ConnectionState {
    return this.connectionState
  }

  /** 建立 WebSocket 连接 */
  connect(): void {
    if (this.socketTask) {
      console.warn('[VoiceWS] 已有连接，跳过重复连接')
      return
    }

    this.intentionalClose = false
    this.listenersBound = false
    this.setState(ConnectionState.CONNECTING)

    // 构建带 token 的连接 URL
    const url = this.options.token
      ? `${this.options.url}?token=${encodeURIComponent(this.options.token)}`
      : this.options.url

    console.log('[VoiceWS] 正在连接:', url)

    // uni.connectSocket 跨平台兼容：H5 底层 WebSocket / APP 原生 Socket / 小程序 wx.connectSocket
    this.socketTask = uni.connectSocket({
      url,
      complete: () => {},
    })

    this.bindListeners()
  }

  /** 绑定 SocketTask 事件监听器 */
  private bindListeners(): void {
    if (!this.socketTask || this.listenersBound) return
    this.listenersBound = true

    this.socketTask.onOpen(() => {
      console.log('[VoiceWS] 连接成功')
      this.reconnectCount = 0
      this.setState(ConnectionState.CONNECTED)
      this.startHeartbeat()
      this.options.onOpen()
    })

    this.socketTask.onMessage((res) => {
      this.lastMessageTime = Date.now()
      try {
        const event = JSON.parse(res.data as string) as ServerEvent
        this.options.onMessage(event)
      }
      catch (e) {
        console.error('[VoiceWS] 消息解析失败:', e, '原始数据:', res.data)
      }
    })

    this.socketTask.onClose((res) => {
      console.log('[VoiceWS] 连接关闭, code:', res.code, 'reason:', res.reason)
      this.stopHeartbeat()
      this.socketTask = null
      this.listenersBound = false

      if (!this.intentionalClose) {
        this.options.onClose(res.code, res.reason)
        this.tryReconnect()
      }
      else {
        this.setState(ConnectionState.IDLE)
        this.options.onClose(res.code, res.reason)
      }
    })

    this.socketTask.onError((err) => {
      console.error('[VoiceWS] 连接错误:', err)
      this.options.onError(new Error(err.errMsg || 'WebSocket 连接错误'))

      // onError 之后通常会触发 onClose，通过 onClose 处理重连
      // 但以防万一，也在这里设置错误状态
      if (!this.intentionalClose && this.reconnectCount >= this.options.reconnectMaxRetries) {
        this.setState(ConnectionState.ERROR)
      }
    })
  }

  /** 断开连接（主动关闭，不触发重连） */
  disconnect(): void {
    this.intentionalClose = true
    this.stopHeartbeat()
    this.clearReconnectTimer()

    if (this.socketTask) {
      this.setState(ConnectionState.DISCONNECTING)
      this.socketTask.close({
        code: 1000,
        reason: '客户端主动断开',
      })
      this.socketTask = null
      this.listenersBound = false
    }

    this.setState(ConnectionState.IDLE)
  }

  /** 发送 JSON 消息 */
  send(message: ClientMessage): boolean {
    if (!this.socketTask || this.connectionState !== ConnectionState.CONNECTED) {
      console.warn('[VoiceWS] 未连接，无法发送消息')
      return false
    }

    try {
      this.socketTask.send({
        data: JSON.stringify(message),
      })
      return true
    }
    catch (e) {
      console.error('[VoiceWS] 发送消息失败:', e)
      return false
    }
  }

  /** 发送 Base64 PCM 音频数据块 */
  sendAudioChunk(pcmBase64: string): boolean {
    return this.send({
      type: 'audio.chunk',
      audio: pcmBase64,
    })
  }

  /** 发送文本消息触发 AI 回复 */
  sendText(text: string): boolean {
    const trimmed = text.trim()
    if (!trimmed) return false
    return this.send({
      type: 'audio.end',
      text: trimmed,
    })
  }

  /** 发送打断指令 */
  sendInterrupt(): boolean {
    return this.send({ type: 'interrupt' })
  }

  /** 更新认证 token（用于重连时获取新 token） */
  updateToken(token: string): void {
    this.options.token = token
  }

  /** 销毁实例，释放所有资源 */
  destroy(): void {
    this.disconnect()
    this.options.onOpen = () => {}
    this.options.onClose = () => {}
    this.options.onError = () => {}
    this.options.onMessage = () => {}
  }

  /** 设置连接状态 */
  private setState(state: ConnectionState): void {
    this.connectionState = state
  }

  /** 启动心跳检测（基于消息接收时间，音频流中断时触发重连） */
  private startHeartbeat(): void {
    this.stopHeartbeat()
    this.lastMessageTime = Date.now()
    this.heartbeatTimer = setInterval(() => {
      if (this.connectionState !== ConnectionState.CONNECTED) {
        this.stopHeartbeat()
        return
      }
      // 超过 3 个心跳周期未收到任何消息，认为连接已断开
      const idleTime = Date.now() - this.lastMessageTime
      if (idleTime > VoiceWebSocketManager.HEARTBEAT_INTERVAL * VoiceWebSocketManager.MAX_HEARTBEAT_MISS) {
        console.warn('[VoiceWS] 心跳超时，连接可能已断开')
        this.handleHeartbeatTimeout()
      }
    }, VoiceWebSocketManager.HEARTBEAT_INTERVAL) as unknown as ReturnType<typeof setInterval>
  }

  /** 停止心跳 */
  private stopHeartbeat(): void {
    if (this.heartbeatTimer !== null) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /** 心跳超时处理：关闭当前连接并触发重连 */
  private handleHeartbeatTimeout(): void {
    this.stopHeartbeat()
    this.intentionalClose = false
    if (this.socketTask) {
      this.socketTask.close({
        code: 1006,
        reason: '心跳超时',
      })
      this.socketTask = null
      this.listenersBound = false
    }
    this.tryReconnect()
  }

  /** 尝试自动重连（指数退避） */
  private tryReconnect(): void {
    if (this.intentionalClose) return
    if (this.reconnectCount >= this.options.reconnectMaxRetries) {
      console.error('[VoiceWS] 重连次数已达上限')
      this.setState(ConnectionState.ERROR)
      this.options.onError(new Error('WebSocket 重连失败，已达最大重试次数'))
      return
    }

    // 指数退避 + 随机抖动
    const baseDelay = this.options.reconnectInterval * 2 ** this.reconnectCount
    const maxDelay = VoiceWebSocketManager.MAX_RECONNECT_INTERVAL
    const jitter = Math.random() * 3000
    const delay = Math.min(baseDelay, maxDelay) + jitter

    this.reconnectCount++
    this.setState(ConnectionState.RECONNECTING)
    console.log(`[VoiceWS] 第 ${this.reconnectCount} 次重连，等待 ${Math.round(delay)}ms`)

    this.clearReconnectTimer()
    this.reconnectTimer = setTimeout(() => {
      this.connect()
    }, delay)
  }

  /** 清除重连定时器 */
  private clearReconnectTimer(): void {
    if (this.reconnectTimer !== null) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }
}
