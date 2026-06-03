export interface ChatImageUploadResult {
  localPath: string
  url: string
  thumbUrl: string
  mediaId: string
  width: number
  height: number
}

export interface ChatImageUploadSource {
  localPath: string
  width?: number
  height?: number
}

export interface MockChatImageUploadOptions {
  delay?: number
}

function getMockImageSize(source: ChatImageUploadSource) {
  return {
    width: source.width && source.width > 0 ? source.width : 1080,
    height: source.height && source.height > 0 ? source.height : 1440,
  }
}

function buildMockImageUrl(localPath: string) {
  return localPath.startsWith('http') ? localPath : localPath
}

export async function mockUploadChatImage(
  source: ChatImageUploadSource,
  options: MockChatImageUploadOptions = {},
): Promise<ChatImageUploadResult> {
  const delay = options.delay ?? 900
  const { width, height } = getMockImageSize(source)

  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        localPath: source.localPath,
        url: buildMockImageUrl(source.localPath),
        thumbUrl: buildMockImageUrl(source.localPath),
        mediaId: `mock-image-${Date.now()}`,
        width,
        height,
      })
    }, delay)
  })
}

export async function mockInsertTempImageToChat(source: ChatImageUploadSource, options: MockChatImageUploadOptions = {}) {
  const result = await mockUploadChatImage(source, options)
  return {
    ...result,
    url: source.localPath,
    thumbUrl: source.localPath,
  }
}
