/**
 * PCM↔WAV 转换工具，用于 APP/小程序端将 PCM 数据转为可播放的 WAV 临时文件
 */

/**
 * 将 Base64 编码的 PCM 数据转换为 WAV Blob
 * @param pcmBase64 - Base64 编码的 PCM 数据 (Int16LE, mono)
 * @param sampleRate - 采样率，默认 24000
 * @returns WAV 格式 Blob
 */
export function pcmToWavBlob(pcmBase64: string, sampleRate: number = 24000): Blob {
  const pcmData = base64ToArrayBuffer(pcmBase64)
  const wavData = buildWav(pcmData, sampleRate)
  return new Blob([wavData], { type: 'audio/wav' })
}

/**
 * 合并多段 PCM ArrayBuffer 为一个 WAV Blob
 * @param chunks - PCM ArrayBuffer 数组
 * @param sampleRate - 采样率
 * @returns WAV 格式 Blob
 */
export function mergePcmToWavBlob(chunks: ArrayBuffer[], sampleRate: number): Blob {
  const totalLength = chunks.reduce((sum, chunk) => sum + chunk.byteLength, 0)
  const merged = new Uint8Array(totalLength)
  let offset = 0
  for (const chunk of chunks) {
    merged.set(new Uint8Array(chunk), offset)
    offset += chunk.byteLength
  }
  const wavData = buildWav(merged.buffer, sampleRate)
  return new Blob([wavData], { type: 'audio/wav' })
}

/**
 * 将 Blob 转为本地临时文件路径（用于 InnerAudioContext.src）
 * H5 使用 URL.createObjectURL，APP/小程序使用文件系统写入
 * @param blob - 音频 Blob
 * @returns 临时文件路径或 ObjectURL
 */
export async function blobToTempPath(blob: Blob): Promise<string> {
  // #ifdef H5
  return URL.createObjectURL(blob)
  // #endif

  // #ifndef H5
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onloadend = () => {
      const arrayBuffer = reader.result as ArrayBuffer
      // #ifdef MP-WEIXIN
      const mpFilePath = `${wx.env.USER_DATA_PATH}/tts_${Date.now()}_${Math.random().toString(36).slice(2)}.wav`
      const fs = wx.getFileSystemManager()
      fs.writeFile({
        filePath: mpFilePath,
        data: arrayBuffer,
        success: () => resolve(mpFilePath),
        fail: (err: any) => reject(err),
      })
      // #endif
      // #ifdef APP-PLUS
      const appFilePath = `tts_${Date.now()}_${Math.random().toString(36).slice(2)}.wav`
      plus.io.requestFileSystem(
        1,
        (fs: any) => {
          fs.root.getFile(
            appFilePath,
            { create: true },
            (fileEntry: any) => {
              fileEntry.createWriter(
                (writer: any) => {
                  writer.onwrite = () => resolve(fileEntry.fullPath)
                  writer.onerror = (e: any) => reject(e)
                  writer.write(arrayBuffer)
                },
                (e: any) => reject(e),
              )
            },
            (e: any) => reject(e),
          )
        },
        (e: any) => reject(e),
      )
      // #endif
    }
    reader.onerror = () => reject(new Error('读取 Blob 失败'))
    reader.readAsArrayBuffer(blob)
  })
  // #endif
}

/**
 * 构建 WAV 文件二进制数据
 * WAV 格式: RIFF header + fmt chunk + data chunk
 */
function buildWav(pcmData: ArrayBuffer, sampleRate: number): ArrayBuffer {
  const numChannels = 1
  const bitsPerSample = 16
  const byteRate = sampleRate * numChannels * bitsPerSample / 8
  const blockAlign = numChannels * bitsPerSample / 8
  const dataSize = pcmData.byteLength
  const headerSize = 44
  const buffer = new ArrayBuffer(headerSize + dataSize)
  const view = new DataView(buffer)

  // RIFF 头
  writeString(view, 0, 'RIFF')
  view.setUint32(4, 36 + dataSize, true) // 文件大小 - 8
  writeString(view, 8, 'WAVE')

  // fmt chunk
  writeString(view, 12, 'fmt ')
  view.setUint32(16, 16, true) // fmt chunk 大小
  view.setUint16(20, 1, true) // PCM 格式
  view.setUint16(22, numChannels, true)
  view.setUint32(24, sampleRate, true)
  view.setUint32(28, byteRate, true)
  view.setUint16(32, blockAlign, true)
  view.setUint16(34, bitsPerSample, true)

  // data chunk
  writeString(view, 36, 'data')
  view.setUint32(40, dataSize, true)

  // PCM 数据
  const pcmView = new Uint8Array(pcmData)
  const outView = new Uint8Array(buffer)
  outView.set(pcmView, 44)

  return buffer
}

/**
 * 在 DataView 指定位置写入字符串
 */
function writeString(view: DataView, offset: number, str: string): void {
  for (let i = 0; i < str.length; i++) {
    view.setUint8(offset + i, str.charCodeAt(i))
  }
}

/**
 * Base64 字符串转 ArrayBuffer
 */
export function base64ToArrayBuffer(base64: string): ArrayBuffer {
  const binaryString = atob(base64)
  const len = binaryString.length
  const bytes = new Uint8Array(len)
  for (let i = 0; i < len; i++) {
    bytes[i] = binaryString.charCodeAt(i)
  }
  return bytes.buffer
}

/**
 * ArrayBuffer 转 Base64 字符串
 */
export function arrayBufferToBase64(buffer: ArrayBufferLike): string {
  const bytes = new Uint8Array(buffer)
  let binary = ''
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i])
  }
  return btoa(binary)
}
