import './assets/main.css'
import 'ant-design-vue/dist/reset.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { message } from 'ant-design-vue'

import App from './App.vue'
import router from './router'
import { canAccessRoute } from '@/router/access.ts'
import { useLoginUserStore } from '@/router/loginUser.ts'

// antd 日期组件的月份、星期等文案由 dayjs 控制，需单独设为中文
dayjs.locale('zh-cn')

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

const loginUserStore = useLoginUserStore(pinia)

router.beforeEach(async (to) => {
  // 刷新受保护页面时先恢复 Session 登录态，再决定是否允许导航。
  await loginUserStore.ensureLoginUserLoaded()

  document.title = to.meta.title ? `${to.meta.title} | Vireo` : 'Vireo'

  if (canAccessRoute(to, loginUserStore.loginUser)) {
    return true
  }

  if (!loginUserStore.loginUser.id) {
    return {
      path: '/user/login',
      query: { redirect: to.fullPath },
    }
  }

  message.warning('你没有权限访问该页面')
  return '/'
})

app.use(router)

app.mount('#app')
