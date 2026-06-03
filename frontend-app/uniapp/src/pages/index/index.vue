<template>
  <view class="h-screen flex flex-col bg-[#EDEDED]">
    <view class="sticky top-0 z-9 bg-white border-b border-gray-100">
      <kl-status-bar background="#fff" />
      <view class="flex items-center justify-between px-[32rpx] py-[20rpx]">
        <view class="w-[60rpx] h-[60rpx] flex items-center justify-center">
          <view class="i-carbon-home text-[40rpx] color-[#333]" />
        </view>
        <view class="flex-1 flex justify-center">
          <text class="text-[34rpx] fw-600 color-[#333]">AI秘书</text>
        </view>
        <view class="w-[60rpx]"></view>
      </view>
    </view>

    <!-- 聊天页面：数组 index0 最新，展示时 reverse -->
    <scroll-view
      class="chat-scroll flex-1 bg-[#EDEDED] min-h-0"
      scroll-y
      :refresher-enabled="hasMoreHistory"
      refresher-background="transparent"
      refresher-default-style="none"
      :refresher-triggered="historyRefresherActive"
      :style="{ marginBottom: `${scrollBottomGap}px` }"
      :scroll-top="programmaticScrollTop"
      :scroll-into-view="scrollToId"
      :scroll-with-animation="scrollWithAnimation"
      :upper-threshold="80"
      @refresherrefresh="onHistoryRefresherRefresh"
      @scrolltoupper="onScrollToUpper"
      @scroll="onScroll"
      @click="scrollViewClick"
    >
      <!-- 自定义下拉加载指示器 -->
      <view
        v-if="historyRefresherActive"
        class="refresher-indicator"
        :class="{ 'refresher-indicator--fade': isRefresherFading }"
      >
        <view class="refresher-indicator__spinner" />
        <text class="refresher-indicator__text">加载历史消息...</text>
      </view>
      <view class="chat-list-inner flex flex-col gap-[32rpx] px-[32rpx] py-[24rpx]">

        <template v-for="item in displayMessages" :key="item.id">
          <!-- 系统提示 -->
          <view v-if="item.type==='system'" :id="`msg-${item.id}`" class="flex justify-center">
            <text class="text-[22rpx] color-[#999] bg-[#E8E8E8] px-[24rpx] py-[8rpx] rounded-[20rpx] ">
              {{ item.text }}
            </text>
          </view>

          <!-- self -->
          <view v-else-if="item.type==='self'" :id="`msg-${item.id}`" class="flex justify-end items-start gap-[16rpx]">
            <view v-if="item.contentType === 'image'" class="chat-image-message-shell chat-image-message-shell--self">
              <chat-image-message
                :message-id="item.id"
                :image-url="item.imageUrl ?? ''"
                :is-self="true"
                @preview="onImagePreview"
              />
            </view>
            <view v-else-if="item.contentType === 'file'" class="chat-file-message-shell chat-file-message-shell--self">
              <chat-file-message
                :message-id="item.id"
                :file="item.file!"
                :is-self="true"
                @preview="onFilePreview"
              />
            </view>
            <view
              v-else-if="item.contentType !== 'voice'"
              class="color-white bg-[#2065EB] self-box-bg  rounded-[8rpx] max-w-[70%] p-2 "
            >
              <text class="text-[28rpx] lh-[1.5] break-words">{{ item.text }}</text>
            </view>
            <chat-voice-message
              v-else-if="item.contentType === 'voice'"
              :message-id="item.id"
              :duration="item.duration ?? 1"
              :is-self="true"
              :is-playing="playingVoiceId === item.id"
              @play="onVoiceMessagePlay"
            />
            <view class="chat-avatar">
              <image src="/static/images/user.jpg" mode="aspectFill" class="chat-avatar__image" />
            </view>
          </view>

          <!-- other -->
          <view
            v-else-if="item.type==='other'"
            :id="`msg-${item.id}`"
            class="flex justify-start items-start gap-[16rpx]"
          >
            <view class="chat-avatar">
              <image src="/static/images/bot.jpg" mode="aspectFill" class="chat-avatar__image" />
            </view>
            <view v-if="item.contentType === 'image'" class="chat-image-message-shell">
              <chat-image-message
                :message-id="item.id"
                :image-url="item.imageUrl ?? ''"
                @preview="onImagePreview"
              />
            </view>
            <view v-else-if="item.contentType === 'file'" class="chat-file-message-shell">
              <chat-file-message
                :message-id="item.id"
                :file="item.file!"
                @preview="onFilePreview"
              />
            </view>
            <view
              v-else-if="item.contentType !== 'voice'"
              class="color-[#333] bg-white rounded-[8rpx] max-w-[70%] p-2 "
            >
              <text class="text-[28rpx] lh-[1.5] break-words">{{ item.text }}</text>
            </view>
            <chat-voice-message
              v-else-if="item.contentType === 'voice'"
              :message-id="item.id"
              :duration="item.duration ?? 1"
              :is-playing="playingVoiceId === item.id"
              @play="onVoiceMessagePlay"
            />
          </view>
        </template>
      </view>
    </scroll-view>

    <!-- 浏览历史时的新消息提示（左下角） -->
    <view
      v-if="hasNewMessage"
      class="new-msg-tip"
      :style="{ bottom: `${newMsgTipBottom}px` }"
      @click="scrollToBottom"
    >
      <text class="new-msg-tip__text">{{ newMessageTipText }}</text>
    </view>

    <view v-if="voiceOverlayVisible" class="voice-record-mask">
      <view class="voice-record-panel" :class="{ 'voice-record-panel--cancel': isRecordCanceling }">
        <view class="voice-record-wave">
          <view class="voice-record-wave__bar voice-record-wave__bar--1" />
          <view class="voice-record-wave__bar voice-record-wave__bar--2" />
          <view class="voice-record-wave__bar voice-record-wave__bar--3" />
          <view class="voice-record-wave__bar voice-record-wave__bar--4" />
        </view>
        <text class="voice-record-panel__text">{{ voiceOverlayText }}</text>
      </view>
    </view>

    <!-- 底部输入框（仅输入区随键盘上移，navbar 不动） -->
    <view
      class="chat-input-bar"
      :style="inputBarStyle"
    >
      <view class="chat-input-row">
        <view class="chat-input-icon" @click="toggleInputMode">
          <view v-if="inputMode === 'keyboard'" class="i-carbon-volume-up text-[36rpx] color-[#4B5563]" />
          <view v-else class="i-carbon-keyboard text-[36rpx] color-[#4B5563]" />
        </view>
        <view v-if="inputMode === 'keyboard'" class="chat-input-wrap relative">
          <textarea
            class="chat-textarea"
            v-model="inputMessage"
            placeholder="发消息"
            auto-height
            :show-confirm-bar="false"
            :maxlength="-1"
            :adjust-position="false"
            :focus="textareaFocus"
            @focus="onInputFocus"
            @input="onTextareaInput"
            @keyboardheightchange="keyBoardHeightChange"
            @linechange="handleLineChange"
            @confirm="sendMessage"
          />
          <view
            v-if="!inputMessage.trim()"
            class="chat-stt-button"
            :class="{ 'chat-stt-button--recording': isSttRecording, 'chat-stt-button--loading': isSttTranscribing }"
            @click="onSttButtonClick"
          >
            <view class="i-carbon-microphone chat-stt-button__icon" />
          </view>
        </view>
        <view
          v-else
          class="chat-voice-button"
          :class="{ 'chat-voice-button--active': isRecording }"
          @touchstart="onVoiceTouchStart"
          @touchmove="onVoiceTouchMove"
          @touchend="onVoiceTouchEnd"
          @touchcancel="onVoiceTouchCancel"
        >
          <text>{{ voiceButtonText }}</text>
        </view>
        <view
          class="chat-input-send"
          :class="{ 'chat-input-send--active': !!inputMessage.trim() }"
          @click="onSendClick"
        >
          <view class="chat-input-send__icon i-carbon-add-alt color-[#4B5563]" />
          <view class="chat-input-send__text-wrap">
            <text class="chat-input-send__text">发送</text>
          </view>
        </view>
      </view>
    </view>

    <view v-show="isActionPanelVisible" class="chat-action-panel" :style="panelStyle">
      <view
        v-for="item in actionItems"
        :key="item.key"
        class="chat-action-item"
        @click="onActionItemClick(item.key)"
      >
        <view class="chat-action-item__icon-wrap">
          <view :class="['chat-action-item__icon', item.icon]" />
        </view>
        <text class="chat-action-item__text">{{ item.label }}</text>
      </view>
    </view>

  </view>
