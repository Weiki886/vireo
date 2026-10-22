<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

// useRouter / useRoute 依赖组件注入，必须在 setup 顶层调用
const router = useRouter()
const route = useRoute()

// 菜单项：key 直接使用路由 path，点击时可直接用于跳转
const items = ref<MenuProps['items']>([
  { key: '/', label: '首页' },
  { key: '/about', label: '关于' },
])

// 当前高亮的菜单项
const selectedKeys = ref<string[]>([])

// 跟随当前路由同步高亮，覆盖刷新页面、浏览器前进/后退等非点击场景
watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
  },
  { immediate: true },
)

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}
</script>

<template>
  <a-menu :selectedKeys="selectedKeys" mode="horizontal" :items="items" @click="handleMenuClick" />
</template>
