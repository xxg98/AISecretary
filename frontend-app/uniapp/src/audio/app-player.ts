// #ifdef APP-PLUS
import RecordApp from 'recorder-core/src/app-support/app'
// #endif

import type { IPlayer, PlayerOptions } from './player'

/**
 * APP 原生 PCM 播放器（Recorder-NativePlugin pcmPlayer）
 *
 * pcmPlayer 是原生实现的低延迟流式 PCM 播放器。
 * 相比旧版 NativePlayer（PCM→WAV 文件→InnerAudioContext），
 * pcmPlayer 直接将 PCM 数据送入音频输出缓冲区，延迟显著降低（~300ms 缓冲）。
 *
 * 数据流：TTS Chunk(Base64 PCM) → enqueuePcm → pcmPlayer_input → 扬声器
 */
export class AppPlayer implements IPlayer {
  private options: PlayerOptions | null = null
  private playing = false
  private sampleRate = 24000
  /** pcmPlayer_create 返回的播放器标识 */
  private playerId: any = null
  /** 播放器是否已创建 */
  private playerCreated = false

  async start(options: PlayerOptions): Promise<void> {
    this.options = options
    this.playing = true
    this.sampleRate = options.defaultSampleRate || 24000
    await this.createPlayer()
  }

  async enqueuePcm(pcmBase64: string, _sampleRate?: number): Promise<void> {
    if (!this.playing) return
    if (!this.playerCreated) {
      await this.createPlayer()
    }
    // #ifdef APP-PLUS
    try {
      await RecordApp.UniNativeUtsPluginCallAsync('pcmPlayer_input', {
        player: this.playerId,
        pcmDataBase64: pcmBase64,
      })
    }
    catch (e) {
      this.options?.onError?.(e as Error)
    }
    // #endif
  }

  async clearQueue(): Promise<void> {
    // #ifdef APP-PLUS
    try {
      // 清除原生播放器缓冲中的数据（不销毁播放器实例）
      await RecordApp.UniNativeUtsPluginCallAsync('pcmPlayer_clearInput', {
        player: this.playerId,
      })
    }
    catch (e) {
      // 如果 clearInput 不可用，降级为销毁重建
      try {
        await this.destroyPlayer()
        if (this.playing) {
          await this.createPlayer()
        }
      }
      catch (_e) {
        // 忽略重建错误
      }
    }
    // #endif
  }

  async stop(): Promise<void> {
    this.playing = false
    await this.destroyPlayer()
    this.options = null
  }

  isPlaying(): boolean {
    return this.playing
  }

  async destroy(): Promise<void> {
    await this.stop()
  }

  /** 创建原生 PCM 播放器实例 */
  private async createPlayer(): Promise<void> {
    if (this.playerCreated) return
    // #ifdef APP-PLUS
    try {
      const result = await RecordApp.UniNativeUtsPluginCallAsync('pcmPlayer_create', {
        sampleRate: this.sampleRate,
        realtime: { maxDelay: 300 },
        timeUpdateInterval: 100,
        // 使用扬声器播放（非听筒）
        setSpeakerOff: { off: false, headset: true },
      })
      this.playerId = result?.player ?? result
      this.playerCreated = true
    }
    catch (e) {
      console.error('[AppPlayer] 创建播放器失败:', e)
      this.options?.onError?.(e as Error)
    }
    // #endif
  }

  /** 销毁原生 PCM 播放器实例 */
  private async destroyPlayer(): Promise<void> {
    if (!this.playerCreated) return
    // #ifdef APP-PLUS
    try {
      await RecordApp.UniNativeUtsPluginCallAsync('pcmPlayer_destroy', {
        player: this.playerId,
      })
    }
    catch (e) {
      console.error('[AppPlayer] 销毁播放器失败:', e)
    }
    // #endif
    this.playerCreated = false
    this.playerId = null
  }
}
