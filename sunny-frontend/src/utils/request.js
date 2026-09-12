let BASE_URL = 'http://localhost:8080/api'
let fileBaseServer = 'http://localhost:8081/sunny_file'

// #ifdef H5
if (process.env.NODE_ENV === 'development') {
  BASE_URL = '/api'
  fileBaseServer = '/sunny_file'
}
// #endif

export const getBaseUrl = () => BASE_URL

export const getFileBaseServer = () => {
  return fileBaseServer
}

export const setFileBaseServer = (server) => {
  if (server) {
    fileBaseServer = server.replace(/\/+$/, '')
  }
}

export const formatImageUrl = (path) => {
  if (!path) return ''
  let targetPath = path
  if (typeof path === 'object') {
    targetPath = path.relativePath || path.url || path.path || ''
  }
  if (!targetPath || typeof targetPath !== 'string') return ''

  if (
    targetPath.startsWith('http://') ||
    targetPath.startsWith('https://') ||
    targetPath.startsWith('blob:') ||
    targetPath.startsWith('data:')
  ) {
    return targetPath
  }

  if (targetPath.startsWith('/static/images/') || targetPath.startsWith('/static/tabbar/')) {
    return targetPath
  }

  const cleanPath = targetPath.startsWith('/') ? targetPath : `/${targetPath}`
  const base = fileBaseServer.replace(/\/+$/, '')
  return `${base}${cleanPath}`
}

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    const url = (options.url.startsWith('http://') || options.url.startsWith('https://'))
      ? options.url
      : `${BASE_URL}${options.url.startsWith('/') ? options.url : '/' + options.url}`

    uni.request({
      url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          resolve(res.data.data)
        } else {
          uni.showToast({
            title: (res.data && res.data.message) || '请求失败',
            icon: 'none'
          })
          reject(res.data)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default {
  get(url, data) {
    return request({ url, method: 'GET', data })
  },
  post(url, data) {
    return request({ url, method: 'POST', data })
  },
  put(url, data) {
    return request({ url, method: 'PUT', data })
  },
  delete(url, data) {
    return request({ url, method: 'DELETE', data })
  },
  formatImageUrl,
  getFileBaseServer,
  setFileBaseServer
}
