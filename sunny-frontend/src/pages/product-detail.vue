<template>
  <view class="product-detail-page" v-if="product">
    <view class="swiper-container">
      <swiper
        class="product-swiper"
        indicator-dots
        indicator-color="rgba(255,255,255,0.5)"
        indicator-active-color="#fff"
        autoplay
        circular
      >
        <swiper-item v-for="(imgUrl, index) in displayImageList" :key="index">
          <image
            class="swiper-image"
            :src="imgUrl"
            mode="aspectFill"
            @click="previewImage(imgUrl)"
            @error="handleImageError(index)"
          ></image>
        </swiper-item>
        <swiper-item v-if="displayImageList.length === 0">
          <image class="swiper-image" :src="defaultImage" mode="aspectFill"></image>
        </swiper-item>
      </swiper>
      <view class="off-shelf-banner-mask" v-if="product.status === 0">
        <text class="off-shelf-banner-text">商品已下架</text>
      </view>
    </view>

    <view class="product-info-card">
      <view class="price-row">
        <view class="price-main">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ product.price }}</text>
          <text class="price-unit">/{{ product.unit }}</text>
        </view>
        <view class="sales-stock-info">
          <text class="stat-item">已售 {{ product.sales || 0 }}</text>
          <text class="stat-item">库存 {{ product.stock != null ? product.stock : 999 }}</text>
        </view>
      </view>
      <text class="product-name">{{ product.name }}</text>
      <!-- 商品标签区域：完全由接口返回的标签数据渲染，无标签时不显示 -->
      <view class="product-tags" v-if="product.tagList && product.tagList.length > 0">
        <view
          class="tag-badge"
          :class="{ 'tag-badge-hot': tag.isHotselling === 1 }"
          v-for="tag in product.tagList"
          :key="tag.id"
        >
          <image v-if="tag.image" class="tag-badge-icon" :src="formatUrl(tag.image)" mode="aspectFit"></image>
          <text class="tag-badge-name" :class="{ 'tag-name-hot': tag.isHotselling === 1 }">{{ tag.name }}</text>
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
          <text class="info-label">产地</text>
          <text class="info-value">{{ product.origin || '大山村' }}</text>
        </view>
      </view>
    </view>

    <view class="desc-section">
      <view class="section-title">商品描述</view>
      <view class="desc-content">
        <text>{{ product.description || '农家好货，品质保证。来自大山深处的天然农产品，新鲜采摘，精心挑选，确保每一份都是优质好货。' }}</text>
      </view>
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
      <view class="bar-right" v-if="product.status !== 0">
        <view class="bar-btn add-cart" @click="addToCart">
          <text>加入购物车</text>
        </view>
        <view class="bar-btn buy-now" @click="buyNow">
          <text>立即购买</text>
        </view>
      </view>
      <view class="bar-right-off" v-else>
        <view class="bar-btn-off">
          <text>该商品已下架</text>
        </view>
      </view>
    </view>

    <view class="quantity-popup" v-if="showQuantity">
      <view class="popup-mask" @click="showQuantity = false"></view>
      <view class="popup-content">
        <view class="popup-header">
          <image class="popup-image" :src="formatUrl(product.image) || defaultImage" mode="aspectFill"></image>
          <view class="popup-info">
            <view class="popup-price">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ product.price }}</text>
            </view>
            <text class="popup-stock">库存: {{ product.stock || 999 }}</text>
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
import { formatImageUrl, setFileBaseServer } from '../utils/request'
import { checkLogin, isLoggedIn, updateTabBarCartBadge } from '../utils/auth'

