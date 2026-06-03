import type { IRecorder } from './recorder'
import type { IPlayer } from './player'
import { RecorderFactory } from './recorder'
import { PlayerFactory } from './player'

export type { IRecorder, IPlayer }
export { RecorderFactory, PlayerFactory }

/**
 * 创建录音器实例（平台感知）
 * H5: Web Audio API → getUserMedia + ScriptProcessorNode
 * APP: RecordApp + Recorder-NativePlugin 原生录音
 * 小程序: RecorderManager + onFrameRecorded
 */
export function createRecorder(pageInstance?: any): IRecorder {
  return RecorderFactory.create(pageInstance)
}

/**
 * 创建播放器实例（平台感知）
 * H5: AudioContext 直接播放 PCM
 * APP: Recorder-NativePlugin pcmPlayer 低延迟流式播放
 * 小程序: PCM→WAV 临时文件 → InnerAudioContext
 */
export function createPlayer(): IPlayer {
  return PlayerFactory.create()
}
