<template>
  <view class="address-page">
    <scroll-view scroll-y class="address-scroll">
      <view class="address-list" v-if="addressList.length > 0">
        <view 
          class="address-item" 
          v-for="item in addressList" 
          :key="item.id"
          @click="handleSelectAddress(item)"
        >
          <view class="item-main">
            <view class="item-header">
              <text class="receiver-name">{{ item.receiverName }}</text>
              <text class="receiver-phone">{{ item.phone }}</text>
              <text class="type-tag" :class="'tag-' + item.addressType" v-if="item.addressType">
                {{ item.addressType }}
              </text>
              <text class="default-badge" v-if="item.isDefault === 1">默认</text>
            </view>
            <view class="item-detail">
              <text class="address-text">{{ formatFullAddress(item) }}</text>
            </view>
          </view>

          <view class="item-actions">
            <view class="set-default" @click.stop="handleSetDefault(item)">
              <text class="radio-icon" :class="{ checked: item.isDefault === 1 }">
                {{ item.isDefault === 1 ? '●' : '○' }}
              </text>
              <text class="radio-label">{{ item.isDefault === 1 ? '默认地址' : '设为默认' }}</text>
            </view>
            <view class="action-btns">
              <view class="action-btn" @click.stop="handleEdit(item)">
                <text class="btn-text">编辑</text>
              </view>
              <view class="action-btn delete" @click.stop="handleDelete(item)">
                <text class="btn-text">删除</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-container" v-else-if="!loading">
        <text class="empty-icon">📍</text>
        <text class="empty-text">暂无收货地址</text>
        <text class="empty-subtext">添加收货地址，下单更便捷</text>
      </view>
    </scroll-view>

    <!-- 底部新增按钮 -->
    <view class="bottom-bar">
      <button class="add-btn" @click="handleAddAddress">
        <text class="add-plus">+</text>
        <text>新增收货地址</text>
      </button>
    </view>
  </view>
</template>

<script>
import api from '../../api/index'
import { isLoggedIn } from '../../utils/auth'

export default {
  data() {
    return {
      addressList: [],
      loading: false,
      isSelectMode: false
    }
  },
  onLoad(options) {
    if (options && options.select) {
      this.isSelectMode = true
    }
  },
  onShow() {
    if (!isLoggedIn()) {
      uni.showModal({
        title: '提示',
        content: '请先登录后再管理收货地址',
        showCancel: false,
        success: () => {
          uni.navigateTo({ url: '/pages/login' })
        }
      })
      return
    }
    this.loadAddressList()
  },
  methods: {
    formatFullAddress(item) {
      if (!item) return ''
      const detail = item.detailAddress || ''
      const house = item.houseNumber ? item.houseNumber.trim() : ''
      return house ? `${detail} ${house}` : detail
    },
    async loadAddressList() {
      this.loading = true
      try {
        const res = await api.getAddressList()
        this.addressList = res || []
      } catch (e) {
        console.error('获取收货地址失败', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    handleAddAddress() {
      uni.navigateTo({
        url: '/pages/address/edit'
      })
    },
    handleEdit(item) {
      uni.navigateTo({
        url: `/pages/address/edit?id=${item.id}`
      })
    },
    async handleSetDefault(item) {
      if (item.isDefault === 1) return
      try {
        uni.showLoading({ title: '设置中...' })
        await api.setDefaultAddress(item.id)
        uni.showToast({ title: '设置成功', icon: 'success' })
        this.loadAddressList()
      } catch (e) {
        uni.showToast({ title: e.message || '设置失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    handleDelete(item) {
      uni.showModal({
        title: '提示',
        content: '确定要删除该收货地址吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({ title: '删除中...' })
              await api.deleteAddress(item.id)
              uni.showToast({ title: '删除成功', icon: 'success' })
              this.loadAddressList()
            } catch (e) {
              uni.showToast({ title: e.message || '删除失败', icon: 'none' })
            } finally {
              uni.hideLoading()
            }
          }
        }
      })
    },
    handleSelectAddress(item) {
      if (this.isSelectMode) {
        uni.$emit('selectAddress', item)
        uni.navigateBack()
      }
    }
  }
}
</script>

<style scoped>
.address-page {
  display: flex;
  flex-direction: column;
  height: 100%; /* 100vh 会比 uni-page-body 高（被 tabbar 挤压），导致页面级滚动；用 100% 服从父级 */
  background: #f8f8f8;
  box-sizing: border-box;
  overflow: hidden;
}

.address-scroll {
  flex: 1;
  min-height: 0; /* flex 子元素关键：允许缩小不被内容撑开 */
  padding: 20rpx;
  box-sizing: border-box;
  padding-bottom: 140rpx;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.address-item {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}

.item-main {
  border-bottom: 1rpx solid #f0f0f0;
  padding-bottom: 20rpx;
}

.item-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.receiver-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.receiver-phone {
  font-size: 28rpx;
  color: #666666;
}

.type-tag {
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
  background: #e8f5e9;
  color: #2e7d32;
}

.type-tag.tag-公司 {
  background: #e3f2fd;
  color: #1565c0;
}

.type-tag.tag-学校 {
  background: #fff3e0;
  color: #e65100;
}

.default-badge {
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
  background: #ffebee;
  color: #d32f2f;
  font-weight: 500;
}

.item-detail {
  font-size: 28rpx;
  color: #444444;
  line-height: 1.5;
}

.item-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 18rpx;
}

.set-default {
  display: flex;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
}

.radio-icon {
  font-size: 28rpx;
  color: #999;
}

.radio-icon.checked {
  color: #4CAF50;
}

.radio-label {
  font-size: 26rpx;
  color: #666;
}

.action-btns {
  display: flex;
  gap: 24rpx;
}

.action-btn {
  padding: 6rpx 12rpx;
  cursor: pointer;
}

.btn-text {
  font-size: 26rpx;
  color: #666666;
}

.action-btn.delete .btn-text {
  color: #ff4d4f;
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #333333;
  margin-bottom: 8rpx;
}

.empty-subtext {
  font-size: 24rpx;
  color: #999999;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}

.add-btn {
  background: #4CAF50;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 44rpx;
  height: 88rpx;
  line-height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  border: none;
}

.add-btn::after {
  border: none;
}

.add-plus {
  font-size: 36rpx;
  line-height: 1;
}
</style>

<!-- 非 scoped 样式：uni-app 框架元素（uni-scroll-view 等）无 data-v 属性，
     scoped 选择器无法匹配，必须用全局样式。通过 .address-page 前缀限定范围 -->
<!-- 注意：uni-app 编译后 scroll-view 生成的元素只有 .address-scroll class，
     没有 .uni-scroll-view class，选择器直接写 .address-scroll 即可 -->
<style>
/* 1. 让 uni-scroll-view 组件服从父容器的 flex 高度约束，并启用滚动 */
.address-page .address-scroll {
  height: 0 !important;        /* 配合 flex:1 + min-height:0 让组件不再撑开父容器 */
  min-height: 0 !important;
  overflow-y: auto !important; /* 强制启用垂直滚动 */
  overflow-x: hidden !important;
}

/* 2. 滚动条美化 - Webkit (Chrome / Safari / Edge) */
.address-page .address-scroll::-webkit-scrollbar {
  width: 6px;
}
.address-page .address-scroll::-webkit-scrollbar-track {
  background: transparent;
}
.address-page .address-scroll::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.18);
  border-radius: 3px;
}
.address-page .address-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.32);
}

/* 3. 滚动条美化 - Firefox */
.address-page .address-scroll {
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.18) transparent;
}
</style>
