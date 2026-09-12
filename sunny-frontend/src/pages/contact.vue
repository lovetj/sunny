<template>
  <view class="contact-page">
    <view class="contact-header">
      <image class="logo" src="/static/images/logo.png" mode="aspectFit"></image>
      <text class="title">Sunny农产品</text>
      <text class="subtitle">大山里的好货，城里的心意</text>
    </view>

    <view class="contact-section">
      <view class="section-title">联系方式</view>
      <view class="contact-item" @click="copyWechat">
        <view class="contact-icon wechat">微</view>
        <view class="contact-info">
          <text class="contact-label">微信号</text>
          <text class="contact-value">{{ config.wechat }}</text>
        </view>
        <text class="contact-action">复制</text>
      </view>
      <view class="contact-item" @click="copyDouyin">
        <view class="contact-icon douyin">抖</view>
        <view class="contact-info">
          <text class="contact-label">抖音号</text>
          <text class="contact-value">{{ config.douyin }}</text>
        </view>
        <text class="contact-action">复制</text>
      </view>
      <view class="contact-item" @click="callPhone">
        <view class="contact-icon phone">电</view>
        <view class="contact-info">
          <text class="contact-label">联系电话</text>
          <text class="contact-value">{{ config.phone }}</text>
        </view>
        <text class="contact-action">拨打</text>
      </view>
    </view>

    <view class="info-section">
      <view class="section-title">关于我们</view>
      <view class="info-card">
        <text class="info-text">
          我们是一群热爱大山的人，常年往返于城乡之间，将山里的优质农产品带到城里，也将城里的好货送到农村。
        </text>
        <text class="info-text">
          我们的产品都是来自大山深处的天然好货，干货、蔬菜、生态油、农家酿酒、养殖产品，每一样都是精心挑选。
        </text>
        <text class="info-text">
          关注我们的抖音号，观看直播，了解更多大山故事！
        </text>
      </view>
    </view>

    <view class="qrcode-section">
      <view class="section-title">扫码关注</view>
      <view class="qrcode-placeholder">
        <text>抖音二维码</text>
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
  onLoad() {
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
.contact-page {
  background: #f8f8f8;
  min-height: 100vh;
}

.contact-header {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 20rpx;
}

.title {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 12rpx;
}

.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
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
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.contact-item:last-child {
  border-bottom: none;
}

.contact-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #fff;
  margin-right: 20rpx;
}

.contact-icon.wechat {
  background: #07c160;
}

.contact-icon.douyin {
  background: #000;
}

.contact-icon.phone {
  background: #4CAF50;
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
  font-size: 30rpx;
  color: #333;
  margin-top: 8rpx;
  display: block;
}

.contact-action {
  font-size: 24rpx;
  color: #4CAF50;
  padding: 12rpx 24rpx;
  border: 1rpx solid #4CAF50;
  border-radius: 30rpx;
}

.info-section {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.info-card {
  background: #f8f8f8;
  border-radius: 12rpx;
  padding: 24rpx;
}

.info-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  display: block;
  margin-bottom: 16rpx;
}

.info-text:last-child {
  margin-bottom: 0;
}

.qrcode-section {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.qrcode-placeholder {
  width: 300rpx;
  height: 300rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  color: #999;
  font-size: 28rpx;
}
</style>
