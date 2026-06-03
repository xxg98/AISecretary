<script setup lang="ts">
import { CallState } from '@/websocket/types'
import { computed, ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps<{
  callState: CallState
  audioLevel: number
  connected: boolean
}>()

/** 柱状条高度数组（模拟波形） */
const barHeights = ref<number[]>([4, 4, 4, 4, 4, 4, 4, 4, 4, 4])

/** 动画定时器 */
let animTimer: ReturnType<typeof setInterval> | null = null

/** 柱状条颜色 */
const barColor = computed(() => {
  switch (props.callState) {
    case CallState.USER_SPEAKING: return '#4CAF50'
    case CallState.ASSISTANT_SPEAKING: return '#2065EB'
    case CallState.THINKING: return '#FF9800'
    case CallState.LISTENING: return '#BDBDBD'
    case CallState.ERROR: return '#F44336'
    default: return '#E0E0E0'
  }
})

/** 是否显示活动动画 */
const isActive = computed(() =>
  props.callState === CallState.USER_SPEAKING
  || props.callState === CallState.ASSISTANT_SPEAKING,
)

/** 开始波形动画 */
function startAnimation(): void {
  stopAnimation()
  animTimer = setInterval(() => {
    barHeights.value = barHeights.value.map(() => {
      // 基于 audioLevel 或随机生成柱状高度
      const base = props.audioLevel > 0.01 ? props.audioLevel : Math.random() * 0.6 + 0.1
      const variance = (Math.random() - 0.5) * 0.3
      return Math.max(2, Math.min(40, base * 40 + variance * 40))
    })
  }, 120)
}

/** 停止波形动画 */
function stopAnimation(): void {
  if (animTimer !== null) {
    clearInterval(animTimer)
    animTimer = null
  }
  // 重置为一排短柱
  barHeights.value = barHeights.value.map(() => 4)
}

/** 同时监听 active 和 connected 状态 */
watch(
  [isActive, () => props.connected],
  ([active, isConn]) => {
    if (active && isConn) {
      startAnimation()
    }
    else {
      stopAnimation()
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  stopAnimation()
})
</script>

<template>
  <view class="flex justify-center items-end gap-[8rpx] h-[80rpx] py-[16rpx]">
    <view
      v-for="(h, i) in barHeights"
      :key="i"
      class="rounded-[4rpx] transition-all duration-100 ease-linear"
      :style="{
        width: '6rpx',
        height: `${h}rpx`,
        backgroundColor: barColor,
      }"
    />
  </view>
</template>
