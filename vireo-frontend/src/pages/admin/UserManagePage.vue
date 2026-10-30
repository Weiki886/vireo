<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  message,
  type FormInstance,
  type TableColumnsType,
  type TablePaginationConfig,
  type TableProps,
} from 'ant-design-vue'
import { DeleteOutlined, EditOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'

import { deleteUser, listUserVoByPage, updateUser } from '@/api/userController.ts'

const columns: TableColumnsType = [
  { title: 'ID', dataIndex: 'id', width: 200, ellipsis: true },
  { title: '账号', dataIndex: 'userAccount', width: 140 },
  { title: '用户名', dataIndex: 'userName', width: 140 },
  { title: '头像', dataIndex: 'userAvatar', width: 80 },
  { title: '简介', dataIndex: 'userProfile', ellipsis: true },
  { title: '用户角色', dataIndex: 'userRole', width: 110 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' },
]

const data = ref<API.UserVO[]>([])
const total = ref(0)
const loading = ref(false)
const deletingId = ref<string>()
const editOpen = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive<API.UserUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage({ ...searchParams })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error(res.data.message || '获取用户列表失败')
    }
  } catch {
    // 网络错误由全局响应拦截器统一提示。
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void fetchData()
})

const pagination = computed<TablePaginationConfig>(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (count: number) => `共 ${count} 条`,
}))

const doTableChange: TableProps['onChange'] = (page) => {
  searchParams.pageNum = page.current ?? 1
  searchParams.pageSize = page.pageSize ?? 10
  void fetchData()
}

const doSearch = () => {
  searchParams.pageNum = 1
  void fetchData()
}

const resetSearch = () => {
  searchParams.userAccount = undefined
  searchParams.userName = undefined
  doSearch()
}

const doDelete = async (id?: string) => {
  if (!id || deletingId.value) {
    return
  }
  deletingId.value = id
  try {
    const res = await deleteUser({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      if (data.value.length === 1 && (searchParams.pageNum ?? 1) > 1) {
        searchParams.pageNum = (searchParams.pageNum ?? 1) - 1
      }
      await fetchData()
    } else {
      message.error(res.data.message || '删除失败')
    }
  } catch {
    // 网络错误由全局响应拦截器统一提示。
  } finally {
    deletingId.value = undefined
  }
}

const openEditModal = (record: API.UserVO) => {
  editForm.id = record.id
  editForm.userName = record.userName ?? ''
  editForm.userAvatar = record.userAvatar ?? ''
  editForm.userProfile = record.userProfile ?? ''
  editForm.userRole = record.userRole ?? 'user'
  editOpen.value = true
}

const handleEditSubmit = async () => {
  if (editLoading.value || !editForm.id) {
    return
  }
  editLoading.value = true
  const valid = await editFormRef.value?.validate().then(
    () => true,
    () => false,
  )
  if (!valid) {
    editLoading.value = false
    return
  }
  try {
    const res = await updateUser({
      id: editForm.id,
      userName: editForm.userName?.trim(),
      userAvatar: editForm.userAvatar?.trim() ?? '',
      userProfile: editForm.userProfile?.trim() ?? '',
      userRole: editForm.userRole,
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('用户信息已更新')
      editOpen.value = false
      await fetchData()
    } else {
      message.error(res.data.message || '更新用户失败')
    }
  } catch {
    // 网络错误由全局响应拦截器统一提示。
  } finally {
    editLoading.value = false
  }
}

const rowKey = (record: API.UserVO) => record.id ?? ''
</script>

<template>
  <main class="user-manage-page">
    <div class="page-heading">
      <h1>用户管理</h1>
      <p>查询和维护平台用户</p>
    </div>

    <a-form class="search-form" layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" allow-clear placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" allow-clear placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit" :loading="loading">
            <template #icon><SearchOutlined /></template>
            搜索
          </a-button>
          <a-button :disabled="loading" @click="resetSearch">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <a-table
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="pagination"
      :row-key="rowKey"
      :scroll="{ x: 1100 }"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-avatar :src="record.userAvatar || undefined">
            {{ record.userName?.slice(0, 1) || '?' }}
          </a-avatar>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <a-tag :color="record.userRole === 'admin' ? 'green' : 'blue'">
            {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ record.createTime ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space :size="4">
            <a-button type="link" @click="openEditModal(record)">
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-popconfirm
              title="确定删除该用户吗？"
              ok-text="删除"
              cancel-text="取消"
              @confirm="doDelete(record.id)"
            >
              <a-button danger type="link" :loading="deletingId === record.id">
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="editOpen"
      title="编辑用户"
      ok-text="保存"
      cancel-text="取消"
      :confirm-loading="editLoading"
      @ok="handleEditSubmit"
    >
      <a-form ref="editFormRef" :model="editForm" layout="vertical">
        <a-form-item
          label="昵称"
          name="userName"
          :rules="[
            { required: true, whitespace: true, message: '请输入昵称' },
            { max: 256, message: '昵称不能超过 256 个字符' },
          ]"
        >
          <a-input v-model:value="editForm.userName" :maxlength="256" show-count />
        </a-form-item>
        <a-form-item
          label="头像地址"
          name="userAvatar"
          :rules="[{ max: 1024, message: '头像地址不能超过 1024 个字符' }]"
        >
          <a-input
            v-model:value="editForm.userAvatar"
            placeholder="https://example.com/avatar.png"
          />
        </a-form-item>
        <a-form-item
          label="个人简介"
          name="userProfile"
          :rules="[{ max: 512, message: '个人简介不能超过 512 个字符' }]"
        >
          <a-textarea v-model:value="editForm.userProfile" :maxlength="512" :rows="4" show-count />
        </a-form-item>
        <a-form-item label="用户角色" name="userRole" :rules="[{ required: true }]">
          <a-select v-model:value="editForm.userRole">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </main>
</template>

<style scoped>
.user-manage-page {
  padding: 24px;
}

.page-heading {
  margin-bottom: 24px;
}

.page-heading h1 {
  margin: 0;
  font-size: 24px;
}

.page-heading p {
  margin: 8px 0 0;
  color: #666;
}

.search-form {
  row-gap: 12px;
  margin-bottom: 20px;
}

@media (max-width: 640px) {
  .user-manage-page {
    padding: 16px;
  }
}
</style>
