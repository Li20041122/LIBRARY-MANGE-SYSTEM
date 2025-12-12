import axios from 'axios'
import router from '../router'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 5000,
  withCredentials: true
})

// 响应拦截器
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        // 未登录或登录过期
        localStorage.removeItem('userInfo')
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      } else if (error.response.data && error.response.data.message) {
        ElMessage.error(error.response.data.message)
      }
    }
    return Promise.reject(error)
  }
)

// 认证API
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  getCurrentUser: () => api.get('/auth/current'),
  register: (data) => api.post('/auth/register', data),
  changePassword: (data) => api.post('/auth/change-password', data)
}

// 图书API
export const bookApi = {
  getAll: () => api.get('/book'),
  getPage: (params) => api.get('/book/page', { params }),
  getById: (id) => api.get(`/book/${id}`),
  create: (data) => api.post('/book', data),
  update: (id, data) => api.put(`/book/${id}`, data),
  delete: (id) => api.delete(`/book/${id}`)
}

// 用户API
export const userApi = {
  getAll: () => api.get('/user'),
  getPage: (params) => api.get('/user/page', { params }),
  getById: (id) => api.get(`/user/${id}`),
  create: (data) => api.post('/user', data),
  update: (id, data) => api.put(`/user/${id}`, data),
  delete: (id) => api.delete(`/user/${id}`)
}

// 借阅API
export const borrowApi = {
  getAll: () => api.get('/borrow'),
  getPage: (params) => api.get('/borrow/page', { params }),
  getByKey: (userid, bookid) => api.get(`/borrow/${userid}/${bookid}`),
  create: (data) => api.post('/borrow', data),
  update: (userid, bookid, data) => api.put(`/borrow/${userid}/${bookid}`, data),
  delete: (userid, bookid) => api.delete(`/borrow/${userid}/${bookid}`)
}

// 部门API
export const departApi = {
  getAll: () => api.get('/depart'),
  getPage: (params) => api.get('/depart/page', { params }),
  getById: (id) => api.get(`/depart/${id}`),
  create: (data) => api.post('/depart', data),
  update: (id, data) => api.put(`/depart/${id}`, data),
  delete: (id) => api.delete(`/depart/${id}`)
}

export default api
