<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <div class="logo">📚</div>
        <h1 class="title">用户注册</h1>
        <p class="subtitle">创建您的图书管理系统账号</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        label-position="top"
      >
        <el-form-item label="用户ID" prop="userid">
          <el-input
            v-model="form.userid"
            placeholder="请输入用户ID（唯一标识）"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="UserFilled"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（6-20位）"
            prefix-icon="Lock"
            show-password
          />
          <div class="password-strength" v-if="form.password">
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

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="手机号码" prop="phonenum">
          <el-input
            v-model="form.phonenum"
            placeholder="请输入手机号码（选填）"
            prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span class="footer-text">已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)

    const form = ref({
      userid: '',
      username: '',
      password: '',
      confirmPassword: '',
      phonenum: '',
      sex: '男'
    })

    // 密码强度计算
    const passwordStrength = computed(() => {
      const pwd = form.value.password
      if (!pwd) return { level: '', text: '', percent: 0 }

      let score = 0
      // 长度检查
      if (pwd.length >= 6) score += 1
      if (pwd.length >= 8) score += 1
      if (pwd.length >= 12) score += 1
      // 包含数字
      if (/\d/.test(pwd)) score += 1
      // 包含小写字母
      if (/[a-z]/.test(pwd)) score += 1
      // 包含大写字母
      if (/[A-Z]/.test(pwd)) score += 1
      // 包含特殊字符
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
      if (value !== form.value.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    const rules = {
      userid: [
        { required: true, message: '请输入用户ID', trigger: 'blur' },
        { max: 20, message: '用户ID长度不能超过20', trigger: 'blur' }
      ],
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { max: 50, message: '用户名长度不能超过50', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需在6-20位之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }

    const handleRegister = async () => {
      try {
        await formRef.value.validate()
        loading.value = true

        const response = await authApi.register(form.value)
        if (response.data.code === 200) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(response.data.message || '注册失败')
        }
      } catch (error) {
        if (error !== false && error.response?.data?.message) {
          ElMessage.error(error.response.data.message)
        } else if (error !== false) {
          ElMessage.error('注册失败，请稍后重试')
        }
      } finally {
        loading.value = false
      }
    }

    return {
      formRef,
      form,
      rules,
      loading,
      passwordStrength,
      handleRegister
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dark-bg-primary);
  padding: var(--spacing-lg);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.register-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 30%, rgba(0, 212, 255, 0.15) 0%, transparent 50%),
              radial-gradient(circle at 70% 70%, rgba(0, 153, 204, 0.15) 0%, transparent 50%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.register-card {
  width: 100%;
  max-width: 460px;
  padding: var(--spacing-xl);
  background: var(--dark-bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  position: relative;
  z-index: 1;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.register-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.logo {
  font-size: 48px;
  margin-bottom: var(--spacing-md);
}

.title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  margin: var(--spacing-sm) 0 0 0;
  color: var(--text-tertiary);
  font-size: 14px;
}

.register-form {
  margin-top: var(--spacing-lg);
}

.register-form :deep(.el-form-item__label) {
  color: var(--text-secondary) !important;
  font-size: 13px !important;
  padding-bottom: 4px !important;
}

.register-form :deep(.el-input__wrapper) {
  background: var(--dark-bg-tertiary) !important;
  border: 1px solid var(--border-color) !important;
  border-radius: var(--radius-md) !important;
  box-shadow: none !important;
  height: 44px !important;
}

.register-form :deep(.el-input__wrapper:hover) {
  border-color: var(--text-tertiary) !important;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--neon-cyan) !important;
  box-shadow: var(--shadow-glow-cyan) !important;
}

.register-form :deep(.el-input__inner) {
  color: var(--text-primary) !important;
  font-size: 14px !important;
}

.register-form :deep(.el-input__inner::placeholder) {
  color: var(--text-tertiary) !important;
}

.register-form :deep(.el-input__prefix) {
  color: var(--text-tertiary) !important;
}

.register-form :deep(.el-radio__label) {
  color: var(--text-secondary) !important;
}

.register-form :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: var(--neon-cyan) !important;
  border-color: var(--neon-cyan) !important;
}

.register-form :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: var(--neon-cyan) !important;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: var(--gradient-primary) !important;
  border: none !important;
  border-radius: var(--radius-md) !important;
  letter-spacing: 2px;
  transition: all var(--transition-base);
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow-cyan);
  filter: brightness(1.1);
}

.register-button:active {
  transform: translateY(0);
}

.register-footer {
  margin-top: var(--spacing-lg);
  text-align: center;
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--border-color);
}

.footer-text {
  color: var(--text-tertiary);
  font-size: 14px;
}

.login-link {
  color: var(--neon-cyan);
  text-decoration: none;
  font-weight: 500;
  margin-left: var(--spacing-xs);
  transition: all var(--transition-fast);
}

.login-link:hover {
  text-decoration: underline;
  filter: brightness(1.2);
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

@media (max-width: 480px) {
  .register-card {
    padding: var(--spacing-lg);
  }

  .title {
    font-size: 24px;
  }

  .logo {
    font-size: 40px;
  }
}
</style>
