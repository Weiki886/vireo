import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'

// 创建 Axios 实例
const myAxios = axios.create({
  // 后端 server.port=8123，context-path=/api
  baseURL: 'http://localhost:8123/api',
  timeout: 60000,
  // 跨域请求携带 Cookie，登录态依赖它；后端 CorsConfig 也已开启 allowCredentials
  withCredentials: true,
})

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // 请求发出前的统一处理，目前不需要
    return config
  },
  function (error) {
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 40100 对应后端 ErrorCode.NOT_LOGIN_ERROR
    if (data.code === 40100) {
      const { path, fullPath } = router.currentRoute.value
      // 获取登录用户的请求本身可以未登录；已经在登录页时也不再跳转
      if (!response.request.responseURL.includes('user/get/login') && path !== '/user/login') {
        message.warning('请先登录')
        // 站内跳转不刷新页面；redirect 只记录站内路径，query 参数由 Vue Router 自动编码
        router.push({ path: '/user/login', query: { redirect: fullPath } })
      }
    }
    return response
  },
  function (error) {
    // 网络错误、超时、HTTP 状态码不是 2xx 时进入这里
    message.error('请求失败，请检查网络或稍后重试')
    return Promise.reject(error)
  },
)

export default myAxios
