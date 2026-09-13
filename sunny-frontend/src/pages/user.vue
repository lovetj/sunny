<template>
  <view class="user-page">
    <view class="user-header" @click="handleHeaderClick">
      <image class="avatar" src="/static/images/avatar.png" mode="aspectFill"></image>
      <text class="nickname">{{ userInfo ? (userInfo.nickname || userInfo.username) : '点击登录/注册' }}</text>
      <text class="user-tip" v-if="!userInfo">登录后享受更多专属服务</text>
    </view>

    <view class="menu-section">
      <view class="menu-item" @click="goToOrder">
        <text class="menu-icon">📋</text>
        <text class="menu-text">我的订单</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToAddress">
        <text class="menu-icon">📍</text>
        <text class="menu-text">收货地址</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToLogistics">
        <text class="menu-icon">🚚</text>
        <text class="menu-text">物流信息</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <view class="contact-section">
      <view class="section-title">联系我们</view>
      <view class="contact-item" @click="copyWechat">
        <text class="contact-icon">💬</text>
        <view class="contact-info">
          <text class="contact-label">微信号</text>
          <text class="contact-value">{{ config.wechat }}</text>
        </view>
      </view>
      <view class="contact-item" @click="copyDouyin">
        <text class="contact-icon">🎵</text>
        <view class="contact-info">
          <text class="contact-label">抖音号</text>
          <text class="contact-value">{{ config.douyin }}</text>
        </view>
      </view>
      <view class="contact-item" @click="callPhone">
        <text class="contact-icon">📞</text>
        <view class="contact-info">
          <text class="contact-label">联系电话</text>
          <text class="contact-value">{{ config.phone }}</text>
        </view>
      </view>
    </view>

    <view class="logout-section" v-if="userInfo">
      <view class="logout-btn" @click="handleLogout">退出登录</view>
    </view>
  </view>
</template>

<script>
import api from '../api/index'
import { getUserInfo, clearLoginData, isLoggedIn, updateTabBarCartBadge } from '../utils/auth'

export default {
  data() {
    return {
      userInfo: null,
      config: {
        wechat: '',
        douyin: '',
        phone: ''
      }
    }
  },
  onShow() {
    this.checkUser()
    this.loadConfig()
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
    async loadConfig() {
      try {
        const [wechat, douyin, phone] = await Promise.all([
          api.getConfig('wechat'),
          api.getConfig('douyin'),
          api.getConfig('phone')
        ])
        this.config.wechat = wechat || ''
        this.config.douyin = douyin || ''
        this.config.phone = phone || ''
      } catch (e) {
        console.error(e)
      }
    },
    goToOrder() {
      if (!isLoggedIn()) {
        uni.navigateTo({ url: '/pages/login' })
        return
      }
      uni.navigateTo({ url: '/pages/order' })
    },
    goToAddress() {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
    goToLogistics() {
      uni.navigateTo({ url: '/pages/logistics' })
    },
    copyWechat() {
      if (!this.config.wechat) return
      uni.setClipboardData({
        data: this.config.wechat,
        success: () => {
          uni.showToast({ title: '微信号已复制', icon: 'success' })
        }
      })
    },
    copyDouyin() {
      if (!this.config.douyin) return
      uni.setClipboardData({
        data: this.config.douyin,
        success: () => {
          uni.showToast({ title: '抖音号已复制', icon: 'success' })
        }
      })
    },
    callPhone() {
      if (this.config.phone) {
        uni.makePhoneCall({ phoneNumber: this.config.phone })
      }
    }
  }
}
</script>

<style scoped>
.user-page {
  background: #f8f8f8;
  min-height: 100vh;
  padding-bottom: 40rpx;
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

.contact-section {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.contact-item:last-child {
  border-bottom: none;
}

.contact-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.contact-info {
  flex: 1;
}

.contact-label {
  font-size: 24rpx;
  color: #999;
  display: block;
}

.contact-value {
  font-size: 28rpx;
  color: #333;
  margin-top: 8rpx;
  display: block;
}

.logout-section {
  margin: 40rpx 20rpx;
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