</template>

<script lang="ts" setup>
import { safeAreaInsets } from '@/utils/systemInfo'
import ChatImageMessage from '@/components/chat/ChatImageMessage.vue'
import ChatFileMessage from '@/components/chat/ChatFileMessage.vue'
import {
  chooseChatFiles,
  createChatFileItem,
  isLargeTextPreviewFile,
  isTextPreviewFile,
  saveChatFilePreviewState,
  type ChatFileItem,
  type ChatFilePreviewState
} from '@/utils/chatFile'

defineOptions({
  name: 'Home'
})
definePage({
  type: 'home',
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '首页',
  },
})

type MessageType = 'system' | 'self' | 'other'
type MessageContentType = 'text' | 'voice' | 'image' | 'file'
type ActionKey = 'image' | 'camera' | 'favorite' | 'file'
type InputMode = 'keyboard' | 'voice'
type RecorderPurpose = '' | 'voice-message' | 'speech-to-text'

interface ChatMessage {
  id: string
  type: MessageType
  contentType?: MessageContentType
  text?: string
  voicePath?: string
  imageUrl?: string
  file?: ChatFileItem
  duration?: number
}

interface ActionItem {
  key: ActionKey
  label: string
  icon: string
}

interface RecorderStopResult {
  tempFilePath: string
}

interface SpeechToTextResponse {
  text?: string
  result?: string
  data?: {
    text?: string
    result?: string
  }
}

interface ImagePreviewItem {
  id: string
  url: string
}

const SCROLL_BOTTOM_THRESHOLD = 80
/** 可视区下方超过约 2 条消息高度 → 视为浏览历史 */
const VIEW_HISTORY_THRESHOLD = 120
/** 滚动到距离顶部30%时触发历史加载 */
const HISTORY_LOAD_THRESHOLD_RATIO = 0.3

const inputMessage = ref('')
const scrollToId = ref('')
/** 仅程序化滚动的短绑定，用后置空，避免与用户滚动冲突闪烁 */
const programmaticScrollTop = ref<number | string>('')
const scrollWithAnimation = ref(true)
const hasNewMessage = ref(false)
const newMessageCount = ref(0)
const isAtBottom = ref(true)
const isViewingHistory = ref(false)
const hasMoreHistory = ref(true)
const historyRefresherActive = ref(false)
const isRefresherFading = ref(false)
const scrollViewHeight = ref(0)
const manualPanelVisible = ref(false)

