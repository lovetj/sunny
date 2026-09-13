<template>
  <view class="category-page">
    <view class="search-header">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          type="text"
          v-model="keyword"
          :focus="isSearchFocus"
          placeholder="请输入商品名称搜索..."
          confirm-type="search"
          @confirm="onSearchInputImmediate"
          @input="onSearchInput"
          @blur="onSearchBlur"
        />
        <text class="clear-icon" v-if="keyword" @click="clearSearch">✕</text>
      </view>
    </view>

    <view class="category-body">
      <scroll-view scroll-y class="category-tabs" :show-scrollbar="true">
        <view 
          class="tab-item" 
          :class="{ active: activeCategory === 0 }" 
          @click="selectCategory(0)">
          <view class="tab-icon">
            <image 
              class="tab-icon-img" 
              :src="activeCategory === 0 ? allCategoryIconActive : allCategoryIcon" 
              mode="aspectFit"
            ></image>
          </view>
          <text class="tab-name">全部</text>
        </view>
        <view 
          class="tab-item" 
          :class="{ active: activeCategory === 'hotselling' }" 
          @click="selectCategory('hotselling')">
          <view class="tab-icon hot-tab-icon">
            <text class="hot-tab-emoji">🔥</text>
          </view>
          <text class="tab-name">热销商品</text>
        </view>
        <view 
          class="tab-item" 
          :class="{ active: activeCategory === item.id }" 
          v-for="(item, index) in categoryList" 
          :key="item.id || index"
          @click="selectCategory(item.id)">
          <view class="tab-icon">
            <image
              v-if="hasImageIcon(item)"
              class="tab-icon-img"
              :src="formatUrl(item.icon)"
              mode="aspectFill"
              @error="handleCategoryIconError(index)"
            ></image>
          </view>
          <text class="tab-name">{{ item.name }}</text>
        </view>
      </scroll-view>

      <view 
        class="product-container"
        @touchstart="onTouchStart"
        @touchmove="onTouchMove"
        @touchend="onTouchEnd"
        @touchcancel="onTouchCancel"
        @wheel="onWheel"
      >
        <scroll-view 
          scroll-y 
          class="product-scroll"
          :show-scrollbar="true"
          @scroll="onProductScroll"
          @scrolltolower="onScrollToLower"
          :lower-threshold="50"
        >
          <view class="product-list">
            <view 
              class="product-item" 
              :class="{ 'product-item-off': item.status === 0, 'is-disabled': item.status === 0 }"
              v-for="item in productList" 
              :key="item.id" 
              @click="goToDetail(item.id)"
            >
              <view class="product-image-wrap">
                <image 
                  class="product-image" 
                  :src="formatUrl(item.image) || defaultProductImage" 
                  mode="aspectFill"
                ></image>
                <view class="off-shelf-badge" v-if="item.status === 0">已下架</view>
              </view>
              <view class="product-info">
                <view class="product-info-top">
                  <view class="product-name-row">
                    <text class="product-name" :class="{ 'text-disabled': item.status === 0 }">{{ item.name }}</text>
                    <text class="off-shelf-tag" v-if="item.status === 0">已下架</text>
                  </view>
                  <text class="product-desc" :class="{ 'text-disabled': item.status === 0 }">{{ item.description || '农家好货，品质保证' }}</text>
                </view>
                <view class="product-bottom">
                  <view class="product-price">
                    <text class="price-symbol" :class="{ 'text-disabled': item.status === 0 }">¥</text>
                    <text class="price" :class="{ 'text-disabled': item.status === 0 }">{{ item.price }}</text>
                    <text class="unit" :class="{ 'text-disabled': item.status === 0 }">/{{ item.unit }}</text>
                  </view>
                  <view class="add-cart" v-if="item.status !== 0" @click.stop="addToCart(item)">
                    <text class="cart-plus">+</text>
                    <text class="cart-text">加购物车</text>
                    <view class="cart-badge" v-if="getCartItemQuantity(item.id) > 0">
                      {{ getCartItemQuantity(item.id) }}
                    </view>
                  </view>
                  <view class="quantity-disabled" v-else>
                    <text class="quantity-tip">已不可购买</text>
                  </view>
                </view>
              </view>
            </view>

            <!-- 底部上拉加载指示器：有更多数据且未在加载中时展示 -->
            <view class="pull-up-tip" v-if="hasMore && !loading && productList.length > 0" @click="triggerAppendData">
              <text class="pull-arrow" :class="{ rotate: pullUpDistance >= pullUpThreshold }">↑</text>
              <text class="pull-text">{{ pullUpDistance >= pullUpThreshold ? '释放立即加载更多' : '上拉加载更多' }}</text>
            </view>

            <!-- 加载中动画 -->
            <view class="loading-more" v-if="loading && productList.length > 0">
              <view class="loading-spinner"></view>
              <text class="loading-text">正在加载更多商品...</text>
            </view>

            <!-- 无更多商品提示（上拉到底或无数据时触发，展示2秒） -->
            <view class="no-more-toast" v-if="showNoMoreTip || (!hasMore && productList.length > 0)">
              <text class="no-more-text">没有更多商品</text>
            </view>

            <view class="empty" v-if="productList.length === 0 && !loading">
              <text class="empty-icon">📦</text>
              <text class="empty-text">暂无相关商品</text>
            </view>
          </view>
        </scroll-view>
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
      categoryList: [],
      productList: [],
      cartItemMap: {},
      activeCategory: 0,
      fromHome: false,
      defaultProductImage: '/static/images/product-default.jpg',
      allCategoryIconActive: '/static/tabbar/category-active.png',
      allCategoryIcon: '/static/tabbar/category.png',
      keyword: '',
      isSearchFocus: false,
      searchTimer: null,
      pageNum: 1,
      pageSize: 8,
      total: 0,
      totalPages: 1,
      hasMore: true,
      loading: false,
      showNoMoreTip: false,
      noMoreTipTimer: null,
      pullUpDistance: 0,
      pullUpThreshold: 45,
      touchStartY: 0,
      touchLastY: 0,
      isScrollAtBottom: false
    }
  },
  async onLoad(options) {
    await this.initFileConfig()
    if (options && (options.from === 'home' || options.fromHome)) {
      this.fromHome = true
    }
    if (options && options.categoryId !== undefined) {
      this.activeCategory = options.categoryId === 'hotselling' ? 'hotselling' : Number(options.categoryId)
    }
    if (options && (options.focus === '1' || options.focus === true)) {
      this.triggerSearchFocus()
    }
    await this.loadCategory()
    await this.loadProducts(true)
  },
  async onShow() {
    const isFromHome = uni.getStorageSync('category_from_home')
    const targetId = uni.getStorageSync('selectedCategoryId')
    const shouldFocus = uni.getStorageSync('category_focus_search')

    if (isFromHome) {
      this.fromHome = true
      uni.removeStorageSync('category_from_home')
    }

    if (shouldFocus) {
      uni.removeStorageSync('category_focus_search')
      this.triggerSearchFocus()
    }

    this.updateNavBackButton(this.fromHome)
    await this.loadCartMap()
    updateTabBarCartBadge()

    if (targetId !== null && targetId !== undefined && targetId !== '') {
      this.activeCategory = targetId === 'hotselling' ? 'hotselling' : Number(targetId)
      uni.removeStorageSync('selectedCategoryId')
      await this.loadProducts(true)
    } else {
      await this.loadProducts(true)
    }
  },
  onHide() {
    this.fromHome = false
    this.isSearchFocus = false
    this.updateNavBackButton(false)
  },
  beforeUnmount() {
    this.updateNavBackButton(false)
    if (this.noMoreTipTimer) {
      clearTimeout(this.noMoreTipTimer)
    }
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }
  },
  onUnload() {
    this.updateNavBackButton(false)
    if (this.noMoreTipTimer) {
      clearTimeout(this.noMoreTipTimer)
    }
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }
  },
  methods: {
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
      this.fromHome = false
      this.updateNavBackButton(false)
      const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
      if (pages && pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.switchTab({ url: '/pages/index' })
      }
    },
    async initFileConfig() {
      try {
        const cfg = await api.getFileConfig()
        if (cfg && cfg.baseServer) {
          setFileBaseServer(cfg.baseServer)
        }
      } catch (e) {
      }
    },
    formatUrl(path) {
      if (!path) return ''
      return formatImageUrl(path)
    },
    hasImageIcon(item) {
      if (!item || !item.icon || item.iconError) return false
      return this.isImageIcon(item.icon)
    },
    isImageIcon(icon) {
      if (!icon || typeof icon !== 'string') return false
      const trimmed = icon.trim()
      return (
        trimmed.startsWith('http://') ||
        trimmed.startsWith('https://') ||
        trimmed.startsWith('//') ||
        trimmed.startsWith('/static/') ||
        trimmed.startsWith('/sunny_file') ||
        trimmed.startsWith('/category/') ||
        trimmed.startsWith('data:image') ||
        /\.(png|jpe?g|gif|svg|webp)($|\?)/i.test(trimmed)
      )
    },
    handleCategoryIconError(index) {
      if (this.categoryList && this.categoryList[index]) {
        this.$set ? this.$set(this.categoryList[index], 'iconError', true) : (this.categoryList[index].iconError = true)
      }
    },
    async loadCategory() {
      try {
        const data = await api.getCategoryList()
        this.categoryList = Array.isArray(data) ? data : []
      } catch (e) {
        console.error(e)
      }
    },
    async loadProducts(isRefresh = false) {
      if (this.loading) return
      if (isRefresh) {
        this.pageNum = 1
        this.hasMore = true
      }
      if (!isRefresh && !this.hasMore) {
        this.triggerNoMoreTip()
        return
      }

      this.loading = true
      try {
        let records = []
        let total = 0
        let pages = 1

        if (this.activeCategory === 'hotselling') {
          // 热销商品
          const hotList = await api.getHotsellingList()
          let list = Array.isArray(hotList) ? hotList : []
          if (this.keyword && this.keyword.trim()) {
            const kw = this.keyword.trim().toLowerCase()
            list = list.filter(item => (item.name && item.name.toLowerCase().includes(kw)) || (item.description && item.description.toLowerCase().includes(kw)))
          }
          list.sort((a, b) => {
            const statusA = a.status != null ? a.status : 1
            const statusB = b.status != null ? b.status : 1
            if (statusA !== statusB) {
              return statusB - statusA
            }
            const sortA = a.sort != null ? a.sort : 0
            const sortB = b.sort != null ? b.sort : 0
            return sortA - sortB
          })
          total = list.length
          pages = Math.ceil(total / this.pageSize) || 1
          const startIndex = (this.pageNum - 1) * this.pageSize
          records = list.slice(startIndex, startIndex + this.pageSize)
        } else {
          const params = {
            pageNum: this.pageNum,
            pageSize: this.pageSize
          }
          if (this.keyword && this.keyword.trim()) {
            params.keyword = this.keyword.trim()
          }
          if (this.activeCategory !== 0) {
            params.categoryId = this.activeCategory
          }

          const res = await api.getProductPage(params)
          records = (res && Array.isArray(res.records)) ? res.records : []
          total = (res && typeof res.total === 'number') ? res.total : (Number(res && res.total) || records.length)
          pages = (res && typeof res.pages === 'number') ? res.pages : (Math.ceil(total / this.pageSize) || 1)
        }
        
        this.total = total
        this.totalPages = pages

        if (isRefresh) {
          this.productList = records
        } else {
          this.productList = [...this.productList, ...records]
        }

        if (records.length < this.pageSize || this.productList.length >= this.total || this.pageNum >= this.totalPages) {
          this.hasMore = false
        } else {
          this.hasMore = true
        }
      } catch (e) {
        console.error('加载商品失败:', e)
        if (!isRefresh) {
          this.pageNum = Math.max(1, this.pageNum - 1)
        }
      } finally {
        this.loading = false
      }
    },
    onSearchBlur() {
      this.isSearchFocus = false
    },
    triggerSearchFocus() {
      this.isSearchFocus = false
      this.$nextTick(() => {
        setTimeout(() => {
          this.isSearchFocus = true
          // #ifdef H5
          const inputEl = document.querySelector('.search-input input') || document.querySelector('.search-input')
          if (inputEl && typeof inputEl.focus === 'function') {
            inputEl.focus()
          }
          // #endif
        }, 150)
      })
    },
    clearSearch() {
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
        this.searchTimer = null
      }
      this.keyword = ''
      this.loadProducts(true)
    },
    onSearchInput() {
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
        this.searchTimer = null
      }
      this.searchTimer = setTimeout(() => {
        this.loadProducts(true)
      }, 300)
    },
    onSearchInputImmediate() {
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
        this.searchTimer = null
      }
      this.loadProducts(true)
    },
    selectCategory(categoryId) {
      if (this.activeCategory === categoryId) return
      this.activeCategory = categoryId
      this.isScrollAtBottom = false
      this.loadProducts(true)
    },
    onProductScroll(e) {
      const detail = e && e.detail
      if (!detail) return
      const { scrollTop, scrollHeight } = detail
      // 记录最新滚动位置
      this.currentScrollTop = scrollTop || 0
      this.currentScrollHeight = scrollHeight || 0
      // 触底判定（考虑不同的视口高度）
      if (scrollHeight > 0 && scrollTop + 500 >= scrollHeight - 120) {
        this.isScrollAtBottom = true
      }
    },
    onScrollToLower() {
      this.isScrollAtBottom = true
      // 原生触底时，如果还有更多数据则自动触发加载
      if (this.hasMore && !this.loading) {
        this.triggerAppendData()
      }
    },
    onTouchStart(e) {
      if (e.touches && e.touches.length > 0) {
        this.touchStartY = e.touches[0].clientY
        this.touchLastY = e.touches[0].clientY
      }
    },
    onTouchMove(e) {
      if (e.touches && e.touches.length > 0) {
        this.touchLastY = e.touches[0].clientY
        if (this.touchStartY) {
          const delta = this.touchStartY - this.touchLastY
          if (delta > 0) {
            this.pullUpDistance = Math.min(delta, 100)
          } else {
            this.pullUpDistance = 0
          }
        }
      }
    },
    onTouchEnd(e) {
      if (!this.touchStartY) return
      const endY = (e.changedTouches && e.changedTouches.length > 0) ? e.changedTouches[0].clientY : this.touchLastY
      const deltaY = this.touchStartY - endY
      const currentPullDistance = this.pullUpDistance || deltaY
      this.touchStartY = 0
      this.touchLastY = 0
      this.pullUpDistance = 0

      // 手指上拉（deltaY > 20 说明是向上滑动/拉拽）
      if (currentPullDistance >= this.pullUpThreshold || deltaY > 25) {
        if (!this.hasMore && !this.loading) {
          this.triggerNoMoreTip()
        } else if (this.hasMore && !this.loading) {
          this.triggerAppendData()
        }
      }
    },
    onTouchCancel() {
      this.touchStartY = 0
      this.touchLastY = 0
      this.pullUpDistance = 0
    },
    onWheel(e) {
      // 针对 PC / 鼠标滚轮向上滑动到底后继续滚动
      if (e && e.deltaY > 0) {
        if (!this.hasMore && !this.loading) {
          this.triggerNoMoreTip()
        } else if (this.hasMore && !this.loading) {
          this.triggerAppendData()
        }
      }
    },
    triggerAppendData() {
      if (this.loading) return
      if (this.hasMore) {
        this.pageNum += 1
        this.loadProducts(false)
      } else {
        this.triggerNoMoreTip()
      }
    },
    triggerNoMoreTip() {
      if (this.productList.length === 0) return
      if (this.noMoreTipTimer) {
        clearTimeout(this.noMoreTipTimer)
        this.noMoreTipTimer = null
      }
      this.showNoMoreTip = true
      this.noMoreTipTimer = setTimeout(() => {
        this.showNoMoreTip = false
        this.noMoreTipTimer = null
      }, 2000)
    },
    goToDetail(id) {
      uni.navigateTo({ url: `/pages/product-detail?id=${id}` })
    },
    getCartItemQuantity(productId) {
      if (!productId || !this.cartItemMap) return 0
      return this.cartItemMap[productId] || 0
    },
    async loadCartMap() {
      if (!isLoggedIn()) {
        this.cartItemMap = {}
        return
      }
      try {
        const list = await api.getCartList()
        const map = {}
        if (Array.isArray(list)) {
          list.forEach(item => {
            if (item.productId != null) {
              map[item.productId] = item.quantity || 0
            }
          })
        }
        this.cartItemMap = map
      } catch (e) {
        this.cartItemMap = {}
      }
    },
    async addToCart(item) {
      if (!checkLogin()) return
      if (!item || !item.id) return
      try {
        await api.addToCart({
          productId: item.id,
          quantity: 1
        })
        const prevQty = this.cartItemMap[item.id] || 0
        const newQty = prevQty + 1
        this.cartItemMap = {
          ...this.cartItemMap,
          [item.id]: newQty
        }
        updateTabBarCartBadge()
        uni.showToast({ title: '已加入购物车', icon: 'success' })
      } catch (e) {
        console.error(e)
      }
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
.product-scroll,
.category-tabs,
.product-scroll .uni-scroll-view,
.category-tabs .uni-scroll-view {
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.18) transparent;
}

