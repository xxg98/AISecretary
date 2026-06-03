// ---- 客户端→服务器消息 ----

/** 音频数据块 - 持续发送，PCM 16kHz 16bit mono，Base64 编码 */
export interface AudioChunkMessage {
  type: 'audio.chunk'
  audio: string // Base64 编码的 PCM 数据
}

/** 文本输入 - 替代语音，直接触发 AI 回复 */
export interface AudioEndMessage {
  type: 'audio.end'
  text: string
}

/** 打断当前 AI 回复 */
export interface InterruptMessage {
  type: 'interrupt'
}

/** 客户端发送的所有消息类型 */
export type ClientMessage = AudioChunkMessage | AudioEndMessage | InterruptMessage

// ---- 服务器→客户端事件 ----

/** VAD 检测到人声开始 */
export interface VadSpeechStartEvent {
  type: 'vad.speech_start'
}

/** VAD 检测到人声结束 */
export interface VadSpeechEndEvent {
  type: 'vad.speech_end'
}

/** ASR 语音识别中间结果 */
export interface AsrPartialEvent {
  type: 'asr.partial'
  text: string
}

/** ASR 语音识别最终结果 */
export interface AsrFinalEvent {
  type: 'asr.final'
  text: string
  sessionId?: string
}

/** LLM 流式输出 */
export interface LlmDeltaEvent {
  type: 'llm.delta'
  text: string
}

/** TTS 开始播放 */
export interface TtsStartEvent {
  type: 'tts.start'
  sampleRate?: number // 默认 24000
}

/** TTS 音频数据块 */
export interface TtsChunkEvent {
  type: 'tts.chunk'
  audio: string // Base64 编码 PCM 数据
  sampleRate?: number // 默认 24000
}

/** TTS 播放完毕 */
export interface TtsDoneEvent {
  type: 'tts.done'
}

/** 被服务器打断 */
export interface ServerInterruptEvent {
  type: 'interrupt'
  reason?: string
}

/** 服务器错误 */
export interface ServerErrorEvent {
  type: 'error'
  reason: string
}

/** 服务器下发的所有事件类型 */
export type ServerEvent =
  | VadSpeechStartEvent
  | VadSpeechEndEvent
  | AsrPartialEvent
  | AsrFinalEvent
  | LlmDeltaEvent
  | TtsStartEvent
  | TtsChunkEvent
  | TtsDoneEvent
  | ServerInterruptEvent
  | ServerErrorEvent

// ---- 状态枚举 ----

/** WebSocket 连接状态 */
export enum ConnectionState {
  /** 空闲，未连接 */
  IDLE = 'idle',
  /** 正在连接中 */
  CONNECTING = 'connecting',
  /** 已连接 */
  CONNECTED = 'connected',
  /** 正在主动断开 */
  DISCONNECTING = 'disconnecting',
  /** 正在重连 */
  RECONNECTING = 'reconnecting',
  /** 连接错误（重连次数耗尽） */
  ERROR = 'error',
}

/** 通话状态 */
export enum CallState {
  /** 空闲 */
  IDLE = 'idle',
  /** 监听中，等待用户说话 */
  LISTENING = 'listening',
  /** 用户正在说话 */
  USER_SPEAKING = 'user_speaking',
  /** AI 处理中 */
  THINKING = 'thinking',
  /** AI 正在说话（TTS 播放中） */
  ASSISTANT_SPEAKING = 'assistant_speaking',
  /** 被打断 */
  INTERRUPTED = 'interrupted',
  /** 错误状态 */
  ERROR = 'error',
}