const isActionPanelVisible = computed(() =>
  keyboardHeight.value > 0 || manualPanelVisible.value,
)

const inputMode = ref<InputMode>('keyboard')
const isRecording = ref(false)
const isRecordCanceling = ref(false)
const isTranscribing = ref(false)
const isSttRecording = ref(false)
const isSttTranscribing = ref(false)
const playingVoiceId = ref('')
const voiceOverlayVisible = computed(() => isRecording.value || isTranscribing.value)
const voiceButtonText = computed(() => isRecording.value ? '松开 结束' : '按住 说话')
const voiceOverlayText = computed(() => {
  if (isTranscribing.value)
    return '正在转文字...'
  return isRecordCanceling.value ? '松开取消' : '正在录音，松开发送'
})
let isLoadingHistory = false
let recorderManager: UniApp.RecorderManager | null = null
let sttRecorderManager: UniApp.RecorderManager | null = null
let audioContext: UniApp.InnerAudioContext | null = null
let recordStartTime = 0
let shouldCancelRecord = false
let recordTouchStartY = 0
let sttRecordStartTime = 0
let shouldCancelStt = false
let sttRequestSeq = 0
let activeRecorderPurpose: RecorderPurpose = ''
const androidRecordPermission = 'android.permission.RECORD_AUDIO'

function resetVoiceRecordState() {
  activeRecorderPurpose = ''
  isRecording.value = false
  isRecordCanceling.value = false
}

const imagePreviewItems = computed<ImagePreviewItem[]>(() => [...messages.value].reverse()
  .filter(item => item.contentType === 'image' && item.imageUrl)
  .map(item => ({
    id: item.id,
    url: item.imageUrl!,
  })),
)

const actionItems: ActionItem[] = [
  { key: 'image', label: '图片', icon: 'i-carbon-image' },
  { key: 'camera', label: '拍摄', icon: 'i-carbon-camera' },
  { key: 'favorite', label: '收藏', icon: 'i-carbon-star' },
  { key: 'file', label: '文件', icon: 'i-carbon-document' },
]

/** index 0 为最新，展示时反转 */
const displayMessages = computed(() => [...messages.value].reverse())

const mockHistoryMessages: ChatMessage[] = [
  { id: 'history-1', type: 'system', text: '2024年4月' },
  { id: 'history-2', type: 'other', text: '你好！很高兴认识你~' },
  { id: 'history-3', type: 'self', text: '你好！' },
  { id: 'history-4', type: 'other', text: '今天天气不错呢' },
  { id: 'history-5', type: 'self', text: '是的，很适合出去玩' },
  { id: 'history-6', type: 'other', text: '你周末有什么安排吗？' },
  { id: 'history-7', type: 'self', text: '打算去公园逛逛' },
  { id: 'history-8', type: 'other', text: '听起来很棒！' },
  { id: 'history-9', type: 'other', text: '记得带上防晒霜' },
  { id: 'history-10', type: 'self', text: '好的，谢谢提醒' },
  { id: 'history-11', type: 'system', text: '2024年3月' },
  { id: 'history-12', type: 'other', text: '今天开会的内容你记一下' },
  { id: 'history-13', type: 'self', text: '好的，我都记下来了' },
  { id: 'history-14', type: 'other', text: '重点是下周三之前要完成' },
  { id: 'history-15', type: 'self', text: '没问题，我会按时完成的' },
  { id: 'history-16', type: 'other', text: '加油！' },
  { id: 'history-17', type: 'system', text: '2024年2月' },
  { id: 'history-18', type: 'other', text: '新年快乐！' },
  { id: 'history-19', type: 'self', text: '新年快乐！恭喜发财' },
  { id: 'history-20', type: 'other', text: '红包拿来~' },
  { id: 'history-21', type: 'self', text: '哈哈，好的' },
  { id: 'history-22', type: 'other', text: '今年有什么计划吗？' },
  { id: 'history-23', type: 'self', text: '打算去旅游，你呢？' },
  { id: 'history-24', type: 'other', text: '我也想去，一起吧' },
  { id: 'history-25', type: 'system', text: '2024年1月' },
  { id: 'history-26', type: 'other', text: '今天天气真冷啊' },
  { id: 'history-27', type: 'self', text: '是啊，多穿点衣服' },
  { id: 'history-28', type: 'other', text: '你今天穿几件？' },
  { id: 'history-29', type: 'self', text: '穿了三件，够暖了' },
  { id: 'history-30', type: 'other', text: '那就好，别感冒了' },
]

const createInitialMessages = (): ChatMessage[] => [
  { id: 'text-20', type: 'other', text: 'Hello, 这里是Ai秘书！' },
  { id: 'text-19', type: 'self', text: '你好！Ai秘书！' },
]

const messages = ref<ChatMessage[]>(createInitialMessages())
let historyIndex = 0
let lastScrollTop = 0
let lastScrollHeight = 0

const INPUT_BAR_BASE_RPX = 120
const measuredInputBarHeight = ref(uni.upx2px(INPUT_BAR_BASE_RPX))

// 键盘与安全区
const safeAreaBottom = ref(safeAreaInsets?.bottom ?? 0)
const keyboardHeight = ref(0)
const lastKeyboardHeight = ref(0)
const keyboardDuration = ref(0.25)

