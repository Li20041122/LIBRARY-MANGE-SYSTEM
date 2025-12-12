<template>
  <div class="dashboard">
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">
          <span class="greeting">{{ greeting }}</span>
          <span class="username">{{ userInfo?.username || '用户' }}</span>
        </h1>
        <p class="welcome-subtitle">欢迎使用图书借阅管理系统</p>
      </div>
      <div class="welcome-decoration">
        <span class="decoration-icon">📚</span>
      </div>
    </div>

    <div class="stats-grid" v-loading="loading">
      <div class="stat-card books">
        <div class="stat-icon">📖</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.books }}</span>
          <span class="stat-label">图书总数</span>
        </div>
        <div class="stat-bg"></div>
      </div>

      <div class="stat-card users">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.users }}</span>
          <span class="stat-label">用户总数</span>
        </div>
        <div class="stat-bg"></div>
      </div>

      <div class="stat-card borrows">
        <div class="stat-icon">📋</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.borrows }}</span>
          <span class="stat-label">借阅记录</span>
        </div>
        <div class="stat-bg"></div>
      </div>

      <div class="stat-card departs">
        <div class="stat-icon">🏢</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.departs }}</span>
          <span class="stat-label">部门数量</span>
        </div>
        <div class="stat-bg"></div>
      </div>
    </div>

    <div class="quick-actions">
      <h3 class="section-title">快捷操作</h3>
      <div class="actions-grid">
        <router-link to="/books" class="action-card">
          <span class="action-icon">📚</span>
          <span class="action-text">浏览图书</span>
        </router-link>
        <router-link to="/borrows" class="action-card">
          <span class="action-icon">📖</span>
          <span class="action-text">我的借阅</span>
        </router-link>
        <router-link to="/users" class="action-card" v-if="isAdmin">
          <span class="action-icon">👥</span>
          <span class="action-text">管理用户</span>
        </router-link>
        <router-link to="/departs" class="action-card" v-if="isAdmin">
          <span class="action-icon">🏢</span>
          <span class="action-text">管理部门</span>
        </router-link>
      </div>
    </div>

    <div class="charts-section">
      <div class="chart-card">
        <h3 class="chart-title">📊 数据概览</h3>
        <div ref="barChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <h3 class="chart-title">📈 数据占比</h3>
        <div ref="pieChartRef" class="chart-container"></div>
      </div>
    </div>

    <div class="info-section">
      <div class="info-card">
        <h3 class="info-title">系统信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">当前用户</span>
            <span class="info-value">{{ userInfo?.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">用户角色</span>
            <el-tag :type="isAdmin ? 'danger' : 'info'" size="small">
              {{ isAdmin ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">用户ID</span>
            <span class="info-value">{{ userInfo?.userid || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">所属部门</span>
            <span class="info-value">{{ userInfo?.departid || '无' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { bookApi, userApi, borrowApi, departApi } from '../api'
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  setup() {
    const loading = ref(false)
    const barChartRef = ref(null)
    const pieChartRef = ref(null)
    let barChart = null
    let pieChart = null

    const stats = ref({
      books: 0,
      users: 0,
      borrows: 0,
      departs: 0
    })

    const userInfo = computed(() => {
      const info = localStorage.getItem('userInfo')
      return info ? JSON.parse(info) : null
    })

    const isAdmin = computed(() => {
      return userInfo.value?.role === 'admin'
    })

    const greeting = computed(() => {
      const hour = new Date().getHours()
      if (hour < 6) return '夜深了'
      if (hour < 9) return '早上好'
      if (hour < 12) return '上午好'
      if (hour < 14) return '中午好'
      if (hour < 18) return '下午好'
      if (hour < 22) return '晚上好'
      return '夜深了'
    })

    const initCharts = () => {
      // 柱状图
      if (barChartRef.value) {
        barChart = echarts.init(barChartRef.value)
        const barOption = {
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'axis',
            backgroundColor: '#161b22',
            borderColor: '#30363d',
            textStyle: { color: '#f0f6fc' }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: ['图书', '用户', '借阅', '部门'],
            axisLine: { lineStyle: { color: '#30363d' } },
            axisLabel: { color: '#8b949e' }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#30363d' } },
            axisLabel: { color: '#8b949e' },
            splitLine: { lineStyle: { color: '#21262d' } }
          },
          series: [{
            data: [
              { value: stats.value.books, itemStyle: { color: '#00d4ff' } },
              { value: stats.value.users, itemStyle: { color: '#00a8e8' } },
              { value: stats.value.borrows, itemStyle: { color: '#3fb950' } },
              { value: stats.value.departs, itemStyle: { color: '#f85149' } }
            ],
            type: 'bar',
            barWidth: '50%',
            itemStyle: {
              borderRadius: [6, 6, 0, 0]
            }
          }]
        }
        barChart.setOption(barOption)
      }

      // 饼图
      if (pieChartRef.value) {
        pieChart = echarts.init(pieChartRef.value)
        const pieOption = {
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'item',
            backgroundColor: '#161b22',
            borderColor: '#30363d',
            textStyle: { color: '#f0f6fc' }
          },
          legend: {
            bottom: '5%',
            textStyle: { color: '#8b949e' }
          },
          series: [{
            name: '数据统计',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#161b22',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 20,
                fontWeight: 'bold',
                color: '#f0f6fc'
              }
            },
            labelLine: { show: false },
            data: [
              { value: stats.value.books, name: '图书', itemStyle: { color: '#00d4ff' } },
              { value: stats.value.users, name: '用户', itemStyle: { color: '#00a8e8' } },
              { value: stats.value.borrows, name: '借阅', itemStyle: { color: '#3fb950' } },
              { value: stats.value.departs, name: '部门', itemStyle: { color: '#f85149' } }
            ]
          }]
        }
        pieChart.setOption(pieOption)
      }
    }

    const updateCharts = () => {
      if (barChart) {
        barChart.setOption({
          series: [{
            data: [
              { value: stats.value.books, itemStyle: { color: '#00d4ff' } },
              { value: stats.value.users, itemStyle: { color: '#00a8e8' } },
              { value: stats.value.borrows, itemStyle: { color: '#3fb950' } },
              { value: stats.value.departs, itemStyle: { color: '#f85149' } }
            ]
          }]
        })
      }
      if (pieChart) {
        pieChart.setOption({
          series: [{
            data: [
              { value: stats.value.books, name: '图书', itemStyle: { color: '#00d4ff' } },
              { value: stats.value.users, name: '用户', itemStyle: { color: '#00a8e8' } },
              { value: stats.value.borrows, name: '借阅', itemStyle: { color: '#3fb950' } },
              { value: stats.value.departs, name: '部门', itemStyle: { color: '#f85149' } }
            ]
          }]
        })
      }
    }

    const handleResize = () => {
      barChart?.resize()
      pieChart?.resize()
    }

    const loadStats = async () => {
      loading.value = true
      try {
        const [booksRes, usersRes, borrowsRes, departsRes] = await Promise.all([
          bookApi.getPage({ page: 1, size: 1 }),
          userApi.getPage({ page: 1, size: 1 }),
          borrowApi.getPage({ page: 1, size: 1 }),
          departApi.getPage({ page: 1, size: 1 })
        ])

        if (booksRes.data.code === 200) stats.value.books = booksRes.data.data.total
        if (usersRes.data.code === 200) stats.value.users = usersRes.data.data.total
        if (borrowsRes.data.code === 200) stats.value.borrows = borrowsRes.data.data.total
        if (departsRes.data.code === 200) stats.value.departs = departsRes.data.data.total

        await nextTick()
        updateCharts()
      } catch (error) {
        console.error('加载统计数据失败', error)
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      initCharts()
      loadStats()
      window.addEventListener('resize', handleResize)
    })

    onUnmounted(() => {
      window.removeEventListener('resize', handleResize)
      barChart?.dispose()
      pieChart?.dispose()
    })

    return {
      loading,
      stats,
      userInfo,
      isAdmin,
      greeting,
      barChartRef,
      pieChartRef
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 0;
  animation: fadeIn 0.4s ease-out;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-xl);
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.15) 0%, rgba(0, 153, 204, 0.15) 100%);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-lg);
  position: relative;
  overflow: hidden;
}

.welcome-content {
  position: relative;
  z-index: 1;
}

.welcome-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
}