.product-scroll::-webkit-scrollbar,
.category-tabs::-webkit-scrollbar,
.product-scroll .uni-scroll-view::-webkit-scrollbar,
.category-tabs .uni-scroll-view::-webkit-scrollbar {
  display: block !important;
  width: 5px !important;
  height: 5px !important;
  background: transparent !important;
}

.product-scroll::-webkit-scrollbar-thumb,
.category-tabs::-webkit-scrollbar-thumb,
.product-scroll .uni-scroll-view::-webkit-scrollbar-thumb,
.category-tabs .uni-scroll-view::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.18) !important;
  border-radius: 6px !important;
}

.product-scroll::-webkit-scrollbar-thumb:hover,
.category-tabs::-webkit-scrollbar-thumb:hover,
.product-scroll .uni-scroll-view::-webkit-scrollbar-thumb:hover,
.category-tabs .uni-scroll-view::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.32) !important;
}

.product-scroll::-webkit-scrollbar-track,
.category-tabs::-webkit-scrollbar-track,
.product-scroll .uni-scroll-view::-webkit-scrollbar-track,
.category-tabs .uni-scroll-view::-webkit-scrollbar-track {
  background: transparent !important;
}

.product-scroll .uni-scroll-view,
.product-scroll .uni-scroll-view-wrap,
.product-scroll .uni-scroll-view-content,
.category-tabs .uni-scroll-view,
.category-tabs .uni-scroll-view-wrap,
.category-tabs .uni-scroll-view-content {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box !important;
  overflow-x: hidden !important;
  touch-action: pan-y;
  -webkit-overflow-scrolling: touch;
}
</style>

