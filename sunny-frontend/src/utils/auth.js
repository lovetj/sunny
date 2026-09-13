import api from '../api/index'

export const TOKEN_KEY = 'token'
export const USER_INFO_KEY = 'user_info'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function getUserInfo() {
  return uni.getStorageSync(USER_INFO_KEY) || null
}

export function isLoggedIn() {
  return !!getToken()
}

export function setLoginData(token, user) {
  if (token) {
    uni.setStorageSync(TOKEN_KEY, token)
  }
  if (user) {
    uni.setStorageSync(USER_INFO_KEY, user)
  }
}

export function clearLoginData() {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(USER_INFO_KEY)
  try {
    uni.removeTabBarBadge({
      index: 2,
      fail: () => {}
    })
  } catch (e) {}
}

export async function updateTabBarCartBadge() {
  if (!isLoggedIn()) {
    try {
      uni.removeTabBarBadge({
        index: 2,
        fail: () => {}
      })
    } catch (e) {}
    return 0
  }

  try {
    const count = await api.getCartCount()
    const totalCount = Number(count) || 0
    if (totalCount > 0) {
      uni.setTabBarBadge({
        index: 2,
        text: totalCount > 99 ? '99+' : String(totalCount),
        fail: () => {}
      })
    } else {
      uni.removeTabBarBadge({
        index: 2,
        fail: () => {}
      })
    }
    return totalCount
  } catch (e) {
    return 0
  }
}

export function checkLogin(redirectToLogin = true) {
  if (!isLoggedIn()) {
    if (redirectToLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login'
        })
      }, 500)
    }
    return false
  }
  return true
}

export default {
  getToken,
  getUserInfo,
  isLoggedIn,
  setLoginData,
  clearLoginData,
  updateTabBarCartBadge,
  checkLogin
}