const PANEL_DEFAULT_HEIGHT = 300

const effectiveBottom = computed(() => {
  if (keyboardHeight.value > 0) return keyboardHeight.value
  if (isActionPanelVisible.value) return lastKeyboardHeight.value || PANEL_DEFAULT_HEIGHT
  return safeAreaBottom.value
})

const panelStyle = computed(() => ({
  bottom: '0',
  height: `${effectiveBottom.value}px`,
  transitionDuration: `${keyboardDuration.value}s`,
}))

const scrollBottomGap = computed(() =>
  measuredInputBarHeight.value + effectiveBottom.value,
)

const newMsgTipBottom = computed(() =>
  measuredInputBarHeight.value + effectiveBottom.value + 16,
)

const newMessageTipText = computed(() => {
  const count = newMessageCount.value
  if (count <= 1)
    return '有新消息，点击查看'
  if (count > 99)
    return '99+条新消息，点击查看'
  return `${count}条新消息，点击查看`
})

const inputBarStyle = computed(() => ({
  bottom: `${effectiveBottom.value}px`,
  transitionDuration: `${keyboardDuration.value}s`,
}))

function getRecorderManager() {
  if (!recorderManager) {
    recorderManager = uni.getRecorderManager()
    recorderManager.onStop((res) => {
      if (activeRecorderPurpose === 'speech-to-text') {
        onSttRecorderStop(res)
        return
      }

      onRecorderStop(res)
    })
    recorderManager.onError((error) => {
      console.error('录音失败:', error)
      if (activeRecorderPurpose === 'speech-to-text') {
        shouldCancelStt = true
        isSttRecording.value = false
        isSttTranscribing.value = false
      }
      activeRecorderPurpose = ''
      isRecording.value = false
      isRecordCanceling.value = false
      isTranscribing.value = false
      const message = error?.errMsg || '请检查麦克风权限'
      uni.showToast({ title: `录音失败：${message}`, icon: 'none' })
    })
  }
  return recorderManager
}

function startVoiceRecorder() {
  const start = () => {
    getRecorderManager().start({
      duration: 60000,
      format: 'mp3',
    })
  }

  // #ifdef APP-ANDROID
  plus.android.requestPermissions(
    [androidRecordPermission],
    (result: any) => {
      const granted = Array.isArray(result.granted) && result.granted.includes(androidRecordPermission)
      if (granted) {
        start()
        return
      }

      resetVoiceRecordState()
      uni.showToast({ title: '麦克风权限被拒绝', icon: 'none' })
    },
    (error: any) => {
      console.error('请求麦克风权限失败:', error)
      resetVoiceRecordState()
      uni.showToast({ title: '请求麦克风权限失败', icon: 'none' })
    },
  )
  return
  // #endif

  start()
}

function getSttRecorderManager() {
  sttRecorderManager = getRecorderManager()
  return sttRecorderManager
}

function resolveSpeechText(res: SpeechToTextResponse | string) {
  if (typeof res === 'string')
    return res

  return res.text || res.result || res.data?.text || res.data?.result || ''
}

async function speechToText(filePath: string): Promise<string> {
  // 当前使用模拟数据，后续对接接口时保留 filePath、fileName 和返回结构兼容处理即可。
  const mockResponse: SpeechToTextResponse = await new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        data: {
          text: `这是模拟语音转文字内容，录音文件：${filePath ? '已获取' : '未获取'}`,
        },
      })
    }, 800)
  })

  return resolveSpeechText(mockResponse)
}

function getAudioContext() {
  if (!audioContext) {
    audioContext = uni.createInnerAudioContext()
    audioContext.onEnded(() => {
      playingVoiceId.value = ''
    })
    audioContext.onStop(() => {
      playingVoiceId.value = ''
    })
    audioContext.onError(() => {
      playingVoiceId.value = ''
      uni.showToast({ title: '语音播放失败', icon: 'none' })
    })
  }
  return audioContext
}

function measureInputBar() {
  nextTick(() => {
    uni.createSelectorQuery()
      .select('.chat-input-bar')
      .boundingClientRect((rect) => {
        if (rect && !Array.isArray(rect))
          measuredInputBarHeight.value = rect.height
      })
      .exec()
  })
}

function measureScrollView() {
  uni.createSelectorQuery()
    .select('.chat-scroll')
    .boundingClientRect((rect) => {
      if (rect && !Array.isArray(rect))
        scrollViewHeight.value = rect.height
    })
    .exec()
}

const onScroll = (e: UniHelper.ScrollViewOnScrollEvent) => {
  const { scrollTop: top, scrollHeight } = e.detail

  const viewH = scrollViewHeight.value
  if (!viewH)
    return

  const distanceToBottom = scrollHeight - top - viewH
  isAtBottom.value = distanceToBottom <= SCROLL_BOTTOM_THRESHOLD
  isViewingHistory.value = distanceToBottom > VIEW_HISTORY_THRESHOLD

  if (isAtBottom.value) {
    hasNewMessage.value = false
    newMessageCount.value = 0
  }

  const distanceToTop = top
  const loadThreshold = scrollHeight * HISTORY_LOAD_THRESHOLD_RATIO
  const isScrollingUp = top < lastScrollTop

  if (distanceToTop <= loadThreshold && isScrollingUp) {
    loadHistoryCore()
  }

  lastScrollTop = top
  lastScrollHeight = scrollHeight
}

