// 开发环境使用代理
export const API_BASE_URL = process.env.NODE_ENV === 'development' ? '' : 'http://localhost:8080'
// 图片URL不走代理
export const IMAGE_BASE_URL = 'http://localhost:8080'

export default {
  API_BASE_URL,
  IMAGE_BASE_URL
} 