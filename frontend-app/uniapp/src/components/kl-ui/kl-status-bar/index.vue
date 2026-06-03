<template>
  <view
    :style="{ height: `${statusBarHeight}px`, background, zIndex }"
    class="w-full" :class="{ 'sticky top-0': topSticky }"
  />
</template>

<script lang="ts" setup>
// 定义 Props 类型
interface StatusBarProps {
  /** 背景色 */
  background?: string
  /** 是否吸顶 */
  topSticky?: boolean
  /** 吸顶时的层级 */
  zIndexNumber?: number
}

// Props 默认值
const props = withDefaults(defineProps<StatusBarProps>(), {
  background: '#F8F8F8',
  topSticky: false,
  zIndexNumber: 9999,
})

const { background, topSticky, zIndexNumber } = toRefs(props)

// 计算层级：吸顶时使用配置层级，否则不占层级
const zIndex = computed(() => (topSticky.value ? zIndexNumber.value : 0))

// 状态栏高度
const statusBarHeight = ref<number>(20)

onMounted(() => {
  try {
    const systemInfo = uni.getSystemInfoSync()
    statusBarHeight.value = systemInfo.statusBarHeight ?? 20
  } catch (error) {
    console.warn('获取状态栏高度失败，使用默认值', error)
  }
})
</script>

<style lang="scss" scoped>
</style>
