<template>
  <div id="app">
    <!-- 登录/注册页面不显示布局 -->
    <router-view v-if="$route.path === '/login' || $route.path === '/register'" />

    <!-- 主布局 -->
    <el-container v-else class="app-container">
      <el-header class="app-header">
        <div class="header-content">
          <h2 class="app-title">图书借阅管理系统</h2>
          <div class="header-right">
            <span class="user-info" v-if="userInfo">
              <span class="user-role">{{ userInfo.role === 'admin' ? '管理员' : '用户' }}</span>
              <span class="user-name">{{ userInfo.username }}</span>
            </span>
            <router-link to="/profile" class="profile-link">个人中心</router-link>
            <el-button type="text" class="logout-btn" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-container class="main-container">
        <el-aside class="app-sidebar" width="240px">
          <div class="sidebar-content">
            <el-menu :default-active="$route.path" router class="sidebar-menu">
              <el-menu-item index="/dashboard" class="menu-item">
                <span class="menu-icon">🏠</span>
                <span class="menu-text">首页</span>
              </el-menu-item>
              <el-menu-item index="/books" class="menu-item">
                <span class="menu-icon">📚</span>
                <span class="menu-text">图书管理</span>
              </el-menu-item>
              <el-menu-item index="/users" class="menu-item">
                <span class="menu-icon">👥</span>
                <span class="menu-text">用户管理</span>
              </el-menu-item>
              <el-menu-item index="/borrows" class="menu-item">
                <span class="menu-icon">📖</span>
                <span class="menu-text">借阅管理</span>
              </el-menu-item>
              <el-menu-item index="/departs" class="menu-item">
                <span class="menu-icon">🏢</span>
                <span class="menu-text">部门管理</span>
              </el-menu-item>
            </el-menu>
          </div>
        </el-aside>
        <el-main class="app-main">
          <div class="main-content">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from './api'

export default {
  name: 'App',
  setup() {
    const router = useRouter()

    const userInfo = computed(() => {
      const info = localStorage.getItem('userInfo')
      return info ? JSON.parse(info) : null
    })

    const handleLogout = async () => {
      try {
        await authApi.logout()
      } catch (e) {
        // 忽略错误
      }
      localStorage.removeItem('userInfo')
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    return {
      userInfo,
      handleLogout
    }
  }
}
</script>

<style>
#app {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  height: 100vh;
  overflow: hidden;
  background: var(--dark-bg-primary);
}

body {
  margin: 0;
  padding: 0;
}

.app-container {
  height: 100vh;
  background: var(--dark-bg-primary);
}

/* 顶部导航栏 - 暗黑风格 */
.app-header {
  background: var(--dark-bg-secondary);
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  height: 70px !important;
  padding: 0 var(--spacing-xl);
  display: flex;
  align-items: center;
  position: relative;
  z-index: 100;
}

.header-content {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.app-title {
  margin: 0;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-primary);
}

.user-role {
  background: var(--gradient-primary);
  padding: 6px 14px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.user-name {
  font-weight: 600;
  color: var(--text-primary);
}

.logout-btn {
  color: var(--text-secondary) !important;
  font-size: 14px;
  padding: 8px 16px !important;
  border-radius: var(--radius-md) !important;
  transition: all var(--transition-fast) !important;
}

.logout-btn:hover {
  color: var(--neon-red) !important;
  background: rgba(248, 81, 73, 0.1) !important;
}

.profile-link {
  color: var(--text-secondary);
  font-size: 14px;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.profile-link:hover {
  color: var(--neon-cyan);
  background: rgba(0, 212, 255, 0.1);
}

.main-container {
  height: calc(100vh - 70px);
  overflow: hidden;
}

/* 侧边栏 - 暗黑风格 */
.app-sidebar {
  background: var(--dark-bg-secondary);
  border-right: 1px solid var(--border-color);
  padding: var(--spacing-md);
  overflow-y: auto;
}

.sidebar-content {
  height: 100%;
}

.sidebar-menu {
  background: transparent !important;
  border: none !important;
}

.menu-item {
  margin: var(--spacing-sm) 0 !important;
  border-radius: var(--radius-md) !important;
  height: 50px !important;
  line-height: 50px !important;
  transition: all var(--transition-fast) !important;
  font-size: 15px !important;
  padding: 0 var(--spacing-md) !important;
  color: var(--text-secondary) !important;
  background: transparent !important;
  border: 1px solid transparent !important;
}

.menu-item:hover {
  background: var(--dark-bg-tertiary) !important;
  color: var(--text-primary) !important;
  border-color: var(--border-color) !important;
  transform: translateX(4px);
}

.menu-item.is-active {
  background: var(--gradient-primary) !important;
  color: white !important;
  border-color: transparent !important;
  box-shadow: var(--shadow-glow-cyan);
}

.menu-item.is-active .menu-icon {
  transform: scale(1.2);
}

.menu-icon {
  font-size: 20px;
  margin-right: var(--spacing-sm);
  display: inline-block;
  transition: transform var(--transition-fast);
}

.menu-text {
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 主内容区域 */
.app-main {
  background: var(--dark-bg-primary);
  padding: var(--spacing-lg);
  overflow-y: auto;
  overflow-x: hidden;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  animation: fadeIn 0.4s ease-out;
}

/* 滚动条美化 */
.app-sidebar::-webkit-scrollbar,
.app-main::-webkit-scrollbar {
  width: 6px;
}

.app-sidebar::-webkit-scrollbar-track,
.app-main::-webkit-scrollbar-track {
  background: transparent;
}

.app-sidebar::-webkit-scrollbar-thumb,
.app-main::-webkit-scrollbar-thumb {
  background: var(--dark-bg-elevated);
  border-radius: 3px;
}

.app-sidebar::-webkit-scrollbar-thumb:hover,
.app-main::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 var(--spacing-md);
    height: 60px !important;
  }

  .app-title {
    font-size: 18px;
  }

  .app-sidebar {
    width: 200px !important;
    padding: var(--spacing-sm);
  }

  .app-main {
    padding: var(--spacing-md);
  }

  .menu-item {
    height: 44px !important;
    line-height: 44px !important;
    font-size: 14px !important;
  }

  .menu-icon {
    font-size: 16px;
  }
}
</style>