// 点击聊天记录区域 收起功能区 取消聚焦
const scrollViewClick = () => {
  uni.hideKeyboard()
  keyboardHeight.value = 0
  textareaFocus.value = false
  manualPanelVisible.value = false
}


/** 加载更早历史 */
async function loadHistoryCore() {
  if (!hasMoreHistory.value || isLoadingHistory)
    return

  isLoadingHistory = true

  // 模拟加载延迟，延迟 200ms
  await new Promise(resolve => setTimeout(resolve, 200))

  const start = historyIndex * 3
  const batch = mockHistoryMessages.slice(start, start + 3)

  if (batch.length === 0) {
    hasMoreHistory.value = false
    // 插入"没有更多记录了"提示（只在首次提示）
    const hasNoMoreTip = messages.value.some(m => m.id === 'no-more-history')
    if (!hasNoMoreTip) {
      messages.value = [...messages.value, { id: 'no-more-history', type: 'system', text: '没有更多记录了' }]
    }
    isLoadingHistory = false
    return
  }

  // 直接将历史消息插入到 messages 数组末尾（因为 index 0 是最新，所以历史消息应该加到后面）
  messages.value = [...messages.value, ...batch]

  historyIndex++
  isLoadingHistory = false
}

/** 下拉刷新：可连续下拉，不依赖 scrolltoupper 重复触发 */
async function onHistoryRefresherRefresh() {
  isRefresherFading.value = false
  historyRefresherActive.value = true
  await loadHistoryCore()
  isRefresherFading.value = true
  setTimeout(() => {
    historyRefresherActive.value = false
    isRefresherFading.value = false
  }, 300)
}

/** 滚到顶部时兜底触发（与下拉刷新共用逻辑） */
function onScrollToUpper() {
  if (isLoadingHistory || !hasMoreHistory.value)
    return
  loadHistoryCore()
}

// 监听行数变化，重新测量输入栏高度
const handleLineChange = () => {
  measureInputBar()
}

// 键盘高度变化（duration 单位为秒）
const keyBoardHeightChange = (e: UniHelper.TextareaOnKeyboardheightchangeEvent) => {
  const { height, duration } = e.detail
  if (duration > 0) {
    const normalizedDuration = duration > 10 ? duration / 1000 : duration
    keyboardDuration.value = normalizedDuration
  }

  keyboardHeight.value = height

  if (height > 0) {
    if (height > lastKeyboardHeight.value) lastKeyboardHeight.value = height
    scrollToBottom()
  }
  measureInputBar()
}

const textareaFocus = ref<boolean>(false)

// 输入框获取焦点触发
const onInputFocus = (event: UniHelper.TextareaOnFocusEvent) => {
  textareaFocus.value = true
  scrollToBottom()
}

const onTextareaInput = (event: any) => {
  const value = event.detail.value?.trim() ?? ''
  if (value)
    cancelSttByManualInput()
}

// 切换输入模式
const toggleInputMode = () => {
  cancelSttByManualInput()
  inputMode.value = inputMode.value === 'keyboard' ? 'voice' : 'keyboard'
  manualPanelVisible.value = false
  if (inputMode.value === 'voice') {
    uni.hideKeyboard()
    keyboardHeight.value = 0
  } else {
    textareaFocus.value = true
    manualPanelVisible.value = true
  }
  measureInputBar()
}


const scrollToBottom = () => {
  nextTick(() => {
    const latest = messages.value[0]
    if (!latest)
      return

    programmaticScrollTop.value = ''
    scrollWithAnimation.value = true
    scrollToId.value = ''
    nextTick(() => {
      scrollToId.value = `msg-${latest.id}`
      isAtBottom.value = true
      isViewingHistory.value = false
      hasNewMessage.value = false
      newMessageCount.value = 0
    })
  })
}

/** 新消息插 index 0；在最新页自动滚底，浏览历史时显示浮窗 */
function pushMessage(msg: ChatMessage, options?: { forceScroll?: boolean }) {
  messages.value.unshift(msg)

  nextTick(() => {
    if (options?.forceScroll || isAtBottom.value) {
      scrollToBottom()
    } else if (isViewingHistory.value) {
      newMessageCount.value++
      hasNewMessage.value = true
    }
  })
}

function stopVoicePlayback() {
  if (!audioContext)
    return

  audioContext.stop()
  playingVoiceId.value = ''
}

function onVoiceMessagePlay(messageId: string) {
  const message = messages.value.find(item => item.id === messageId)
  if (!message?.voicePath)
    return

  if (playingVoiceId.value === messageId) {
    stopVoicePlayback()
    return
  }

  const audio = getAudioContext()
  if (playingVoiceId.value)
    audio.stop()

  audio.src = message.voicePath
  playingVoiceId.value = messageId
  audio.play()
}

// 发送消息
const sendMessage = () => {
  const text = inputMessage.value.trim()
  if (!text)
    return

  stopVoicePlayback()
  pushMessage({ id: `text-${Date.now()}`, type: 'self', text }, { forceScroll: true })
  inputMessage.value = ''

  setTimeout(() => {
    const replies = [
      '收到消息收到消息收到消息收到消息收到消息收到消息收到消息收到消息收到消息收到消息收到消息收到消息收到消息收到消息',
      '好的，我明白了',
      '这个问题很有趣！',
    ]
    const reply = replies[Math.floor(Math.random() * replies.length)]
    pushMessage({ id: `text-${Date.now()}`, type: 'other', text: reply })
  }, 1000)
}