export default {
  data() {
    return {
      product: null,
      imageList: [],
      displayImageList: [],
      quantity: 1,
      showQuantity: false,
      actionType: 'cart',
      cartCount: 0,
      defaultImage: '/static/images/product-default.jpg'
    }
  },
  async onLoad(options) {
    await this.initFileConfig()
    if (options.id) {
      this.loadDetail(options.id)
    }
    this.updateCartCount()
  },
  onShow() {
    this.updateCartCount()
  },
  methods: {
    async initFileConfig() {
      try {
        const cfg = await api.getFileConfig()
        if (cfg && cfg.baseServer) {
          setFileBaseServer(cfg.baseServer)
        }
      } catch (e) {
        // 使用默认文件服务配置
      }
    },
    formatUrl(path) {
      if (!path) return ''
      return formatImageUrl(path)
    },
    async loadDetail(id) {
      try {
        uni.setStorageSync('last_viewed_product_id', id)
        const data = await api.getProductDetail(id)
        this.product = data
        let rawImages = []
        if (data && data.image && typeof data.image === 'string' && data.image.trim()) {
          rawImages.push(data.image.trim())
        }
        if (data && data.images) {
          let extraImages = []
          try {
            if (Array.isArray(data.images)) {
              extraImages = data.images
            } else if (typeof data.images === 'string') {
              const str = data.images.trim()
              if (str.startsWith('[')) {
                const parsed = JSON.parse(str)
                extraImages = Array.isArray(parsed) ? parsed : []
              } else {
                extraImages = str.split(',').map(s => s.trim()).filter(Boolean)
              }
            }
          } catch (e) {
            if (typeof data.images === 'string') {
              extraImages = data.images.split(',').map(s => s.trim()).filter(Boolean)
            }
          }
          extraImages.forEach(img => {
            if (typeof img === 'string') {
              const item = img.trim()
              if (item && !rawImages.includes(item)) {
                rawImages.push(item)
              }
            }
          })
        }
        this.imageList = rawImages
        const formatted = rawImages.map(img => this.formatUrl(img)).filter(Boolean)
        this.displayImageList = Array.from(new Set(formatted))
      } catch (e) {
        console.error(e)
      }
    },
    handleImageError(index) {
      if (this.displayImageList && this.displayImageList[index]) {
        this.displayImageList[index] = this.defaultImage
      }
    },
    async updateCartCount() {
      if (!isLoggedIn()) {
        this.cartCount = 0
        updateTabBarCartBadge()
        return
      }
      try {
        const count = await api.getCartCount()
        this.cartCount = count || 0
        updateTabBarCartBadge()
      } catch (e) {
        this.cartCount = 0
      }
    },
    previewImage(url) {
      const urls = this.displayImageList.length > 0 ? this.displayImageList : [url]
      uni.previewImage({
        current: url,
        urls: urls
      })
    },
    goBack() {
      uni.navigateBack()
    },
    goHome() {
      uni.switchTab({ url: '/pages/index' })
    },
    goCart() {
      uni.setStorageSync('cart_from_detail', true)
      uni.switchTab({
        url: '/pages/cart',
        fail: () => {
          uni.navigateTo({
            url: '/pages/cart?from=detail',
            fail: () => {
              uni.reLaunch({ url: '/pages/cart' })
            }
          })
        }
      })
    },
    shareProduct() {
      uni.showToast({ title: '分享功能开发中', icon: 'none' })
    },
    addToCart() {
      if (!checkLogin()) return
      this.actionType = 'cart'
      this.quantity = 1
      this.showQuantity = true
    },
    buyNow() {
      if (!checkLogin()) return
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
    async confirmAction() {
      if (!checkLogin()) return
      try {
        await api.addToCart({
          productId: this.product.id,
          quantity: this.quantity
        })
        this.showQuantity = false
        await this.updateCartCount()

        if (this.actionType === 'cart') {
          uni.showToast({ title: '已加入购物车', icon: 'success' })
        } else {
          this.goCart()
        }
      } catch (e) {
        console.error(e)
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

.swiper-container {
  position: relative;
  width: 100%;
  height: 750rpx;
  overflow: hidden;
}

.product-swiper {
  width: 100%;
  height: 100%;
  background: #fff;
}

.off-shelf-banner-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  pointer-events: none;
}

.off-shelf-banner-text {
  color: #fff;
  font-size: 38rpx;
  font-weight: bold;
  letter-spacing: 6rpx;
  padding: 16rpx 48rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 40rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
}

.product-swiper image,
.swiper-image {
  width: 100%;
  height: 100%;
  display: block;
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

.sales-stock-info,
.sales-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6rpx;
}

.stat-item {
  font-size: 24rpx;
  color: #999;
  line-height: 1.3;
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

.tag-badge {
  display: inline-flex;
  align-items: center;
  background: #f6ffed;
  border: 1rpx solid #b7eb8f;
  padding: 6rpx 16rpx;
  border-radius: 24rpx;
}

.tag-badge.tag-badge-hot {
  background: #fff2e8;
  border-color: #ffbb96;
}

.tag-badge-icon {
  width: 28rpx;
  height: 28rpx;
  margin-right: 8rpx;
  border-radius: 4rpx;
  flex-shrink: 0;
}

.tag-badge-name {
  font-size: 24rpx;
  color: #389e0d;
  font-weight: 500;
  line-height: 1.2;
}

.tag-badge-name.tag-name-hot {
  color: #fa541c;
  font-weight: 600;
}

.tag {
  background: #f5f5f5;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #666;
}

.info-section, .desc-section {
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
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 18rpx 20rpx;
  background: #fafafa;
  border-radius: 12rpx;
}

.info-label {
  width: 140rpx;
  flex-shrink: 0;
  font-size: 26rpx;
  color: #999;
}

.info-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
  word-break: break-all;
}

.desc-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
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

.bar-right-off {
  flex: 1;
  display: flex;
  margin-left: 20rpx;
}

.bar-btn-off {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
  background: #e0e0e0;
  color: #999;
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
  
  .product-info-card, .info-section, .desc-section {
    max-width: 1200rpx;
    margin: 20rpx auto;
  }
}
</style>
