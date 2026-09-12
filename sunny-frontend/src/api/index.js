import request from '../utils/request'

export default {
  getBannerList() {
    return request.get('/banner/list')
  },

  getFileConfig() {
    return request.get('/file/config')
  },

  getCategoryList() {
    return request.get('/category/list')
  },

  getProductList() {
    return request.get('/product/list')
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

  getLogisticsList() {
    return request.get('/logistics/list')
  },

  getConfig(key) {
    return request.get(`/config/${key}`)
  },

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
