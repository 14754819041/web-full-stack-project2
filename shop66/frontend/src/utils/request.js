import axios from 'axios'
import store from '@/store'
import router from '@/router'
import { Toast } from 'vant'
import { API_BASE_URL } from './config'

const service = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token

      // 从localStorage获取用户信息
      const user = JSON.parse(localStorage.getItem('user'))
      if (user && (user.id || user.userId)) {
        // 如果是GET请求，添加userId到查询参数
        if (config.method === 'get') {
          config.params = {
            ...config.params,
            userId: user.id || user.userId
          }
        }
        // 如果是表单提交，添加userId到请求体
        else if (config.headers['Content-Type'] === 'application/x-www-form-urlencoded') {
          if (config.data instanceof URLSearchParams) {
            config.data.append('userId', user.id || user.userId)
          }
        }
        // 如果是JSON请求，添加userId到请求体
        else if (config.data && typeof config.data === 'object') {
          config.data.userId = user.id || user.userId
        }
      }
    }
    return config
  },
  error => {
    Toast.fail('请求错误')
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    console.log('API原始响应:', res)
    
    if (res.code !== 200) {
      const errorMsg = res.message || '请求失败'
      console.error('API错误响应:', {
        code: res.code,
        message: errorMsg,
        data: res.data
      })

      // 处理特定错误码
      switch (res.code) {
        case 401:
          Toast.fail('登录已过期，请重新登录')
          store.commit('clearUser')
          router.push('/login')
          break
        case 403:
          Toast.fail('没有权限')
          break
        default:
          Toast.fail(errorMsg.split('\n')[0])
      }
      
      return Promise.reject(new Error(errorMsg))
    }
    return res
  },
  error => {
    console.error('请求错误:', error.response || error)
    if (error.response) {
      switch (error.response.status) {
        case 401:
          Toast.fail('请先登录')
          store.commit('clearUser')
          router.push('/login')
          break
        case 403:
          Toast.fail('没有权限')
          break
        case 404:
          Toast.fail('资源不存在')
          break
        case 500:
          Toast.fail('服务器错误')
          break
        default:
          Toast.fail(error.message || '网络错误')
      }
    } else {
      Toast.fail('网络错误')
    }
    return Promise.reject(error)
  }
)

export default service 