<script setup lang="ts">
import { CallState } from '@/websocket/types'
import { ref } from 'vue'

const props = defineProps<{
  callState: CallState
  connected: boolean
  textInputMode: boolean
}>()

const emit = defineEmits<{
  (e: 'toggleTextInput'): void
  (e: 'sendText', text: string): void
  (e: 'interrupt'): void
  (e: 'hangup'): void
}>()

/** 文本输入内容 */
const textInput = ref('')

/** 是否正在展示文本输入框 */
const showTextInput = ref(false)

/** 切换文本输入框显示 */
function toggleTextInput(): void {
  showTextInput.value = !showTextInput.value
  if (!showTextInput.value) {
    textInput.value = ''
  }
  emit('toggleTextInput')
}

/** 发送文本 */
function handleSendText(): void {
  const trimmed = textInput.value.trim()
  if (!trimmed) return
  emit('sendText', trimmed)
  textInput.value = ''
  showTextInput.value = false
}

/** AI 是否正在说话（可被打断的状态） */
const canInterrupt = computed(() =>
  props.callState === CallState.ASSISTANT_SPEAKING,
)
</script>

<template>
  <view class="flex flex-col gap-[24rpx] px-[32rpx] pb-[48rpx]">
    <!-- 文本输入区域 -->
    <view v-if="showTextInput" class="flex items-center gap-[16rpx]">
      <input
        v-model="textInput"
        class="flex-1 h-[72rpx] px-[24rpx] bg-[#F5F5F5] rounded-[36rpx] text-[28rpx] color-[#333]"
        placeholder="输入文本发送给 AI..."
        placeholder-style="color:#999;font-size:28rpx"
        confirm-type="send"
        :focus="true"
        @confirm="handleSendText"
      />
      <button
        class="w-[120rpx] h-[72rpx] rounded-[36rpx] bg-[#2065EB] text-white text-[26rpx]"
        @tap="handleSendText"
      >
        发送
      </button>
    </view>

    <!-- 底部按钮栏 -->
    <view class="flex items-center justify-between" :class="showTextInput ? '' : 'pt-[16rpx]'">
      <!-- 文本输入按钮 -->
      <view
        class="flex flex-col items-center gap-[8rpx]"
        @tap="toggleTextInput"
      >
        <view
          class="w-[88rpx] h-[88rpx] rounded-full flex items-center justify-center"
          :class="showTextInput ? 'bg-[#2065EB]' : 'bg-[#F0F0F0]'"
        >
          <view
            class="i-carbon-keyboard text-[40rpx]"
            :class="showTextInput ? 'color-white' : 'color-[#666]'"
          />
        </view>
        <text class="text-[22rpx] color-[#999]">文本</text>
      </view>

      <!-- 打断按钮 -->
      <view
        class="flex flex-col items-center gap-[8rpx]"
        @tap="emit('interrupt')"
      >
        <view
          class="w-[96rpx] h-[96rpx] rounded-full flex items-center justify-center"
          :class="canInterrupt ? 'bg-[#FF9800]' : 'bg-[#F0F0F0]'"
        >
          <view
            class="i-carbon-pause-filled text-[44rpx]"
            :class="canInterrupt ? 'color-white' : 'color-[#BDBDBD]'"
          />
        </view>
        <text
          class="text-[22rpx]"
          :class="canInterrupt ? 'color-[#FF9800]' : 'color-[#BDBDBD]'"
        >
          打断
        </text>
      </view>

      <!-- 挂断按钮 -->
      <view
        class="flex flex-col items-center gap-[8rpx]"
        @tap="emit('hangup')"
      >
        <view class="w-[96rpx] h-[96rpx] rounded-full bg-[#F44336] flex items-center justify-center">
          <view class="i-carbon-phone-filled text-[44rpx] color-white transform rotate-[135deg]" />
        </view>
        <text class="text-[22rpx] color-[#F44336]">挂断</text>
      </view>
    </view>
  </view>
</template>
