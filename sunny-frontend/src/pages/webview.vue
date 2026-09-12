<template>
  <view class="webview-container">
    <!-- #ifdef H5 -->
    <view class="iframe-wrap" v-if="targetUrl">
      <view class="loading-mask" v-if="loading">
        <view class="loading-spinner"></view>
        <text class="loading-text">页面加载中...</text>
      </view>
      <iframe
        ref="webIframe"
        class="web-iframe"
        :src="targetUrl"
        frameborder="0"
        allowfullscreen
        sandbox="allow-scripts allow-same-origin allow-forms allow-popups allow-downloads"
        @load="onIframeLoad"
        @error="onIframeError"
      ></iframe>
    </view>
    <view class="empty-state" v-else>
      <text class="empty-icon">⚠️</text>
      <text class="empty-text">无效的网页链接</text>
      <button class="back-home-btn" @click="goHome">返回首页</button>
    </view>
    <!-- #endif -->

    <!-- #ifndef H5 -->
    <web-view v-if="targetUrl" :src="targetUrl" @message="onWebviewMessage"></web-view>
    <view class="empty-state" v-else>
      <text class="empty-icon">⚠️</text>
      <text class="empty-text">无效的网页链接</text>
      <button class="back-home-btn" @click="goHome">返回首页</button>
    </view>
    <!-- #endif -->
  </view>
</template>

<script>
export default {
  data() {
    return {
      targetUrl: '',
      pageTitle: '',
      loading: true,
      loadTimeout: null
    }
  },
  onLoad(options) {
    if (options && options.url) {
      try {
        let decoded = decodeURIComponent(options.url)
        if (!/^https?:\/\//i.test(decoded)) {
          decoded = 'https://' + decoded
        }
        this.targetUrl = decoded
      } catch (e) {
        this.targetUrl = options.url
      }
    }

    if (options && options.title) {
      try {
        this.pageTitle = decodeURIComponent(options.title)
      } catch (e) {
        this.pageTitle = options.title
      }
      uni.setNavigationBarTitle({
        title: this.pageTitle
      })
    }

    // 设置加载超时兜底
    this.loadTimeout = setTimeout(() => {
      this.loading = false
    }, 5000)
  },
  onUnload() {
    if (this.loadTimeout) {
      clearTimeout(this.loadTimeout)
    }
  },
  methods: {
    onIframeLoad() {
      this.loading = false
      if (this.loadTimeout) {
        clearTimeout(this.loadTimeout)
      }
    },
    onIframeError() {
      this.loading = false
    },
    onWebviewMessage(e) {
      console.log('webview message:', e)
    },
    goHome() {
      uni.switchTab({
        url: '/pages/index',
        fail: () => {
          uni.reLaunch({ url: '/pages/index' })
        }
      })
    }
  }
}
</script>

<style scoped>
.webview-container {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  background-color: #f5f7fa;
  overflow: hidden;
}

/* #ifdef H5 */
.iframe-wrap {
  flex: 1;
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.web-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 5;
}

.loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #e0e0e0;
  border-top-color: #4CAF50;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16rpx;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 26rpx;
  color: #666;
}
/* #endif */

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #666;
  margin-bottom: 40rpx;
}

.back-home-btn {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  color: #fff;
  font-size: 28rpx;
  padding: 16rpx 48rpx;
  border-radius: 40rpx;
  border: none;
}
</style>
