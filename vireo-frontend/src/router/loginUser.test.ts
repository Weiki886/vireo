import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import { getLoginUser } from '@/api/userController.ts'
import { useLoginUserStore } from '@/router/loginUser.ts'

vi.mock('@/api/userController.ts', () => ({
  getLoginUser: vi.fn(),
}))

const mockedGetLoginUser = vi.mocked(getLoginUser)

function apiResponse(data: API.BaseResponseLoginUserVO) {
  return { data } as Awaited<ReturnType<typeof getLoginUser>>
}

describe('loginUser store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockedGetLoginUser.mockReset()
  })

  it('stores the returned user after a successful refresh', async () => {
    mockedGetLoginUser.mockResolvedValue(
      apiResponse({
        code: 0,
        data: { id: '1893756824288542721', userName: 'Vireo User' },
      }),
    )
    const store = useLoginUserStore()

    await store.fetchLoginUser()

    expect(store.loginUser).toEqual({ id: '1893756824288542721', userName: 'Vireo User' })
  })

  it('clears stale user state when the session is no longer valid', async () => {
    mockedGetLoginUser.mockResolvedValue(apiResponse({ code: 40100 }))
    const store = useLoginUserStore()
    store.setLoginUser({ id: '1893756824288542721', userName: 'Old User' })

    await store.fetchLoginUser()

    expect(store.loginUser.id).toBeUndefined()
  })

  it('clears stale user state and handles a failed refresh request', async () => {
    mockedGetLoginUser.mockRejectedValue(new Error('network unavailable'))
    const store = useLoginUserStore()
    store.setLoginUser({ id: '1893756824288542721', userName: 'Old User' })

    await expect(store.fetchLoginUser()).resolves.toBeUndefined()

    expect(store.loginUser.id).toBeUndefined()
  })

  it('shares one initialization request between concurrent callers', async () => {
    mockedGetLoginUser.mockResolvedValue(
      apiResponse({
        code: 0,
        data: { id: '1893756824288542721', userName: 'Vireo User' },
      }),
    )
    const store = useLoginUserStore()

    await Promise.all([store.ensureLoginUserLoaded(), store.ensureLoginUserLoaded()])

    expect(mockedGetLoginUser).toHaveBeenCalledTimes(1)
  })
})
