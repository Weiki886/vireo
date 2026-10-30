import { renderToString } from '@vue/server-renderer'
import { createSSRApp, h } from 'vue'
import { describe, expect, it } from 'vitest'

import AuthPageShell from './AuthPageShell.vue'

describe('AuthPageShell', () => {
  it('renders the shared Vireo introduction before the form content', async () => {
    const app = createSSRApp({
      render: () =>
        h(
          AuthPageShell,
          {
            title: '欢迎回来',
            description: '使用账号和密码继续进入 Vireo。',
          },
          {
            default: () => h('form', { class: 'auth-form-probe' }, '表单内容'),
          },
        ),
    })

    const html = await renderToString(app)

    expect(html).toContain('alt="Vireo"')
    expect(html).toContain('>Vireo</span>')
    expect(html).toContain('一个专注于内容创作与项目协作的数字工作空间。')
    expect(html).toContain('>欢迎回来</h1>')
    expect(html).toContain('使用账号和密码继续进入 Vireo。')
    expect(html.indexOf('一个专注于内容创作与项目协作的数字工作空间。')).toBeLessThan(
      html.indexOf('auth-form-probe'),
    )
  })
})
