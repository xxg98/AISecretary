<script setup lang="ts">
definePage({
  style: {
    navigationBarTitleText: '语音通话',
  },
})

import type { ServerEvent } from '@/websocket/types'
import type { IRecorder, IPlayer } from '@/audio'

import { VoiceWebSocketManager } from '@/websocket'
import { createRecorder, createPlayer } from '@/audio'
import { useVoiceCallStore } from '@/store/voiceCall'
import { useTokenStore } from '@/store/token'
import { CallState } from '@/websocket/types'

// #ifdef APP-PLUS
import { getCurrentInstance } from 'vue'
import Recorder from 'recorder-core'
import RecordApp from 'recorder-core/src/app-support/app'
import '@/uni_modules/Recorder-UniCore/app-uni-support.js'

// 防止 recorder-core 被 tree-shaking 移除
Recorder.a = 1
// #endif

import VoiceStatusBar from './components/VoiceStatusBar.vue'
import VoiceWaveform from './components/VoiceWaveform.vue'
import VoiceControlBar from './components/VoiceControlBar.vue'

// ============ Store ============

const voiceStore = useVoiceCallStore()
const tokenStore = useTokenStore()

// #ifdef APP-PLUS
const instance = getCurrentInstance()
// #endif

// ============ 核心实例 ============

let wsManager: VoiceWebSocketManager | null = null
let recorder: IRecorder | null = null
let player: IPlayer | null = null

/** 服务器地址（优先使用环境变量） */
const serverUrl = import.meta.env.VITE_WS_BASEURL || voiceStore.serverUrl

/** 是否处于文本输入模式 */
const textInputMode = ref(false)

// ============ 计算属性 ============

/** AI 回复文本在 UI 中显示（按换行展示） */
const displayAiText = computed(() => voiceStore.aiText || (voiceStore.callState === CallState.THINKING ? '思考中...' : ''))

/** 是否可以挂断（已连接或连接中） */
const canHangup = computed(() => voiceStore.connectionState !== 'idle' && voiceStore.connectionState !== 'error')

// ============ 通话生命周期 ============

/** 发起通话 */
async function startCall(): Promise<void> {
  // 请求录音权限
  try {
    // #ifdef MP-WEIXIN
    await requestWxRecordPermission()
    // #endif

    voiceStore.connect()

    // 初始化录音器和播放器（APP 平台需传入页面实例）
    // #ifdef APP-PLUS
    recorder = createRecorder(instance?.proxy)
    // #endif
    // #ifndef APP-PLUS
    recorder = createRecorder()
    // #endif
    player = createPlayer()

    // 创建 WebSocket 管理器
    const token = await tokenStore.tryGetValidToken()
    wsManager = new VoiceWebSocketManager({
      url: serverUrl,
      token,
      onOpen: handleWsOpen,
      onMessage: handleWsMessage,
      onClose: handleWsClose,
      onError: handleWsError,
    })

    wsManager.connect()
  }
  catch (e) {
    voiceStore.log(`启动失败: ${(e as Error).message}`)
    voiceStore.disconnect()
  }
}

/** 结束通话 */
function endCall(): void {
  recorder?.stop()
  player?.stop()
  wsManager?.disconnect()
  voiceStore.disconnect()
}

/** 发送文本 */
function handleSendText(text: string): void {
  if (!wsManager) return
  const sent = wsManager.sendText(text)
  if (sent) {
    voiceStore.log(`发送文本: ${text}`)
    voiceStore.handleServerEvent({ type: 'vad.speech_end' } as ServerEvent)
  }
  else {
    uni.showToast({ title: '发送失败，未连接', icon: 'none' })
  }
}

/** 打断 AI 回复 */
function handleInterrupt(): void {
  player?.clearQueue()
  wsManager?.sendInterrupt()
  voiceStore.handleServerEvent({ type: 'interrupt', reason: '用户打断' } as ServerEvent)
}

/** 切换文本输入模式 */
function handleToggleTextInput(): void {
  textInputMode.value = !textInputMode.value
}

