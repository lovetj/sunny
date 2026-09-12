<template>
  <view class="product-detail-page" v-if="product">
    <swiper class="product-swiper" indicator-dots indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff" circular>
      <swiper-item v-if="product.image">
        <image :src="product.image" mode="aspectFill" @click="previewImage(product.image)"></image>
      </swiper-item>
      <swiper-item v-for="(img, index) in imageList" :key="index">
        <image :src="img" mode="aspectFill" @click="previewImage(img)"></image>
      </swiper-item>
    </swiper>

    <view class="product-info-card">
      <view class="price-row">
        <view class="price-main">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ product.price }}</text>
          <text class="price-unit">/{{ product.unit }}</text>
        </view>
        <view class="sales-info">
          <text>已售 {{ product.sales || 0 }}</text>
        </view>
      </view>
      <text class="product-name">{{ product.name }}</text>
      <view class="product-tags">
        <view class="tag" v-if="product.origin">
          <text>📍 {{ product.origin }}</text>
        </view>
        <view class="tag">
          <text>✓ 品质保证</text>
        </view>
        <view class="tag">
          <text>🚚 新鲜直达</text>
        </view>
      </view>
    </view>

    <view class="info-section">
      <view class="section-title">商品信息</view>
      <view class="info-grid">
        <view class="info-item">
          <text class="info-label">分类</text>
          <text class="info-value">{{ product.categoryName || '农产品' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">库存</text>
          <text class="info-value">{{ product.stock || 999 }}{{ product.unit }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">产地</text>
          <text class="info-value">{{ product.origin || '大山村' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">单位</text>
          <text class="info-value">{{ product.unit || '斤' }}</text>
        </view>
      </view>
    </view>

    <view class="desc-section">
      <view class="section-title">商品描述</view>
      <view class="desc-content">
        <text>{{ product.description || '农家好货，品质保证。来自大山深处的天然农产品，新鲜采摘，精心挑选，确保每一份都是优质好货。' }}</text>
      </view>
    </view>

    <view class="recommend-section" v-if="recommendList.length > 0">
      <view class="section-title">猜你喜欢</view>
      <scroll-view scroll-x class="recommend-scroll">
        <view class="recommend-list">
          <view class="recommend-item" v-for="item in recommendList" :key="item.id" @click="goToDetail(item.id)">
            <image class="recommend-image" :src="item.image || defaultImage" mode="aspectFill"></image>
            <text class="recommend-name">{{ item.name }}</text>
            <text class="recommend-price">¥{{ item.price }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="bottom-bar">
      <view class="bar-left">
        <view class="bar-item" @click="goHome">
          <text class="bar-icon">🏠</text>
          <text class="bar-text">首页</text>
        </view>
        <view class="bar-item" @click="goCart">
          <text class="bar-icon">🛒</text>
          <text class="bar-text">购物车</text>
          <view class="cart-badge" v-if="cartCount > 0">{{ cartCount }}</view>
        </view>
      </view>
      <view class="bar-right">
        <view class="bar-btn add-cart" @click="addToCart">
          <text>加入购物车</text>
        </view>
        <view class="bar-btn buy-now" @click="buyNow">
          <text>立即购买</text>
        </view>
      </view>
    </view>

    <view class="quantity-popup" v-if="showQuantity">
      <view class="popup-mask" @click="showQuantity = false"></view>
      <view class="popup-content">
        <view class="popup-header">
          <image class="popup-image" :src="product.image || defaultImage" mode="aspectFill"></image>
          <view class="popup-info">
            <view class="popup-price">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ product.price }}</text>
            </view>
            <text class="popup-stock">库存: {{ product.stock || 999 }}{{ product.unit }}</text>
          </view>
          <view class="popup-close" @click="showQuantity = false">✕</view>
        </view>
        <view class="quantity-row">
          <text class="quantity-label">购买数量</text>
          <view class="quantity-control">
            <view class="control-btn" :class="{ disabled: quantity <= 1 }" @click="decreaseQuantity">-</view>
            <text class="control-num">{{ quantity }}</text>
            <view class="control-btn" :class="{ disabled: quantity >= (product.stock || 999) }" @click="increaseQuantity">+</view>
          </view>
        </view>
        <view class="popup-total">
          <text class="total-label">小计:</text>
          <text class="total-price">¥{{ (product.price * quantity).toFixed(2) }}</text>
        </view>
        <view class="popup-btn" @click="confirmAction">确定</view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../api/index'

export default {
  data() {
    return {
      product: null,
      imageList: [],
      recommendList: [],
      quantity: 1,
      showQuantity: false,
      actionType: 'cart',
      cartCount: 0,
      defaultImage: '/static/images/product-default.jpg'
    }
  },
  onLoad(options) {
    if (options.id) {
      this.loadDetail(options.id)
    }
    this.loadRecommend()
    this.updateCartCount()
  },
  onShow() {
    this.updateCartCount()
  },
  methods: {
    async loadDetail(id) {
      try {
        const data = await api.getProductDetail(id)
        this.product = data
        if (data && data.images) {
          try {
            this.imageList = JSON.parse(data.images)
          } catch (e) {
            this.imageList = []
          }
        }
      } catch (e) {
        console.error(e)
      }
    },
    async loadRecommend() {
      try {
        const data = await api.getProductList()
        this.recommendList = (data || []).slice(0, 6)
      } catch (e) {
        console.error(e)
      }
    },
    updateCartCount() {
      const cart = uni.getStorageSync('cart') || []
      this.cartCount = cart.reduce((sum, item) => sum + item.quantity, 0)
    },
    previewImage(url) {
      uni.previewImage({ urls: [url] })
    },
    goBack() {
      uni.navigateBack()
    },
    goHome() {
      uni.switchTab({ url: '/pages/index' })
    },
    goCart() {
      uni.switchTab({ url: '/pages/cart' })
    },
    shareProduct() {
      uni.showToast({ title: '分享功能开发中', icon: 'none' })
    },
    addToCart() {
      this.actionType = 'cart'
      this.quantity = 1
      this.showQuantity = true
    },
    buyNow() {
      this.actionType = 'buy'
      this.quantity = 1
      this.showQuantity = true
    },
    decreaseQuantity() {
      if (this.quantity > 1) {
        this.quantity--
      }
    },
    increaseQuantity() {
      if (this.quantity < (this.product.stock || 999)) {
        this.quantity++
      }
    },
    confirmAction() {
      const cart = uni.getStorageSync('cart') || []
      const existIndex = cart.findIndex(p => p.id === this.product.id)
      if (existIndex > -1) {
        cart[existIndex].quantity += this.quantity
      } else {
        cart.push({ ...this.product, quantity: this.quantity })
      }
      uni.setStorageSync('cart', cart)
      this.showQuantity = false
      this.updateCartCount()
      
      if (this.actionType === 'cart') {
        uni.showToast({ title: '已加入购物车', icon: 'success' })
      } else {
        uni.switchTab({ url: '/pages/cart' })
      }
    },
    goToDetail(id) {
      if (id !== this.product.id) {
        uni.redirectTo({ url: `/pages/product-detail?id=${id}` })
      }
    }
  }
}
</script>

<style>
page {
  background: #f5f5f5;
}
</style>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.product-swiper {
  width: 100%;
  height: 750rpx;
  background: #fff;
}

.product-swiper image {
  width: 100%;
  height: 100%;
}

.product-info-card {
  background: #fff;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-main {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 28rpx;
  color: #FF5722;
  font-weight: bold;
}

.price-value {
  font-size: 48rpx;
  color: #FF5722;
  font-weight: bold;
}

.price-unit {
  font-size: 24rpx;
  color: #999;
  margin-left: 8rpx;
}

.sales-info {
  font-size: 24rpx;
  color: #999;
}

.product-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-top: 16rpx;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 20rpx;
}

.tag {
  background: #f5f5f5;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #666;
}

.info-section, .desc-section, .recommend-section {
  background: #fff;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx;
  background: #fafafa;
  border-radius: 12rpx;
}

.info-label {
  font-size: 26rpx;
  color: #999;
}

.info-value {
  font-size: 26rpx;
  color: #333;
}

.desc-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
}

.recommend-scroll {
  white-space: nowrap;
}

.recommend-list {
  display: inline-flex;
  gap: 20rpx;
}

.recommend-item {
  width: 200rpx;
  display: inline-block;
}

.recommend-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
}

.recommend-name {
  font-size: 26rpx;
  color: #333;
  display: block;
  margin-top: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-price {
  font-size: 28rpx;
  color: #FF5722;
  font-weight: bold;
  display: block;
  margin-top: 8rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.bar-left {
  display: flex;
}

.bar-item {
  width: 100rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.bar-icon {
  font-size: 40rpx;
}

.bar-text {
  font-size: 20rpx;
  color: #666;
  margin-top: 4rpx;
}

.cart-badge {
  position: absolute;
  top: -8rpx;
  right: 10rpx;
  background: #FF5722;
  color: #fff;
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 20rpx;
  min-width: 30rpx;
  text-align: center;
}

.bar-right {
  flex: 1;
  display: flex;
  margin-left: 20rpx;
}

.bar-btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
}

.add-cart {
  background: linear-gradient(135deg, #FF9800, #FFC107);
  color: #fff;
  margin-right: 16rpx;
}

.buy-now {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  color: #fff;
}

.quantity-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

.popup-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.popup-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

.popup-header {
  display: flex;
  align-items: center;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
  position: relative;
}

.popup-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
}

.popup-info {
  flex: 1;
  padding-left: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.popup-price {
  display: flex;
  align-items: baseline;
}

.popup-price .price-symbol {
  font-size: 28rpx;
}

.popup-price .price-value {
  font-size: 40rpx;
}

.popup-stock {
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
}

.popup-close {
  position: absolute;
  top: 0;
  right: 0;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #999;
}

.quantity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 0;
}

.quantity-label {
  font-size: 30rpx;
  color: #333;
}

.quantity-control {
  display: flex;
  align-items: center;
}

.control-btn {
  width: 64rpx;
  height: 64rpx;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  border-radius: 12rpx;
}

.control-btn.disabled {
  opacity: 0.5;
}

.control-num {
  width: 100rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
}

.popup-total {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 20rpx 0;
  border-top: 1rpx solid #f0f0f0;
}

.total-label {
  font-size: 28rpx;
  color: #666;
  margin-right: 16rpx;
}

.total-price {
  font-size: 40rpx;
  color: #FF5722;
  font-weight: bold;
}

.popup-btn {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  color: #fff;
  text-align: center;
  padding: 28rpx;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: bold;
  margin-top: 24rpx;
}

@media (min-width: 768px) {
  .product-swiper {
    height: 600rpx;
  }
  
  .product-info-card, .info-section, .desc-section, .recommend-section {
    max-width: 1200rpx;
    margin: 20rpx auto;
  }
}
</style>
