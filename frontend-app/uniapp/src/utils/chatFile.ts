export type ChatFileStatus = 'local' | 'uploading' | 'uploaded' | 'failed'

export interface ChatFileItem {
  id: string
  name: string
  size: number
  ext: string
  localPath: string
  remoteUrl?: string
  mimeType?: string
  status?: ChatFileStatus
  textPreview?: string
}

export interface ChatFilePreviewState {
  files: ChatFileItem[]
  currentId: string
}

export interface PickedChatFile {
  tempFilePath: string
  name: string
  size: number
  ext: string
  mimeType?: string
}

// #ifdef APP-PLUS
import { chooseFile, readTextFile } from '@/uni_modules/dh-choose-file'
// #endif

const TEXT_FILE_EXTENSIONS = ['txt', 'md', 'json', 'csv', 'log']
const OFFICE_FILE_EXTENSIONS = ['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'pdf']
export const CHAT_FILE_PREVIEW_STORAGE_KEY = 'chat-file-preview-state'
export const TEXT_INLINE_PREVIEW_SIZE_LIMIT = 512 * 1024
const APP_FILE_EXTENSIONS = ['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt', 'md', 'json', 'csv', 'log', 'pdf']

export function getFileExt(fileNameOrPath: string) {
  const match = fileNameOrPath.toLowerCase().match(/\.([a-z0-9]+)(?:[?#].*)?$/)
  return match?.[1] ?? ''
}

function getExtFromMimeType(mimeType?: string) {
  if (!mimeType)
    return ''

  const parts = mimeType.toLowerCase().split('/')
  return parts.length > 1 ? parts[parts.length - 1] : mimeType.toLowerCase()
}

export function formatFileSize(size: number) {
  if (!Number.isFinite(size) || size <= 0)
    return '0 B'

  if (size < 1024)
    return `${size} B`
  if (size < 1024 * 1024)
    return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

export function isTextPreviewFile(ext: string) {
  return TEXT_FILE_EXTENSIONS.includes(ext.toLowerCase())
}

export function isLargeTextPreviewFile(size: number) {
  return Number.isFinite(size) && size > TEXT_INLINE_PREVIEW_SIZE_LIMIT
}

export function isOfficePreviewFile(ext: string) {
  return OFFICE_FILE_EXTENSIONS.includes(ext.toLowerCase())
}

export function normalizePickedChatFile(file: {
  tempFilePath: string
  name?: string
  size?: number
  type?: string
}): PickedChatFile {
  const fallbackName = file.tempFilePath.split('/').pop() ?? `file-${Date.now()}`
  const name = file.name || fallbackName
  const ext = getFileExt(name || file.tempFilePath) || getExtFromMimeType(file.type)

  return {
    tempFilePath: file.tempFilePath,
    name,
    size: file.size ?? 0,
    ext,
    mimeType: file.type,
  }
}

export async function readChatTextFile(filePath: string): Promise<string> {
  if (!filePath)
    return ''

  // #ifdef APP-PLUS
  return readTextFile(filePath)
  // #endif

  // #ifdef H5
  try {
    const response = await fetch(filePath)
    return await response.text()
  } catch (error) {
    console.error('读取 H5 文本文件失败:', error)
    return ''
  }
  // #endif

  // #ifndef H5
  return await new Promise((resolve) => {
    uni.getFileSystemManager().readFile({
      filePath,
      encoding: 'utf-8',
      success: (res) => {
        resolve((res.data as string) ?? '')
      },
      fail: () => resolve(''),
    })
  })
  // #endif
}

export async function chooseChatFiles(count = 9): Promise<PickedChatFile[]> {
  // #ifdef APP-PLUS
  return await new Promise((resolve, reject) => {
    chooseFile({
      count,
      extensions: APP_FILE_EXTENSIONS,
      success: (res: {
        files?: Array<{ path?: string, name?: string, size?: number | null, mimeType?: string, extension?: string }>
      }) => {
        const files = (res.files ?? []).map((file) => {
          const tempFilePath = file.path || ''
          const ext = file.extension || getFileExt(file.name || tempFilePath) || getExtFromMimeType(file.mimeType)
          return normalizePickedChatFile({
            tempFilePath,
            name: file.name,
            size: file.size ?? 0,
            type: file.mimeType || ext,
          })
        }).filter(file => !!file.tempFilePath)
        resolve(files)
      },
      fail: reject,
    })
  })
  // #endif

  // #ifdef MP-WEIXIN
  return await new Promise((resolve, reject) => {
    ;(wx as any).chooseMessageFile({
      count,
      type: 'file',
      success: (res: any) => {
        const files = (res.tempFiles ?? []).map((file: any) => normalizePickedChatFile({
          tempFilePath: file.path || file.tempFilePath,
          name: file.name,
          size: file.size,
          type: file.type,
        }))
        resolve(files)
      },
      fail: reject,
    })
  })
  // #endif

  // #ifdef H5
  return await new Promise((resolve, reject) => {
    uni.chooseFile({
      count,
      type: 'all',
      extension: ['.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx', '.txt', '.md', '.json', '.csv', '.log', '.pdf'],
      success: (res: any) => {
        const tempFilePaths = Array.isArray(res.tempFilePaths) ? res.tempFilePaths : []
        const files = (res.tempFiles ?? []).map((file: any, index: number) => normalizePickedChatFile({
          tempFilePath: tempFilePaths[index] || file.path,
          name: file.name,
          size: file.size,
          type: file.type,
        }))
        resolve(files)
      },
      fail: reject,
    })
  })
  // #endif

  // #ifndef MP-WEIXIN
  // #ifndef H5
  uni.showToast({ title: '当前平台暂不支持文件选择', icon: 'none' })
  return []
  // #endif
  // #endif
}

export function createChatFileItem(file: PickedChatFile): ChatFileItem {
  return {
    id: `file-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    name: file.name,
    size: file.size,
    ext: file.ext,
    localPath: file.tempFilePath,
    mimeType: file.mimeType,
    status: 'local',
  }
}

export function saveChatFilePreviewState(state: ChatFilePreviewState) {
  uni.setStorageSync(CHAT_FILE_PREVIEW_STORAGE_KEY, state)
}

export function readChatFilePreviewState() {
  return uni.getStorageSync(CHAT_FILE_PREVIEW_STORAGE_KEY) as ChatFilePreviewState | undefined
}

export function clearChatFilePreviewState() {
  uni.removeStorageSync(CHAT_FILE_PREVIEW_STORAGE_KEY)
}
