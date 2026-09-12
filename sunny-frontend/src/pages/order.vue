<template>
  <view class="order-page">
    <view class="order-tabs">
      <view class="tab-item" :class="{ active: activeStatus === -1 }" @click="changeStatus(-1)">全部</view>
      <view class="tab-item" :class="{ active: activeStatus === 0 }" @click="changeStatus(0)">待付款</view>
      <view class="tab-item" :class="{ active: activeStatus === 1 }" @click="changeStatus(1)">待发货</view>
      <view class="tab-item" :class="{ active: activeStatus === 2 }" @click="changeStatus(2)">待收货</view>
    </view>

    <view class="order-list" v-if="orderList.length > 0">
      <view class="order-item" v-for="item in orderList" :key="item.id">
        <view class="order-header">
          <text class="order-no">订单号: {{ item.orderNo }}</text>
          <text class="order-status">{{ getStatusText(item.status) }}</text>
        </view>
        <view class="order-products">
          <view class="product-item" v-for="(prod, idx) in item.items" :key="idx">
            <image class="product-image" :src="prod.productImage || '/static/images/default.png'" mode="aspectFill"></image>
            <view class="product-info">
              <text class="product-name">{{ prod.productName }}</text>
              <view class="product-bottom">
                <text class="product-price">¥{{ prod.price }}</text>
                <text class="product-quantity">x{{ prod.quantity }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="order-footer">
          <text class="order-total">共{{ getTotalQuantity(item.items) }}件 合计: ¥{{ item.totalAmount }}</text>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无订单</text>
    </view>
  </view>
</template>

<script>
import api from '../api/index'

export default {
  data() {
    return {
      orderList: [],
      activeStatus: -1
    }
  },
  onLoad() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      try {
        const data = await api.getOrderList({ pageNum: 1, pageSize: 20 })
        this.orderList = (data && data.records) || []
      } catch (e) {
        console.error(e)
      }
    },
    changeStatus(status) {
      this.activeStatus = status
    },
    getStatusText(status) {
      const statusMap = {
        0: '待付款',
        1: '待发货',
        2: '待收货',
        3: '已完成',
        4: '已取消'
      }
      return statusMap[status] || '未知'
    },
    getTotalQuantity(items) {
      if (!items) return 0
      return items.reduce((sum, item) => sum + item.quantity, 0)
    }
  }
}
</script>

<style scoped>
.order-page {
  background: #f8f8f8;
  min-height: 100vh;
}

.order-tabs {
  display: flex;
  background: #fff;
  padding: 20rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  font-size: 28rpx;
  color: #666;
  padding: 16rpx 0;
}

.tab-item.active {
  color: #4CAF50;
  font-weight: bold;
  border-bottom: 4rpx solid #4CAF50;
}

.order-list {
  padding: 20rpx;
}

.order-item {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.order-no {
  font-size: 24rpx;
  color: #999;
}

.order-status {
  font-size: 24rpx;
  color: #4CAF50;
}

.order-products {
  padding: 20rpx 24rpx;
}

.product-item {
  display: flex;
  margin-bottom: 16rpx;
}

.product-item:last-child {
  margin-bottom: 0;
}

.product-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  background: #f5f5f5;
}

.product-info {
  flex: 1;
  padding-left: 16rpx;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  display: block;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  margin-top: 16rpx;
}

.product-price {
  font-size: 28rpx;
  color: #ff5722;
}

.product-quantity {
  font-size: 24rpx;
  color: #999;
}

.order-footer {
  padding: 20rpx 24rpx;
  border-top: 1rpx solid #f0f0f0;
  text-align: right;
}

.order-total {
  font-size: 28rpx;
  color: #333;
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
}
</style>
