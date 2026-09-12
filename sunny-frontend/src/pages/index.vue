<template>
  <view class="index-page">
    <view class="search-bar" @click="goSearch">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <text class="search-placeholder">搜索商品...</text>
      </view>
    </view>

    <scroll-view scroll-y class="index-scroll" :show-scrollbar="true">
      <view class="index-content">
        <!-- 轮播图模块：支持接口获取、展示正常图片、点击系统内嵌跳转 -->
        <swiper class="banner" indicator-dots indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff" autoplay circular>
          <swiper-item v-for="(item, index) in displayBannerList" :key="item.id || index">
            <image
              class="banner-image"
              :src="item.formattedImage || defaultBanner"
              mode="aspectFill"
              @click="handleBannerClick(item)"
              @error="handleBannerImgError(index)"
            ></image>
            <view class="banner-overlay" v-if="item.title && item.title.trim()" @click="handleBannerClick(item)">
              <text class="banner-title">{{ item.title }}</text>
            </view>
          </swiper-item>
          <swiper-item v-if="displayBannerList.length === 0">
            <image class="banner-image" :src="defaultBanner" mode="aspectFill"></image>
          </swiper-item>
        </swiper>

        <!-- 分类导航 quick-nav：全量分类接口数据，一行最多4个，图标从接口获取 -->
        <view class="quick-nav" v-if="categoryList && categoryList.length > 0">
          <view
            class="nav-item"
            v-for="(item, index) in categoryList"
            :key="item.id || index"
            @click="goToCategory(item.id)"
          >
            <view class="nav-icon">
              <image
                v-if="isImageIcon(item.icon) && !item.iconError"
                class="nav-icon-img"
                :src="formatUrl(item.icon)"
                mode="aspectFit"
                @error="handleCategoryIconError(index)"
              ></image>
              <text v-else class="nav-icon-text">{{ getCategoryEmoji(item) }}</text>
            </view>
            <text class="nav-name">{{ item.name }}</text>
          </view>
        </view>

        <view class="section product-section">
          <view class="section-header">
            <view class="section-title-area">
              <text class="section-title">🔥 热销商品</text>
              <text class="section-subtitle">精选好货 品质保证</text>
            </view>
            <view class="section-more" @click="goToProductList">
              <text>查看全部</text>
              <text class="arrow">›</text>
            </view>
          </view>
          <view
            class="product-scroll-container"
            @mousedown="handleProductMouseDown"
            @touchstart="handleProductTouchStart"
            @touchmove="handleProductTouchMove"
            @touchend="handleProductTouchEnd"
          >
            <scroll-view
              scroll-x
              class="product-scroll"
              :show-scrollbar="true"
              :scroll-left="productScrollLeft"
              @scroll="onProductScroll"
            >
              <view class="product-scroll-inner">
                <view class="product-card" v-for="item in productList" :key="item.id" @click="goToDetail(item.id)">
                  <view class="product-image-wrap">
                    <image class="product-image" :src="formatUrl(item.image) || defaultProduct" mode="aspectFill"></image>
                    <view class="product-tag" v-if="item.sales > 50">热卖</view>
                  </view>
                  <view class="product-content">
                    <text class="product-name">{{ item.name }}</text>
                    <text class="product-origin">{{ item.origin || '大山村' }}</text>
                    <view class="product-bottom">
                      <view class="product-price">
                        <text class="price-symbol">¥</text>
                        <text class="price-value">{{ item.price }}</text>
                        <text class="price-unit">/{{ item.unit }}</text>
                      </view>
                      <view class="add-btn" @click.stop="addToCart(item)">+</view>
                    </view>
                  </view>
                </view>
              </view>
            </scroll-view>
          </view>
        </view>

        <view class="footer">
          <text class="footer-text">© 2026 Sunny农产品</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import api from '../api/index'
import { formatImageUrl, setFileBaseServer } from '../utils/request'

