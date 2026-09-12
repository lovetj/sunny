<template>
  <view class="cart-page">
    <view class="cart-list" v-if="cartList.length > 0">
      <view class="cart-item" v-for="(item, index) in cartList" :key="item.id">
        <view class="item-check" @click="toggleSelect(index)">
          <view class="check-box" :class="{ checked: item.selected }">✓</view>
        </view>
        <image class="item-image" :src="item.image || '/static/images/default.png'" mode="aspectFill"></image>
        <view class="item-info">
          <text class="item-name">{{ item.name }}</text>
          <view class="item-bottom">
            <text class="item-price">¥{{ item.price }}</text>
            <view class="quantity-control">
              <view class="control-btn" @click="decreaseQuantity(index)">-</view>
              <text class="control-num">{{ item.quantity }}</text>
              <view class="control-btn" @click="increaseQuantity(index)">+</view>
            </view>
          </view>
        </view>
        <view class="item-delete" @click="deleteItem(index)">
          <text>删除</text>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text class="empty-icon">🛒</text>
      <text class="empty-text">购物车是空的</text>
      <view class="empty-btn" @click="goShopping">去逛逛</view>
    </view>

    <view class="bottom-bar" v-if="cartList.length > 0">
      <view class="select-all" @click="toggleSelectAll">
        <view class="check-box" :class="{ checked: isAllSelected }">✓</view>
        <text>全选</text>
      </view>
      <view class="total-info">
        <text class="total-label">合计:</text>
        <text class="total-price">¥{{ totalPrice }}</text>
      </view>
      <view class="submit-btn" @click="submitOrder">结算({{ selectedCount }})</view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      cartList: []
    }
  },
  computed: {
    isAllSelected() {
      return this.cartList.length > 0 && this.cartList.every(item => item.selected)
    },
    selectedCount() {
      return this.cartList.filter(item => item.selected).reduce((sum, item) => sum + item.quantity, 0)
    },
    totalPrice() {
      return this.cartList.filter(item => item.selected).reduce((sum, item) => {
        return sum + item.price * item.quantity
      }, 0).toFixed(2)
    }
  },
  onShow() {
    this.loadCart()
  },
  methods: {
    loadCart() {
      const cart = uni.getStorageSync('cart') || []
      this.cartList = cart.map(item => ({ ...item, selected: item.selected !== false }))
    },
    saveCart() {
      uni.setStorageSync('cart', this.cartList)
    },
    toggleSelect(index) {
      this.cartList[index].selected = !this.cartList[index].selected
      this.saveCart()
    },
    toggleSelectAll() {
      const newState = !this.isAllSelected
      this.cartList.forEach(item => {
        item.selected = newState
      })
      this.saveCart()
    },
    decreaseQuantity(index) {
      if (this.cartList[index].quantity > 1) {
        this.cartList[index].quantity--
        this.saveCart()
      }
    },
    increaseQuantity(index) {
      this.cartList[index].quantity++
      this.saveCart()
    },
    deleteItem(index) {
      uni.showModal({
        title: '提示',
        content: '确定删除该商品吗?',
        success: (res) => {
          if (res.confirm) {
            this.cartList.splice(index, 1)
            this.saveCart()
          }
        }
      })
    },
    goShopping() {
      uni.switchTab({ url: '/pages/index' })
    },
    submitOrder() {
      const selectedItems = this.cartList.filter(item => item.selected)
      if (selectedItems.length === 0) {
        uni.showToast({ title: '请选择商品', icon: 'none' })
        return
      }
      uni.setStorageSync('orderItems', selectedItems)
      uni.navigateTo({ url: '/pages/order' })
    }
  }
}
</script>

<style scoped>
.cart-page {
  background: #f8f8f8;
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.cart-list {
  padding: 20rpx;
}

.cart-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.item-check {
  padding-right: 16rpx;
}

.check-box {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: transparent;
}

.check-box.checked {
  background: #4CAF50;
  border-color: #4CAF50;
  color: #fff;
}

.item-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
}

.item-info {
  flex: 1;
  padding: 0 16rpx;
}

.item-name {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.item-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-price {
  font-size: 32rpx;
  color: #ff5722;
  font-weight: bold;
}

.quantity-control {
  display: flex;
  align-items: center;
}

.control-btn {
  width: 50rpx;
  height: 50rpx;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  border-radius: 8rpx;
}

.control-num {
  width: 60rpx;
  text-align: center;
  font-size: 28rpx;
}

.item-delete {
  padding: 16rpx;
  color: #999;
  font-size: 24rpx;
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
  background: #4CAF50;
  color: #fff;
  padding: 16rpx 60rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
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
  padding: 0 20rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.select-all {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: #333;
}

.select-all .check-box {
  margin-right: 12rpx;
}

.total-info {
  flex: 1;
  text-align: right;
  padding-right: 20rpx;
}

.total-label {
  font-size: 28rpx;
  color: #333;
}

.total-price {
  font-size: 36rpx;
  color: #ff5722;
  font-weight: bold;
}

.submit-btn {
  background: #4CAF50;
  color: #fff;
  padding: 20rpx 40rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
}
</style>