.greeting {
  color: var(--text-secondary);
  margin-right: 8px;
}

.username {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  margin: 8px 0 0 0;
  font-size: 15px;
  color: var(--text-tertiary);
}

.welcome-decoration {
  position: relative;
  z-index: 1;
}

.decoration-icon {
  font-size: 64px;
  opacity: 0.8;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.stat-card {
  position: relative;
  padding: var(--spacing-xl);
  background: var(--dark-bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  overflow: hidden;
  transition: all var(--transition-base);
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: var(--text-tertiary);
}

.stat-card.books:hover {
  box-shadow: 0 8px 30px rgba(0, 212, 255, 0.25);
}

.stat-card.users:hover {
  box-shadow: 0 8px 30px rgba(0, 168, 232, 0.25);
}

.stat-card.borrows:hover {
  box-shadow: 0 8px 30px rgba(63, 185, 80, 0.2);
}

.stat-card.departs:hover {
  box-shadow: 0 8px 30px rgba(248, 81, 73, 0.2);
}

.stat-icon {
  font-size: 40px;
  position: relative;
  z-index: 1;
}

.stat-info {
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.stat-bg {
  position: absolute;
  right: -20px;
  bottom: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.1;
}

.stat-card.books .stat-bg {
  background: var(--neon-cyan);
}

.stat-card.users .stat-bg {
  background: var(--neon-teal);
}

.stat-card.borrows .stat-bg {
  background: var(--neon-green);
}

.stat-card.departs .stat-bg {
  background: var(--neon-red);
}

.quick-actions {
  margin-bottom: var(--spacing-lg);
}

.section-title {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
  background: var(--dark-bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-base);
}

.action-card:hover {
  background: var(--dark-bg-tertiary);
  border-color: var(--text-tertiary);
  transform: translateY(-2px);
}

.action-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.action-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.action-card:hover .action-text {
  color: var(--text-primary);
}

.info-section {
  display: grid;
  grid-template-columns: 1fr;
}

.info-card {
  padding: var(--spacing-xl);
  background: var(--dark-bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.info-title {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: var(--text-tertiary);
}

.info-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.chart-card {
  padding: var(--spacing-xl);
  background: var(--dark-bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.chart-title {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-container {
  width: 100%;
  height: 280px;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    text-align: center;
    padding: var(--spacing-lg);
  }

  .welcome-title {
    font-size: 24px;
  }

  .decoration-icon {
    font-size: 48px;
    margin-top: var(--spacing-md);
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-value {
    font-size: 28px;
  }
}
</style>
