<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { message, type MenuProps } from 'ant-design-vue'
import { LogoutOutlined } from '@ant-design/icons-vue'

import { useLoginUserStore } from '@/router/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import vireoLogo from '@/assets/vireo-logo.png'

const loginUserStore = useLoginUserStore()

// useRouter / useRoute 依赖组件注入，必须在 setup 顶层调用
const router = useRouter()
const route = useRoute()

// 菜单项：key 直接使用路由 path，点击时可直接用于跳转
const items = computed<MenuProps['items']>(() => [
  { key: '/', label: '首页' },
  ...(loginUserStore.loginUser.userRole === 'admin'
    ? [{ key: '/admin/userManage', label: '用户管理' }]
    : []),
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

const logoutLoading = ref(false)

const doLogout = async () => {
  if (logoutLoading.value) {
    return
  }
  logoutLoading.value = true
  try {
    const res = await userLogout()
    if (res.data.code !== 0) {
      message.error('退出登录失败，' + (res.data.message ?? '请稍后重试'))
      return
    }
    loginUserStore.setLoginUser({ userName: '未登录' })
    message.success('退出登录成功')
    await router.push('/user/login')
  } catch {
    // 网络错误由全局响应拦截器统一提示。
  } finally {
    logoutLoading.value = false
  }
}
</script>

<template>
  <header class="global-header">
    <RouterLink class="global-header__brand" to="/" aria-label="Vireo 首页">
      <img class="global-header__brand-logo" :src="vireoLogo" alt="" />
      <span class="global-header__brand-name">Vireo</span>
    </RouterLink>

    <a-menu
      class="global-header__menu"
      :selectedKeys="selectedKeys"
      mode="horizontal"
      :items="items"
      @click="handleMenuClick"
    />

    <div class="global-header__user">
      <a-dropdown v-if="loginUserStore.loginUser.id" :trigger="['hover']">
        <a-space class="global-header__profile">
          <a-avatar :src="loginUserStore.loginUser.userAvatar" />
          <span>{{ loginUserStore.loginUser.userName || '未命名' }}</span>
        </a-space>
        <template #overlay>
          <a-menu>
            <a-menu-item key="logout" :disabled="logoutLoading" @click="doLogout">
              <LogoutOutlined />
              退出登录
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
      <a-button v-else type="primary" @click="router.push('/user/login')">登录</a-button>
    </div>
  </header>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  gap: 28px;
  height: 64px;
  padding: 0 clamp(20px, 4vw, 72px) 0 clamp(12px, 2vw, 32px);
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}

.global-header__brand {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  gap: 7px;
  color: #1f1f1f;
  text-decoration: none;
}

.global-header__brand:hover {
  color: #1f1f1f;
}

.global-header__brand:focus-visible {
  outline: 2px solid #1677ff;
  outline-offset: 4px;
  border-radius: 4px;
}

.global-header__brand-logo {
  display: block;
  width: 30px;
  height: 30px;
  /* 原始 PNG 自带较多透明留白，cover 后鸟形才能填满 30px 的视觉框。 */
  object-fit: cover;
  object-position: 65% 50%;
}

.global-header__brand-name {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0;
  line-height: 22px;
}

.global-header__menu {
  flex: 1;
  min-width: 0;
  border-bottom: 0;
}

.global-header__user {
  flex-shrink: 0;
}

.global-header__profile {
  cursor: pointer;
}

@media (max-width: 600px) {
  .global-header {
    gap: 16px;
    padding: 0 20px;
  }

  .global-header__brand-name {
    display: none;
  }
}
</style>
