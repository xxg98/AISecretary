<template>
  <view
    class="chat-image-message"
    :class="{
      'chat-image-message--self': isSelf,
      'chat-image-message--portrait': imageOrientation === 'portrait',
      'chat-image-message--landscape': imageOrientation === 'landscape',
    }"
    @click="emitPreview"
  >
    <image
      class="chat-image-message__image"
      :src="imageUrl"
      mode="widthFix"
    />
  </view>
</template>

<script setup lang="ts">
const props = defineProps<{
  messageId: string
  imageUrl: string
  isSelf?: boolean
}>()

const emit = defineEmits<{
  preview: [messageId: string]
}>()

type ImageOrientation = 'portrait' | 'landscape'

const imageOrientation = ref<ImageOrientation>('landscape')

onMounted(() => {
  uni.getImageInfo({
    src: props.imageUrl,
    success: (res) => {
      imageOrientation.value = res.height > res.width ? 'portrait' : 'landscape'
    },
    fail: () => {
      imageOrientation.value = 'landscape'
    },
  })
})

function emitPreview() {
  emit('preview', props.messageId)
}
</script>

<style lang="scss" scoped>
.chat-image-message {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 12rpx;
  background: #f3f4f6;
}

.chat-image-message--self {
  margin-left: auto;
}

.chat-image-message--landscape {
  max-width: 50vw;
}

.chat-image-message--portrait {
  width: 220rpx;
}

.chat-image-message__image {
  display: block;
}
</style>
