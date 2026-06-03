<template>
  <view class="chat-voice-message" :class="{ 'chat-voice-message--self': isSelf }" @click="emitPlay">
    <view class="chat-voice-message__waves" :class="{ 'chat-voice-message__waves--playing': isPlaying }">
      <view class="chat-voice-message__wave chat-voice-message__wave--1" />
      <view class="chat-voice-message__wave chat-voice-message__wave--2" />
      <view class="chat-voice-message__wave chat-voice-message__wave--3" />
    </view>
    <text class="chat-voice-message__duration">{{ durationText }}</text>
  </view>
</template>

<script setup lang="ts">
const props = defineProps<{
  messageId: string
  duration: number
  isSelf?: boolean
  isPlaying?: boolean
}>()

const emit = defineEmits<{
  play: [messageId: string]
}>()

const durationText = computed(() => `${Math.max(1, Math.round(props.duration))}"`)

function emitPlay() {
  emit('play', props.messageId)
}
</script>

<style lang="scss" scoped>
.chat-voice-message {
  min-width: 168rpx;
  max-width: 360rpx;
  height: 72rpx;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 28rpx;
  box-sizing: border-box;
  padding: 0 24rpx;
  background: #fff;
  border-radius: 8rpx;
  color: #333;
}

.chat-voice-message--self {
  flex-direction: row-reverse;
  background: #2065EB;
  color: #fff;
}

.chat-voice-message__waves {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.chat-voice-message__wave {
  width: 8rpx;
  height: 18rpx;
  background: currentColor;
  border-radius: 999rpx;
  opacity: 0.72;
}

.chat-voice-message__waves--playing .chat-voice-message__wave {
  animation: chat-voice-playing 0.72s ease-in-out infinite;
}

.chat-voice-message__wave--1 {
  height: 14rpx;
}

.chat-voice-message__wave--2 {
  height: 24rpx;
  animation-delay: 0.12s;
}

.chat-voice-message__wave--3 {
  height: 34rpx;
  animation-delay: 0.24s;
}

.chat-voice-message__duration {
  font-size: 28rpx;
  line-height: 1;
}

@keyframes chat-voice-playing {
  0%,
  100% {
    transform: scaleY(0.6);
    opacity: 0.55;
  }

  50% {
    transform: scaleY(1.2);
    opacity: 1;
  }
}
</style>
