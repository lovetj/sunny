<template>
  <view class="edit-page">
    <view class="form-container">
      <!-- 收货人 -->
      <view class="form-item">
        <text class="form-label"><text class="required">*</text>收货人</text>
        <view class="input-wrap">
          <input 
            class="form-input" 
            type="text" 
            v-model="form.receiverName" 
            placeholder="请填写收货人姓名" 
            maxlength="20"
          />
          <text class="char-count">{{ (form.receiverName || '').length }}/20</text>
        </view>
      </view>

      <!-- 手机号码 -->
      <view class="form-item">
        <text class="form-label"><text class="required">*</text>手机号码</text>
        <view class="input-wrap">
          <input 
            class="form-input" 
            type="number" 
            v-model="form.phone" 
            placeholder="请填写11位手机号码" 
            maxlength="11"
          />
        </view>
      </view>

      <!-- 详细地址（小区/学校/大厦）+ 地图选址与搜索 -->
      <view class="form-item align-top">
        <text class="form-label"><text class="required">*</text>详细地址</text>
        <view class="location-input-wrap">
          <view class="location-bar" @click="openLocationPicker">
            <text class="location-icon">📍</text>
            <text class="location-btn-text">地图选址 / 搜索</text>
          </view>
          <textarea 
            class="form-textarea" 
            v-model="form.detailAddress" 
            placeholder="点击上方在地图中搜索定位小区/学校/大厦等，或直接在此输入"
            :auto-height="true"
            maxlength="200"
          />
        </view>
      </view>

      <!-- 楼号门牌 -->
      <view class="form-item">
        <text class="form-label">楼号门牌</text>
        <view class="input-wrap">
          <input 
            class="form-input" 
            type="text" 
            v-model="form.houseNumber" 
            placeholder="例: 3号楼102室 / A座12层 (选填)" 
            maxlength="50"
          />
        </view>
      </view>

      <!-- 地址标签 -->
      <view class="form-item">
        <text class="form-label">地址标签</text>
        <view class="tags-group">
          <view 
            class="tag-pill" 
            v-for="type in typeOptions" 
            :key="type"
            :class="{ active: form.addressType === type }"
            @click="form.addressType = type"
          >
            {{ type }}
          </view>
        </view>
      </view>

      <!-- 设为默认 -->
      <view class="form-item switch-item">
        <view class="switch-left">
          <text class="form-label mb-0">设为默认收货地址</text>
          <text class="switch-desc">每次下单时会优先使用该地址</text>
        </view>
        <switch 
          :checked="form.isDefault === 1" 
          color="#4CAF50" 
          @change="onDefaultChange" 
        />
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="save-bar">
      <button class="save-btn" :loading="submitting" @click="handleSubmit">
        保存收货地址
      </button>
    </view>

    <!-- 地图选址与搜索弹窗组件 (兼容 H5/小程序/App) -->
    <view class="map-modal-mask" v-if="showSearchModal" @click.self="showSearchModal = false">
      <view class="map-modal-card">
        <view class="modal-header">
          <text class="modal-title">搜索并选择地址</text>
          <text class="modal-close" @click="showSearchModal = false">✕</text>
        </view>

        <!-- 城市选择 + 搜索输入框 -->
        <view class="search-row">
          <picker 
            :range="cityOptions" 
            :value="cityIndex" 
            class="city-picker"
            @change="onCityChange"
          >
            <view class="city-picker-box">
              <text class="city-text">{{ selectedCity || '全国' }}</text>
              <text class="city-arrow">▾</text>
            </view>
          </picker>
          <view class="search-input-box">
            <text class="search-icon">🔍</text>
            <input 
              class="search-input" 
              type="text" 
              v-model="searchKeyword" 
              placeholder="输入小区、写字楼、大厦、学校等关键词"
              @input="handleSearchInput"
              confirm-type="search"
              @confirm="executeSearch"
            />
            <text class="clear-btn" v-if="searchKeyword" @click="clearSearch">✕</text>
          </view>
        </view>

        <!-- 快速定位当前位置 (带定位动画) -->
        <view class="current-location-btn" :class="{ locating: locating }" @click="fetchCurrentLocation">
          <view class="location-icon-wrap">
            <text class="target-icon">🎯</text>
            <view class="locate-ring" v-if="locating"></view>
            <view class="locate-ring ring2" v-if="locating"></view>
          </view>
          <text class="target-text">{{ locating ? '定位中...' : '获取当前定位地址' }}</text>
        </view>

        <!-- 搜索结果列表 -->
        <scroll-view scroll-y class="poi-list">
          <view class="loading-poi" v-if="locating">
            <view class="loading-spinner"></view>
            <text>正在获取当前位置...</text>
          </view>

          <view class="loading-poi" v-else-if="searching">
            <view class="loading-spinner"></view>
            <text>正在搜索周边地址...</text>
          </view>

          <view 
            class="poi-item" 
            v-for="(item, index) in searchResults" 
            :key="index"
            @click="selectPoi(item)"
          >
            <view class="poi-left">
              <text class="poi-name">{{ item.name }}</text>
              <text class="poi-address">{{ item.address || item.district }}</text>
            </view>
            <text class="poi-choose">选择</text>
          </view>

          <view class="empty-poi" v-if="!searching && searchResults.length === 0 && searchKeyword">
            <text>未找到相关地址，可换个关键词或手动输入</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../api/index'
