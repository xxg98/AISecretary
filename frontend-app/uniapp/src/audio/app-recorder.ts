import type { IRecorder, RecorderOptions } from './recorder'

// #ifdef APP-PLUS
import Recorder from 'recorder-core'
import RecordApp from 'recorder-core/src/app-support/app'
import '@/uni_modules/Recorder-UniCore/app-uni-support.js'

// 模块顶层一次性配置：启用原生插件 + 逻辑层运行模式
RecordApp.UniNativeUtsPlugin = { nativePlugin: true }
RecordApp.UniWithoutAppRenderjs = true
// 防止 recorder-core 被 tree-shaking 移除
Recorder.a = 1
// #endif

import { arrayBufferToBase64 } from '@/utils/audioBuffer'

/**
 * APP 原生录音实现（RecordApp + Recorder-NativePlugin）
 *
 * 使用 RecordApp.UniWithoutAppRenderjs=true 模式，
 * RecordApp 完全运行在逻辑层，无需 renderjs 模块。
 *
 * 数据流：麦克风 → 原生插件 PCM → onProcess 回调 → Int16 分帧 → Base64 → WebSocket
 */
export class AppRecorder implements IRecorder {
  /** 当前页面/组件实例（供 RecordApp.UniWebViewActivate 使用） */
  private pageInstance: any
  private recording = false
  private sampleRate = 16000
  private frameSize = 640
  /** Recorder.SampleData 返回的累积采样缓冲区 */
  private sampleData: any = null

  constructor(pageInstance: any) {
    this.pageInstance = pageInstance
  }

  async start(options: RecorderOptions): Promise<void> {
    this.sampleRate = options.sampleRate
    this.frameSize = options.frameSize
    this.sampleData = null

    // #ifdef APP-PLUS
    // 1. 激活当前页面 WebView（RecordApp 必需前置步骤）
    RecordApp.UniWebViewActivate(this.pageInstance)

    // 2. 请求录音权限
    await new Promise<void>((resolve, reject) => {
      RecordApp.RequestPermission(
        () => resolve(),
        (msg: string) => reject(new Error(msg)),
      )
    })

    // 3. 启动录音
    return new Promise<void>((resolve, reject) => {
      const onProcess = (
        buffers: Float32Array[],
        powerLevel: number,
        _duration: number,
        sr: number,
        _newBufferIdx: number,
        _asyncEnd?: () => void,
      ) => {
        if (!this.recording) return
        try {
          // Recorder.SampleData 处理重采样和缓冲累积，输出 Int16 PCM
          this.sampleData = Recorder.SampleData(
            buffers,
            sr,
            this.sampleRate,
            this.sampleData,
          )
          const pcmInt16 = this.sampleData?.data as Int16Array | null
          if (!pcmInt16 || pcmInt16.length === 0) return

          // 通知音量级别（powerLevel 范围 0-100，归一化到 0-1）
          options.onAudioLevel?.(powerLevel / 100)

          // 按帧大小切割并发送
          const frameSamples = this.frameSize / 2
          const totalLen = pcmInt16.length

          // 从上次剩余位置开始
          let offset = (this.sampleData as any)._offset || 0

          while (offset + frameSamples <= totalLen) {
            const frame = pcmInt16.slice(offset, offset + frameSamples)
            const frameBuf = new Int16Array(frame)
            const base64 = arrayBufferToBase64(frameBuf.buffer)
            options.onAudioFrame(base64)
            offset += frameSamples
          }

          // 保存剩余数据位置，供下次 onProcess 继续消费
          if (offset < totalLen) {
            (this.sampleData as any)._offset = offset
          }
          else {
            this.sampleData = null // 全部消费完毕
          }
        }
        catch (e) {
          options.onError(e as Error)
        }
      }

      RecordApp.Start(
        {
          type: 'pcm' as any,
          sampleRate: this.sampleRate,
          // 告知原生插件直接以目标采样率录音，避免额外重采样
          appNativePlugin_sampleRate: this.sampleRate,
          // Android 语音通信模式（VOICE_COMMUNICATION），带回声消除
          android_audioSource: 7,
          audioTrackSet: {
            echoCancellation: true,
            noiseSuppression: true,
            autoGainControl: true,
          },
          onProcess,
        },
        () => {
          this.recording = true
          resolve()
        },
        (msg: string) => {
          this.recording = false
          reject(new Error(msg))
        },
      )
    })
    // #endif

    // 非 APP-PLUS 平台兜底（不应被执行到，因为工厂方法有条件编译保护）
    throw new Error('AppRecorder 仅支持 APP-PLUS 平台')
  }

  stop(): void {
    if (!this.recording) return
    this.recording = false
    this.sampleData = null
    // #ifdef APP-PLUS
    RecordApp.Stop(
      () => { /* 丢弃录音文件 */ },
      (msg: string) => console.error('[AppRecorder] Stop 错误:', msg),
    )
    // #endif
  }

  isRecording(): boolean {
    return this.recording
  }

  destroy(): void {
    this.stop()
  }
}
