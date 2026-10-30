<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

import { userLogin } from '@/api/userController.ts'
import AuthPageShell from '@/components/AuthPageShell.vue'
import { useLoginUserStore } from '@/router/loginUser.ts'
import { resolveLoginRedirect } from '@/router/redirect.ts'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const submitLoading = ref(false)

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const handleSubmit = async (values: API.UserLoginRequest) => {
  if (submitLoading.value) {
    return
  }
  submitLoading.value = true
  try {
    const res = await userLogin(values)
    if (res.data.code === 0 && res.data.data) {
      loginUserStore.setLoginUser(res.data.data)
      message.success('登录成功')
      await router.replace(resolveLoginRedirect(route.query.redirect))
    } else {
      message.error(res.data.message || '登录失败，请稍后重试')
    }
  } catch {
    // 网络错误由全局响应拦截器统一提示。
  } finally {
    submitLoading.value = false
  }
}
</script>

<template>
  <AuthPageShell title="欢迎回来" description="登录你的 Vireo 账号，继续管理项目与内容。">
    <a-form
      class="auth-form"
      :model="formState"
      layout="vertical"
      autocomplete="on"
      @finish="handleSubmit"
    >
      <a-form-item
        label="账号"
        name="userAccount"
        :rules="[
          { required: true, message: '请输入账号' },
          { pattern: /^[A-Za-z0-9_]{4,16}$/, message: '账号为 4-16 位字母、数字或下划线' },
        ]"
      >
        <a-input
          v-model:value="formState.userAccount"
          autocomplete="username"
          placeholder="请输入账号"
        />
      </a-form-item>
      <a-form-item
        label="密码"
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码不能少于 8 位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          autocomplete="current-password"
          placeholder="请输入密码"
        />
      </a-form-item>
      <div class="auth-switch">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button block type="primary" html-type="submit" :loading="submitLoading">登录</a-button>
      </a-form-item>
    </a-form>
  </AuthPageShell>
</template>