export default {
  data() {
    return {
      bannerList: [],
      categoryList: [],
      productList: [],
      productScrollLeft: 0,
      isDraggingProduct: false,
      dragStartX: 0,
      dragStartScrollLeft: 0,
      touchStartX: null,
      hasMovedDrag: false,
      defaultBanner: '/static/images/banner.jpg',
      defaultProduct: '/static/images/product-default.jpg'
    }
  },
  computed: {
    displayBannerList() {
      return (this.bannerList || []).map(item => {
        const formatted = this.formatUrl(item.image)
        return {
          ...item,
          formattedImage: formatted || this.defaultBanner
        }
      })
    }
  },
  async onLoad() {
    await this.initFileConfig()
    this.loadData()
    // #ifdef H5
    if (typeof window !== 'undefined') {
      window.addEventListener('mousemove', this.onGlobalMouseMove, { passive: false })
      window.addEventListener('mouseup', this.onGlobalMouseUp)
      this.$nextTick(() => {
        // uni-app H5 对 <view> 模板上的鼠标事件(@wheel/@mousedown 等)编译不可靠，
        // 命令式绑定确保滚轮横向滚动与鼠标拖拽生效
        const container = document.querySelector('.product-scroll-container')
        if (container) {
          this._productScrollContainer = container
          container.addEventListener('wheel', this.handleProductWheel, { passive: false })
          container.addEventListener('mousedown', this.handleProductMouseDown)
        }
        this.enforceProductScrollLayout()
      })
    }
    // #endif
  },
  onUnload() {
    // #ifdef H5
    if (typeof window !== 'undefined') {
      window.removeEventListener('mousemove', this.onGlobalMouseMove)
      window.removeEventListener('mouseup', this.onGlobalMouseUp)
      if (this._productScrollContainer) {
        this._productScrollContainer.removeEventListener('wheel', this.handleProductWheel)
        this._productScrollContainer.removeEventListener('mousedown', this.handleProductMouseDown)
        this._productScrollContainer = null
      }
    }
    // #endif
  },
  beforeUnmount() {
    // #ifdef H5
    if (typeof window !== 'undefined') {
      window.removeEventListener('mousemove', this.onGlobalMouseMove)
      window.removeEventListener('mouseup', this.onGlobalMouseUp)
      if (this._productScrollContainer) {
        this._productScrollContainer.removeEventListener('wheel', this.handleProductWheel)
        this._productScrollContainer.removeEventListener('mousedown', this.handleProductMouseDown)
        this._productScrollContainer = null
      }
    }
    // #endif
  },
  onPullDownRefresh() {
    this.loadData().finally(() => {
      uni.stopPullDownRefresh()
    })
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
    handleBannerImgError(index) {
      if (this.bannerList && this.bannerList[index]) {
        this.$set ? this.$set(this.bannerList[index], 'image', this.defaultBanner) : (this.bannerList[index].image = this.defaultBanner)
      }
    },
    async loadData() {
      try {
        const [banner, category, product] = await Promise.all([
          api.getBannerList().catch(() => []),
          api.getCategoryList().catch(() => []),
          api.getProductList().catch(() => [])
        ])
        this.bannerList = Array.isArray(banner) ? banner : []
        this.categoryList = Array.isArray(category) ? category : []
        this.productList = (Array.isArray(product) ? product : []).slice(0, 10)
        // 商品渲染后再强制一次横向布局，确保内容撑开、触摸可滚动
        this.$nextTick(() => this.enforceProductScrollLayout())
      } catch (e) {
        console.error('加载首页数据异常', e)
      }
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
    getCategoryColor(code) {
      const colors = {
        'ganhuo': 'linear-gradient(135deg, #FF6B6B, #FF8E53)',
        'shucai': 'linear-gradient(135deg, #4CAF50, #8BC34A)',
        'shengtaiyou': 'linear-gradient(135deg, #FFC107, #FF9800)',
        'jiu': 'linear-gradient(135deg, #9C27B0, #E91E63)',
        'yangzhi': 'linear-gradient(135deg, #2196F3, #03A9F4)'
      }
      return colors[code] || 'linear-gradient(135deg, #4CAF50, #8BC34A)'
    },
    getCategoryEmoji(item) {
      if (!item) return '🌾'
      if (typeof item === 'string') {
        const emojis = {
          'ganhuo': '🌶️',
          'shucai': '🥬',
          'shengtaiyou': '🫒',
          'jiu': '🍶',
          'yangzhi': '🐔'
        }
        return emojis[item] || '🌾'
      }
      if (item.icon && !this.isImageIcon(item.icon)) {
        return item.icon
      }
      const emojis = {
        'ganhuo': '🌶️',
        'shucai': '🥬',
        'shengtaiyou': '🫒',
        'jiu': '🍶',
        'yangzhi': '🐔'
      }
      return emojis[item.code] || '🌾'
    },
    handleBannerClick(item) {
      if (!item || !item.link || !item.link.trim()) {
        return
      }
      const rawLink = item.link.trim()

      // 1. 外部链接跳转（内嵌在系统内，避免跨域报错）
      if (/^https?:\/\//i.test(rawLink) || rawLink.startsWith('//')) {
        let externalUrl = rawLink
        if (externalUrl.startsWith('//')) {
          // #ifdef H5
          externalUrl = window.location.protocol + externalUrl
          // #endif
          // #ifndef H5
          externalUrl = 'https:' + externalUrl
          // #endif
        }
        
        const title = item.title || '网页详情'
        uni.navigateTo({
          url: `/pages/webview?url=${encodeURIComponent(externalUrl)}&title=${encodeURIComponent(title)}`,
          fail: (err) => {
            console.error('打开内嵌网页失败', err)
          }
        })
        return
      }

      // 2. 内部路由跳转
      let internalUrl = rawLink
      if (!internalUrl.startsWith('/')) {
        internalUrl = '/' + internalUrl
      }

      const tabBarPages = [
        '/pages/index',
        '/pages/category',
        '/pages/cart',
        '/pages/user'
      ]
      const purePath = internalUrl.split('?')[0]

      if (tabBarPages.includes(purePath)) {
        uni.switchTab({
          url: purePath,
          fail: () => {
            uni.reLaunch({ url: internalUrl })
          }
        })
      } else {
        uni.navigateTo({
          url: internalUrl,
          fail: () => {
            uni.switchTab({
              url: purePath,
              fail: () => {
                uni.reLaunch({ url: internalUrl })
              }
            })
          }
        })
      }
    },
    goSearch() {
      uni.setStorageSync('category_focus_search', true)
      uni.setStorageSync('category_from_home', true)
      // 首页搜索框始终定位到分类页的"全部"
      uni.setStorageSync('selectedCategoryId', 0)
      uni.switchTab({
        url: '/pages/category',
        fail: () => {
          uni.navigateTo({
            url: '/pages/category?focus=1&from=home'
          })
        }
      })
    },
    goToCategory(categoryId) {
      const targetId = categoryId ? Number(categoryId) : 0
      uni.setStorageSync('selectedCategoryId', targetId)
      uni.setStorageSync('category_from_home', true)
      uni.switchTab({
        url: '/pages/category',
        fail: () => {
          uni.navigateTo({
            url: `/pages/category?categoryId=${targetId}`
          })
        }
      })
    },
    goToProductList() {
      uni.navigateTo({ url: '/pages/product-list' })
    },
    goToDetail(id) {
      // #ifdef H5
      if (this.hasMovedDrag) {
        this.hasMovedDrag = false
        return
      }
      // #endif
      uni.navigateTo({ url: `/pages/product-detail?id=${id}` })
    },
    addToCart(item) {
      const cart = uni.getStorageSync('cart') || []
      const existIndex = cart.findIndex(p => p.id === item.id)
      if (existIndex > -1) {
        cart[existIndex].quantity += 1
      } else {
        cart.push({ ...item, quantity: 1 })
      }
      uni.setStorageSync('cart', cart)
      uni.showToast({ title: '已加入购物车', icon: 'success' })
    },
    enforceProductScrollLayout() {
      // 用内联样式强制热销区横向布局，不依赖任何 scoped/:deep 选择器和框架内部 class 结构：
      // 1) 内容层不换行并撑开宽度 → 保证一定产生横向溢出（否则无内容可滚）
      // 2) 横向滚动区域内的 uni-scroll-view 覆盖 touch-action 为 pan-x pan-y
      //    （全局 .uni-scroll-view { touch-action: pan-y } 只给纵向 scroll-view 用）
      //
      // 祖先链 touch-action 的策略已统一在 index.html 中管理：
      //   - 框架基础层 (html/body/uni-page/...) → pan-x pan-y（允许所有手势）
      //   - 纵向 scroll-view (.uni-scroll-view) → pan-y（只负责纵向，不抢横向）
      //   - 横向区域 (.product-scroll-container *) → pan-x pan-y !important（覆盖纵向）
      // JS 不再向上遍历修改祖先链，避免误把外层纵向 scroll-view 也改成 pan-x。
      // #ifdef H5
      if (typeof document === 'undefined') return
      const root = document.querySelector('.product-scroll')
      if (!root) return
      const inner = root.querySelector('.product-scroll-inner')
      if (inner) {
        inner.style.display = 'inline-flex'
        inner.style.flexWrap = 'nowrap'
        inner.style.width = 'max-content'
        inner.style.minWidth = '100%'
        inner.style.whiteSpace = 'nowrap'
      }
      // 横向区域内的 uni-scroll-view 覆盖 touch-action（index.html 全局给的是 pan-y）
      root.querySelectorAll('.uni-scroll-view, .uni-scroll-view-wrap').forEach(el => {
        el.style.touchAction = 'pan-x pan-y'
        el.style.overflowX = 'auto'
        el.style.overflowY = 'hidden'
      })
      const container = document.querySelector('.product-scroll-container')
      if (container) {
        container.style.touchAction = 'pan-x pan-y'
      }
      const scroller = this.getProductScrollEl()
      if (scroller) {
        scroller.style.touchAction = 'pan-x pan-y'
      }
      // #endif
    },
    getProductScrollEl() {
      // #ifdef H5
      if (typeof document !== 'undefined') {
        const root = document.querySelector('.product-scroll')
        if (!root) return null
        // uni-app H5 的 scroll-view 内部结构随版本变化（可能是多层 .uni-scroll-view 或 .uni-scroll-view-wrap），
        // 必须同时满足两个条件才是「真正可横向滚动」的元素：
        //   1) 存在横向溢出 scrollWidth > clientWidth
        //   2) 计算样式 overflow-x 为 auto/scroll
        // 此前只按 scrollWidth 判断会命中 uni-scroll-view-content（overflow: visible 的内容包装层），
        // 对它设置 scrollLeft 无效。这里取所有合格元素中层级最深的一个，即浏览器实际负责滚动的容器。
        const all = [root, ...Array.from(root.querySelectorAll('*'))]
        let target = null
        for (let i = 0; i < all.length; i++) {
          const el = all[i]
          if (el.scrollWidth > el.clientWidth + 1) {
            const cs = window.getComputedStyle(el)
            if (cs.overflowX === 'auto' || cs.overflowX === 'scroll') {
              target = el
            }
          }
        }
        return target
      }
      // #endif
      return null
    },
    onProductScroll(e) {
      if (e && e.detail && typeof e.detail.scrollLeft === 'number') {
        this.productScrollLeft = e.detail.scrollLeft
      }
    },
    syncScrollLeft(left) {
      this.productScrollLeft = left
      // #ifdef H5
      if (typeof document !== 'undefined') {
        const el = this.getProductScrollEl()
        if (el) el.scrollLeft = left
      }
      // #endif
    },
    handleProductWheel(e) {
      if (!e) return
      // #ifdef H5
      const scrollEl = this.getProductScrollEl()
      const delta = Math.abs(e.deltaX) > Math.abs(e.deltaY) ? e.deltaX : e.deltaY
      if (scrollEl && delta) {
        const nextLeft = Math.max(0, scrollEl.scrollLeft + delta)
        this.syncScrollLeft(nextLeft)
        if (typeof e.preventDefault === 'function') {
          e.preventDefault()
        }
        return
      }
      // #endif
      const deltaNormal = (e.deltaX && Math.abs(e.deltaX) > Math.abs(e.deltaY)) ? e.deltaX : (e.deltaY || 0)
      if (deltaNormal) {
        const current = this.productScrollLeft || 0
        this.syncScrollLeft(Math.max(0, current + deltaNormal))
      }
    },
    handleProductMouseDown(e) {
      // #ifdef H5
      if (e && (e.button === 0 || e.button === undefined)) {
        this.isDraggingProduct = true
        this.dragStartX = e.pageX
        const scrollEl = this.getProductScrollEl()
        this.dragStartScrollLeft = scrollEl ? scrollEl.scrollLeft : (this.productScrollLeft || 0)
        this.hasMovedDrag = false
      }
      // #endif
    },
    onGlobalMouseMove(e) {
      // #ifdef H5
      if (!this.isDraggingProduct) return
      const curX = e.pageX
      const deltaX = curX - this.dragStartX
      if (Math.abs(deltaX) > 4) {
        this.hasMovedDrag = true
        if (typeof e.preventDefault === 'function') {
          e.preventDefault()
        }
      }
      const targetLeft = Math.max(0, this.dragStartScrollLeft - deltaX)
      if (Number.isFinite(targetLeft)) {
        this.syncScrollLeft(targetLeft)
      }
      // #endif
    },
    onGlobalMouseUp() {
      // #ifdef H5
      if (this.isDraggingProduct) {
        this.isDraggingProduct = false
        setTimeout(() => {
          this.hasMovedDrag = false
        }, 80)
      }
      // #endif
    },
    handleProductTouchStart(e) {
      // 触摸拖动交给 uni-scroll-view 原生横向滚动处理，
      // 这里仅记录起点，用于判断“拖拽后松开”时避免误触商品跳转
      if (e && e.touches && e.touches[0]) {
        this.touchStartX = e.touches[0].pageX
        this.hasMovedDrag = false
      }
    },
    handleProductTouchMove(e) {
      // 不干预原生横向滚动，仅判断是否发生了拖拽，避免松手后误触跳转
      if (!e || !e.touches || !e.touches[0]) return
      const deltaX = e.touches[0].pageX - (this.touchStartX ?? 0)
      if (Math.abs(deltaX) > 8) {
        this.hasMovedDrag = true
      }
    },
    handleProductTouchEnd() {
      this.isDraggingProduct = false
      setTimeout(() => {
        this.hasMovedDrag = false
      }, 80)
    }
  }
}
</script>

<style>
/* === 注意：uni-app H5 会把 <style>（即使非 scoped）编译成带 [data-v-xxx] 的作用域选择器，
   所以 page/uni-page/uni-page-wrapper/uni-page-body 等框架元素的规则无法命中。
   这些元素的全局样式（height/overflow/touch-action）统一在 index.html 内联 style 中设置。 */
.index-page {
  height: 100%;
  width: 100%;
  max-width: 100vw;
  overflow: hidden;
  background: #f5f5f5;
}

/* 首页内部美化滚动条样式 */
.index-scroll,
.index-scroll .uni-scroll-view,
.index-scroll .uni-scroll-view-wrap {
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
}

.index-scroll::-webkit-scrollbar,
.index-scroll .uni-scroll-view::-webkit-scrollbar,
.index-scroll .uni-scroll-view-wrap::-webkit-scrollbar {
  display: block !important;
  width: 6px !important;
  height: 6px !important;
  background: transparent !important;
}

.index-scroll::-webkit-scrollbar-thumb,
.index-scroll .uni-scroll-view::-webkit-scrollbar-thumb,
.index-scroll .uni-scroll-view-wrap::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2) !important;
  border-radius: 6px !important;
}