<style scoped>
.category-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  max-width: 100vw;
  background: #f8f8f8;
  overflow: hidden;
  box-sizing: border-box;
}

.search-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16rpx 20rpx;
  background: #ffffff;
  border-bottom: 1rpx solid #eeeeee;
  z-index: 10;
  flex-shrink: 0;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.search-box {
  flex: 1;
  width: 100%;
  min-width: 0;
  display: flex;
  align-items: center;
  background: #f4f5f7;
  border-radius: 36rpx;
  padding: 0 24rpx;
  height: 68rpx;
  box-sizing: border-box;
  border: 1rpx solid #eaeaea;
}

.search-icon {
  font-size: 26rpx;
  margin-right: 12rpx;
  color: #999;
}

.search-input {
  flex: 1;
  min-width: 0;
  font-size: 26rpx;
  color: #333;
  height: 100%;
  background: transparent;
}

.clear-icon {
  font-size: 20rpx;
  color: #fff;
  background: #bbb;
  width: 30rpx;
  height: 30rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  margin-left: 12rpx;
}

.category-body {
  flex: 1;
  display: flex;
  flex-direction: row;
  height: calc(100% - 100rpx);
  width: 100%;
  max-width: 100%;
  overflow: hidden;
  box-sizing: border-box;
}

.category-tabs {
  width: 160rpx;
  min-width: 160rpx;
  max-width: 160rpx;
  background: #f7f8fa;
  height: 100%;
  box-sizing: border-box;
  flex-shrink: 0;
  border-right: 1rpx solid #ececec;
  overflow-x: hidden;
  touch-action: pan-y;
  -webkit-overflow-scrolling: touch;
  transform: translateZ(0);
}

