<template>
  <div class="depart-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">🏢 部门管理</h2>
        <p class="page-subtitle">管理组织机构部门信息</p>
      </div>
      <div class="header-right">
        <el-input
          v-model="keyword"
          placeholder="搜索部门名称、ID..."
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
          <span class="button-icon">+</span> 添加部门
        </el-button>
      </div>
    </div>

    <el-table :data="departs" class="dark-table" stripe v-loading="loading">
      <template #empty>
        <div class="empty-state">
          <span class="empty-state-icon">🏢</span>
          <h3 class="empty-state-title">暂无部门数据</h3>
          <p class="empty-state-desc">{{ keyword ? '未找到匹配的部门，请尝试其他关键词' : '点击"添加部门"开始录入部门信息' }}</p>
        </div>
      </template>
      <el-table-column prop="departid" label="部门ID" width="150" />
      <el-table-column prop="departname" label="部门名称" width="200" />
      <el-table-column prop="parentdepartid" label="上级部门ID" width="150" />
      <el-table-column v-if="isAdmin" label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editDepart(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteDepart(row.departid)">删除</el-button>
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
        @size-change="loadDeparts"
        @current-change="loadDeparts"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="部门ID" prop="departid">
          <el-input v-model="form.departid" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="部门名称" prop="departname">
          <el-input v-model="form.departname" />
        </el-form-item>
        <el-form-item label="上级部门ID">
          <el-input v-model="form.parentdepartid" placeholder="留空则为顶级部门" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDepart" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { departApi } from '../api'

export default {
  name: 'DepartList',
  setup() {
    const departs = ref([])
    const loading = ref(false)
    const saving = ref(false)
    const dialogVisible = ref(false)
    const dialogTitle = ref('添加部门')
    const isEdit = ref(false)
    const formRef = ref(null)
    const keyword = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    const form = ref({
      departid: '',
      departname: '',
      parentdepartid: ''
    })

    const rules = {
      departid: [{ required: true, message: '请输入部门ID', trigger: 'blur' }],
      departname: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
    }

    const isAdmin = computed(() => {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        const user = JSON.parse(userInfo)
        return user.role === 'admin'
      }
      return false
    })

    const loadDeparts = async () => {
      loading.value = true
      try {
        const response = await departApi.getPage({
          keyword: keyword.value,
          page: currentPage.value,
          size: pageSize.value
        })
        if (response.data.code === 200) {
          departs.value = response.data.data.list
          total.value = response.data.data.total
        }
      } catch (error) {
        ElMessage.error('加载部门列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      currentPage.value = 1
      loadDeparts()
    }

    const showAddDialog = () => {
      dialogTitle.value = '添加部门'
      isEdit.value = false
      form.value = {
        departid: '',
        departname: '',
        parentdepartid: ''
      }
      dialogVisible.value = true
    }

    const editDepart = (depart) => {
      dialogTitle.value = '编辑部门'
      isEdit.value = true
      form.value = { ...depart }
      dialogVisible.value = true
    }

    const saveDepart = async () => {
      try {
        await formRef.value.validate()
        saving.value = true
        if (isEdit.value) {
          await departApi.update(form.value.departid, form.value)
          ElMessage.success('更新成功')
        } else {
          await departApi.create(form.value)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadDeparts()
      } catch (error) {
        if (error !== false) {
          ElMessage.error('保存失败')
        }
      } finally {
        saving.value = false
      }
    }

    const deleteDepart = (id) => {
      ElMessageBox.confirm('确定要删除这个部门吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await departApi.delete(id)
          ElMessage.success('删除成功')
          loadDeparts()
        } catch (error) {
          ElMessage.error('删除失败')
        }
      })
    }

    onMounted(() => {
      loadDeparts()
    })

    return {
      departs,
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
      loadDeparts,
      handleSearch,
      showAddDialog,
      editDepart,
      saveDepart,
      deleteDepart
    }
  }
}
</script>

<style scoped>
.depart-list {
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
