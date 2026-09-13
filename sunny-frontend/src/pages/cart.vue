<template>
  <view class="cart-page">
    <!-- #ifndef H5 -->
    <view class="custom-nav-bar" v-if="fromDetail">
      <view class="nav-back-btn" @click="goBack">
        <text class="nav-back-icon">＜</text>
      </view>
      <text class="nav-title">购物车</text>
      <view class="nav-placeholder"></view>
    </view>
    <!-- #endif -->

    <scroll-view 
      scroll-y 
      class="cart-scroll" 
      :show-scrollbar="true"
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      refresher-background="#f8f8f8"
      @refresherrefresh="onRefresh"
      @refresherrestore="onRestore"
    >
      <view class="cart-list" v-if="isLogin && cartList.length > 0">
        <view class="cart-item" :class="{ 'is-disabled': item.status === 0 }" v-for="(item, index) in cartList" :key="item.id || item.productId || index">
          <view class="item-check" @click="toggleSelect(index)">
            <view class="check-box" :class="{ checked: item.selected && item.status !== 0, disabled: item.status === 0 }">
              <text v-if="item.selected && item.status !== 0" class="check-mark">✓</text>
            </view>
          </view>
          <view class="item-image-wrapper" @click="goToDetail(item.productId)">
            <image
              class="item-image"
              :src="formatUrl(item.image)"
              mode="aspectFill"
              @error="handleImageError(index)"
            ></image>
            <view class="off-shelf-badge" v-if="item.status === 0">已下架</view>
          </view>
          <view class="item-info">
            <view class="item-title-row" @click="goToDetail(item.productId)">
              <text class="item-name" :class="{ 'text-disabled': item.status === 0 }">{{ item.name }}</text>
              <text class="off-shelf-tag" v-if="item.status === 0">已下架</text>
            </view>
            <view class="item-bottom">
              <text class="item-price" :class="{ 'text-disabled': item.status === 0 }">¥{{ item.price }}</text>
              <view class="quantity-control" v-if="item.status !== 0">
                <view class="control-btn" :class="{ disabled: item.quantity <= 1 }" @click="decreaseQuantity(index)">-</view>
                <text class="control-num">{{ item.quantity }}</text>
                <view class="control-btn" :class="{ disabled: item.stock && item.quantity >= item.stock }" @click="increaseQuantity(index)">+</view>
              </view>
              <view class="quantity-disabled" v-else>
                <text class="quantity-tip">已不可购买</text>
              </view>
            </view>
          </view>
          <view class="item-delete" @click="deleteItem(index)">
            <text class="delete-btn-text">删除</text>
          </view>
        </view>
      </view>

      <!-- 未登录状态 -->
      <view class="empty" v-else-if="!isLogin">
        <text class="empty-icon">🔒</text>
        <text class="empty-text">您还未登录，请先登录查看购物车</text>
        <view class="empty-btn" @click="goToLogin">去登录</view>
      </view>

      <!-- 登录后购物车为空 -->
      <view class="empty" v-else>
        <text class="empty-icon">🛒</text>
        <text class="empty-text">购物车是空的</text>
        <view class="empty-btn" @click="goShopping">去逛逛</view>
      </view>
    </scroll-view>

    <view class="bottom-bar" v-if="isLogin && cartList.length > 0">
      <view class="select-all" @click="toggleSelectAll">
        <view class="check-box" :class="{ checked: isAllSelected, disabled: availableItemCount === 0 }">
          <text v-if="isAllSelected" class="check-mark">✓</text>
        </view>
        <text class="select-all-text">全选</text>
      </view>
      <view class="total-info">
        <text class="total-label">合计:</text>
        <text class="total-price">¥{{ totalPrice }}</text>
      </view>
      <view class="submit-btn" :class="{ disabled: selectedCount === 0 }" @click="handleCheckout">
        <text>结算</text>
        <text v-if="selectedCount > 0" class="btn-count">({{ selectedCount }})</text>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../api/index'
import { formatImageUrl, setFileBaseServer } from '../utils/request'
import { isLoggedIn, updateTabBarCartBadge } from '../utils/auth'