.index-scroll::-webkit-scrollbar-thumb:hover,
.index-scroll .uni-scroll-view::-webkit-scrollbar-thumb:hover,
.index-scroll .uni-scroll-view-wrap::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.35) !important;
}

.index-scroll::-webkit-scrollbar-track,
.index-scroll .uni-scroll-view::-webkit-scrollbar-track,
.index-scroll .uni-scroll-view-wrap::-webkit-scrollbar-track {
  background: transparent !important;
}

/* 横向滚动区域内强制 pan-x pan-y，覆盖 index.html 中给 .uni-scroll-view 的 pan-y 规则
   （用 !important 确保 CSS 优先级够高，同时 JS enforceProductScrollLayout 也会内联设置） */
.product-scroll-container,
.product-scroll,
.product-scroll * {
  touch-action: pan-x pan-y !important;
}

/* 祖先链 touch-action 基础 (html/body/框架元素) 已在 index.html 中正确设置，
   此处不再重复声明 — 这些选择器在 uni-app H5 下因 scoped 编译无法命中框架层元素 */

/* 热销区域横向滚动条美化与强制显示 */
.product-scroll-container {
  width: 100%;
  position: relative;
}

.product-scroll,
.product-scroll .uni-scroll-view,
.product-scroll .uni-scroll-view-wrap,
.product-scroll::-webkit-scrollbar,
.product-scroll .uni-scroll-view::-webkit-scrollbar,
.product-scroll .uni-scroll-view-wrap::-webkit-scrollbar {
  scrollbar-width: auto !important;
  scrollbar-color: #888888 #e8e8e8 !important;
}

