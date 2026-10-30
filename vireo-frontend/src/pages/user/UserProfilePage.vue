<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { SaveOutlined, UserOutlined } from '@ant-design/icons-vue'

import { updateMyProfile } from '@/api/userController.ts'
import { useLoginUserStore } from '@/router/loginUser.ts'

const loginUserStore = useLoginUserStore()
const submitLoading = ref(false)

const formState = reactive<API.UserProfileUpdateRequest>({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const syncForm = () => {
  formState.userName = loginUserStore.loginUser.userName ?? ''
  formState.userAvatar = loginUserStore.loginUser.userAvatar ?? ''
  formState.userProfile = loginUserStore.loginUser.userProfile ?? ''
}

watch(() => loginUserStore.loginUser, syncForm, { immediate: true })

const handleSubmit = async () => {
  if (submitLoading.value) {
    return
  }

  submitLoading.value = true
  try {
    const res = await updateMyProfile({
      userName: formState.userName?.trim(),
      // 空字符串表示主动清空头像或简介，不能转成 undefined 后省略字段。
      userAvatar: formState.userAvatar?.trim() ?? '',
      userProfile: formState.userProfile?.trim() ?? '',
    })
    if (res.data.code === 0 && res.data.data) {
      loginUserStore.setLoginUser(res.data.data)
      message.success('个人资料已更新')
    } else {
      message.error(res.data.message || '更新个人资料失败')
    }
  } catch {
    // 网络错误由全局响应拦截器统一提示。
  } finally {
    submitLoading.value = false
  }
}
</script>

<template>
  <main class="profile-page">
    <section class="profile-card" aria-labelledby="profile-title">
      <header class="profile-card__header">
        <a-avatar :size="72" :src="loginUserStore.loginUser.userAvatar || undefined">
          <template #icon><UserOutlined /></template>
        </a-avatar>
        <div>
          <h1 id="profile-title">个人设置</h1>
          <p>更新昵称、头像和简介，它们会展示在你的账号信息中。</p>
        </div>
      </header>

      <a-descriptions class="profile-card__account" :column="2" size="small" bordered>
        <a-descriptions-item label="账号">
          {{ loginUserStore.loginUser.userAccount || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="角色">
          {{ loginUserStore.loginUser.userRole === 'admin' ? '管理员' : '普通用户' }}
        </a-descriptions-item>
      </a-descriptions>

      <a-form :model="formState" layout="vertical" @finish="handleSubmit">
        <a-form-item
          label="昵称"
          name="userName"
          :rules="[
            { required: true, whitespace: true, message: '请输入昵称' },
            { max: 256, message: '昵称不能超过 256 个字符' },
          ]"
        >
          <a-input v-model:value="formState.userName" :maxlength="256" show-count />
        </a-form-item>
        <a-form-item
          label="头像地址"
          name="userAvatar"
          :rules="[{ max: 1024, message: '头像地址不能超过 1024 个字符' }]"
        >
          <a-input
            v-model:value="formState.userAvatar"
            placeholder="https://example.com/avatar.png"
          />
        </a-form-item>
        <a-form-item
          label="个人简介"
          name="userProfile"
          :rules="[{ max: 512, message: '个人简介不能超过 512 个字符' }]"
        >
          <a-textarea
            v-model:value="formState.userProfile"
            :maxlength="512"
            :rows="4"
            show-count
            placeholder="简单介绍一下自己"
          />
        </a-form-item>
        <a-form-item class="profile-card__actions">
          <a-button type="primary" html-type="submit" :loading="submitLoading">
            <template #icon><SaveOutlined /></template>
            保存修改
          </a-button>
        </a-form-item>
      </a-form>
    </section>
  </main>
</template>

<style scoped>
.profile-page {
  min-height: calc(100vh - 64px);
  padding: 40px 20px;
  background: linear-gradient(135deg, rgb(22 119 255 / 7%), transparent 42%), #f5f7fa;
}

.profile-card {
  width: min(100%, 760px);
  margin: 0 auto;
  padding: 32px;
  border: 1px solid #e5eaf0;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 16px 40px rgb(15 23 42 / 8%);
}

.profile-card__header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-card__header h1 {
  margin: 0;
  color: #18262f;
  font-size: 26px;
}

.profile-card__header p {
  margin: 6px 0 0;
  color: #66747d;
}

.profile-card__account {
  margin: 28px 0;
}

.profile-card__actions {
  margin-bottom: 0;
}

@media (max-width: 600px) {
  .profile-page {
    padding: 20px 14px;
  }

  .profile-card {
    padding: 24px 20px;
  }

  .profile-card__header {
    align-items: flex-start;
  }
}
</style>