export default {
  data() {
    return {
      isLogin: false,
      cartList: [],
      fromDetail: false,
      loadingOperation: false,
      isRefreshing: false,
      defaultImage: '/static/images/product-default.jpg'
    }
  },
  computed: {
    availableItems() {
      return this.cartList.filter(item => item.status !== 0)
    },
    availableItemCount() {
      return this.availableItems.length
    },
    isAllSelected() {
      return this.availableItems.length > 0 && this.availableItems.every(item => item.selected)
    },
    selectedCount() {
      return this.cartList
        .filter(item => item.selected && item.status !== 0)
        .reduce((sum, item) => sum + (item.quantity || 1), 0)
    },
    totalPrice() {
      const sum = this.cartList
        .filter(item => item.selected && item.status !== 0)
        .reduce((total, item) => {
          const price = parseFloat(item.price) || 0
          const quantity = parseInt(item.quantity, 10) || 1
          return total + price * quantity
        }, 0)
      return sum.toFixed(2)
    }
  },
  onLoad(options) {
    if (options && (options.from === 'detail' || options.fromDetail)) {
      this.fromDetail = true
    }
  },
  async onShow() {
    const isFromDetail = uni.getStorageSync('cart_from_detail')
    if (isFromDetail) {
      this.fromDetail = true
      uni.removeStorageSync('cart_from_detail')
    }
    this.updateNavBackButton(this.fromDetail)
    await this.initFileConfig()
    await this.loadCart()
    updateTabBarCartBadge()
  },
  onHide() {
    this.fromDetail = false
    this.updateNavBackButton(false)
  },
  beforeUnmount() {
    this.updateNavBackButton(false)
  },
  onUnload() {
    this.updateNavBackButton(false)
  },
  async onPullDownRefresh() {
    try {
      await this.loadCart()
      updateTabBarCartBadge()
    } finally {
      uni.stopPullDownRefresh()
    }
  },
  methods: {
    async onRefresh() {
      if (this.isRefreshing) return
      this.isRefreshing = true
      try {
        await this.loadCart()
        updateTabBarCartBadge()
      } catch (e) {
        console.error('刷新购物车失败', e)
      } finally {
        setTimeout(() => {
          this.isRefreshing = false
        }, 300)
      }
    },
    onRestore() {
      this.isRefreshing = false
    },
    async initFileConfig() {
      try {
        const cfg = await api.getFileConfig()
        if (cfg && cfg.baseServer) {
          setFileBaseServer(cfg.baseServer)
        }
      } catch (e) {
        // 使用默认配置
      }
    },
    formatUrl(path) {
      if (!path) return this.defaultImage
      return formatImageUrl(path) || this.defaultImage
    },
    handleImageError(index) {
      if (this.cartList && this.cartList[index]) {
        this.cartList[index].image = this.defaultImage
      }
    },
    updateNavBackButton(show) {
      // #ifdef H5
      this.$nextTick(() => {
        const headHd = document.querySelector('uni-page-head .uni-page-head-hd')
        if (!headHd) return

        const existing = headHd.querySelector('.custom-nav-back-btn')
        if (existing) {
          existing.remove()
        }

        if (show) {
          const backBtn = document.createElement('div')
          backBtn.className = 'uni-page-head-btn custom-nav-back-btn'
          backBtn.setAttribute('title', '返回')
          backBtn.style.cssText = 'display: flex; align-items: center; justify-content: center; width: 44px; height: 100%; cursor: pointer; -webkit-tap-highlight-color: transparent;'
          backBtn.innerHTML = `
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="display: block;">
              <path d="M15 19L8 12L15 5" stroke="#333333" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          `
          backBtn.onclick = (e) => {
            e.preventDefault()
            e.stopPropagation()
            this.goBack()
          }
          headHd.appendChild(backBtn)
        }
      })
      // #endif
    },
    goBack() {
      this.fromDetail = false
      this.updateNavBackButton(false)
      const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
      if (pages && pages.length > 1) {
        uni.navigateBack()
      } else {
        const lastProductId = uni.getStorageSync('last_viewed_product_id')
        if (lastProductId) {
          uni.navigateTo({
            url: `/pages/product-detail?id=${lastProductId}`,
            fail: () => {
              uni.switchTab({ url: '/pages/index' })
            }
          })
        } else {
          uni.switchTab({ url: '/pages/index' })
        }
      }
    },
    async loadCart() {
      this.isLogin = isLoggedIn()
      if (!this.isLogin) {
        this.cartList = []
        return
      }

      try {
        const data = await api.getCartList()
        const currentSelectedMap = {}
        this.cartList.forEach(item => {
          if (item.productId != null) {
            currentSelectedMap[item.productId] = item.selected
          }
        })

        this.cartList = (data || []).map(item => {
          const isOffShelf = item.status === 0
          const prevSelected = currentSelectedMap[item.productId]
          return {
            ...item,
            selected: isOffShelf ? false : (prevSelected !== undefined ? prevSelected : true)
          }
        })
      } catch (e) {
        console.error('加载购物车失败', e)
      }
    },
    toggleSelect(index) {
      const item = this.cartList[index]
      if (item.status === 0) {
        uni.showToast({ title: '已下架的物品不可选中', icon: 'none' })
        return
      }
      item.selected = !item.selected
    },
    toggleSelectAll() {
      if (this.availableItemCount === 0) {
        uni.showToast({ title: '暂无可选商品', icon: 'none' })
        return
      }
      const newState = !this.isAllSelected
      this.cartList.forEach(item => {
        if (item.status === 0) {
          item.selected = false
        } else {
          item.selected = newState
        }
      })
    },
    async decreaseQuantity(index) {
      if (this.loadingOperation) return
      const item = this.cartList[index]
      if (!item || item.status === 0) return

      if (item.quantity <= 1) {
        uni.showToast({ title: '商品数量不能少于1', icon: 'none' })
        return
      }

      const targetQuantity = item.quantity - 1
      try {
        this.loadingOperation = true
        await api.updateCartQuantity({
          productId: item.productId,
          quantity: targetQuantity
        })
        // 接口成功之后前端页面才实时变化
        item.quantity = targetQuantity
        updateTabBarCartBadge()
      } catch (e) {
        console.error(e)
      } finally {
        this.loadingOperation = false
      }
    },
    async increaseQuantity(index) {
      if (this.loadingOperation) return
      const item = this.cartList[index]
      if (!item || item.status === 0) return

      if (item.stock != null && item.quantity >= item.stock) {
        uni.showToast({ title: '库存不足', icon: 'none' })
        return
      }

      const targetQuantity = item.quantity + 1
      try {
        this.loadingOperation = true
        await api.updateCartQuantity({
          productId: item.productId,
          quantity: targetQuantity
        })
        // 接口成功之后前端页面才实时变化
        item.quantity = targetQuantity
        updateTabBarCartBadge()
      } catch (e) {
        console.error(e)
      } finally {
        this.loadingOperation = false
      }
    },
    deleteItem(index) {
      const item = this.cartList[index]
      if (!item) return

      uni.showModal({
        title: '提示',
        content: '确定要删除该商品吗?',
        success: async (res) => {
          if (res.confirm) {
            try {
              if (item.productId != null) {
                await api.deleteCartByProduct(item.productId)
              } else if (item.id != null) {
                await api.deleteCartItem(item.id)
              }
              this.cartList.splice(index, 1)
              updateTabBarCartBadge()
              uni.showToast({ title: '删除成功', icon: 'success' })
            } catch (e) {
              console.error(e)
            }
          }
        }
      })
    },
    goToDetail(productId) {
      if (productId) {
        uni.navigateTo({ url: `/pages/product-detail?id=${productId}` })
      }
    },
    goToLogin() {
      uni.navigateTo({ url: '/pages/login' })
    },
    goShopping() {
      uni.switchTab({ url: '/pages/index' })
    },
    handleCheckout() {
      if (this.selectedCount === 0) {
        uni.showToast({ title: '请先勾选要结算的商品', icon: 'none' })
        return
      }
      uni.showModal({
        title: '结算确认',
        content: `已勾选 ${this.selectedCount} 件商品，合计 ¥${this.totalPrice}，是否确认结算？`,
        confirmText: '去结算',
        cancelText: '再想想',
        success: (res) => {
          if (res.confirm) {
            uni.showToast({ title: '结算功能开发中', icon: 'none' })
          }
        }
      })
    }
  }
}
</script>

