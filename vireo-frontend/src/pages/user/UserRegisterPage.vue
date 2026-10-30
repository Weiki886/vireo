<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import AuthPageShell from '@/components/AuthPageShell.vue'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'

const router = useRouter()
const submitLoading = ref(false)

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (
  _rule: unknown,
  value: string,
  callback: (error?: string) => void,
) => {
  if (value && value !== formState.userPassword) {
    callback('两次输入密码不一致')
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  if (submitLoading.value) {
    return
  }
  submitLoading.value = true
  try {
    const res = await userRegister(values)
    if (res.data.code === 0) {
      message.success('注册成功，请登录')
      await router.replace('/user/login')
    } else {
      message.error(res.data.message || '注册失败，请稍后重试')
    }
  } catch {
    // 网络错误由全局响应拦截器统一提示。
  } finally {
    submitLoading.value = false
  }
}
</script>

<template>
  <AuthPageShell title="创建账号" description="填写账号信息，创建你的 Vireo 工作空间访问凭证。">
    <a-form
      class="auth-form"
      :model="formState"
      layout="vertical"
      autocomplete="off"
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
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        label="密码"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item
        name="checkPassword"
        label="确认密码"
        :rules="[
          { required: true, message: '请确认密码' },
          { min: 8, message: '密码不能小于 8 位' },
          { validator: validateCheckPassword },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请确认密码" />
      </a-form-item>
      <div class="auth-switch">
        已有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <a-form-item>
        <a-button block type="primary" html-type="submit" :loading="submitLoading">注册</a-button>
      </a-form-item>
    </a-form>
  </AuthPageShell>
</template>