// ============ WebSocket 回调 ============

/** WebSocket 连接成功 */
function handleWsOpen(): void {
  voiceStore.onConnected()
  voiceStore.log('WebSocket 连接成功，开始录音')

  // 连接成功后立即开始录音
  recorder?.start({
    sampleRate: 16000,
    frameSize: 640, // 20ms @ 16kHz 16bit mono
    onAudioFrame: (pcmBase64: string) => {
      wsManager?.sendAudioChunk(pcmBase64)
    },
    onAudioLevel: (level: number) => {
      voiceStore.setAudioLevel(level)
    },
    onError: (error: Error) => {
      voiceStore.log(`录音错误: ${error.message}`)
    },
  }).catch((e) => {
    voiceStore.log(`录音启动失败: ${(e as Error).message}`)
  })
}

/** 收到服务器消息 */
function handleWsMessage(event: ServerEvent): void {
  // 更新 Store 状态
  voiceStore.handleServerEvent(event)

  // 根据事件类型执行音频操作
  switch (event.type) {
    case 'tts.start':
      player?.start({
        defaultSampleRate: event.sampleRate ?? 24000,
        onChunkPlayed: (idx) => {
          if (idx % 10 === 0) {
            voiceStore.log(`TTS 已播放 ${idx + 1} 个音频块`)
          }
        },
        onPlaybackEnded: () => {
          voiceStore.log('TTS 播放队列清空')
        },
        onError: (e) => {
          voiceStore.log(`TTS 播放错误: ${e.message}`)
        },
      })
      break

    case 'tts.chunk':
      player?.enqueuePcm(event.audio, event.sampleRate)
      break

    case 'tts.done':
      // 播放器继续处理已入队的 chunk，等全部播完后触发 onPlaybackEnded
      break

    case 'interrupt':
      player?.clearQueue()
      break
  }
}

/** WebSocket 连接关闭 */
function handleWsClose(code: number, reason: string): void {
  voiceStore.log(`WebSocket 断开: ${code} - ${reason}`)
  recorder?.stop()
  player?.stop()
}

/** WebSocket 连接错误 */
function handleWsError(error: Error): void {
  voiceStore.log(`WebSocket 错误: ${error.message}`)
  uni.showToast({ title: `连接错误: ${error.message}`, icon: 'none' })
}

// ============ 小程序录音权限 ============

// #ifdef MP-WEIXIN
async function requestWxRecordPermission(): Promise<void> {
  return new Promise((resolve, reject) => {
    wx.authorize({
      scope: 'scope.record',
      success: () => resolve(),
      fail: () => {
        wx.showModal({
          title: '需要录音权限',
          content: '语音通话需要使用麦克风',
          success: (res) => {
            if (res.confirm) {
              wx.openSetting({ success: () => resolve(), fail: () => reject(new Error('用户拒绝录音权限')) })
            }
            else {
              reject(new Error('用户取消录音权限'))
            }
          },
        })
      },
    })
  })
}
// #endif

// ============ 页面生命周期 ============

// #ifdef APP-PLUS
/** APP 平台：挂载完成后注册 RecordApp 页面显示回调 */
onMounted(() => {
  RecordApp.UniPageOnShow(instance?.proxy)
})
// #endif

/** 页面显示时（APP 平台需要重新激活 RecordApp WebView） */
onShow(() => {
  // #ifdef APP-PLUS
  RecordApp.UniPageOnShow(instance?.proxy)
  // #endif
})

/** 页面隐藏时保持连接（切换后台不自动断开） */
onHide(() => {
  voiceStore.log('页面隐藏')
})

/** 页面卸载时释放所有资源 */
onUnload(() => {
  voiceStore.log('页面卸载，释放资源')
  endCall()
  wsManager?.destroy()
  wsManager = null
  recorder?.destroy()
  recorder = null
  player?.destroy()
  player = null
})

/** 页面加载完成后自动发起通话 */
onReady(() => {
  startCall()
})
</script>

