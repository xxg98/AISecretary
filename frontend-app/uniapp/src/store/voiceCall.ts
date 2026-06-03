import type { ServerEvent } from '@/websocket/types'
import { ConnectionState, CallState } from '@/websocket/types'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

/**
 * 语音通话状态管理
 * 对应安卓示例中 RealtimeUiState + ViewModel 状态管理逻辑
 * 不持久化 —— 通话状态不应跨页面保持
 */
export const useVoiceCallStore = defineStore('voiceCall', () => {
  // ============ 连接状态 ============

  /** WebSocket 连接状态 */
  const connectionState = ref<ConnectionState>(ConnectionState.IDLE)

  /** 通话状态 */
  const callState = ref<CallState>(CallState.IDLE)

  /** 是否已连接 */
  const connected = computed(() => connectionState.value === ConnectionState.CONNECTED)

  /** 是否正在通话中 */
  const inCall = computed(() =>
    connected.value && callState.value !== CallState.IDLE,
  )

  // ============ 业务数据 ============

  /** ASR 语音识别中间/最终文本 */
  const asrText = ref('')

  /** AI 回复文本（llm.delta 流式累积） */
  const aiText = ref('')

  /** 录音音量级别 (RMS, 0-1)，用于波形动画 */
  const audioLevel = ref(0)

  /** 服务器 WebSocket 地址 */
  const serverUrl = ref('ws://127.0.0.1:8080/ws/realtime')

  /** 事件日志（调试用，最多保留 200 条） */
  const eventLogs = ref<string[]>([])

  // ============ 操作方法 ============

  /** 发起连接 */
  function connect(): void {
    connectionState.value = ConnectionState.CONNECTING
  }

  /** 连接成功 */
  function onConnected(): void {
    connectionState.value = ConnectionState.CONNECTED
    callState.value = CallState.LISTENING
  }

  /** 断开连接 */
  function disconnect(): void {
    connectionState.value = ConnectionState.IDLE
    callState.value = CallState.IDLE
    asrText.value = ''
    aiText.value = ''
    audioLevel.value = 0
  }

  /** 重置所有状态 */
  function reset(): void {
    connectionState.value = ConnectionState.IDLE
    callState.value = CallState.IDLE
    asrText.value = ''
    aiText.value = ''
    audioLevel.value = 0
    eventLogs.value = []
  }

  /** 添加事件日志 */
  function log(msg: string): void {
    eventLogs.value.push(`[${new Date().toLocaleTimeString()}] ${msg}`)
    if (eventLogs.value.length > 200) {
      eventLogs.value = eventLogs.value.slice(-200)
    }
  }

  /**
   * 处理服务器事件，驱动状态流转
   * 对应安卓示例 RealtimeViewModel.handleEvent()
   */
  function handleServerEvent(event: ServerEvent): void {
    switch (event.type) {
      case 'vad.speech_start':
        callState.value = CallState.USER_SPEAKING
        log('VAD: 检测到用户开始说话')
        break

      case 'vad.speech_end':
        callState.value = CallState.THINKING
        log('VAD: 用户说话结束')
        break

      case 'asr.partial':
        asrText.value = event.text
        log(`ASR(partial): ${event.text}`)
        break

      case 'asr.final':
        asrText.value = event.text
        aiText.value = ''
        log(`ASR(final): ${event.text}`)
        break

      case 'llm.delta':
        aiText.value += event.text
        log(`LLM: ${event.text}`)
        break

      case 'tts.start':
        callState.value = CallState.ASSISTANT_SPEAKING
        log(`TTS: 开始播放 (采样率: ${event.sampleRate ?? 24000})`)
        break

      case 'tts.chunk':
        // TTS 音频数据由播放器处理，此处仅记录状态
        // callState 已在 tts.start 中设为 ASSISTANT_SPEAKING
        break

      case 'tts.done':
        callState.value = CallState.LISTENING
        log('TTS: 播放完毕')
        break

      case 'interrupt':
        callState.value = CallState.INTERRUPTED
        log(`打断: ${event.reason ?? '用户中断'}`)
        // 短暂停留后回到监听状态
        setTimeout(() => {
          if (callState.value === CallState.INTERRUPTED && connected.value) {
            callState.value = CallState.LISTENING
          }
        }, 300)
        break

      case 'error':
        callState.value = CallState.ERROR
        log(`服务器错误: ${event.reason}`)
        break

      default:
        log(`未知事件: ${JSON.stringify(event)}`)
    }
  }

  /** 设置录音音量（RMS 归一化值） */
  function setAudioLevel(level: number): void {
    audioLevel.value = Math.max(0, Math.min(1, level))
  }

  return {
    // 状态
    connectionState,
    callState,
    connected,
    inCall,
    asrText,
    aiText,
    audioLevel,
    serverUrl,
    eventLogs,

    // 方法
    connect,
    onConnected,
    disconnect,
    reset,
    log,
    handleServerEvent,
    setAudioLevel,
  }
}, {
  persist: false, // 通话状态不持久化
})