import { isLoggedIn } from '../../utils/auth'
import { getAmapConfig, amapWebRequest, signAmapParams } from '../../config/index'

export default {
  data() {
    return {
      id: null,
      submitting: false,
      form: {
        receiverName: '',
        phone: '',
        detailAddress: '',
        houseNumber: '',
        addressType: '家',
        province: '',
        city: '',
        district: '',
        latitude: null,
        longitude: null,
        isDefault: 0
      },
      typeOptions: ['家', '公司', '学校'],
      showSearchModal: false,
      searchKeyword: '',
      searching: false,
      locating: false,
      searchResults: [],
      searchTimer: null,
      // 城市选择器相关
      cityOptions: [
        '全国',
        '北京', '上海', '广州', '深圳', '杭州', '南京', '成都', '重庆',
        '武汉', '西安', '苏州', '天津', '长沙', '郑州', '青岛', '大连',
        '宁波', '厦门', '福州', '济南', '合肥', '沈阳', '哈尔滨', '昆明',
        '太原', '贵阳', '南宁', '兰州', '乌鲁木齐', '拉萨', '呼和浩特',
        '石家庄', '南昌', '长春', '温州', '佛山', '东莞', '无锡', '珠海'
      ],
      selectedCity: '全国',
      cityIndex: 0,
      // 城市模拟数据映射（用于高德 API 不可用时的降级展示）
      cityMeta: {
        '北京':  { pname: '北京市',   adname: '海淀区',   lat: 39.9928, lng: 116.3125, roads: ['中关村北大街', '颐和园路', '学院路', '成府路'] },
        '上海':  { pname: '上海市',   adname: '浦东新区', lat: 31.2304, lng: 121.4737, roads: ['世纪大道', '陆家嘴环路', '张江高科技园区', '漕溪北路'] },
        '广州':  { pname: '广东省',   adname: '天河区',   lat: 23.1291, lng: 113.2644, roads: ['天河路', '体育西路', '珠江新城', '岗顶路'] },
        '深圳':  { pname: '广东省',   adname: '南山区',   lat: 22.5401, lng: 113.9523, roads: ['深南大道', '科苑路', '科技园路', '后海大道'] },
        '杭州':  { pname: '浙江省',   adname: '西湖区',   lat: 30.2590, lng: 120.1358, roads: ['文三路', '西湖大道', '武林路', '延安路'] },
        '南京':  { pname: '江苏省',   adname: '鼓楼区',   lat: 32.0603, lng: 118.7969, roads: ['中山路', '新街口', '湖南路', '中山北路'] },
        '成都':  { pname: '四川省',   adname: '武侯区',   lat: 30.5728, lng: 104.0668, roads: ['人民南路', '春熙路', '天府大道', '科华北路'] },
        '重庆':  { pname: '重庆市',   adname: '渝中区',   lat: 29.5630, lng: 106.5516, roads: ['解放碑步行街', '中山路', '上清寺路', '南滨路'] },
        '武汉':  { pname: '湖北省',   adname: '洪山区',   lat: 30.5928, lng: 114.3055, roads: ['珞喻路', '光谷大道', '中南路', '解放大道'] },
        '西安':  { pname: '陕西省',   adname: '雁塔区',   lat: 34.2258, lng: 108.9402, roads: ['小寨路', '科技路', '高新路', '长安路'] },
        '苏州':  { pname: '江苏省',   adname: '姑苏区',   lat: 31.2990, lng: 120.5853, roads: ['人民路', '观前街', '干将路', '三香路'] },
        '天津':  { pname: '天津市',   adname: '南开区',   lat: 39.1422, lng: 117.1767, roads: ['南京路', '鞍山西道', '海光寺', '卫津路'] },
        '长沙':  { pname: '湖南省',   adname: '芙蓉区',   lat: 28.2282, lng: 112.9388, roads: ['五一大道', '解放东路', '芙蓉路', '中山路'] },
        '郑州':  { pname: '河南省',   adname: '金水区',   lat: 34.7466, lng: 113.6254, roads: ['花园路', '农业路', '金水大道', '经三路'] },
        '青岛':  { pname: '山东省',   adname: '市南区',   lat: 36.0671, lng: 120.3826, roads: ['香港中路', '中山路', '栈桥路', '东海西路'] },
        '大连':  { pname: '辽宁省',   adname: '中山区',   lat: 38.9140, lng: 121.6147, roads: ['人民路', '中山路', '三八广场', '港湾广场'] },
        '宁波':  { pname: '浙江省',   adname: '海曙区',   lat: 29.8683, lng: 121.5440, roads: ['中山路', '药行街', '天一广场', '解放路'] },
        '厦门':  { pname: '福建省',   adname: '思明区',   lat: 24.4798, lng: 118.0894, roads: ['中山路', '思明南路', '厦禾路', '莲前西路'] },
        '福州':  { pname: '福建省',   adname: '鼓楼区',   lat: 26.0745, lng: 119.2965, roads: ['东街口', '五四路', '中山路', '杨桥路'] },
        '济南':  { pname: '山东省',   adname: '历下区',   lat: 36.6512, lng: 117.1201, roads: ['泉城路', '泺源大街', '解放路', '花园路'] },
        '合肥':  { pname: '安徽省',   adname: '庐阳区',   lat: 31.8612, lng: 117.2830, roads: ['长江中路', '淮河路', '徽州大道', '金寨路'] },
        '沈阳':  { pname: '辽宁省',   adname: '和平区',   lat: 41.8057, lng: 123.4315, roads: ['太原街', '中华路', '南京街', '文化路'] },
        '哈尔滨':{ pname: '黑龙江省', adname: '南岗区',   lat: 45.7861, lng: 126.5349, roads: ['中山路', '红军街', '果戈里大街', '东大直街'] },
        '昆明':  { pname: '云南省',   adname: '五华区',   lat: 25.0389, lng: 102.7183, roads: ['人民路', '正义路', '金碧路', '翠湖南路'] },
        '太原':  { pname: '山西省',   adname: '迎泽区',   lat: 37.8649, lng: 112.5489, roads: ['解放路', '迎泽大街', '府西街', '柳巷'] },
        '贵阳':  { pname: '贵州省',   adname: '云岩区',   lat: 26.5783, lng: 106.7135, roads: ['中华路', '延安路', '遵义路', '宝山路'] },
        '南宁':  { pname: '广西壮族自治区', adname: '青秀区', lat: 22.8170, lng: 108.3665, roads: ['民族大道', '朝阳路', '七星路', '园湖南路'] },
        '兰州':  { pname: '甘肃省',   adname: '城关区',   lat: 36.0611, lng: 103.8343, roads: ['东方红广场', '张掖路', '东岗西路', '定西路'] },
        '乌鲁木齐': { pname: '新疆维吾尔自治区', adname: '天山区', lat: 43.8256, lng: 87.6168, roads: ['人民路', '中山路', '解放南路', '光明路'] },
        '拉萨':  { pname: '西藏自治区', adname: '城关区',   lat: 29.6520, lng: 91.1721, roads: ['宇拓路', '八廓街', '北京东路', '林廓东路'] },
        '呼和浩特': { pname: '内蒙古自治区', adname: '新城区', lat: 40.8428, lng: 111.7492, roads: ['新华大街', '中山路', '锡林郭勒北路', '大学路'] },
        '石家庄':{ pname: '河北省',   adname: '长安区',   lat: 38.0428, lng: 114.5149, roads: ['中山路', '广安大街', '中山东路', '建设大街'] },
        '南昌':  { pname: '江西省',   adname: '东湖区',   lat: 28.6820, lng: 115.8579, roads: ['八一大道', '中山路', '叠山路', '南京西路'] },
        '长春':  { pname: '吉林省',   adname: '朝阳区',   lat: 43.8171, lng: 125.3235, roads: ['重庆路', '人民广场', '同志街', '解放大路'] },
        '温州':  { pname: '浙江省',   adname: '鹿城区',   lat: 28.0006, lng: 120.6722, roads: ['人民路', '解放街', '五马街', '府前街'] },
        '佛山':  { pname: '广东省',   adname: '禅城区',   lat: 23.0218, lng: 113.1219, roads: ['祖庙路', '人民路', '普澜路', '汾江路'] },
        '东莞':  { pname: '广东省',   adname: '莞城区',   lat: 23.0430, lng: 113.7633, roads: ['莞太路', '鸿福路', '解放路', '旗峰路'] },
        '无锡':  { pname: '江苏省',   adname: '梁溪区',   lat: 31.4912, lng: 120.3119, roads: ['中山路', '人民路', '解放南路', '学前东路'] },
        '珠海':  { pname: '广东省',   adname: '香洲区',   lat: 22.2710, lng: 113.5767, roads: ['凤凰路', '人民路', '海滨南路', '九洲大道'] }
      }
    }
  },
  onLoad(options) {
    if (!isLoggedIn()) {
      uni.navigateTo({ url: '/pages/login' })
      return
    }
    if (options && options.id) {
      this.id = options.id
      uni.setNavigationBarTitle({ title: '编辑收货地址' })
      this.loadAddressDetail(this.id)
    } else {
      uni.setNavigationBarTitle({ title: '新增收货地址' })
    }
  },
  methods: {
    async loadAddressDetail(id) {
      try {
        uni.showLoading({ title: '加载中...' })
        const data = await api.getAddressDetail(id)
        if (data) {
          this.form = {
            id: data.id,
            receiverName: data.receiverName || '',
            phone: data.phone || '',
            detailAddress: data.detailAddress || '',
            houseNumber: data.houseNumber || '',
            addressType: data.addressType || '家',
            province: data.province || '',
            city: data.city || '',
            district: data.district || '',
            latitude: data.latitude || null,
            longitude: data.longitude || null,
            isDefault: data.isDefault || 0
          }
        }
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    onDefaultChange(e) {
      this.form.isDefault = e.detail.value ? 1 : 0
    },

    // 唤起地图选址
    openLocationPicker() {
      // #ifdef MP-WEIXIN || APP-PLUS
      // 小程序和App原生支持 uni.chooseLocation 直接调起地图选址和搜索
      uni.chooseLocation({
        success: (res) => {
          this.applyLocationResult({
            name: res.name,
            address: res.address,
            latitude: res.latitude,
            longitude: res.longitude
          })
        },
        fail: (err) => {
          if (err.errMsg && (err.errMsg.includes('auth deny') || err.errMsg.includes('authorize'))) {
            uni.showModal({
              title: '授权提示',
              content: '需要获取您的地理位置权限以选择地址',
              success: (modalRes) => {
                if (modalRes.confirm) {
                  uni.openSetting()
                }
              }
            })
          } else {
            // 降级使用内置搜索弹窗
            this.openSearchModal()
          }
        }
      })
      // #endif

      // #ifndef MP-WEIXIN || APP-PLUS
      // H5 / 网页端直接使用内置地图定位与搜索弹窗
      this.openSearchModal()
      // #endif
    },

    openSearchModal() {
      this.showSearchModal = true
      this.searchKeyword = ''
      this.searchResults = []
      // 重置为全国，然后自动尝试定位获取当前城市
      this.selectedCity = '全国'
      this.cityIndex = 0
      this.autoDetectCity()
    },

    // 自动检测当前城市 (弹窗打开时调用, 带定位动画)
    async autoDetectCity() {
      this.locating = true
      try {
        // 优先: uni.getLocation, 加手动超时兜底 (某些浏览器/环境下 uni.getLocation 会卡住不回调)
        const locationRes = await new Promise((resolve) => {
          let done = false
          const timeoutId = setTimeout(() => {
            if (!done) { done = true; resolve(null) }
          }, 5000) // 5 秒超时强制降级

          uni.getLocation({
            type: 'gcj02',
            geocode: true,
            success: (res) => {
              if (!done) { done = true; clearTimeout(timeoutId); resolve(res) }
            },
            fail: () => {
              if (!done) { done = true; clearTimeout(timeoutId); resolve(null) }
            }
          })
        })

        // 有经纬度 → 用高德逆地理编码拿到精确地址
        if (locationRes && locationRes.latitude && locationRes.longitude) {
          try {
            const regeo = await amapWebRequest('/v3/geocode/regeo', {
              location: `${locationRes.longitude},${locationRes.latitude}`,
              keywords: '',
              extensions: 'base',
              radius: 1000
            })
            if (regeo && regeo.regeocode) {
              const addr = regeo.regeocode.addressComponent
              const cityName = addr.city || addr.province || ''
              console.log('[定位] 逆地理编码成功, 城市:', cityName)
              this.setCityFromLocation(cityName)
              return
            }
          } catch (err) {
            console.warn('[定位] 逆地理编码失败, 降级 IP 定位:', err.message)
          }
        }

        // 无经纬度或逆地理编码失败 → 高德 IP 定位兜底
        console.log('[定位] 尝试 IP 定位...')
        const ipData = await amapWebRequest('/v3/ip', {})
        if (ipData && ipData.city) {
          console.log('[定位] IP 定位成功, 城市:', ipData.city)
          this.setCityFromLocation(ipData.city)
        }
      } catch (err) {
        console.warn('[定位] 自动检测城市失败:', err.message)
      } finally {
        this.locating = false
      }
    },

    // 用户点击 "获取当前定位地址" 按钮
    async fetchCurrentLocation() {
      this.locating = true
      uni.showLoading({ title: '定位中...' })

      try {
        // 1. 先尝试 uni.getLocation (最精准), 加手动超时兜底
        const locationRes = await new Promise((resolve) => {
          let done = false
          const timeoutId = setTimeout(() => {
            if (!done) { done = true; resolve(null) }
          }, 5000) // 5 秒超时强制降级

          uni.getLocation({
            type: 'gcj02',
            geocode: true,
            success: (res) => {
              if (!done) { done = true; clearTimeout(timeoutId); resolve(res) }
            },
            fail: () => {
              if (!done) { done = true; clearTimeout(timeoutId); resolve(null) }
            }
          })
        })

        // 有经纬度 → 用逆地理编码拿精确地址
        if (locationRes && locationRes.latitude && locationRes.longitude) {
          const lng = locationRes.longitude
          const lat = locationRes.latitude
          console.log('[定位] 经纬度:', lng, lat)

          try {
            const regeo = await amapWebRequest('/v3/geocode/regeo', {
              location: `${lng},${lat}`,
              keywords: '',
              extensions: 'base',
              radius: 1000
            })
            if (regeo && regeo.regeocode) {
              const addr = regeo.regeocode.addressComponent
              const addressComponent = {
                province: addr.province || '',
                city: addr.city || addr.province || '',
                district: addr.district || '',
                street: addr.street || '',
                building: addr.building || '',
                neighborhood: addr.neighborhood || ''
              }

              // 更新城市下拉框
              this.setCityFromLocation(addressComponent.city)

              // 用逆地理的 formatted_address 作为定位结果
              const formattedAddr = regeo.regeocode.formatted_address ||
                `${addressComponent.province}${addressComponent.city}${addressComponent.district}${addressComponent.street}`

              this.searchResults = [{
                name: addressComponent.neighborhood || addressComponent.building || addressComponent.street || '当前所在位置',
                address: formattedAddr,
                pname: addressComponent.province,
                cityname: addressComponent.city,
                adname: addressComponent.district,
                latitude: lat,
                longitude: lng
              }]

              console.log('[定位] 成功, 城市:', addressComponent.city)
              uni.showToast({ title: '定位成功', icon: 'success' })
              return
            }
          } catch (err) {
            console.warn('[定位] 逆地理编码失败:', err.message)
            // 降级: 用 uni.getLocation 返回的 address (如果有)
            if (locationRes.address) {
              const address = locationRes.address
              this.setCityFromLocation(address.city || address.province)
              this.searchResults = [{
                name: address.street || address.poiName || '当前所在位置',
                address: `${address.province || ''}${address.city || ''}${address.district || ''}${address.street || ''}`,
                pname: address.province || '',
                cityname: address.city || '',
                adname: address.district || '',
                latitude: lat,
                longitude: lng
              }]
              uni.showToast({ title: '定位成功', icon: 'success' })
              return
            }
          }
        }

        // 2. 没经纬度 (电脑没 GPS) → IP 定位兜底
        console.log('[定位] 无 GPS, 尝试 IP 定位...')
        const ipData = await amapWebRequest('/v3/ip', {})
        if (ipData && ipData.city) {
          this.setCityFromLocation(ipData.city)

          // 用 IP 定位的城市中心坐标构造一个定位 POI
          const adcode = ipData.adcode
          // 用 geocode 接口把 adcode 转成城市中心经纬度
          let centerLng = null
          let centerLat = null
          try {
            const geo = await amapWebRequest('/v3/geocode/geo', {
              address: ipData.city,
              city: ipData.city
            })
            if (geo && geo.geocodes && geo.geocodes[0]) {
              const loc = geo.geocodes[0].location.split(',')
              centerLng = parseFloat(loc[0])
              centerLat = parseFloat(loc[1])
            }
          } catch (e) {
            console.warn('[定位] geocode 降级失败:', e.message)
          }

          this.searchResults = [{
            name: `当前位置 (${ipData.city})`,
            address: ipData.province + ipData.city,
            pname: ipData.province,
            cityname: ipData.city,
            adname: '',
            latitude: centerLat,
            longitude: centerLng
          }]

          console.log('[定位] IP 定位成功, 城市:', ipData.city)
          uni.showToast({ title: '定位成功', icon: 'success' })
          return
        }

        // 3. 所有方式都失败
        uni.showToast({ title: '定位失败, 请手动输入', icon: 'none' })
      } catch (err) {
        console.error('[定位] 最终失败:', err.message)
        uni.showToast({ title: '定位失败, 请手动输入', icon: 'none' })
      } finally {
        uni.hideLoading()
        this.locating = false
      }
    },

    // 根据定位返回的城市名自动匹配下拉选项
    setCityFromLocation(cityName) {
      if (!cityName) return
      const trimmed = String(cityName).replace(/市$/, '').trim()
      const idx = this.cityOptions.indexOf(trimmed)
      if (idx > 0) {
        this.selectedCity = this.cityOptions[idx]
        this.cityIndex = idx
        console.log('[定位] 城市下拉框已更新为:', this.selectedCity)
      } else {
        console.log('[定位] 城市 "' + trimmed + '" 不在预设选项中, 保持 "全国"')
      }
    },

    // 城市下拉选择变更
    onCityChange(e) {
      this.cityIndex = e.detail.value
      this.selectedCity = this.cityOptions[this.cityIndex]
      if (this.searchKeyword.trim()) {
        this.executeSearch()
      }
    },

    clearSearch() {
      this.searchKeyword = ''
      this.searchResults = []
    },

    handleSearchInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.executeSearch()
      }, 400)
    },

    // 执行地图 POI 搜索 (全平台统一走高德 Web 服务 API)
    async executeSearch() {
      const kw = (this.searchKeyword || '').trim()
      if (!kw) {
        this.searchResults = []
        return
      }
      this.searching = true
      try {
        const isNationwide = this.selectedCity === '全国'
        const params = {
          keywords: kw,
          city: isNationwide ? undefined : this.selectedCity,
          citylimit: !isNationwide,
          offset: 20,
          page: 1
        }
        Object.keys(params).forEach(k => params[k] === undefined && delete params[k])

        const data = await amapWebRequest('/v3/place/text', params)
        if (data.pois && data.pois.length > 0) {
          const sc = this.selectedCity
          this.searchResults = data.pois.map((poi) => {
            const loc = (poi.location || '').split(',')
            return {
              name: poi.name,
              address: this.normalizePoiAddress(poi, sc),
              pname: poi.pname || '',
              cityname: poi.cityname || '',
              adname: poi.adname || '',
              longitude: loc[0] ? parseFloat(loc[0]) : null,
              latitude: loc[1] ? parseFloat(loc[1]) : null
            }
          })
          console.log('[高德] POI 搜索成功, 共', this.searchResults.length, '条')
        } else {
          this.fallbackSearch(kw)
        }
      } catch (err) {
        console.warn('[高德] POI 搜索失败, 降级:', err.message)
        this.fallbackSearch(kw)
      } finally {
        this.searching = false
      }
    },

    // 清洗高德返回的 POI 地址, 去掉硬拼进去的"全国"前缀, 用真实行政区划重建
    normalizePoiAddress(poi, selectedCity) {
      const raw = (poi.address && typeof poi.address === 'string') ? poi.address : ''
      const pname = poi.pname || ''
      const cityname = poi.cityname || ''
      const adname = poi.adname || ''
      const realCity = cityname || pname

      let cleaned = raw
      if (selectedCity && raw.startsWith(selectedCity) && realCity !== selectedCity) {
        cleaned = raw.substring(selectedCity.length).replace(/^[、,\s]+/, '')
      }

      if (pname && !cleaned.startsWith(pname) && cleaned) {
        let detailStart = 0
        if (adname && cleaned.startsWith(adname)) {
          detailStart = adname.length
        } else if (cityname && cityname !== pname && cleaned.startsWith(cityname)) {
          detailStart = cityname.length
        }
        const detail = cleaned.substring(detailStart).replace(/^[、,\s]+/, '')
        const parts = [pname, cityname, adname].filter(Boolean)
        const deduped = []
        for (const p of parts) {
          if (deduped.length === 0 || deduped[deduped.length - 1] !== p) {
            deduped.push(p)
          }
        }
        cleaned = deduped.join('') + detail
      }

      return cleaned || poi.name || ''
    },

    // 高德 API 不可用时的降级模拟数据
    fallbackSearch(kw) {
      if (!kw) return
      const city = this.selectedCity || '全国'
      const meta = this.cityMeta[city]
      const base = meta || {
        pname: '中国', adname: '', lat: 39.9042, lng: 116.4074,
        roads: ['主干道', '人民路', '中山路', '解放路']
      }
      const offsetLat = (d) => Number((base.lat + d).toFixed(4))
      const offsetLng = (d) => Number((base.lng + d).toFixed(4))
      const pickRoad = (idx) => base.roads[idx % base.roads.length]

      this.searchResults = [
        { name: `${kw}`, address: `${base.adname ? base.pname.replace(/市$/, '') + base.adname : base.pname} ${kw}`, pname: base.pname, cityname: city === '全国' ? '' : city, adname: base.adname, latitude: offsetLat(0), longitude: offsetLng(0) },
        { name: `${kw} (1号院/东区)`, address: `${base.pname.replace(/市$/, '')}${base.adname || ''} ${pickRoad(0)} ${kw} 1号院`, pname: base.pname, cityname: city === '全国' ? '' : city, adname: base.adname, latitude: offsetLat(0.0012), longitude: offsetLng(0.0018) },
        { name: `${kw} (2号院/西区)`, address: `${base.pname.replace(/市$/, '')}${base.adname || ''} ${pickRoad(1)} ${kw} 2号院`, pname: base.pname, cityname: city === '全国' ? '' : city, adname: base.adname, latitude: offsetLat(-0.0015), longitude: offsetLng(-0.0022) },
        { name: `${kw} - 正门入口`, address: `${base.pname.replace(/市$/, '')}${base.adname || ''} ${kw} 正门南侧`, pname: base.pname, cityname: city === '全国' ? '' : city, adname: base.adname, latitude: offsetLat(0.0008), longitude: offsetLng(0.0005) }
      ]

      if (!this._fallbackNotified) {
        this._fallbackNotified = true
        setTimeout(() => {
          uni.showToast({ title: '地图服务暂不可用, 展示模拟数据', icon: 'none', duration: 2000 })
        }, 300)
      }
    },

    selectPoi(item) {
      this.applyLocationResult({
        name: item.name,
        address: item.address,
        pname: item.pname,
        cityname: item.cityname,
        adname: item.adname,
        latitude: item.latitude,
        longitude: item.longitude
      })
      this.showSearchModal = false
    },

    applyLocationResult(data) {
      const fullDetail = (data.address && data.address !== data.name)
        ? `${data.address} (${data.name})`
        : (data.address || data.name)

      this.form.detailAddress = fullDetail
      if (data.pname) this.form.province = data.pname
      if (data.cityname) this.form.city = data.cityname
      if (data.adname) this.form.district = data.adname
      if (data.latitude) this.form.latitude = data.latitude
      if (data.longitude) this.form.longitude = data.longitude
      uni.showToast({ title: '已选择地址', icon: 'success' })
    },

    async handleSubmit() {
      const receiverName = (this.form.receiverName || '').trim()
      const phone = (this.form.phone || '').trim()
      const detailAddress = (this.form.detailAddress || '').trim()
      const houseNumber = (this.form.houseNumber || '').trim()

      if (!receiverName) {
        uni.showToast({ title: '请输入收货人姓名', icon: 'none' })
        return
      }
      if (receiverName.length > 20) {
        uni.showToast({ title: '收货人姓名不能超过20个字', icon: 'none' })
        return
      }
      if (!phone) {
        uni.showToast({ title: '请输入手机号码', icon: 'none' })
        return
      }
      const phoneRegex = /^1[3-9]\d{9}$/
      if (!phoneRegex.test(phone)) {
        uni.showToast({ title: '手机号码格式不正确', icon: 'none' })
        return
      }
      if (!detailAddress) {
        uni.showToast({ title: '请选择或输入详细地址', icon: 'none' })
        return
      }

      const payload = {
        id: this.id ? Number(this.id) : undefined,
        receiverName,
        phone,
        detailAddress,
        houseNumber,
        addressType: this.form.addressType || '家',
        province: this.form.province,
        city: this.form.city,
        district: this.form.district,
        latitude: this.form.latitude,
        longitude: this.form.longitude,
        isDefault: this.form.isDefault
      }

      this.submitting = true
      try {
        if (this.id) {
          await api.updateAddress(payload)
          uni.showToast({ title: '修改成功', icon: 'success' })
        } else {
          await api.addAddress(payload)
          uni.showToast({ title: '保存成功', icon: 'success' })
        }
        setTimeout(() => {
          uni.navigateBack()
        }, 800)
      } catch (e) {
        uni.showToast({ title: e.message || '保存失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.edit-page {
  min-height: 100vh;
  background: #f8f8f8;
  padding: 24rpx;
  box-sizing: border-box;
  padding-bottom: 160rpx;
}

.form-container {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 0 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-item.align-top {
  align-items: flex-start;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  width: 160rpx;
  font-size: 28rpx;
  color: #333333;
  font-weight: 500;
  flex-shrink: 0;
}

.required {
  color: #ff4d4f;
  margin-right: 6rpx;
  font-weight: bold;
}

.mb-0 {
  margin-bottom: 0;
}

.input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  position: relative;
}

.form-input {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
  height: 60rpx;
}

.char-count {
  font-size: 24rpx;
  color: #999999;
  margin-left: 12rpx;
}

.location-input-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.location-bar {
  display: flex;
  align-items: center;
  background: #f0f8f0;
  border: 1rpx solid #c8e6c9;
  padding: 14rpx 20rpx;
  border-radius: 10rpx;
  cursor: pointer;
  align-self: flex-start;
  gap: 8rpx;
}

.location-icon {
  font-size: 28rpx;
}

.location-btn-text {
  font-size: 26rpx;
  color: #2e7d32;
  font-weight: 500;
}

.form-textarea {
  width: 100%;
  min-height: 100rpx;
  font-size: 28rpx;
  color: #333333;
  line-height: 1.4;
  box-sizing: border-box;
}

.tags-group {
  display: flex;
  gap: 20rpx;
}

.tag-pill {
  padding: 10rpx 32rpx;
  border-radius: 30rpx;
  border: 1rpx solid #d9d9d9;
  font-size: 26rpx;
  color: #666666;
  background: #fafafa;
  cursor: pointer;
}

.tag-pill.active {
  border-color: #4CAF50;
  background: #e8f5e9;
  color: #2e7d32;
  font-weight: 500;
}

.switch-item {
  justify-content: space-between;
}

.switch-left {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.switch-desc {
  font-size: 22rpx;
  color: #999999;
}

.save-bar {
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

.save-btn {
  background: #4CAF50;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 44rpx;
  height: 88rpx;
  line-height: 88rpx;
  border: none;
}

.save-btn::after {
  border: none;
}

/* 地图搜索弹窗样式 */
.map-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.map-modal-card {
  background: #ffffff;
  border-radius: 24rpx 24rpx 0 0;
  height: 80vh;
  display: flex;
  flex-direction: column;
  padding: 30rpx;
  box-sizing: border-box;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.modal-close {
  font-size: 36rpx;
  color: #999999;
  cursor: pointer;
  padding: 8rpx;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.city-picker {
  flex-shrink: 0;
}

.city-picker-box {
  display: flex;
  align-items: center;
  gap: 4rpx;
  background: #4CAF50;
  color: #ffffff;
  padding: 16rpx 20rpx;
  border-radius: 36rpx;
  white-space: nowrap;
  min-width: 110rpx;
  justify-content: center;
}

.city-text {
  font-size: 26rpx;
  font-weight: 500;
  color: #ffffff;
}

.city-arrow {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-left: 2rpx;
}

.search-input-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 36rpx;
  padding: 16rpx 24rpx;
  gap: 12rpx;
}

.search-icon {
  font-size: 28rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
}

.clear-btn {
  font-size: 28rpx;
  color: #999999;
  cursor: pointer;
}

.current-location-btn {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  color: #4CAF50;
  font-size: 26rpx;
  cursor: pointer;
  gap: 8rpx;
  border-bottom: 1rpx solid #f0f0f0;
  margin-bottom: 12rpx;
  transition: opacity 0.2s;
}

.current-location-btn.locating {
  opacity: 0.7;
  pointer-events: none;
}

.location-icon-wrap {
  position: relative;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.target-icon {
  font-size: 30rpx;
  position: relative;
  z-index: 2;
}

/* 定位脉冲动画 — 双层涟漪 */
.locate-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 3rpx solid #4CAF50;
  transform: translate(-50%, -50%);
  z-index: 1;
  animation: locate-pulse 1.5s ease-out infinite;
}

.locate-ring.ring2 {
  animation-delay: 0.75s;
}

@keyframes locate-pulse {
  0% {
    width: 36rpx;
    height: 36rpx;
    opacity: 0.8;
  }
  100% {
    width: 100rpx;
    height: 100rpx;
    opacity: 0;
  }
}

/* 通用 loading 旋转动画 */
.loading-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 4rpx solid #e0e0e0;
  border-top-color: #4CAF50;
  border-radius: 50%;
  animation: spinner-rotate 0.8s linear infinite;
  flex-shrink: 0;
}

@keyframes spinner-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.poi-list {
  flex: 1;
  min-height: 0;
}

.poi-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f4f4f4;
  cursor: pointer;
}

.poi-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  padding-right: 20rpx;
}

.poi-name {
  font-size: 30rpx;
  color: #333333;
  font-weight: 500;
}

.poi-address {
  font-size: 24rpx;
  color: #888888;
}

.poi-choose {
  font-size: 26rpx;
  color: #4CAF50;
  font-weight: 500;
}

.loading-poi {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
  padding: 60rpx 0;
  font-size: 26rpx;
  color: #999999;
}

.empty-poi {
  text-align: center;
  padding: 60rpx 0;
  font-size: 26rpx;
  color: #999999;
}
</style>
