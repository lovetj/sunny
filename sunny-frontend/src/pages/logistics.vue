<template>
  <view class="logistics-list-page">
    <view class="logistics-tabs">
      <view class="tab-item" :class="{ active: activeType === 0 }" @click="changeType(0)">全部</view>
      <view class="tab-item" :class="{ active: activeType === 1 }" @click="changeType(1)">进城</view>
      <view class="tab-item" :class="{ active: activeType === 2 }" @click="changeType(2)">下乡</view>
    </view>

    <view class="logistics-list">
      <view class="logistics-item" v-for="item in filterList" :key="item.id">
        <view class="item-header">
          <view class="item-type" :class="'type-' + item.type">
            {{ item.type === 1 ? '进城' : '下乡' }}
          </view>
          <text class="item-time">{{ formatDate(item.createTime) }}</text>
        </view>
        <text class="item-title">{{ item.title }}</text>
        <text class="item-content">{{ item.content }}</text>
        <image v-if="item.image" class="item-image" :src="item.image" mode="aspectFill"></image>
      </view>
    </view>

    <view class="empty" v-if="filterList.length === 0">
      <text>暂无物流信息</text>
    </view>
  </view>
</template>

<script>
import api from '../api/index'

export default {
  data() {
    return {
      logisticsList: [],
      activeType: 0
    }
  },
  computed: {
    filterList() {
      if (this.activeType === 0) {
        return this.logisticsList
      }
      return this.logisticsList.filter(item => item.type === this.activeType)
    }
  },
  onLoad() {
    this.loadLogistics()
  },
  methods: {
    async loadLogistics() {
      try {
        const data = await api.getLogisticsList()
        this.logisticsList = data || []
      } catch (e) {
        console.error(e)
      }
    },
    changeType(type) {
      this.activeType = type
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      return dateStr.substring(0, 10)
    }
  }
}
</script>

<style scoped>
.logistics-list-page {
  background: #f8f8f8;
  min-height: 100vh;
}

.logistics-tabs {
  display: flex;
  background: #fff;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  font-size: 28rpx;
  color: #666;
  padding: 16rpx 0;
  border-radius: 8rpx;
}

.tab-item.active {
  background: #4CAF50;
  color: #fff;
}

.logistics-list {
  padding: 0 20rpx;
}

.logistics-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.item-type {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.type-1 {
  background: #e3f2fd;
  color: #1976d2;
}

.type-2 {
  background: #fff3e0;
  color: #f57c00;
}

.item-time {
  font-size: 24rpx;
  color: #999;
}

.item-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}

.item-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  display: block;
}

.item-image {
  width: 100%;
  height: 300rpx;
  margin-top: 16rpx;
  border-radius: 12rpx;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}
</style>
