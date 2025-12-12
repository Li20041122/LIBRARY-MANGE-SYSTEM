<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar-section">
        <div class="avatar">
          {{ userInfo?.username?.charAt(0)?.toUpperCase() || '?' }}
        </div>
        <div class="user-info">
          <h2 class="username">{{ userInfo?.username || '未知用户' }}</h2>
          <el-tag :type="isAdmin ? 'danger' : 'info'" size="small">
            {{ isAdmin ? '管理员' : '普通用户' }}
          </el-tag>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <div class="info-card">
        <h3 class="card-title">
          <span class="card-icon">👤</span>
          基本信息
        </h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">用户ID</span>
            <span class="info-value">{{ userInfo?.userid || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ userInfo?.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">所属部门</span>
            <span class="info-value">{{ userInfo?.departid || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">账户角色</span>
            <span class="info-value">{{ isAdmin ? '管理员' : '普通用户' }}</span>
          </div>
        </div>
      </div>

      <div class="info-card">
        <h3 class="card-title">
          <span class="card-icon">🔒</span>
          修改密码
        </h3>
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-position="top"
          class="password-form"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入原密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码（6-20位）"
              show-password
            />
            <div class="password-strength" v-if="passwordForm.newPassword">
              <div class="strength-bar">
                <div
                  class="strength-fill"
                  :class="passwordStrength.level"
                  :style="{ width: passwordStrength.percent + '%' }"
                ></div>
              </div>
              <span class="strength-text" :class="passwordStrength.level">
                {{ passwordStrength.text }}
              </span>
            </div>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="saving"
              @click="handleChangePassword"
            >
              确认修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

export default {
  name: 'Profile',
  setup() {
    const passwordFormRef = ref(null)
    const saving = ref(false)

    const passwordForm = ref({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const userInfo = computed(() => {
      const info = localStorage.getItem('userInfo')
      return info ? JSON.parse(info) : null
    })

    const isAdmin = computed(() => {
      return userInfo.value?.role === 'admin'
    })

    // 密码强度计算
    const passwordStrength = computed(() => {
      const pwd = passwordForm.value.newPassword
      if (!pwd) return { level: '', text: '', percent: 0 }

      let score = 0
      if (pwd.length >= 6) score += 1
      if (pwd.length >= 8) score += 1
      if (pwd.length >= 12) score += 1
      if (/\d/.test(pwd)) score += 1
      if (/[a-z]/.test(pwd)) score += 1
      if (/[A-Z]/.test(pwd)) score += 1
      if (/[!@#$%^&*(),.?":{}|<>]/.test(pwd)) score += 2

      if (score <= 2) {
        return { level: 'weak', text: '弱', percent: 33 }
      } else if (score <= 5) {
        return { level: 'medium', text: '中', percent: 66 }
      } else {
        return { level: 'strong', text: '强', percent: 100 }
      }
    })

    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== passwordForm.value.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入原密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需在6-20位之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }

    const handleChangePassword = async () => {
      try {
        await passwordFormRef.value.validate()
        saving.value = true

        const response = await authApi.changePassword(passwordForm.value)
        if (response.data.code === 200) {
          ElMessage.success('密码修改成功')
          // 清空表单
          passwordForm.value = {
            oldPassword: '',
            newPassword: '',
            confirmPassword: ''
          }
          passwordFormRef.value.resetFields()
        } else {
          ElMessage.error(response.data.message || '修改失败')
        }
      } catch (error) {
        if (error !== false && error.response?.data?.message) {
          ElMessage.error(error.response.data.message)
        } else if (error !== false) {
          ElMessage.error('修改失败，请稍后重试')
        }
      } finally {
        saving.value = false
      }
    }

    return {
      passwordFormRef,
      passwordForm,
      passwordRules,
      saving,
      userInfo,
      isAdmin,
      passwordStrength,
      handleChangePassword
    }
  }
}
</script>

<style scoped>
.profile-page {
  animation: fadeIn 0.4s ease-out;
}

.profile-header {
  padding: var(--spacing-xl);
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.15) 0%, rgba(0, 153, 204, 0.15) 100%);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-lg);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: white;
  box-shadow: var(--shadow-glow-cyan);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.username {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.profile-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.info-card {
  padding: var(--spacing-xl);
  background: var(--dark-bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.card-title {
  margin: 0 0 var(--spacing-lg) 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-icon {
  font-size: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.info-item {
  padding: var(--spacing-md);
  background: var(--dark-bg-tertiary);
  border-radius: var(--radius-md);
}

.info-label {
  display: block;
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 4px;
}

.info-value {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.password-form {
  max-width: 400px;
}

.password-form :deep(.el-form-item__label) {
  color: var(--text-secondary) !important;
}

.password-form :deep(.el-input__wrapper) {
  background: var(--dark-bg-tertiary) !important;
  border: 1px solid var(--border-color) !important;
}

/* 密码强度指示器 */
.password-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: var(--dark-bg-elevated);
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.strength-fill.weak {
  background: linear-gradient(90deg, #f85149, #ff6b6b);
}

.strength-fill.medium {
  background: linear-gradient(90deg, #d29922, #f0883e);
}

.strength-fill.strong {
  background: linear-gradient(90deg, #3fb950, #56d364);
}

.strength-text {
  font-size: 12px;
  font-weight: 500;
  min-width: 20px;
}

.strength-text.weak {
  color: #f85149;
}

.strength-text.medium {
  color: #d29922;
}

.strength-text.strong {
  color: #3fb950;
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .avatar {
    width: 60px;
    height: 60px;
    font-size: 24px;
  }

  .username {
    font-size: 20px;
  }
}
</style>
