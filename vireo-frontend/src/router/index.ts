import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'

const UserLoginPage = () => import('@/pages/user/UserLoginPage.vue')
const UserRegisterPage = () => import('@/pages/user/UserRegisterPage.vue')
const UserProfilePage = () => import('@/pages/user/UserProfilePage.vue')
const UserManagePage = () => import('@/pages/admin/UserManagePage.vue')

export const appRoutes = [
  {
    path: '/',
    name: '主页',
    component: HomePage,
    meta: { title: '首页', access: 'public', menu: true, menuOrder: 10 },
  },
  {
    path: '/user/login',
    name: '用户登录',
    component: UserLoginPage,
    meta: { title: '用户登录', access: 'public' },
  },
  {
    path: '/user/register',
    name: '用户注册',
    component: UserRegisterPage,
    meta: { title: '用户注册', access: 'public' },
  },
  {
    path: '/user/profile',
    name: '个人设置',
    component: UserProfilePage,
    meta: { title: '个人设置', access: 'authenticated' },
  },
  {
    path: '/admin/userManage',
    name: '用户管理',
    component: UserManagePage,
    meta: { title: '用户管理', access: 'admin', menu: true, menuOrder: 20 },
  },
] satisfies RouteRecordRaw[]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: appRoutes,
})

export default router
