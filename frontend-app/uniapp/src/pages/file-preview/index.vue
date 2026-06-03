<template>
  <view class="file-preview-page">
    <swiper
      class="file-preview-page__swiper"
      :current="currentIndex"
      circular
      @change="onChange"
    >
      <swiper-item v-for="item in files" :key="item.id" class="file-preview-page__item">
        <scroll-view scroll-y class="file-preview-page__scroll">
          <view class="file-preview-page__card">
            <view class="file-preview-page__name">{{ item.name }}</view>
            <view class="file-preview-page__meta">{{ item.ext ? item.ext.toUpperCase() : 'FILE' }} ·
              {{ formatFileSize(item.size) }}
            </view>

            <view v-if="canInlinePreviewText(item)" class="file-preview-page__text">
              <text selectable>{{ fileTextMap[item.id] || '正在读取...' }}</text>
            </view>

            <view v-else class="file-preview-page__tip">
              <text>{{ isPlainTextFile(item.ext) ? '文件较大，将使用系统能力打开预览。' : '该文件类型将使用系统能力打开预览。' }}</text>
              <button class="file-preview-page__button" @click="openFile(item)">打开文件</button>
            </view>
          </view>
        </scroll-view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup lang="ts">
import {
  clearChatFilePreviewState,
  formatFileSize,
  isLargeTextPreviewFile,
  isOfficePreviewFile,
  isTextPreviewFile,
  readChatFilePreviewState,
  readChatTextFile
} from '@/utils/chatFile'

const previewState = readChatFilePreviewState()
const files = ref(previewState?.files ?? [])
const currentId = ref(previewState?.currentId ?? '')
const currentIndex = computed(() => Math.max(0, files.value.findIndex(item => item.id === currentId.value)))
const fileTextMap = reactive<Record<string, string>>({})

function onChange(event: any) {
  const current = files.value[event.detail.current]
  if (current)
    currentId.value = current.id
}

function isPlainTextFile(ext: string) {
  return isTextPreviewFile(ext)
}

function canInlinePreviewText(item: typeof files.value[number]) {
  return isPlainTextFile(item.ext) && !isLargeTextPreviewFile(item.size)
}

async function loadTextPreview(item: typeof files.value[number]) {
  if (!canInlinePreviewText(item))
    return

  if (fileTextMap[item.id])
    return

  const text = await readChatTextFile(item.localPath)
  fileTextMap[item.id] = text || '暂无可预览内容'
}

function openFile(item: typeof files.value[number]) {
  uni.openDocument({
    filePath: item.remoteUrl || item.localPath,
    fileType: item.ext as any,
    showMenu: true,
  })
}

onMounted(() => {
  files.value.forEach((item) => {
    if (canInlinePreviewText(item))
      loadTextPreview(item)
  })
})

watch(currentIndex, (index) => {
  const item = files.value[index]
  if (item && canInlinePreviewText(item))
    loadTextPreview(item)
}, { immediate: true })

onUnmounted(() => {
  clearChatFilePreviewState()
})
</script>

<style lang="scss" scoped>
.file-preview-page {
  min-height: 100vh;
  background: #f5f5f5;
  color: #333;
}

.file-preview-page__close {
  font-size: 26rpx;
  color: #666;
}

.file-preview-page__swiper {
  height: calc(100vh - 120rpx);
}

.file-preview-page__item,
.file-preview-page__scroll {
  height: 100%;
}

.file-preview-page__scroll {
  box-sizing: border-box;
  padding: 24rpx 32rpx 40rpx;
}

.file-preview-page__card {
  min-height: 100%;
  box-sizing: border-box;
  padding: 28rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.file-preview-page__name {
  font-size: 30rpx;
  font-weight: 600;
}

.file-preview-page__meta {
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #999;
}

.file-preview-page__text {
  margin-top: 24rpx;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 26rpx;
  line-height: 1.7;
}

.file-preview-page__tip {
  margin-top: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.file-preview-page__button {
  width: 220rpx;
  margin: 0;
  background: #2563eb;
  color: #fff;
}
</style>