.tab-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  padding: 24rpx 12rpx;
  border-left: 6rpx solid transparent;
  box-sizing: border-box;
  background: #f7f8fa;
  width: 100%;
  overflow: hidden;
}

.tab-item.active {
  background: #ffffff;
  border-left-color: #4CAF50;
}

.tab-icon {
  width: 32rpx;
  height: 32rpx;
  margin-right: 8rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-icon-img {
  width: 100%;
  height: 100%;
  border-radius: 6rpx;
  display: block;
}

.hot-tab-emoji {
  font-size: 24rpx;
  line-height: 1;
}

.tab-name {
  font-size: 24rpx;
  color: #666;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.tab-item.active .tab-name {
  color: #4CAF50;
  font-weight: bold;
}

.product-container {
  flex: 1;
  width: calc(100% - 160rpx);
  min-width: 0;
  max-width: calc(100% - 160rpx);
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden;
  touch-action: pan-y;
}

.product-scroll {
  width: 100%;
  max-width: 100%;
  height: 100%;
  box-sizing: border-box;
  background: #f8f8f8;
  overflow-x: hidden;
  touch-action: pan-y;
  -webkit-overflow-scrolling: touch;
  transform: translateZ(0);
}

:deep(.uni-scroll-view),
:deep(.uni-scroll-view-wrap),
:deep(.uni-scroll-view-content) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box !important;
  overflow-x: hidden !important;
}