<style>
page,
uni-page,
uni-page-body {
  height: 100% !important;
  width: 100% !important;
  max-width: 100vw !important;
  overflow: hidden !important;
  background: #f8f8f8;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  touch-action: pan-y;
}

/* uni-page-wrapper 的高度必须交给 uni-app 框架控制：
   存在底部 tabbar 时框架会自动将其设为 calc(100% - tabbar高度)，
   若这里强制 height:100%，页面底部就会被固定定位的 uni-tabbar 遮挡 */
uni-page-wrapper {
  width: 100% !important;
  max-width: 100vw !important;
  overflow: hidden !important;
  box-sizing: border-box;
}

/* 内部轻量美化滚动条样式 */
.cart-scroll,
.cart-scroll .uni-scroll-view {
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.18) transparent;
}

.cart-scroll::-webkit-scrollbar,
.cart-scroll .uni-scroll-view::-webkit-scrollbar {
  display: block !important;
  width: 5px !important;
  height: 5px !important;
  background: transparent !important;
}

.cart-scroll::-webkit-scrollbar-thumb,
.cart-scroll .uni-scroll-view::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.18) !important;
  border-radius: 6px !important;
}

.cart-scroll::-webkit-scrollbar-thumb:hover,
.cart-scroll .uni-scroll-view::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.32) !important;
}

