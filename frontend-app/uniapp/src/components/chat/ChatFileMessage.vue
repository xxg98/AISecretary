<template>
  <view class="chat-file-message" :class="{ 'chat-file-message--self': isSelf }" @click="emitPreview">
    <view class="chat-file-message__icon-wrap">
      <view class="chat-file-message__icon i-carbon-document" />
    </view>
    <view class="chat-file-message__content">
      <text class="chat-file-message__name">{{ file.name }}</text>
      <text class="chat-file-message__meta">{{ metaText }}</text>
      <text class="chat-file-message__status" v-if="statusText">{{ statusText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { ChatFileItem } from '@/utils/chatFile'
import { formatFileSize } from '@/utils/chatFile'

const props = defineProps<{
  messageId: string
  file: ChatFileItem
  isSelf?: boolean
}>()

const emit = defineEmits<{
  preview: [messageId: string]
}>()

const metaText = computed(() => `${props.file.ext ? props.file.ext.toUpperCase() : 'FILE'} · ${formatFileSize(props.file.size)}`)

const statusText = computed(() => {
  if (props.file.status === 'uploading')
    return '上传中'
  if (props.file.status === 'failed')
    return '上传失败'
  if (props.file.status === 'uploaded')
    return '已上传'
  return '本地文件'
})

function emitPreview() {
  emit('preview', props.messageId)
}
</script>

<style lang="scss" scoped>
.chat-file-message {
  width: 420rpx;
  max-width: 72vw;
  display: flex;
  gap: 20rpx;
  box-sizing: border-box;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.chat-file-message--self {
  margin-left: auto;
  background: #eef4ff;
}

.chat-file-message__icon-wrap {
  width: 72rpx;
  height: 72rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  background: #2065eb;
  color: #fff;
}

.chat-file-message__icon {
  font-size: 36rpx;
}

.chat-file-message__content {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.chat-file-message__name {
  font-size: 28rpx;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-file-message__meta,
.chat-file-message__status {
  font-size: 22rpx;
  color: #6b7280;
}
</style>
