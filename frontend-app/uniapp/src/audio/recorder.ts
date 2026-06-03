// #ifdef APP-PLUS
import { AppRecorder } from './app-recorder'
// #endif
import { arrayBufferToBase64 } from '@/utils/audioBuffer'

/** 录音器配置 */
export interface RecorderOptions {
  /** 采样率，默认 16000 */
  sampleRate: number
  /** 帧大小（字节），默认 640 */
  frameSize: number
  /** 音频帧回调（Base64 PCM） */
  onAudioFrame: (pcmBase64: string) => void
  /** 音量级别回调（0-1 归一化值），用于波形动画 */
  onAudioLevel?: (level: number) => void
  /** 错误回调 */
  onError: (error: Error) => void
}

/** 录音器统一接口 */
export interface IRecorder {
  start(options: RecorderOptions): Promise<void>
  stop(): void
  isRecording(): boolean
  destroy(): void
}

/** 录音器工厂 —— 按平台分发实现 */
export class RecorderFactory {
  /** @param pageInstance APP 平台需要传入当前页面/组件实例 */
  static create(pageInstance?: any): IRecorder {
    // #ifdef H5
    return new H5Recorder()
    // #endif
    // #ifdef APP-PLUS
    return new AppRecorder(pageInstance)
    // #endif
    // #ifdef MP-WEIXIN
    return new MpRecorder()
    // #endif
    // 默认兜底
    return new H5Recorder()
  }
}

// ============ H5 录音实现 ============

class H5Recorder implements IRecorder {
  private stream: MediaStream | null = null
  private audioContext: AudioContext | null = null
  private scriptNode: ScriptProcessorNode | null = null
  private recording = false

  async start(options: RecorderOptions): Promise<void> {
    // H5 需要用户交互后才能获取媒体权限
    this.stream = await navigator.mediaDevices.getUserMedia({
      audio: {
        sampleRate: options.sampleRate,
        channelCount: 1,
        echoCancellation: true,
        noiseSuppression: true,
        autoGainControl: true,
      },
    })

    this.audioContext = new (window.AudioContext || (window as any).webkitAudioContext)({
      sampleRate: options.sampleRate,
    })

    const source = this.audioContext.createMediaStreamSource(this.stream)
    // ScriptProcessorNode: bufferSize=1024, 1 input channel, 1 output channel
    this.scriptNode = this.audioContext.createScriptProcessor(1024, 1, 1)

    // 残留的 PCM 数据缓冲区（用于拼帧）
    let residual = new Float32Array(0)

    this.scriptNode.onaudioprocess = (event) => {
      if (!this.recording) return
      try {
        const inputData = event.inputBuffer.getChannelData(0)
        // 合并残留数据
        const combined = new Float32Array(residual.length + inputData.length)
        combined.set(residual, 0)
        combined.set(inputData, residual.length)

        const frameSamples = options.frameSize / 2 // 640 bytes = 320 samples (Int16)
        let offset = 0
        while (offset + frameSamples <= combined.length) {
          const frame = combined.slice(offset, offset + frameSamples)
          const int16 = float32ToInt16(frame)
          const base64 = arrayBufferToBase64(int16.buffer)
          options.onAudioFrame(base64)
          offset += frameSamples
        }
        // 剩余不足一帧的数据保留到下次
        residual = combined.slice(offset)
      }
      catch (e) {
        options.onError(e as Error)
      }
    }

    source.connect(this.scriptNode)
    this.scriptNode.connect(this.audioContext.destination) // 本地监听（可选）
    this.recording = true
  }

  stop(): void {
    this.recording = false
    this.scriptNode?.disconnect()
    this.scriptNode = null
    this.stream?.getTracks().forEach(t => t.stop())
    this.stream = null
    this.audioContext?.close()
    this.audioContext = null
  }

  isRecording(): boolean {
    return this.recording
  }

  destroy(): void {
    this.stop()
  }
}

// ============ 小程序录音实现（RecorderManager + onFrameRecorded） ============

class MpRecorder implements IRecorder {
  private recorder: UniApp.RecorderManager | null = null
  private recording = false

  async start(options: RecorderOptions): Promise<void> {
    this.recorder = uni.getRecorderManager()

    this.recorder.onFrameRecorded((res: any) => {
      if (!this.recording) return
      try {
        const base64 = arrayBufferToBase64(res.frameBuffer)
        options.onAudioFrame(base64)
      }
      catch (e) {
        // onFrameRecorded 不可用时静默降级
      }
    })

    this.recorder.onError((err) => {
      options.onError(new Error(err.errMsg || '录音错误'))
    })

    this.recorder.start({
      duration: 600000,
      sampleRate: options.sampleRate,
      numberOfChannels: 1,
      encodeBitRate: 48000,
      format: 'pcm' as any,
      frameSize: options.frameSize,
    })
    this.recording = true
  }

  stop(): void {
    this.recording = false
    this.recorder?.stop()
    this.recorder = null
  }

  isRecording(): boolean {
    return this.recording
  }

  destroy(): void {
    this.stop()
  }
}

// ============ 工具函数 ============

/**
 * Float32 PCM 样本 → Int16 PCM 样本
 * 归一化到 [-32768, 32767]
 */
function float32ToInt16(float32Array: Float32Array): Int16Array {
  const len = float32Array.length
  const int16Array = new Int16Array(len)
  for (let i = 0; i < len; i++) {
    const s = Math.max(-1, Math.min(1, float32Array[i]))
    int16Array[i] = s < 0 ? s * 0x8000 : s * 0x7FFF
  }
  return int16Array
}