.product-list {
  padding: 14rpx;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
}

.product-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  background: #ffffff;
  border-radius: 14rpx;
  padding: 14rpx;
  margin-bottom: 14rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.03);
  border: 1rpx solid #f0f0f0;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
}

.product-item:active {
  opacity: 0.92;
}

.product-item.is-disabled {
  background: #fbfbfb;
}

.product-image-wrap {
  width: 130rpx;
  height: 130rpx;
  border-radius: 12rpx;
  margin-right: 14rpx;
  flex-shrink: 0 !important;
  position: relative;
  overflow: hidden;
  background: #f5f5f5;
}

.product-image {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
  object-fit: cover;
  display: block;
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
  line-height: 32rpx;
  z-index: 2;
}

.product-info {
  flex: 1;
  min-width: 0;
  width: 0;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 130rpx;
  box-sizing: border-box;
  overflow: hidden;
}

.product-info-top {
  width: 100%;
  max-width: 100%;
  overflow: hidden;
}

.product-name-row {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 100%;
}

.product-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  word-break: break-all;
  line-height: 1.3;
  flex: 1;
  min-width: 0;
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
  line-height: 1.2;
}

.text-disabled {
  color: #999999 !important;
}

.quantity-disabled {
  display: flex;
  align-items: center;
}

