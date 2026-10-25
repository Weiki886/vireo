import './assets/main.css'
import 'ant-design-vue/dist/reset.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

import App from './App.vue'
import router from './router'

// antd 日期组件的月份、星期等文案由 dayjs 控制，需单独设为中文
dayjs.locale('zh-cn')

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