.cart-scroll::-webkit-scrollbar-track,
.cart-scroll .uni-scroll-view::-webkit-scrollbar-track {
  background: transparent !important;
}

.cart-scroll .uni-scroll-view,
.cart-scroll .uni-scroll-view-wrap,
.cart-scroll .uni-scroll-view-content {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box !important;
  overflow-x: hidden !important;
  touch-action: pan-y;
  -webkit-overflow-scrolling: touch;
}
</style>

<style scoped>
.custom-nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  background: #ffffff;
  padding: 0 20rpx;
  border-bottom: 1rpx solid #eeeeee;
  position: sticky;
  top: 0;
  z-index: 99;
}

.nav-back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back-icon {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.nav-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.nav-placeholder {
  width: 60rpx;
}

.cart-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  max-width: 100vw;
  background: #f8f8f8;
  overflow: hidden;
  box-sizing: border-box;
}

.cart-scroll {
  flex: 1;
  min-height: 0;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

.cart-list {
  padding: 20rpx;
  box-sizing: border-box;
}

.cart-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
}

.cart-item.is-disabled {
  background: #fbfbfb;
}

.item-check {
  padding-right: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 复选框样式 */
.check-box {
  width: 42rpx;
  height: 42rpx;
  border: 2rpx solid #d9d9d9;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  box-sizing: border-box;
  transition: all 0.2s ease-in-out;
}

.check-box.checked {
  background-color: #4CAF50;
  border-color: #4CAF50;
}

.check-mark {
  color: #ffffff;
  font-size: 26rpx;
  font-weight: bold;
  line-height: 1;
}

.check-box.disabled {
  background-color: #f5f5f5;
  border-color: #e0e0e0;
  cursor: not-allowed;
}

.item-image-wrapper {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
  cursor: pointer;
}

.item-image {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
  background: #f5f5f5;
}

.off-shelf-badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.65);
  color: #ffffff;
  font-size: 20rpx;
  text-align: center;
  line-height: 36rpx;
}

.item-info {
  flex: 1;
  padding: 0 16rpx;
  min-width: 0;
}

.item-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  cursor: pointer;
}

.item-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.off-shelf-tag {
  font-size: 20rpx;
  color: #ff4d4f;
  background: #fff1f0;
  border: 1rpx solid #ffccc7;
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
  margin-left: 8rpx;
  flex-shrink: 0;
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

.text-disabled {
  color: #999999 !important;
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
  color: #333;
  cursor: pointer;
}

.control-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.control-num {
  width: 60rpx;
  text-align: center;
  font-size: 28rpx;
}

.quantity-disabled {
  display: flex;
  align-items: center;
}

.quantity-tip {
  font-size: 22rpx;
  color: #999999;
}

.item-delete {
  padding: 16rpx 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
}

.delete-btn-text {
  color: #ff4d4f;
  font-size: 26rpx;
  font-weight: 500;
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
  flex-shrink: 0;
  width: 100%;
  height: 108rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
  box-sizing: border-box;
}

.select-all {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: #333333;
  cursor: pointer;
  padding: 10rpx 0;
  user-select: none;
}

.select-all .check-box {
  margin-right: 12rpx;
}

.select-all-text {
  font-size: 28rpx;
  color: #333333;
  font-weight: 500;
}

.total-info {
  flex: 1;
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  padding: 0 20rpx;
}

.total-label {
  font-size: 26rpx;
  color: #666666;
  margin-right: 6rpx;
}

.total-price {
  font-size: 38rpx;
  color: #ff5722;
  font-weight: bold;
  line-height: 1;
}

.submit-btn {
  background: linear-gradient(135deg, #5cba60, #4CAF50);
  color: #ffffff;
  height: 76rpx;
  min-width: 180rpx;
  padding: 0 36rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
  transition: all 0.2s ease;
  white-space: nowrap;
  box-sizing: border-box;
}

.submit-btn:active {
  opacity: 0.9;
  transform: scale(0.98);
}

.submit-btn.disabled {
  background: #e0e0e0;
  color: #9e9e9e;
  box-shadow: none;
  cursor: not-allowed;
}

.submit-btn.disabled:active {
  transform: none;
}

.btn-count {
  margin-left: 4rpx;
  font-size: 26rpx;
}
</style>