<template>
  <view class="h-screen flex flex-col bg-[#FAFAFA]">
    <!-- 状态栏 -->
    <VoiceStatusBar
      :connection-state="voiceStore.connectionState"
      :call-state="voiceStore.callState"
    />

    <!-- 中间内容区 -->
    <scroll-view class="flex-1 px-[32rpx] min-h-0" scroll-y>
      <view class="flex flex-col gap-[32rpx] py-[40rpx]">
        <!-- 波形动画 -->
        <view class="flex flex-col items-center gap-[24rpx] pt-[60rpx]">
          <VoiceWaveform
            :call-state="voiceStore.callState"
            :audio-level="voiceStore.audioLevel"
            :connected="voiceStore.connected"
          />

          <!-- 通话状态大标题 -->
          <text
            class="text-[32rpx] fw-500 text-center transition-colors duration-300"
            :class="voiceStore.callState === CallState.ERROR ? 'color-[#F44336]' : 'color-[#333]'"
          >
            <template v-if="voiceStore.callState === CallState.LISTENING">
              请说话...
            </template>
            <template v-else-if="voiceStore.callState === CallState.USER_SPEAKING">
              正在聆听...
            </template>
            <template v-else-if="voiceStore.callState === CallState.THINKING">
              AI 正在思考...
            </template>
            <template v-else-if="voiceStore.callState === CallState.ASSISTANT_SPEAKING">
              AI 回复中...
            </template>
            <template v-else-if="voiceStore.callState === CallState.INTERRUPTED">
              已打断
            </template>
            <template v-else-if="voiceStore.callState === CallState.IDLE">
              等待连接...
            </template>
            <template v-else>
              准备就绪
            </template>
          </text>
        </view>

        <!-- ASR 语音识别文本 -->
        <view
          v-if="voiceStore.asrText"
          class="bg-[#E8F5E9] rounded-[12rpx] px-[24rpx] py-[16rpx]"
        >
          <text class="text-[22rpx] color-[#999]">识别结果</text>
          <text class="block text-[28rpx] color-[#333] mt-[8rpx]">
            {{ voiceStore.asrText }}
          </text>
        </view>

        <!-- AI 回复文本 -->
        <view
          v-if="displayAiText"
          class="bg-[#E3F2FD] rounded-[12rpx] px-[24rpx] py-[16rpx]"
        >
          <text class="text-[22rpx] color-[#999]">AI 回复</text>
          <text class="block text-[28rpx] color-[#333] mt-[8rpx] lh-[1.6] whitespace-pre-wrap">
            {{ displayAiText }}
          </text>
        </view>

        <!-- 空状态提示 -->
        <view
          v-if="!voiceStore.asrText && !displayAiText && voiceStore.connected"
          class="flex flex-col items-center gap-[16rpx] pt-[40rpx]"
        >
          <view class="i-carbon-chat text-[64rpx] color-[#E0E0E0]" />
          <text class="text-[26rpx] color-[#BDBDBD] text-center">
            直接开始说话，AI 会实时回复
          </text>
        </view>

        <!-- 未连接时显示连接按钮 -->
        <view
          v-if="voiceStore.connectionState === 'idle' || voiceStore.connectionState === 'error'"
          class="flex justify-center pt-[80rpx]"
        >
          <button
            class="w-[360rpx] h-[88rpx] rounded-[44rpx] bg-[#2065EB] text-white text-[30rpx] fw-500"
            @tap="startCall"
          >
            {{ voiceStore.connectionState === 'error' ? '重新连接' : '开始通话' }}
          </button>
        </view>
      </view>
    </scroll-view>

    <!-- 底部控制栏 -->
    <VoiceControlBar
      :call-state="voiceStore.callState"
      :connected="voiceStore.connected"
      :text-input-mode="textInputMode"
      @toggle-text-input="handleToggleTextInput"
      @send-text="handleSendText"
      @interrupt="handleInterrupt"
      @hangup="endCall"
    />
  </view>
</template>

<style scoped>
/* 页面使用 UnoCSS + scoped 内联样式，保持简约 */
.chat-scroll {
  flex: 1;
  min-height: 0;
}
</style>
