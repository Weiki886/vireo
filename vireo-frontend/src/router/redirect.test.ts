import { describe, expect, it } from 'vitest'

import { resolveLoginRedirect } from '@/router/redirect.ts'

describe('resolveLoginRedirect', () => {
  it.each(['/admin/userManage', '/user/profile?tab=security', '/#profile'])(
    'keeps the internal redirect %s',
    (redirect) => {
      expect(resolveLoginRedirect(redirect)).toBe(redirect)
    },
  )

  it.each([undefined, '', 'admin/userManage', '//example.com', 'https://example.com'])(
    'falls back to home for unsafe redirect %s',
    (redirect) => {
      expect(resolveLoginRedirect(redirect)).toBe('/')
    },
  )
})
