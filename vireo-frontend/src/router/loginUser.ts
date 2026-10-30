import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController.ts'

export const useLoginUserStore = defineStore('loginUser', () => {
  const createGuestUser = (): API.LoginUserVO => ({ userName: '未登录' })

  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })
  let initializationPromise: Promise<void> | null = null

  // 获取登录用户信息
  async function fetchLoginUser() {
    try {
      const res = await getLoginUser()
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
        return
      }
      loginUser.value = createGuestUser()
    } catch {
      // 请求拦截器已提示网络错误，启动时只需清理可能过期的登录态。
      loginUser.value = createGuestUser()
    }
  }

  // 路由守卫和页面初始化可能同时请求登录态，这里共享同一个 Promise，避免重复请求。
  function ensureLoginUserLoaded() {
    if (!initializationPromise) {
      initializationPromise = fetchLoginUser()
    }
    return initializationPromise
  }

  // 更新登录用户信息
  function setLoginUser(newLoginUser: API.LoginUserVO) {
    loginUser.value = newLoginUser
  }
  return { loginUser, setLoginUser, fetchLoginUser, ensureLoginUserLoaded }
})
