<template>
  <view class="user-page">
    <scroll-view scroll-y class="user-scroll" :show-scrollbar="true">
      <view class="user-header" @click="handleHeaderClick">
        <image class="avatar" :src="avatarSrc" mode="aspectFill"></image>
        <text class="nickname">{{ userInfo ? (userInfo.nickname || userInfo.username) : '点击登录/注册' }}</text>
        <text class="user-tip" v-if="!userInfo">登录后享受更多专属服务</text>
      </view>

      <view class="menu-section">
        <view class="menu-item" @click="goToProfile" v-if="userInfo">
          <text class="menu-icon">👤</text>
          <text class="menu-text">基本信息</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToOrder" v-if="userInfo">
          <text class="menu-icon">📋</text>
          <text class="menu-text">我的订单</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToAddress" v-if="userInfo">
          <text class="menu-icon">📍</text>
          <text class="menu-text">收货地址</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToLogistics" v-if="userInfo">
          <text class="menu-icon">🚚</text>
          <text class="menu-text">物流信息</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goToChangePassword" v-if="userInfo">
          <text class="menu-icon">🔐</text>
          <text class="menu-text">更改密码</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="logout-section" v-if="userInfo">
        <view class="logout-btn" @click="handleLogout">退出登录</view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import api from '../api/index'
import request from '../utils/request'
import { getUserInfo, clearLoginData, isLoggedIn, updateTabBarCartBadge } from '../utils/auth'

export default {
  data() {
    return {
      userInfo: null
    }
  },
  computed: {
    avatarSrc() {
      if (this.userInfo && this.userInfo.avatar) {
        return request.formatImageUrl(this.userInfo.avatar)
      }
      return '/static/images/avatar.png'
    }
  },
  onShow() {
    this.checkUser()
    updateTabBarCartBadge()
  },
  methods: {
    async checkUser() {
      if (isLoggedIn()) {
        const localUser = getUserInfo()
        if (localUser) {
          this.userInfo = localUser
        }
        try {
          const remoteUser = await api.getUserInfo()
          if (remoteUser) {
            this.userInfo = remoteUser
            uni.setStorageSync('user_info', remoteUser)
          }
        } catch (e) {
          // Token 可能失效
        }
      } else {
        this.userInfo = null
      }
    },
    handleHeaderClick() {
      if (!this.userInfo) {
        uni.navigateTo({ url: '/pages/login' })
      }
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            clearLoginData()
            this.userInfo = null
            uni.showToast({ title: '已退出登录', icon: 'none' })
          }
        }
      })
    },
    goToOrder() {
      if (!isLoggedIn()) {
        uni.navigateTo({ url: '/pages/login' })
        return
      }
      uni.navigateTo({ url: '/pages/order' })
    },
    goToProfile() {
      uni.navigateTo({ url: '/pages/user/edit' })
    },
    goToAddress() {
      if (!isLoggedIn()) {
        uni.navigateTo({ url: '/pages/login' })
        return
      }
      uni.navigateTo({ url: '/pages/address/list' })
    },
    goToLogistics() {
      uni.navigateTo({ url: '/pages/logistics' })
    },
    goToChangePassword() {
      uni.navigateTo({ url: '/pages/user/password' })
    }
  }
}
</script>

<style scoped>
.user-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  max-width: 100vw;
  background: #f8f8f8;
  overflow: hidden;
  box-sizing: border-box;
}

.user-scroll {
  flex: 1;
  min-height: 0;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

.user-header {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 4rpx solid #fff;
  margin-bottom: 20rpx;
}

.nickname {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
}

.user-tip {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 8rpx;
}

.menu-section {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.menu-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.logout-section {
  margin: 40rpx 20rpx;
  padding-bottom: 40rpx;
}

.logout-btn {
  background: #fff;
  color: #ff4d4f;
  text-align: center;
  padding: 24rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 500;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}
</style>

<style>
page,
uni-page,
uni-page-body {
  height: 100% !important;
  width: 100% !important;
  max-width: 100vw !important;
  overflow: hidden !important;
  background: #f8f8f8;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  touch-action: pan-y;
}

uni-page-wrapper {
  width: 100% !important;
  max-width: 100vw !important;
  overflow: hidden !important;
  box-sizing: border-box;
}

.user-scroll,
.user-scroll .uni-scroll-view {
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.18) transparent;
}

.user-scroll::-webkit-scrollbar,
.user-scroll .uni-scroll-view::-webkit-scrollbar {
  display: block !important;
  width: 5px !important;
  height: 5px !important;
  background: transparent !important;
}

.user-scroll::-webkit-scrollbar-thumb,
.user-scroll .uni-scroll-view::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.18) !important;
  border-radius: 6px !important;
}

.user-scroll::-webkit-scrollbar-thumb:hover,
.user-scroll .uni-scroll-view::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.32) !important;
}

.user-scroll::-webkit-scrollbar-track,
.user-scroll .uni-scroll-view::-webkit-scrollbar-track {
  background: transparent !important;
}

.user-scroll .uni-scroll-view,
.user-scroll .uni-scroll-view-wrap,
.user-scroll .uni-scroll-view-content {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box !important;
}
</style>
