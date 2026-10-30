<script setup lang="ts">
import { RouterView } from 'vue-router'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { useLoginUserStore } from '@/router/loginUser.ts'

const loginUserStore = useLoginUserStore()
void loginUserStore.fetchLoginUser()
</script>

<template>
  <a-config-provider :locale="zhCN">
    <GlobalHeader />
    <RouterView />
  </a-config-provider>
</template>
