import request from '../utils/request'

export default {
  // Banner
  getBannerList() {
    return request.get('/banner/list')
  },

  // File
  getFileConfig() {
    return request.get('/file/config')
  },

  // Category
  getCategoryList() {
    return request.get('/category/list')
  },

  // Product
  getProductList() {
    return request.get('/product/list')
  },

  getHotsellingList(params) {
    return request.get('/product/hotselling', params)
  },

  getProductByCategory(categoryId) {
    return request.get(`/product/category/${categoryId}`)
  },

  getProductDetail(id) {
    return request.get(`/product/${id}`)
  },

  getProductPage(params) {
    return request.get('/product/page', params)
  },

  // User Auth & Info
  login(data) {
    return request.post('/user/login', data)
  },

  register(data) {
    return request.post('/user/register', data)
  },

  getUserInfo() {
    return request.get('/user/info')
  },

  // Shopping Cart
  getCartList() {
    return request.get('/cart/list')
  },

  getCartCount() {
    return request.get('/cart/count')
  },

  addToCart(data) {
    return request.post('/cart/add', data)
  },

  updateCartQuantity(data) {
    return request.post('/cart/update', data)
  },

  changeCartQuantity(data) {
    return request.post('/cart/change', data)
  },

  deleteCartByProduct(productId) {
    return request.delete(`/cart/product/${productId}`)
  },

  deleteCartItem(id) {
    return request.delete(`/cart/${id}`)
  },

  clearCart() {
    return request.delete('/cart/clear')
  },

  // Logistics & Config
  getLogisticsList() {
    return request.get('/logistics/list')
  },

  getConfig(key) {
    return request.get(`/config/${key}`)
  },

  // Order
  createOrder(data) {
    return request.post('/order/create', data)
  },

  getOrderList(params) {
    return request.get('/order/list', params)
  },

  getOrderDetail(id) {
    return request.get(`/order/${id}`)
  }
}