.product-scroll::-webkit-scrollbar,
.product-scroll .uni-scroll-view::-webkit-scrollbar,
.product-scroll .uni-scroll-view-wrap::-webkit-scrollbar {
  display: block !important;
  width: 100% !important;
  height: 10px !important;
  background-color: #e8e8e8 !important;
  border-radius: 5px !important;
}

.product-scroll::-webkit-scrollbar-thumb,
.product-scroll .uni-scroll-view::-webkit-scrollbar-thumb,
.product-scroll .uni-scroll-view-wrap::-webkit-scrollbar-thumb {
  display: block !important;
  background-color: #888888 !important;
  border-radius: 5px !important;
  border: 1px solid #e8e8e8 !important;
}

.product-scroll::-webkit-scrollbar-thumb:hover,
.product-scroll .uni-scroll-view::-webkit-scrollbar-thumb:hover,
.product-scroll .uni-scroll-view-wrap::-webkit-scrollbar-thumb:hover {
  background-color: #555555 !important;
}

.product-scroll::-webkit-scrollbar-track,
.product-scroll .uni-scroll-view::-webkit-scrollbar-track,
.product-scroll .uni-scroll-view-wrap::-webkit-scrollbar-track {
  display: block !important;
  background-color: #e8e8e8 !important;
  border-radius: 5px !important;
}
</style>

