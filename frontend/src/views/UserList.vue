<template>
  <div class="user-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">👥 用户管理</h2>
        <p class="page-subtitle">管理系统的所有用户信息</p>
      </div>
      <div class="header-right">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名、ID..."
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        <el-button v-if="isAdmin" type="primary" @click="showAddDialog" class="add-button">
          <span class="button-icon">+</span> 添加用户
        </el-button>
      </div>
    </div>

    <el-table :data="users" class="dark-table" stripe v-loading="loading">
      <template #empty>
        <div class="empty-state">
          <span class="empty-state-icon">👥</span>
          <h3 class="empty-state-title">暂无用户数据</h3>
          <p class="empty-state-desc">{{ keyword ? '未找到匹配的用户，请尝试其他关键词' : '点击"添加用户"开始录入用户信息' }}</p>
        </div>
      </template>
      <el-table-column prop="userid" label="用户ID" width="120" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="departid" label="部门ID" width="120" />
      <el-table-column prop="phonenum" label="手机号" width="150" />
      <el-table-column prop="sex" label="性别" width="80" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
            {{ row.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="isAdmin" label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editUser(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteUser(row.userid)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadUsers"
        @current-change="loadUsers"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户ID" prop="userid">
          <el-input v-model="form.userid" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="部门ID">
          <el-input v-model="form.departid" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phonenum" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.sex" placeholder="请选择性别">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../api'

export default {
  name: 'UserList',
  setup() {
    const users = ref([])
    const loading = ref(false)
    const saving = ref(false)
    const dialogVisible = ref(false)
    const dialogTitle = ref('添加用户')
    const isEdit = ref(false)
    const formRef = ref(null)
    const keyword = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    const form = ref({
      userid: '',
      username: '',
      password: '',
      departid: '',
      phonenum: '',
      sex: '',
      role: 'user'
    })

    const rules = {
      userid: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
    }

    const isAdmin = computed(() => {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        const user = JSON.parse(userInfo)
        return user.role === 'admin'
      }
      return false
    })

    const loadUsers = async () => {
      loading.value = true
      try {
        const response = await userApi.getPage({
          keyword: keyword.value,
          page: currentPage.value,
          size: pageSize.value
        })
        if (response.data.code === 200) {
          users.value = response.data.data.list
          total.value = response.data.data.total
        }
      } catch (error) {
        ElMessage.error('加载用户列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      currentPage.value = 1
      loadUsers()
    }

    const showAddDialog = () => {
      dialogTitle.value = '添加用户'
      isEdit.value = false
      form.value = {
        userid: '',
        username: '',
        password: '',
        departid: '',
        phonenum: '',
        sex: '',
        role: 'user'
      }
      dialogVisible.value = true
    }

    const editUser = (user) => {
      dialogTitle.value = '编辑用户'
      isEdit.value = true
      form.value = { ...user, password: '' }
      dialogVisible.value = true
    }

    const saveUser = async () => {
      try {
        await formRef.value.validate()
        saving.value = true
        if (isEdit.value) {
          await userApi.update(form.value.userid, form.value)
          ElMessage.success('更新成功')
        } else {
          await userApi.create(form.value)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadUsers()
      } catch (error) {
        if (error !== false) {
          ElMessage.error('保存失败')
        }
      } finally {
        saving.value = false
      }
    }

    const deleteUser = (id) => {
      ElMessageBox.confirm('确定要删除这个用户吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await userApi.delete(id)
          ElMessage.success('删除成功')
          loadUsers()
        } catch (error) {
          ElMessage.error('删除失败')
        }
      })
    }

    onMounted(() => {
      loadUsers()
    })

    return {
      users,
      loading,
      saving,
      dialogVisible,
      dialogTitle,
      isEdit,
      form,
      formRef,
      rules,
      keyword,
      currentPage,
      pageSize,
      total,
      isAdmin,
      loadUsers,
      handleSearch,
      showAddDialog,
      editUser,
      saveUser,
      deleteUser
    }
  }
}
</script>

<style scoped>
.user-list {
  padding: 0;
  animation: fadeIn 0.4s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-xl);
  background: var(--dark-bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  flex: 1;
  min-width: 200px;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 300px;
}

.page-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-subtitle {
  margin: 6px 0 0 0;
  font-size: 14px;
  color: var(--text-tertiary);
}

.add-button {
  height: 42px;
  padding: 0 24px;
  font-size: 14px;
  font-weight: 600;
}

.button-icon {
  font-size: 18px;
  font-weight: 700;
  margin-right: 6px;
}

.dark-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-lg);
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--dark-bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    padding: var(--spacing-lg);
  }

  .header-right {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .add-button {
    width: 100%;
  }

  .page-title {
    font-size: 22px;
  }
}
</style>