.quantity-tip {
  font-size: 22rpx;
  color: #999999;
}

.product-desc {
  font-size: 20rpx;
  color: #999;
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  word-break: break-all;
  line-height: 1.2;
  display: block;
  width: 100%;
  max-width: 100%;
}

.product-bottom {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.product-price {
  display: flex;
  align-items: baseline;
  flex-shrink: 1;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
}

.price-symbol {
  font-size: 20rpx;
  color: #f44336;
  font-weight: bold;
}

.price {
  font-size: 30rpx;
  color: #f44336;
  font-weight: bold;
}

.unit {
  font-size: 20rpx;
  color: #999;
  margin-left: 2rpx;
}

.add-cart {
  background: #4CAF50;
  color: #fff;
  padding: 6rpx 14rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(76, 175, 80, 0.2);
  flex-shrink: 0;
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -10rpx;
  right: -8rpx;
  background: #FF5722;
  color: #fff;
  font-size: 18rpx;
  padding: 2rpx 10rpx;
  border-radius: 20rpx;
  min-width: 28rpx;
  text-align: center;
  line-height: 1.2;
  box-shadow: 0 2rpx 6rpx rgba(255, 87, 34, 0.35);
  font-weight: bold;
  box-sizing: border-box;
}

.cart-plus {
  font-size: 22rpx;
  font-weight: bold;
  margin-right: 4rpx;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -2rpx;
}

.cart-text {
  font-size: 20rpx;
  line-height: 1;
  white-space: nowrap;
}

.pull-up-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 0;
  color: #888;
  font-size: 24rpx;
  width: 100%;
  box-sizing: border-box;
}

.pull-arrow {
  margin-right: 12rpx;
  font-size: 28rpx;
  font-weight: bold;
  color: #4CAF50;
  transition: transform 0.2s ease;
  display: inline-block;
}

.pull-arrow.rotate {
  transform: rotate(180deg);
}

.pull-text {
  font-size: 24rpx;
  color: #888;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0;
  width: 100%;
  box-sizing: border-box;
}

.loading-spinner {
  width: 26rpx;
  height: 26rpx;
  border: 4rpx solid #e0e0e0;
  border-top-color: #4CAF50;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: 12rpx;
}

.loading-text {
  font-size: 24rpx;
  color: #999;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-more-toast {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0 28rpx;
  animation: fadeIn 0.25s ease-in-out;
  width: 100%;
  box-sizing: border-box;
}

.no-more-text {
  font-size: 22rpx;
  color: #999;
  background: #ebebeb;
  padding: 6rpx 24rpx;
  border-radius: 20rpx;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.empty {
  padding-top: 160rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  box-sizing: border-box;
}

.empty-icon {
  font-size: 64rpx;
  margin-bottom: 12rpx;
}

.empty-text {
  color: #999;
  font-size: 26rpx;
}
</style>
