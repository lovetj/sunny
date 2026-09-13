<template>
  <view class="login-page">
    <view class="login-header">
      <text class="logo">🌾</text>
      <text class="title">Sunny农产品商城</text>
      <text class="subtitle">{{ isRegister ? '创建您的新账号' : '欢迎回来，请登录您的账号' }}</text>
    </view>

    <view class="login-form">
      <view class="form-item">
        <text class="form-label">用户名</text>
        <input class="form-input" v-model="form.username" placeholder="请输入用户名" />
      </view>
      <view class="form-item">
        <text class="form-label">密码</text>
        <input class="form-input" v-model="form.password" type="password" placeholder="请输入密码" />
      </view>
      <view class="form-item" v-if="isRegister">
        <text class="form-label">手机号 (选填)</text>
        <input class="form-input" v-model="form.phone" placeholder="请输入手机号" />
      </view>

      <view class="login-btn" @click="handleSubmit">
        {{ isRegister ? '立即注册' : '登 录' }}
      </view>

      <view class="form-footer">
        <text class="toggle-mode" @click="toggleMode">
          {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
        </text>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../api/index'
import { setLoginData, updateTabBarCartBadge } from '../utils/auth'

export default {
  data() {
    return {
      isRegister: false,
      form: {
        username: '',
        password: '',
        phone: ''
      }
    }
  },
  methods: {
    toggleMode() {
      this.isRegister = !this.isRegister
    },
    async handleSubmit() {
      if (!this.form.username || !this.form.username.trim()) {
        uni.showToast({ title: '请输入用户名', icon: 'none' })
        return
      }
      if (!this.form.password || !this.form.password.trim()) {
        uni.showToast({ title: '请输入密码', icon: 'none' })
        return
      }

      try {
        if (this.isRegister) {
          await api.register(this.form)
          uni.showToast({ title: '注册成功，请登录', icon: 'success' })
          this.isRegister = false
        } else {
          const data = await api.login({
            username: this.form.username,
            password: this.form.password
          })
          if (data && data.token) {
            setLoginData(data.token, data.user)
            updateTabBarCartBadge()
            uni.showToast({ title: '登录成功', icon: 'success' })
            setTimeout(() => {
              const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
              if (pages.length > 1) {
                uni.navigateBack()
              } else {
                uni.switchTab({ url: '/pages/index' })
              }
            }, 800)
          }
        }
      } catch (e) {
        console.error(e)
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 140rpx;
  box-sizing: border-box;
}

.login-header {
  text-align: center;
  margin-bottom: 60rpx;
}

.logo {
  font-size: 110rpx;
  display: block;
  margin-bottom: 20rpx;
}

.title {
  font-size: 42rpx;
  color: #fff;
  font-weight: bold;
  display: block;
}

.subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 10rpx;
  display: block;
}

.login-form {
  width: 620rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 44rpx;
  box-sizing: border-box;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
  display: block;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 84rpx;
  padding: 0 24rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 10rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  background: #fafafa;
}

.login-btn {
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  color: #fff;
  text-align: center;
  padding: 24rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: bold;
  margin-top: 36rpx;
  box-shadow: 0 6rpx 16rpx rgba(76, 175, 80, 0.3);
}

.form-footer {
  margin-top: 30rpx;
  text-align: center;
}

.toggle-mode {
  font-size: 26rpx;
  color: #4CAF50;
  padding: 10rpx;
}
</style>