function pushImageMessage(item: { id: string; type: MessageType; imageUrl: string }) {
  pushMessage({
    id: item.id,
    type: item.type,
    contentType: 'image',
    imageUrl: item.imageUrl,
  }, { forceScroll: true })
}

function pushFileMessage(item: { id: string; type: MessageType; file: ChatFileItem }) {
  pushMessage({
    id: item.id,
    type: item.type,
    contentType: 'file',
    file: item.file,
  }, { forceScroll: true })
}

function openFilePreview(files: ChatFileItem[], currentId: string) {
  const state: ChatFilePreviewState = {
    files,
    currentId,
  }
  saveChatFilePreviewState(state)
  uni.navigateTo({ url: '/pages/file-preview/index' })
}

async function insertTempImageToChat(localPath: string, type: MessageType, id = `image-${Date.now()}`) {
  // 先模拟“处理/落地”的时间，后续接真实上传接口时只替换这里。
  await new Promise(resolve => setTimeout(resolve, 300))
  pushImageMessage({
    id,
    type,
    imageUrl: localPath,
  })
}

function cancelSttByManualInput() {
  if (!isSttRecording.value && !isSttTranscribing.value)
    return

  shouldCancelStt = true
  sttRequestSeq++
  isSttTranscribing.value = false

  if (isSttRecording.value) {
    isSttRecording.value = false
    getSttRecorderManager().stop()
  }
}

const onSttButtonClick = () => {
  if (isSttTranscribing.value)
    return

  if (isSttRecording.value) {
    isSttRecording.value = false
    getSttRecorderManager().stop()
    return
  }

  shouldCancelStt = false
  sttRecordStartTime = Date.now()
  isSttRecording.value = true
  activeRecorderPurpose = 'speech-to-text'
  uni.hideKeyboard()
  keyboardHeight.value = 0
  uni.vibrateShort({ type: 'light' })

  getSttRecorderManager().start({
    duration: 60000,
    format: 'mp3',
  })
}

async function onSttRecorderStop(res: RecorderStopResult) {
  activeRecorderPurpose = ''
  const duration = Date.now() - sttRecordStartTime

  if (shouldCancelStt) {
    shouldCancelStt = false
    isSttRecording.value = false
    isSttTranscribing.value = false
    return
  }

  if (duration < 800) {
    isSttRecording.value = false
    uni.showToast({ title: '说话时间太短', icon: 'none' })
    return
  }

  const currentSeq = ++sttRequestSeq
  isSttRecording.value = false
  isSttTranscribing.value = true

  try {
    const text = await speechToText(res.tempFilePath)
    if (shouldCancelStt || currentSeq !== sttRequestSeq)
      return

    if (text) {
      inputMessage.value = inputMessage.value
        ? `${inputMessage.value}${text}`
        : text
      nextTick(() => {
        measureInputBar()
      })
    }
  } catch (error) {
    if (!shouldCancelStt && currentSeq === sttRequestSeq)
      uni.showToast({ title: '语音转文字失败', icon: 'none' })
  } finally {
    if (currentSeq === sttRequestSeq) {
      isSttTranscribing.value = false
      shouldCancelStt = false
    }
  }
}

const onVoiceTouchStart = (event: TouchEvent) => {
  if (isTranscribing.value || isSttRecording.value || isSttTranscribing.value)
    return

  const touch = event.touches?.[0]
  recordTouchStartY = touch?.clientY ?? 0
  recordStartTime = Date.now()
  shouldCancelRecord = false
  isRecording.value = true
  isRecordCanceling.value = false
  manualPanelVisible.value = false
  activeRecorderPurpose = 'voice-message'
  uni.vibrateShort({ type: 'light' })

  startVoiceRecorder()
}

const onVoiceTouchMove = (event: TouchEvent) => {
  if (!isRecording.value)
    return

  const touch = event.touches?.[0]
  const currentY = touch?.clientY ?? recordTouchStartY
  shouldCancelRecord = recordTouchStartY - currentY > 70
  isRecordCanceling.value = shouldCancelRecord
}

const onVoiceTouchEnd = () => {
  if (!isRecording.value)
    return

  isRecording.value = false
  getRecorderManager().stop()
}

const onVoiceTouchCancel = () => {
  if (!isRecording.value)
    return

  shouldCancelRecord = true
  isRecording.value = false
  isRecordCanceling.value = false
  getRecorderManager().stop()
}

async function onRecorderStop(res: RecorderStopResult) {
  activeRecorderPurpose = ''
  const duration = Date.now() - recordStartTime
  const canceled = shouldCancelRecord
  shouldCancelRecord = false
  isRecordCanceling.value = false

  if (canceled)
    return

  if (duration < 800) {
    uni.showToast({ title: '说话时间太短', icon: 'none' })
    return
  }

  stopVoicePlayback()
  pushMessage({
    id: `voice-${Date.now()}`,
    type: 'self',
    contentType: 'voice',
    voicePath: res.tempFilePath,
    duration: duration / 1000,
  }, { forceScroll: true })
}


const onPlusClick = () => {
  manualPanelVisible.value = true
  // 如果是语音消息的模式，则切换为键盘模式并聚焦
  if (inputMode.value === 'voice') {
    textareaFocus.value = false
    inputMode.value = 'keyboard'
    textareaFocus.value = true
  }
  if (keyboardHeight.value > 0) {
    manualPanelVisible.value = true
    uni.hideKeyboard()
  } else {
    if (manualPanelVisible.value) {
      scrollToBottom()
    }
  }
}

