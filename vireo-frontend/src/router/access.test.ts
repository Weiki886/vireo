import { describe, expect, it } from 'vitest'

import { canAccessRoute, getVisibleMenuRoutes } from '@/router/access.ts'

const publicRoute = {
  path: '/',
  meta: { title: '首页', access: 'public', menu: true, menuOrder: 10 },
} as const

const authenticatedRoute = {
  path: '/user/profile',
  meta: { title: '个人设置', access: 'authenticated', menu: true, menuOrder: 20 },
} as const

const adminRoute = {
  path: '/admin/userManage',
  meta: { title: '用户管理', access: 'admin', menu: true, menuOrder: 30 },
} as const

describe('route access rules', () => {
  it('allows guests to visit public routes', () => {
    expect(canAccessRoute(publicRoute, { userName: '未登录' })).toBe(true)
  })

  it('blocks guests from authenticated routes', () => {
    expect(canAccessRoute(authenticatedRoute, { userName: '未登录' })).toBe(false)
  })

  it('blocks normal users from admin routes', () => {
    expect(canAccessRoute(adminRoute, { id: '1', userName: '普通用户', userRole: 'user' })).toBe(
      false,
    )
  })

  it('allows administrators to visit admin routes', () => {
    expect(canAccessRoute(adminRoute, { id: '1', userName: '管理员', userRole: 'admin' })).toBe(
      true,
    )
  })

  it('never grants admin access based on role alone without an authenticated identity', () => {
    expect(canAccessRoute(adminRoute, { userName: '伪造管理员', userRole: 'admin' })).toBe(false)
  })
})

describe('menu access filtering', () => {
  const routes = [adminRoute, publicRoute, authenticatedRoute]

  it('shows only public menu items to guests', () => {
    expect(getVisibleMenuRoutes(routes, { userName: '未登录' }).map((route) => route.path)).toEqual(
      ['/'],
    )
  })

  it('shows authenticated and public menu items to normal users', () => {
    expect(
      getVisibleMenuRoutes(routes, { id: '1', userName: '普通用户', userRole: 'user' }).map(
        (route) => route.path,
      ),
    ).toEqual(['/', '/user/profile'])
  })

  it('shows all permitted menu items to administrators', () => {
    expect(
      getVisibleMenuRoutes(routes, { id: '1', userName: '管理员', userRole: 'admin' }).map(
        (route) => route.path,
      ),
    ).toEqual(['/', '/user/profile', '/admin/userManage'])
  })
})
