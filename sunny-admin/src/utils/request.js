const BASE_URL = 'http://localhost:8080/api'
let fileBaseServer = 'http://localhost:8081/sunny_file'

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
  if (targetPath.startsWith('http://') || targetPath.startsWith('https://') || targetPath.startsWith('blob:') || targetPath.startsWith('data:')) {
    return targetPath
  }
  const cleanPath = targetPath.startsWith('/') ? targetPath : `/${targetPath}`
  const base = fileBaseServer.replace(/\/+$/, '')
  return `${base}${cleanPath}`
}

const request = (options) => {
  const token = uni.getStorageSync('admin_token')
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
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

export const uploadFile = (filePath, module = 'common', fileObj = null) => {
  const token = uni.getStorageSync('admin_token')
  return new Promise((resolve, reject) => {
    const uploadOptions = {
      url: `${BASE_URL}/file/upload`,
      name: 'file',
      formData: {
        module: module
      },
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (uploadRes) => {
        let res = uploadRes.data
        if (typeof res === 'string') {
          try {
            res = JSON.parse(res)
          } catch (e) {
            // not a valid json string
          }
        }

        const isHttpOk = !uploadRes.statusCode || (uploadRes.statusCode >= 200 && uploadRes.statusCode < 300)
        if (isHttpOk && res && (res.code === 200 || res.code === 0)) {
          resolve(res.data)
          return
        }

        // 解析清晰明确的错误信息
        let errMsg = ''
        if (res && typeof res === 'object') {
          errMsg = res.message || res.msg || res.error || ''
        }

        if (!errMsg && typeof res === 'string') {
          const raw = res.toLowerCase()
          if (
            raw.includes('maxuploadsizeexceededexception') ||
            raw.includes('filesizelimitexceededexception') ||
            raw.includes('sizelimitexceededexception') ||
            raw.includes('1048576') ||
            raw.includes('maximum permitted size') ||
            uploadRes.statusCode === 413
          ) {
            errMsg = '文件大小超出服务器限制（单文件最大50MB）'
          } else if (raw.includes('<html') || raw.includes('<!doctype')) {
            if (uploadRes.statusCode === 413) {
              errMsg = '文件大小超出限制（单文件最大50MB）'
            } else if (uploadRes.statusCode === 401) {
              errMsg = '登录凭据已过期，请重新登录'
            } else if (uploadRes.statusCode === 403) {
              errMsg = '无权限上传文件'
            } else if (uploadRes.statusCode === 404) {
              errMsg = '上传接口不存在(404)'
            } else if (uploadRes.statusCode >= 500) {
              errMsg = `服务器处理异常(${uploadRes.statusCode})`
            }
          } else if (res.length > 0 && res.length < 80) {
            errMsg = res
          }
        }

        if (!errMsg) {
          if (uploadRes.statusCode === 413) {
            errMsg = '文件大小超出限制（单文件最大50MB）'
          } else if (uploadRes.statusCode === 401) {
            errMsg = '登录凭据已过期，请重新登录'
          } else if (uploadRes.statusCode === 403) {
            errMsg = '无权限上传文件'
          } else if (uploadRes.statusCode === 404) {
            errMsg = '上传接口未找到(404)'
          } else if (uploadRes.statusCode && uploadRes.statusCode !== 200) {
            errMsg = `上传失败(HTTP ${uploadRes.statusCode})`
          } else {
            errMsg = '上传失败，请检查文件格式或大小'
          }
        }

        reject(new Error(errMsg))
      },
      fail: (err) => {
        console.error('uni.uploadFile fail:', err)
        let failMsg = '网络异常，文件上传失败'
        if (err) {
          const errStr = (err.errMsg || err.message || '').toLowerCase()
          if (errStr.includes('abort') || errStr.includes('cancel')) {
            failMsg = '上传已取消'
          } else if (errStr.includes('timeout')) {
            failMsg = '上传请求超时，图片过大或网络缓慢'
          } else if (errStr.includes('exceed') || errStr.includes('max') || errStr.includes('size')) {
            failMsg = '文件大小超出传输限制'
          } else if (errStr.includes('refused') || errStr.includes('failed') || errStr.includes('network')) {
            failMsg = '无法连接到后端服务器，请检查后端服务是否启动'
          } else if (err.errMsg || err.message) {
            failMsg = `上传失败: ${err.errMsg || err.message}`
          }
        }
        reject(new Error(failMsg))
      }
    }

    if (fileObj) {
      uploadOptions.file = fileObj
      // #ifndef H5
      if (typeof filePath === 'string' && filePath) {
        uploadOptions.filePath = filePath
      }
      // #endif
    } else if (typeof filePath === 'object' && filePath) {
      uploadOptions.file = filePath
    } else if (typeof filePath === 'string' && filePath) {
      uploadOptions.filePath = filePath
    }

    uni.uploadFile(uploadOptions)
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
  uploadFile
}
