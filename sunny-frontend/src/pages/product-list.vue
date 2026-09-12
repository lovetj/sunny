<template>
  <view class="product-list-page">
    <view class="filter-bar">
      <view class="filter-item" :class="{ active: sortType === 'default' }" @click="changeSort('default')">
        <text>综合</text>
      </view>
      <view class="filter-item" :class="{ active: sortType === 'sales' }" @click="changeSort('sales')">
        <text>销量</text>
        <text class="filter-arrow" v-if="sortType === 'sales'">↓</text>
      </view>
      <view class="filter-item" :class="{ active: sortType === 'price' }" @click="changeSort('price')">
        <text>价格</text>
        <text class="filter-arrow" v-if="sortType === 'price'">↑</text>
      </view>
    </view>

    <view class="product-grid" v-if="productList.length > 0">
      <view class="product-card" v-for="item in productList" :key="item.id" @click="goToDetail(item.id)">
        <view class="product-image-wrap">
          <image class="product-image" :src="item.image || defaultImage" mode="aspectFill"></image>
          <view class="product-tag hot" v-if="item.sales > 50">热卖</view>
          <view class="product-tag new" v-else-if="item.sales < 10">新品</view>
        </view>
        <view class="product-content">
          <text class="product-name">{{ item.name }}</text>
          <text class="product-desc">{{ item.description || '农家好货，品质保证' }}</text>
          <view class="product-meta">
            <view class="product-price">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ item.price }}</text>
              <text class="price-unit">/{{ item.unit }}</text>
            </view>
            <text class="product-sales">已售{{ item.sales || 0 }}</text>
          </view>
          <view class="product-origin" v-if="item.origin">
            <text class="origin-icon">📍</text>
            <text>{{ item.origin }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <view class="empty-icon">📦</view>
      <text class="empty-text">暂无商品</text>
      <view class="empty-btn" @click="goBack">返回首页</view>
    </view>
  </view>
</template>

<script>
import api from '../api/index'

export default {
  data() {
    return {
      productList: [],
      categoryId: null,
      categoryName: '',
      sortType: 'default',
      defaultImage: '/static/images/product-default.jpg'
    }
  },
  onLoad(options) {
    if (options.categoryId) {
      this.categoryId = parseInt(options.categoryId)
    }
    if (options.name) {
      this.categoryName = decodeURIComponent(options.name)
      uni.setNavigationBarTitle({
        title: this.categoryName
      })
    }
    this.loadProduct()
  },
  methods: {
    async loadProduct() {
      try {
        let data
        if (this.categoryId) {
          data = await api.getProductByCategory(this.categoryId)
        } else {
          data = await api.getProductList()
        }
        this.productList = data || []
      } catch (e) {
        console.error(e)
      }
    },
    changeSort(type) {
      this.sortType = type
      if (type === 'sales') {
        this.productList.sort((a, b) => (b.sales || 0) - (a.sales || 0))
      } else if (type === 'price') {
        this.productList.sort((a, b) => a.price - b.price)
      } else {
        this.loadProduct()
      }
    },
    goToDetail(id) {
      uni.navigateTo({ url: `/pages/product-detail?id=${id}` })
    },
    goBack() {
      uni.navigateBack()
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
.product-list-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}

.filter-bar {
  display: flex;
  background: #fff;
  padding: 20rpx 0;
  position: sticky;
  top: 0;
  z-index: 99;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.filter-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #666;
  padding: 16rpx 0;
  position: relative;
}

.filter-item.active {
  color: #4CAF50;
  font-weight: bold;
}

.filter-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 4rpx;
  background: #4CAF50;
  border-radius: 2rpx;
}

.filter-arrow {
  font-size: 20rpx;
  margin-left: 4rpx;
}

.product-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx;
  gap: 20rpx;
}

.product-card {
  width: calc(50% - 10rpx);
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.product-image-wrap {
  position: relative;
  width: 100%;
  height: 340rpx;
}

.product-image {
  width: 100%;
  height: 100%;
}

.product-tag {
  position: absolute;
  top: 16rpx;
  left: 16rpx;
  font-size: 20rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  color: #fff;
}

.product-tag.hot {
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
}

.product-tag.new {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
}

.product-content {
  padding: 20rpx;
}

.product-name {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.product-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24rpx;
  color: #FF5722;
  font-weight: bold;
}

.price-value {
  font-size: 36rpx;
  color: #FF5722;
  font-weight: bold;
}

.price-unit {
  font-size: 22rpx;
  color: #999;
  margin-left: 4rpx;
}

.product-sales {
  font-size: 22rpx;
  color: #999;
}

.product-origin {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid #f5f5f5;
}

.origin-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.product-origin text {
  font-size: 22rpx;
  color: #999;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 32rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  color: #fff;
  padding: 20rpx 60rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
}

@media (min-width: 768px) {
  .product-card {
    width: calc(25% - 15rpx);
  }
  
  .product-grid {
    max-width: 1400rpx;
    margin: 0 auto;
  }
}
</style>