const onActionItemClick = (key: ActionKey) => {
  if (key === 'image' || key === 'camera') {
    handleImageAction(key)
    return
  }

  if (key === 'file') {
    handleFileAction()
    return
  }

  if (key === 'favorite') {
    uni.navigateTo({
      url: '/pages/voiceCall/index',
    })
    return
  }

  const label = actionItems.find(item => item.key === key)?.label ?? ''
  uni.showToast({ title: label, icon: 'none' })
}

async function handleFileAction() {
  try {
    const files = await chooseChatFiles(9)
    if (!files.length)
      return

    const chatFiles = files.map(file => createChatFileItem(file))
    chatFiles.forEach((file) => {
      pushFileMessage({
        id: file.id,
        type: 'self',
        file,
      })
    })
    manualPanelVisible.value = false
  } catch (error) {
    // uni.showToast({ title: '选择文件失败，请重试', icon: 'none' })
  }
}

async function handleImageAction(key: 'image' | 'camera') {
  const isCamera = key === 'camera'

  try {
    const chooseResult = await new Promise<{ tempFilePath: string }>((resolve, reject) => {
      const handleSuccess = (tempFilePath: string) => {
        if (!tempFilePath)
          reject(new Error('未获取到图片'))
        else
          resolve({ tempFilePath })
      }

      if (isCamera) {
        // #ifdef MP-WEIXIN
        uni.chooseMedia({
          count: 1,
          mediaType: ['image'],
          sourceType: ['camera'],
          success: (res) => handleSuccess(res.tempFiles[0]?.tempFilePath ?? ''),
          fail: reject,
        })
        // #endif
        // #ifndef MP-WEIXIN
        uni.chooseImage({
          count: 1,
          sourceType: ['camera'],
          success: (res) => handleSuccess(res.tempFilePaths[0] ?? ''),
          fail: reject,
        })
        // #endif
        return
      }

      // #ifdef MP-WEIXIN
      uni.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: ['album'],
        success: (res) => handleSuccess(res.tempFiles[0]?.tempFilePath ?? ''),
        fail: reject,
      })
      // #endif
      // #ifndef MP-WEIXIN
      uni.chooseImage({
        count: 1,
        sourceType: ['album'],
        success: (res) => handleSuccess(res.tempFilePaths[0] ?? ''),
        fail: reject,
      })
      // #endif
    })

    await insertTempImageToChat(chooseResult.tempFilePath, 'self')
    manualPanelVisible.value = false
  } catch (error) {
    // uni.showToast({ title: isCamera ? '拍照失败' : '选择图片失败', icon: 'none' })
  }
}

function onImagePreview(messageId: string) {
  const urls = imagePreviewItems.value.map(item => item.url)
  const target = imagePreviewItems.value.find(item => item.id === messageId)

  if (!target)
    return

  uni.previewImage({
    urls,
    current: target.url,
  })
}

function onFilePreview(messageId: string) {
  const fileMessages = displayMessages.value.filter(item => item.contentType === 'file' && item.file)
  if (!fileMessages.length)
    return

  const current = fileMessages.find(item => item.id === messageId)
  if (!current?.file)
    return

  if (!isTextPreviewFile(current.file.ext) || isLargeTextPreviewFile(current.file.size)) {
    const file = current.file
    uni.openDocument({
      filePath: file.remoteUrl || file.localPath,
      fileType: file.ext as any,
      showMenu: true,
    })
    return
  }

  const files = fileMessages.map(item => item.file!)
  openFilePreview(files, messageId)
}

const onSendClick = () => {
  if (inputMessage.value.trim()) {
    sendMessage()
  } else {
    onPlusClick()
  }
}

// 监听键盘高度
const listener = function ({ height }) {
  textareaFocus.value = height > 0
}

onLoad(() => {
  nextTick(() => {
    measureScrollView()
    measureInputBar()
    setTimeout(() => {
      scrollToBottom()
    }, 0)
  })
  uni.onKeyboardHeightChange(listener)
})

onReady(() => {
  setTimeout(() => {
    scrollToBottom()
  }, 0)
})

onUnload(() => {
  if (isSttRecording.value) {
    shouldCancelStt = true
    getSttRecorderManager().stop()
  }
  stopVoicePlayback()
  audioContext?.destroy()
  audioContext = null

  uni.offKeyboardHeightChange(listener)
})

const isPreventBack = true

onBackPress((options) => {
  if (isPreventBack) {
    return true // 阻止返回
  }
  return false // 允许返回
})

</script>

<style lang="scss" scoped>
page {
  background-color: #EDEDED;
}

.refresher-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16rpx 0;
  opacity: 1;
  transition: opacity 0.3s ease-out;
}

.refresher-indicator--fade {
  opacity: 0;
}

.refresher-indicator__spinner {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid #e0e0e0;
  border-top-color: #2065EB;
  border-radius: 50%;
  animation: refresher-spin 0.8s linear infinite;
}

.refresher-indicator__text {
  font-size: 22rpx;
  color: #999;
  margin-top: 12rpx;
}

@keyframes refresher-spin {
  to {
    transform: rotate(360deg);
  }
}

.new-msg-tip {
  position: fixed;
  left: 24rpx;
  z-index: 10;
  padding: 16rpx 28rpx;
  background: rgba(0, 0, 0, 0.65);
  border-radius: 999rpx;
  animation: tip-bounce 1.2s ease-in-out infinite;
}

