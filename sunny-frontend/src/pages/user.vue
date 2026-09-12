<template>
  <view class="user-page">
    <view class="user-header">
      <image class="avatar" src="/static/images/avatar.png" mode="aspectFill"></image>
      <text class="nickname">山里来的朋友</text>
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
  </view>
</template>

<script>
import api from '../api/index'

export default {
  data() {
    return {
      config: {
        wechat: '',
        douyin: '',
        phone: ''
      }
    }
  },
  onShow() {
    this.loadConfig()
  },
  methods: {
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
      uni.navigateTo({ url: '/pages/order' })
    },
    goToAddress() {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
    goToLogistics() {
      uni.navigateTo({ url: '/pages/logistics' })
    },
    copyWechat() {
      uni.setClipboardData({
        data: this.config.wechat,
        success: () => {
          uni.showToast({ title: '微信号已复制', icon: 'success' })
        }
      })
    },
    copyDouyin() {
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
}

.user-header {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
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
</style>
