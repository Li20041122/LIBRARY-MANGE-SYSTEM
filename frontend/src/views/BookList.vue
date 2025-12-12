<template>
  <div class="book-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">📚 图书管理</h2>
        <p class="page-subtitle">管理图书馆的所有图书信息</p>
      </div>
      <div class="header-right">
        <el-input
          v-model="keyword"
          placeholder="搜索书名、作者、ID..."
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
          <span class="button-icon">+</span> 添加图书
        </el-button>
      </div>
    </div>

    <el-table :data="books" class="dark-table" stripe v-loading="loading">
      <template #empty>
        <div class="empty-state">
          <span class="empty-state-icon">📚</span>
          <h3 class="empty-state-title">暂无图书数据</h3>
          <p class="empty-state-desc">{{ keyword ? '未找到匹配的图书，请尝试其他关键词' : '点击"添加图书"开始录入图书信息' }}</p>
        </div>
      </template>
      <el-table-column prop="bookid" label="图书ID" width="120" />
      <el-table-column prop="bookname" label="书名" width="200" />
      <el-table-column prop="authors" label="作者" width="150" />
      <el-table-column prop="publication" label="出版社" width="180" />
      <el-table-column prop="publicdate" label="出版日期" width="120">
        <template #default="{ row }">
          {{ row.publicdate ? row.publicdate.substring(0, 10) : '' }}
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column label="操作" :width="isAdmin ? 250 : 100">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="borrowBook(row)">借阅</el-button>
          <el-button v-if="isAdmin" size="small" @click="editBook(row)">编辑</el-button>
          <el-button v-if="isAdmin" size="small" type="danger" @click="deleteBook(row.bookid)">删除</el-button>
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
        @size-change="loadBooks"
        @current-change="loadBooks"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="图书ID" prop="bookid">
          <el-input v-model="form.bookid" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="书名" prop="bookname">
          <el-input v-model="form.bookname" />
        </el-form-item>
        <el-form-item label="作者" prop="authors">
          <el-input v-model="form.authors" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="form.publication" />
        </el-form-item>
        <el-form-item label="出版日期">
          <el-date-picker v-model="form.publicdate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="form.price" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBook" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookApi, borrowApi } from '../api'

export default {
  name: 'BookList',
  setup() {
    const books = ref([])
    const loading = ref(false)
    const saving = ref(false)
    const dialogVisible = ref(false)
    const dialogTitle = ref('添加图书')
    const isEdit = ref(false)
    const formRef = ref(null)
    const keyword = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    const form = ref({
      bookid: '',
      bookname: '',
      authors: '',
      publication: '',
      publicdate: '',
      price: 0
    })

    const rules = {
      bookid: [{ required: true, message: '请输入图书ID', trigger: 'blur' }],
      bookname: [{ required: true, message: '请输入书名', trigger: 'blur' }]
    }

    const isAdmin = computed(() => {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        const user = JSON.parse(userInfo)
        return user.role === 'admin'
      }
      return false
    })

    const loadBooks = async () => {
      loading.value = true
      try {
        const response = await bookApi.getPage({
          keyword: keyword.value,
          page: currentPage.value,
          size: pageSize.value
        })
        if (response.data.code === 200) {
          books.value = response.data.data.list
          total.value = response.data.data.total
        }
      } catch (error) {
        ElMessage.error('加载图书列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      currentPage.value = 1
      loadBooks()
    }

    const showAddDialog = () => {
      dialogTitle.value = '添加图书'
      isEdit.value = false
      form.value = {
        bookid: '',
        bookname: '',
        authors: '',
        publication: '',
        publicdate: '',
        price: 0
      }
      dialogVisible.value = true
    }

    const editBook = (book) => {
      dialogTitle.value = '编辑图书'
      isEdit.value = true
      form.value = { ...book }
      dialogVisible.value = true
    }

    const saveBook = async () => {
      try {
        await formRef.value.validate()
        saving.value = true
        if (isEdit.value) {
          await bookApi.update(form.value.bookid, form.value)
          ElMessage.success('更新成功')
        } else {
          await bookApi.create(form.value)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadBooks()
      } catch (error) {
        if (error !== false) {
          ElMessage.error('保存失败')
        }
      } finally {
        saving.value = false
      }
    }

    const deleteBook = (id) => {
      ElMessageBox.confirm('确定要删除这本图书吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await bookApi.delete(id)
          ElMessage.success('删除成功')
          loadBooks()
        } catch (error) {
          ElMessage.error('删除失败')
        }
      })
    }

    const borrowBook = (book) => {
      ElMessageBox.confirm(`确定要借阅《${book.bookname}》吗?`, '借阅确认', {
        confirmButtonText: '确定借阅',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const userInfo = JSON.parse(localStorage.getItem('userInfo'))
          const now = new Date().toISOString()
          await borrowApi.create({
            userid: userInfo.userid,
            bookid: book.bookid,
            borrowtime: now,
            returntime: null,
            penalsum: 0,
            payment: 0
          })
          ElMessage.success('借阅成功')
        } catch (error) {
          ElMessage.error('借阅失败，可能已借阅此书')
        }
      })
    }

    onMounted(() => {
      loadBooks()
    })

    return {
      books,
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
      loadBooks,
      handleSearch,
      showAddDialog,
      editBook,
      saveBook,
      deleteBook,
      borrowBook
    }
  }
}
</script>

<style scoped>
.book-list {
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