.new-msg-tip__text {
  font-size: 24rpx;
  color: #fff;
  white-space: nowrap;
}

.voice-record-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.voice-record-panel {
  width: 300rpx;
  height: 260rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 32rpx;
  background: rgba(0, 0, 0, 0.72);
  border-radius: 24rpx;
}

.voice-record-panel--cancel {
  background: rgba(196, 40, 40, 0.78);
}

.voice-record-wave {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.voice-record-wave__bar {
  width: 14rpx;
  height: 36rpx;
  background: #fff;
  border-radius: 999rpx;
  animation: voice-wave 0.75s ease-in-out infinite;
}

.voice-record-wave__bar--1 {
  animation-delay: -0.3s;
}

.voice-record-wave__bar--2 {
  animation-delay: -0.15s;
}

.voice-record-wave__bar--3 {
  animation-delay: 0s;
}

.voice-record-wave__bar--4 {
  animation-delay: 0.15s;
}

.voice-record-panel__text {
  font-size: 26rpx;
  color: #fff;
}

.chat-avatar {
  width: 70rpx;
  height: 70rpx;
  flex-shrink: 0;
  overflow: hidden;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.06);
  border-radius: 8rpx;
}

.chat-avatar__image {
  display: block;
  width: 100%;
  height: 100%;
}

.chat-input-bar {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 9;
  box-sizing: border-box;
  background: #F7F7F7;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.05);
  transition-property: bottom;
  transition-timing-function: ease-out;
}

.chat-input-row {
  display: flex;
  align-items: flex-end;
  gap: 20rpx;
  box-sizing: border-box;
  padding: 20rpx 24rpx;
}


.chat-input-icon {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: 8rpx;
}

.chat-input-send {
  position: relative;
  width: 100rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: 8rpx;
}

.chat-input-send__icon {
  font-size: 36rpx;
  position: absolute;
  transition: opacity 0.4s ease, transform 0.4s ease;
  will-change: transform, opacity;
}

.chat-input-send__text-wrap {
  overflow: hidden;
  white-space: nowrap;
  max-width: 40rpx;
  opacity: 0;
  transition: max-width 0.45s cubic-bezier(.34, 1.56, .64, 1),
  opacity 0.45s ease;
  will-change: max-width, opacity;
}

.chat-input-send__text {
  height: 1.5em;
  line-height: 1.5em;
  font-size: 28rpx;
  color: #fff;
  background: #2065EB;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
  white-space: nowrap;
  display: inline-block;
}

.chat-input-send--active .chat-input-send__text-wrap {
  max-width: 200rpx;
  opacity: 1;
}

.chat-input-send--active .chat-input-send__icon {
  opacity: 0;
  transform: scaleX(0);
  pointer-events: none;
}

.chat-image-message-shell {
  max-width: 70%;
}

.chat-image-message-shell--self {
  display: flex;
  justify-content: flex-end;
}

.chat-file-message-shell {
  max-width: 70%;
}

.chat-file-message-shell--self {
  display: flex;
  justify-content: flex-end;
}

.chat-input-wrap {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 10rpx 12rpx 10rpx 24rpx;
  background: #FFFFFF;
  border-radius: 8rpx;
  box-sizing: border-box;
}

.chat-stt-button {
  width: 52rpx;
  height: 52rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(75, 85, 99, 0.18);
  border-radius: 50%;
  color: #4B5563;
  background: #fff;
}

.chat-stt-button--recording {
  border-color: rgba(32, 101, 235, 0.6);
  animation: stt-recording-pulse 1.4s ease-in-out infinite;
}

.chat-stt-button--loading {
  opacity: 0.7;
}

.chat-stt-button__icon {
  font-size: 30rpx;
}

.chat-voice-button {
  flex: 1;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8rpx;
  color: #333;
  font-size: 28rpx;
  font-weight: 500;
  user-select: none;
}

.chat-voice-button--active {
  background: #E5E7EB;
}

.chat-textarea {
  display: block;
  flex: 1;
  min-width: 0;
  padding: 8rpx 0;
  font-size: 28rpx;
  line-height: 1.5;
  box-sizing: border-box;
  max-height: 268rpx;
  overflow-y: auto;
}

.chat-action-panel {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 8;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: auto 1fr;
  gap: 20rpx;
  box-sizing: border-box;
  padding: 18rpx 32rpx 32rpx;
  background: #F7F7F7;
  overflow: hidden;
  transition-property: height;
  transition-timing-function: ease-out;
}

.chat-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.chat-action-item__icon-wrap {
  width: 112rpx;
  height: 112rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.06);
  border-radius: 18rpx;
}

.chat-action-item__icon {
  font-size: 46rpx;
  color: #374151;
}

.chat-action-item__text {
  font-size: 24rpx;
  color: #6B7280;
}

@keyframes tip-bounce {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-8rpx);
  }
}

@keyframes voice-wave {
  0%,
  100% {
    transform: scaleY(0.55);
    opacity: 0.65;
  }

  50% {
    transform: scaleY(1.65);
    opacity: 1;
  }
}

@keyframes stt-recording-pulse {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(32, 101, 235, 0.12);
    transform: scale(1);
  }

  50% {
    box-shadow: 0 0 0 8rpx rgba(32, 101, 235, 0.04);
    transform: scale(1.03);
  }
}
</style>
