// #ifdef APP-PLUS
import { AppPlayer } from './app-player'
// #endif
import { base64ToArrayBuffer, mergePcmToWavBlob, blobToTempPath } from '@/utils/audioBuffer'

/** 播放器配置 */
export interface PlayerOptions {
  /** 默认采样率，默认 24000 */
  defaultSampleRate: number
  /** 音频块播放回调 */
  onChunkPlayed?: (index: number) => void
  /** 全部播放完毕回调 */
  onPlaybackEnded?: () => void
  /** 错误回调 */
  onError?: (error: Error) => void
}

/** 播放器统一接口 */
export interface IPlayer {
  start(options: PlayerOptions): void
  /** 入队一段 Base64 PCM 音频 */
  enqueuePcm(pcmBase64: string, sampleRate?: number): void
  /** 清空播放队列（打断时使用） */
  clearQueue(): void
  /** 停止播放并释放资源 */
  stop(): void
  isPlaying(): boolean
  destroy(): void
}

/** 播放器工厂 —— 按平台分发实现 */
export class PlayerFactory {
  static create(): IPlayer {
    // #ifdef H5
    return new H5Player()
    // #endif
    // #ifdef APP-PLUS
    return new AppPlayer()
    // #endif
    // #ifdef MP-WEIXIN
    return new NativePlayer()
    // #endif
    // 默认兜底
    return new H5Player()
  }
}

// ============ H5 播放实现（AudioContext 直接播放 PCM） ============

class H5Player implements IPlayer {
  private audioContext: AudioContext | null = null
  private options: PlayerOptions | null = null
  private playing = false
  private queue: Array<{ pcmBase64: string; sampleRate: number }> = []
  private chunkIndex = 0
  private currentSource: AudioBufferSourceNode | null = null

  start(options: PlayerOptions): void {
    this.options = options
    this.playing = true
    this.queue = []
    this.chunkIndex = 0

    // AudioContext 需在用户交互后创建（浏览器 autoplay policy）
    if (!this.audioContext) {
      this.audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
    }
    // 确保 AudioContext 处于运行状态
    if (this.audioContext.state === 'suspended') {
      this.audioContext.resume()
    }
  }

  enqueuePcm(pcmBase64: string, sampleRate?: number): void {
    if (!this.playing) return
    const sr = sampleRate ?? this.options?.defaultSampleRate ?? 24000
    this.queue.push({ pcmBase64, sampleRate: sr })

    // 如果当前没有正在播放的 source，开始播放
    if (!this.currentSource) {
      this.playNext()
    }
  }

  clearQueue(): void {
    this.queue = []
    // 停止当前正在播放的音频
    this.currentSource?.stop()
    this.currentSource?.disconnect()
    this.currentSource = null
  }

  stop(): void {
    this.playing = false
    this.clearQueue()
    this.audioContext?.close()
    this.audioContext = null
  }

  isPlaying(): boolean {
    return this.playing
  }

  destroy(): void {
    this.stop()
  }

  /** 播放队列中的下一个音频块 */
  private playNext(): void {
    if (!this.playing || !this.audioContext || !this.options) return
    if (this.queue.length === 0) {
      this.currentSource = null
      this.options.onPlaybackEnded?.()
      return
    }

    const chunk = this.queue.shift()!
    try {
      // Base64 → ArrayBuffer → Float32 samples
      const pcmBuffer = base64ToArrayBuffer(chunk.pcmBase64)
      const int16View = new Int16Array(pcmBuffer)
      const float32Array = int16ToFloat32(int16View)

      // 创建 AudioBuffer 并填充 PCM 数据
      const length = float32Array.length
      const audioBuffer = this.audioContext.createBuffer(1, length, chunk.sampleRate)
      audioBuffer.getChannelData(0).set(float32Array)

      // 创建 BufferSource 播放
      const source = this.audioContext.createBufferSource()
      source.buffer = audioBuffer
      source.connect(this.audioContext.destination)

      source.onended = () => {
        this.currentSource = null
        this.options?.onChunkPlayed?.(this.chunkIndex++)
        this.playNext()
      }

      this.currentSource = source
      source.start()
    }
    catch (e) {
      this.options.onError?.(e as Error)
      this.playNext() // 跳过错误块，继续播放下一个
    }
  }
}

