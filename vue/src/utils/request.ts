import axios, { type AxiosResponse, type AxiosRequestConfig, type AxiosError, type InternalAxiosRequestConfig } from 'axios'

// 响应数据类型定义
interface ApiResponse<T = any> {
  code: number
  data: T
  message: string
  success: boolean
}

// 请求配置类型
interface RequestConfig extends AxiosRequestConfig {
  showLoading?: boolean
  showError?: boolean
}

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api', // 基础URL，从环境变量读取
  timeout: 10000, // 超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 在发送请求之前做些什么
    console.log('发送请求:', config)
    return config
  },
  (error: AxiosError) => {
    // 对请求错误做些什么
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    // 对响应数据做点什么
    const { data } = response

    console.log('收到响应:', response)

    // 如果后端返回的不是标准格式，直接返回数据
    if (response.config.responseType !== 'json') {
      return response
    }

    // 根据后端约定的状态码处理
    if (data.code === 200 || data.success) {
      return { ...response, data }
    } else {
      // 业务错误
      console.error('业务错误:', data.message)
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  (error: AxiosError) => {
    // 对响应错误做点什么
    console.error('响应错误:', error)

    let message = '网络错误'

    if (error.response) {
      // 服务器返回了错误状态码
      const { status, data } = error.response
      switch (status) {
        case 400:
          message = '请求参数错误'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        default:
          message = `请求失败 ${status}`
      }

      // 如果后端返回了错误信息，优先使用后端的错误信息
      if (data && (data as any).message) {
        message = (data as any).message
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      message = '网络连接超时'
    } else {
      // 请求配置出错
      message = error.message || '请求配置错误'
    }

    return Promise.reject(new Error(message))
  }
)

// 通用请求方法
export const http = {
  get<T = any>(url: string, config?: RequestConfig): Promise<T> {
    return request.get(url, config).then(res => res.data)
  },

  post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return request.post(url, data, config).then(res => res.data)
  },

  put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return request.put(url, data, config).then(res => res.data)
  },

  patch<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return request.patch(url, data, config).then(res => res.data)
  },

    delete<T = any>(url: string, config?: RequestConfig): Promise<T> {
    return request.delete(url, config).then(res => res.data)
  }
}

// 导出axios实例（如果需要更底层的控制）
export default request

// 导出类型
export { type ApiResponse, type RequestConfig }
