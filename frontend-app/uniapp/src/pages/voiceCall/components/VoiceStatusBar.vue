<script setup lang="ts">
import { ConnectionState, CallState } from '@/websocket/types'
import { computed } from 'vue'

const props = defineProps<{
  connectionState: ConnectionState
  callState: CallState
}>()

/** 连接状态文本映射 */
const connectionText = computed(() => {
  switch (props.connectionState) {
    case ConnectionState.IDLE: return '未连接'
    case ConnectionState.CONNECTING: return '连接中...'
    case ConnectionState.CONNECTED: return '已连接'
    case ConnectionState.DISCONNECTING: return '断开中...'
    case ConnectionState.RECONNECTING: return '重连中...'
    case ConnectionState.ERROR: return '连接失败'
    default: return '未知'
  }
})

/** 连接状态圆点颜色 */
const dotColor = computed(() => {
  switch (props.connectionState) {
    case ConnectionState.CONNECTED: return '#4CAF50'
    case ConnectionState.CONNECTING:
    case ConnectionState.RECONNECTING: return '#FF9800'
    case ConnectionState.ERROR: return '#F44336'
    default: return '#9E9E9E'
  }
})

/** 通话状态文本映射 */
const callStateText = computed(() => {
  switch (props.callState) {
    case CallState.IDLE: return '等待开始'
    case CallState.LISTENING: return '正在聆听...'
    case CallState.USER_SPEAKING: return '正在识别您说的话...'
    case CallState.THINKING: return 'AI 思考中...'
    case CallState.ASSISTANT_SPEAKING: return 'AI 正在回复...'
    case CallState.INTERRUPTED: return '已打断'
    case CallState.ERROR: return '出错了'
    default: return ''
  }
})
</script>

<template>
  <view class="flex items-center justify-between px-[32rpx] py-[16rpx] bg-white/80 backdrop-blur-sm">
    <!-- 连接状态 -->
    <view class="flex items-center gap-[12rpx]">
      <view
        class="w-[16rpx] h-[16rpx] rounded-full transition-all duration-300"
        :class="[
          connectionState === ConnectionState.CONNECTING || connectionState === ConnectionState.RECONNECTING
            ? 'animate-pulse'
            : '',
        ]"
        :style="{ backgroundColor: dotColor }"
      />
      <text class="text-[24rpx] color-[#666]">
        {{ connectionText }}
      </text>
    </view>

    <!-- 通话状态 -->
    <view class="flex items-center gap-[8rpx]">
      <view
        v-if="callState === CallState.ASSISTANT_SPEAKING"
        class="i-carbon-volume-up text-[28rpx] color-[#2065EB]"
      />
      <view
        v-else-if="callState === CallState.USER_SPEAKING"
        class="i-carbon-microphone text-[28rpx] color-[#4CAF50]"
      />
      <text
        class="text-[24rpx]"
        :class="[
          callState === CallState.ERROR ? 'color-[#F44336]' : 'color-[#333]',
        ]"
      >
        {{ callStateText }}
      </text>
    </view>
  </view>
</template>

<style scoped>
/* 脉冲动画 */
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
.animate-pulse {
  animation: pulse 1.5s ease-in-out infinite;
}
</style>