// ============ APP/小程序播放实现（PCM → WAV 文件 → InnerAudioContext） ============

class NativePlayer implements IPlayer {
  private options: PlayerOptions | null = null
  private playing = false
  private audioContext: UniApp.InnerAudioContext | null = null
  private chunkBuffer: ArrayBuffer[] = []
  private currentTempPath: string | null = null

  /** 累积多少帧后写一次文件（约 200ms @ 24000Hz） */
  private static readonly BUFFER_FRAMES = 10

  start(options: PlayerOptions): void {
    this.options = options
    this.playing = true
    this.chunkBuffer = []
    this.createAudioContext()
  }

  enqueuePcm(pcmBase64: string, _sampleRate?: number): void {
    if (!this.playing) return
    const sr = _sampleRate ?? this.options?.defaultSampleRate ?? 24000
    const pcmBuffer = base64ToArrayBuffer(pcmBase64)
    this.chunkBuffer.push(pcmBuffer)

    // 累积达到阈值时写文件并播放
    if (this.chunkBuffer.length >= NativePlayer.BUFFER_FRAMES) {
      this.flushBuffer(sr)
    }
  }

  clearQueue(): void {
    this.chunkBuffer = []
    this.audioContext?.stop()
    this.cleanupTempFile()
  }

  stop(): void {
    this.playing = false

    // 播放剩余缓冲
    if (this.chunkBuffer.length > 0) {
      const sr = this.options?.defaultSampleRate ?? 24000
      this.flushBuffer(sr)
    }

    this.audioContext?.destroy()
    this.audioContext = null
    this.cleanupTempFile()
  }

  isPlaying(): boolean {
    return this.playing
  }

  destroy(): void {
    this.stop()
  }

  /** 将累积的 PCM 缓冲区冲刷为 WAV 文件并播放 */
  private flushBuffer(sampleRate: number): void {
    if (this.chunkBuffer.length === 0) return
    const chunks = this.chunkBuffer.splice(0)
    try {
      const wavBlob = mergePcmToWavBlob(chunks, sampleRate)
      blobToTempPath(wavBlob).then((path) => {
        if (!this.audioContext || !this.playing) return
        // 如果正在播放中，先等当前播放结束再播下一个
        // InnerAudioContext 简化处理：直接覆盖播放
        this.audioContext.src = path
        this.audioContext.play()
        // 清理之前的临时文件
        this.cleanupTempFile()
        this.currentTempPath = path
      }).catch((e) => {
        this.options?.onError?.(e as Error)
      })
    }
    catch (e) {
      this.options?.onError?.(e as Error)
    }
  }

  private createAudioContext(): void {
    this.audioContext = uni.createInnerAudioContext()
    this.audioContext.onEnded(() => {
      this.options?.onPlaybackEnded?.()
    })
    this.audioContext.onError((err) => {
      this.options?.onError?.(new Error(err.errMsg || '音频播放错误'))
    })
  }

  private cleanupTempFile(): void {
    if (this.currentTempPath) {
      // #ifndef H5
      URL.revokeObjectURL?.(this.currentTempPath)
      // #endif
      this.currentTempPath = null
    }
  }
}

// ============ 工具函数 ============

/**
 * Int16 PCM 样本 → Float32（归一化到 [-1, 1]）
 */
function int16ToFloat32(int16Array: Int16Array): Float32Array {
  const len = int16Array.length
  const float32Array = new Float32Array(len)
  for (let i = 0; i < len; i++) {
    float32Array[i] = int16Array[i] / 32768
  }
  return float32Array
}
