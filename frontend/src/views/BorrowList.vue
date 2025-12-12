<template>
  <div class="borrow-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">📖 借阅管理</h2>
        <p class="page-subtitle">管理图书借阅记录</p>
      </div>
      <div class="header-right">
        <el-input
          v-model="keyword"
          placeholder="搜索用户ID、图书ID..."
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
          <span class="button-icon">+</span> 添加借阅
        </el-button>
      </div>
    </div>

    <el-table :data="borrows" class="dark-table" stripe v-loading="loading">
      <template #empty>
        <div class="empty-state">
          <span class="empty-state-icon">📖</span>
          <h3 class="empty-state-title">暂无借阅记录</h3>
          <p class="empty-state-desc">{{ keyword ? '未找到匹配的借阅记录' : '还没有借阅记录，去图书列表借阅图书吧' }}</p>
        </div>
      </template>
      <el-table-column prop="userid" label="用户ID" width="120" />
      <el-table-column prop="bookid" label="图书ID" width="120" />
      <el-table-column prop="borrowtime" label="借阅时间" width="120">
        <template #default="{ row }">
          {{ row.borrowtime ? row.borrowtime.substring(0, 10) : '' }}
        </template>
      </el-table-column>
      <el-table-column prop="returntime" label="归还时间" width="120">
        <template #default="{ row }">
          {{ row.returntime ? row.returntime.substring(0, 10) : '' }}
        </template>
      </el-table-column>
      <el-table-column prop="penalsum" label="罚款金额" width="120" />
      <el-table-column prop="payment" label="已付金额" width="120" />
      <el-table-column label="操作" :width="isAdmin ? 250 : 100">
        <template #default="{ row }">
          <el-button v-if="!row.returntime" size="small" type="success" @click="returnBook(row)">归还</el-button>
          <el-tag v-else type="info" size="small">已归还</el-tag>
          <el-button v-if="isAdmin" size="small" @click="editBorrow(row)">编辑</el-button>
          <el-button v-if="isAdmin" size="small" type="danger" @click="deleteBorrow(row.userid, row.bookid)">删除</el-button>
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
        @size-change="loadBorrows"
        @current-change="loadBorrows"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户ID" prop="userid">
          <el-input v-model="form.userid" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="图书ID" prop="bookid">
          <el-input v-model="form.bookid" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="借阅时间">
          <el-date-picker v-model="form.borrowtime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="归还时间">
          <el-date-picker v-model="form.returntime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="罚款金额">
          <el-input v-model="form.penalsum" type="number" />
        </el-form-item>
        <el-form-item label="已付金额">
          <el-input v-model="form.payment" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBorrow" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { borrowApi } from '../api'

export default {
  name: 'BorrowList',
  setup() {
    const borrows = ref([])
    const loading = ref(false)
    const saving = ref(false)
    const dialogVisible = ref(false)
    const dialogTitle = ref('添加借阅')
    const isEdit = ref(false)
    const formRef = ref(null)
    const keyword = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    const form = ref({
      userid: '',
      bookid: '',
      borrowtime: '',
      returntime: '',
      penalsum: 0,
      payment: 0
    })

    const rules = {
      userid: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
      bookid: [{ required: true, message: '请输入图书ID', trigger: 'blur' }]
    }

    const isAdmin = computed(() => {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        const user = JSON.parse(userInfo)
        return user.role === 'admin'
      }
      return false
    })

    const loadBorrows = async () => {
      loading.value = true
      try {
        const response = await borrowApi.getPage({
          keyword: keyword.value,
          page: currentPage.value,
          size: pageSize.value
        })
        if (response.data.code === 200) {
          borrows.value = response.data.data.list
          total.value = response.data.data.total
        }
      } catch (error) {
        ElMessage.error('加载借阅列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      currentPage.value = 1
      loadBorrows()
    }

    const showAddDialog = () => {
      dialogTitle.value = '添加借阅'
      isEdit.value = false
      form.value = {
        userid: '',
        bookid: '',
        borrowtime: '',
        returntime: '',
        penalsum: 0,
        payment: 0
      }
      dialogVisible.value = true
    }

    const editBorrow = (borrow) => {
      dialogTitle.value = '编辑借阅'
      isEdit.value = true
      form.value = { ...borrow }
      dialogVisible.value = true
    }

    const saveBorrow = async () => {
      try {
        await formRef.value.validate()
        saving.value = true
        if (isEdit.value) {
          await borrowApi.update(form.value.userid, form.value.bookid, form.value)
          ElMessage.success('更新成功')
        } else {
          await borrowApi.create(form.value)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadBorrows()
      } catch (error) {
        if (error !== false) {
          ElMessage.error('保存失败')
        }
      } finally {
        saving.value = false
      }
    }

    const deleteBorrow = (userid, bookid) => {
      ElMessageBox.confirm('确定要删除这条借阅记录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await borrowApi.delete(userid, bookid)
          ElMessage.success('删除成功')
          loadBorrows()
        } catch (error) {
          ElMessage.error('删除失败')
        }
      })
    }

    const returnBook = (borrow) => {
      ElMessageBox.confirm('确定要归还这本书吗?', '归还确认', {
        confirmButtonText: '确定归还',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const now = new Date().toISOString()
          await borrowApi.update(borrow.userid, borrow.bookid, {
            ...borrow,
            returntime: now
          })
          ElMessage.success('归还成功')
          loadBorrows()
        } catch (error) {
          ElMessage.error('归还失败')
        }
      })
    }

    onMounted(() => {
      loadBorrows()
    })

    return {
      borrows,
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
      loadBorrows,
      handleSearch,
      showAddDialog,
      editBorrow,
      saveBorrow,
      deleteBorrow,
      returnBook
    }
  }
}
</script>

<style scoped>
.borrow-list {
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