<style scoped>
.index-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  max-width: 100vw;
  background: #f5f5f5;
  overflow: hidden;
  box-sizing: border-box;
}

.search-bar {
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

.index-scroll {
  flex: 1;
  min-height: 0;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  background: #f5f5f5;
  overflow: hidden;
  touch-action: pan-y;
  -webkit-overflow-scrolling: touch;
}

.index-content {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  padding-bottom: 30rpx;
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

.search-placeholder {
  font-size: 26rpx;
  color: #999;
}

.banner {
  width: 100%;
  height: 360rpx;
}

.banner-image {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
  padding: 40rpx 24rpx 24rpx;
  cursor: pointer;
}

.banner-title {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
}

.quick-nav {
  display: flex;
  flex-wrap: wrap;
  background: #fff;
  padding: 30rpx 10rpx 10rpx;
  margin: 20rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.nav-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24rpx;
  box-sizing: border-box;
  cursor: pointer;
}

.nav-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  border: 2rpx solid #eeeeee;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  box-sizing: border-box;
  overflow: hidden;
}

.nav-icon-img {
  width: 64rpx;
  height: 64rpx;
  display: block;
}

.nav-icon-text {
  font-size: 44rpx;
  line-height: 1;
}

.nav-name {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
  text-align: center;
  max-width: 90%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section {
  background: #fff;
  margin: 20rpx;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title-area {
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.section-subtitle {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.section-more {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #4CAF50;
}

.section-more .arrow {
  font-size: 32rpx;
  margin-left: 4rpx;
}

.product-scroll-container {
  width: 100%;
  position: relative;
  user-select: none;
  cursor: grab;
}

.product-scroll-container:active {
  cursor: grabbing;
}

.product-scroll {
  width: 100%;
  white-space: nowrap;
  margin: 0 -24rpx;
  padding: 0 24rpx 10rpx;
  box-sizing: border-box;
  overflow-x: auto !important;
  overflow-y: hidden !important;
}

.product-scroll :deep(.uni-scroll-view) {
  width: 100% !important;
  overflow-x: auto !important;
  overflow-y: hidden !important;
}

.product-scroll :deep(.uni-scroll-view-wrap) {
  width: 100% !important;
  overflow-x: auto !important;
  overflow-y: hidden !important;
  touch-action: pan-x pan-y !important;
  -webkit-overflow-scrolling: touch !important;
}

.product-scroll :deep(.uni-scroll-view-content) {
  display: inline-flex !important;
  flex-wrap: nowrap !important;
  width: max-content !important;
  min-width: 100% !important;
  white-space: nowrap !important;
}

.product-scroll-inner {
  display: inline-flex;
  flex-wrap: nowrap;
  white-space: nowrap;
  padding-right: 24rpx;
  pointer-events: auto;
}

.product-card {
  width: 280rpx;
  flex-shrink: 0;
  background: #fafafa;
  border-radius: 16rpx;
  overflow: hidden;
  margin-right: 20rpx;
  display: inline-block;
  vertical-align: top;
  cursor: pointer;
  user-select: none;
  -webkit-user-drag: none;
}

.product-image-wrap {
  position: relative;
  width: 100%;
  height: 280rpx;
  pointer-events: none;
}

.product-image {
  width: 100%;
  height: 100%;
  pointer-events: none;
  -webkit-user-drag: none;
}

.product-tag {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.product-content {
  padding: 16rpx;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-origin {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-top: 8rpx;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12rpx;
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

.add-btn {
  width: 48rpx;
  height: 48rpx;
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
}

.footer {
  text-align: center;
  padding: 40rpx 24rpx;
  background: #fff;
  margin-top: 20rpx;
}

.footer-text {
  font-size: 24rpx;
  color: #999;
}

@media (min-width: 768px) {
  .header {
    padding: 24rpx 80rpx;
  }
  
  .header-content {
    max-width: 1200rpx;
    margin: 0 auto;
  }
  
  .banner {
    height: 500rpx;
  }
  
  .quick-nav {
    max-width: 1200rpx;
    margin: 30rpx auto;
    padding: 40rpx 24rpx;
  }
  
  .nav-icon {
    width: 120rpx;
    height: 120rpx;
    font-size: 56rpx;
  }
  
  .nav-name {
    font-size: 28rpx;
  }
  
  .section {
    max-width: 1200rpx;
    margin: 30rpx auto;
    padding: 32rpx;
  }
  
  .product-card {
    width: 320rpx;
  }
  
  .product-image-wrap {
    height: 320rpx;
  }
  
  .contact-cards {
    justify-content: center;
  }
  
  .contact-card {
    width: 300rpx;
    flex: none;
  }
}
</style>
