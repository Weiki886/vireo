import type { RouteMeta } from 'vue-router'

export type RouteAccess = 'public' | 'authenticated' | 'admin'

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面访问权限，未声明时按 public 处理。 */
    access?: RouteAccess
    /** 是否展示在顶部导航菜单中。 */
    menu?: boolean
    /** 顶部导航菜单排序，数值越小越靠前。 */
    menuOrder?: number
    /** 页面标题，同时用于菜单文案。 */
    title?: string
  }
}

interface RouteLike {
  meta?: RouteMeta
}

interface MenuRouteLike extends RouteLike {
  path: string
}

/**
 * 判断用户是否可以访问路由。
 *
 * admin 同时隐含“必须已登录”，避免管理员规则绕过身份校验。
 */
export function canAccessRoute(route: RouteLike, loginUser: API.LoginUserVO): boolean {
  const access = route.meta?.access ?? 'public'
  if (access === 'public') {
    return true
  }
  if (!loginUser.id) {
    return false
  }
  return access === 'authenticated' || loginUser.userRole === 'admin'
}

/**
 * 从同一份路由配置生成可见菜单，组件不再维护额外的权限判断。
 */
export function getVisibleMenuRoutes<T extends MenuRouteLike>(
  routes: readonly T[],
  loginUser: API.LoginUserVO,
): T[] {
  return routes
    .filter((route) => route.meta?.menu === true && canAccessRoute(route, loginUser))
    .sort((left, right) => (left.meta?.menuOrder ?? 0) - (right.meta?.menuOrder ?? 0))
}
