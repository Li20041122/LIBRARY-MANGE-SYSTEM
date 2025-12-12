<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">图书借阅管理系统</h1>
        <p class="login-subtitle">Library Management System</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <p class="default-account">默认管理员账户: admin / admin123</p>
        <div class="register-link-wrapper">
          <span class="footer-text">还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const form = ref({
      username: '',
      password: ''
    })

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    }

    const handleLogin = async () => {
      try {
        await formRef.value.validate()
        loading.value = true

        const response = await authApi.login(form.value)
        if (response.data.code === 200) {
          const userInfo = response.data.data
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
          ElMessage.success('登录成功')
          router.push('/dashboard')
        } else {
          ElMessage.error(response.data.message || '登录失败')
        }
      } catch (error) {
        if (error.response && error.response.data) {
          ElMessage.error(error.response.data.message || '登录失败')
        } else if (error !== false) {
          ElMessage.error('登录失败，请检查网络')
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
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dark-bg-primary);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.login-container::before {
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

.login-card {
  width: 100%;
  max-width: 420px;
  background: var(--dark-bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: 48px 40px;
  box-shadow: var(--shadow-xl);
  position: relative;
  z-index: 1;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--gradient-primary);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
}

.login-subtitle {
  font-size: 14px;
  color: var(--text-tertiary);
  margin: 0;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.login-form {
  margin-top: 30px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  background: var(--dark-bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  height: 50px;
  box-shadow: none;
  transition: all var(--transition-fast);
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: var(--text-tertiary);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--neon-blue);
  box-shadow: var(--shadow-glow-blue);
}

.login-form :deep(.el-input__inner) {
  color: var(--text-primary);
  font-size: 15px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: var(--text-tertiary);
}

.login-form :deep(.el-input__prefix) {
  color: var(--text-tertiary);
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  border: none;
  letter-spacing: 2px;
  transition: all var(--transition-base);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow-cyan);
  filter: brightness(1.1);
}

.login-button:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.default-account {
  font-size: 13px;
  color: var(--text-tertiary);
  margin: 0 0 16px 0;
}

.register-link-wrapper {
  margin-top: 12px;
}

.footer-text {
  color: var(--text-tertiary);
  font-size: 14px;
}

.register-link {
  color: var(--neon-cyan);
  text-decoration: none;
  font-weight: 500;
  margin-left: var(--spacing-xs);
  transition: all var(--transition-fast);
}

.register-link:hover {
  text-decoration: underline;
  filter: brightness(1.2);
}
</style>
