import Vue from 'vue'
import Vuex from 'vuex'
import request from '@/utils/request'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    user: JSON.parse(localStorage.getItem('user')) || null,
    cartItems: [],
    categories: [],
    products: [],
    selectedAddress: null,
    orders: [],
    favorites: [],
    currentOrder: null
  },
  mutations: {
    setUser(state, user) {
      state.user = user
      if (user) {
        localStorage.setItem('user', JSON.stringify(user))
      } else {
        localStorage.removeItem('user')
      }
    },
    setCartItems(state, items) {
      state.cartItems = items
    },
    addToCart(state, product) {
      state.cartItems.push(product)
    },
    removeFromCart(state, productId) {
      state.cartItems = state.cartItems.filter(item => item.productId !== productId)
    },
    setCategories(state, categories) {
      state.categories = categories
    },
    setProducts(state, products) {
      state.products = products
    },
    clearUser(state) {
      state.user = null
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    },
    updateCartItem(state, { productId, quantity }) {
      const item = state.cartItems.find(item => item.productId === productId)
      if (item) {
        item.quantity = quantity
      }
    },
    setSelectedAddress(state, address) {
      state.selectedAddress = address
    },
    setOrders(state, orders) {
      state.orders = orders
    },
    setFavorites(state, favorites) {
      state.favorites = favorites
    },
    setCurrentOrder(state, order) {
      state.currentOrder = order
    },
    updateCartSelected(state, { productId, selected }) {
      const item = state.cartItems.find(item => item.productId === productId)
      if (item) {
        item.selected = selected
      }
    },
    updateCartItemsSelected(state, selected) {
      state.cartItems.forEach(item => {
        item.selected = selected
      })
    }
  },
  actions: {
    async login({ commit }, credentials) {
      try {
        const params = new URLSearchParams()
        params.append('username', credentials.username)
        params.append('password', credentials.password)

        const response = await request.post('/api/user/login', params, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })

        console.log('登录响应:', response)

        if (!response || response.code !== 200) {
          throw new Error(response.message || '登录失败：服务器响应错误')
        }

        if (!response.data) {
          throw new Error('登录失败：响应数据为空')
        }

        const userInfo = response.data
        
        const token = `Bearer ${btoa(JSON.stringify(userInfo))}`
        
        localStorage.setItem('token', token)
        commit('setUser', userInfo)
        
        return userInfo
      } catch (error) {
        console.error('登录失败:', error)
        throw error
      }
    },

    async register(_, userData) {
      const response = await request.post('/api/user/register', userData)
      return response
    },

    async fetchCategories({ commit }) {
      try {
        const response = await request.get('/api/category/list')
        console.log('分类API返回:', response)
        
        // 从response.data中获取分类数组
        const categories = response.data || []
        commit('setCategories', categories)
        return categories
      } catch (error) {
        console.error('获取分类列表失败:', error)
        throw error
      }
    },

    async fetchProducts({ commit }, params) {
      try {
        const defaultParams = {
          current: 1,     // 当前页码
          size: 10,       // 每页数量
          status: 1       // 商品状态：1-上架
        }
        
        const queryParams = { ...defaultParams, ...params }
        const response = await request.get('/api/product/list', { params: queryParams })
        console.log('API返回数据:', response)
        
        const products = response.data.records || []
        
        if (!params || !params.current) {
          commit('setProducts', products)
        }
        return response.data
      } catch (error) {
        console.error('获取商品列表失败:', error)
        throw error
      }
    },

    async addToCart({ commit, state }, { productId, quantity }) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        const response = await request.post('/api/cart/add', {
          userId: state.user.id || state.user.userId,
          productId,
          quantity
        })

        if (response.code === 200) {
          commit('addToCart', response.data)
          return response.data
        } else {
          throw new Error(response.message || '添加购物车失败')
        }
      } catch (error) {
        console.error('添加购物车失败:', error)
        throw error
      }
    },

    async logout({ commit }) {
      // 移除用户信息和token
      commit('clearUser')
      // 清空购物车
      commit('setCartItems', [])
      // 清空订单列表
      commit('setOrders', [])
      // 清空收藏列表
      commit('setFavorites', [])
      // 清空当前订单
      commit('setCurrentOrder', null)
      // 清空选中的地址
      commit('setSelectedAddress', null)
    },

    async fetchProductDetail(_, id) {
      try {
        const response = await request.get(`/api/product/detail/${id}`)
        console.log('商品详情API返回:', response)
        return response
      } catch (error) {
        console.error('获取商品详情失败:', error)
        throw error
      }
    },

    async updateCartItem({ commit, state }, { productId, quantity }) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        const response = await request.put('/api/cart/update', null, {
          params: {
            userId: state.user.id || state.user.userId,
            productId,
            quantity
          }
        })

        if (response.code === 200) {
          commit('updateCartItem', { productId, quantity })
          return response.data
        } else {
          throw new Error(response.message || '更新购物车失败')
        }
      } catch (error) {
        console.error('更新购物车失败:', error)
        throw error
      }
    },

    async removeFromCart({ commit, state }, productId) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        const response = await request.delete('/api/cart/delete', {
          params: {
            userId: state.user.id || state.user.userId,
            productId
          }
        })

        if (response.code === 200) {
          commit('removeFromCart', productId)
          return response.data
        } else {
          throw new Error(response.message || '删除失败')
        }
      } catch (error) {
        console.error('删除购物车商品失败:', error)
        throw error
      }
    },

    async fetchAddresses({ state }) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        const response = await request.get('/api/address/list', {
          params: {
            userId: state.user.id || state.user.userId
          }
        })

        if (response.code === 200) {
          return response.data || []
        } else {
          throw new Error(response.message || '获取地址列表失败')
        }
      } catch (error) {
        console.error('获取地址列表失败:', error)
        throw error
      }
    },

    async saveAddress({ state }, address) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        // 添加用户ID
        address.userId = state.user.id || state.user.userId

        const response = await request.post('/api/address/save', address)
        return response
      } catch (error) {
        console.error('保存地址失败:', error)
        throw error
      }
    },

    async deleteAddress({ state }, id) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        const response = await request.delete(`/api/address/${id}`, {
          params: {
            userId: state.user.id || state.user.userId
          }
        })
        return response
      } catch (error) {
        console.error('删除地址失败:', error)
        throw error
      }
    },

    async fetchAddress({ state }, id) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        const response = await request.get(`/api/address/${id}`, {
          params: {
            userId: state.user.id || state.user.userId
          }
        })
        return response
      } catch (error) {
        console.error('获取地址详情失败:', error)
        throw error
      }
    },

    async fetchCartItems({ commit, state }) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        const response = await request.get('/api/cart/list', {
          params: {
            userId: state.user.id || state.user.userId
          }
        })

        console.log('购物车列表原始响应:', response)

        if (response.code === 200) {
          const items = response.data || []
          console.log('购物车列表数据:', items)

          // 确保每个商品都有必要的属性
          const itemsWithSelected = items.map(item => {
            console.log('处理前的商品数据:', item)
            const processedItem = {
              ...item,
              id: item.id || item.cartId, // 确保有购物车ID
              productId: item.productId,
              quantity: parseInt(item.quantity || 1),
              price: parseFloat(item.price || item.productPrice || 0),
              selected: item.selected === true || item.selected === 'true' || item.selected === 1
            }
            console.log('处理后的商品数据:', processedItem)
            return processedItem
          })

          console.log('处理后的购物车列表:', itemsWithSelected)
          commit('setCartItems', itemsWithSelected)
          return itemsWithSelected
        } else {
          throw new Error(response.message || '获取购物车失败')
        }
      } catch (error) {
        console.error('获取购物车失败:', error)
        throw error
      }
    },

    async createOrder({ state, commit }, orderData) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        // 添加用户ID
        orderData.userId = state.user.id || state.user.userId

        // 确保有购物车ID数组
        if (!orderData.cartIds || !orderData.cartIds.length) {
          throw new Error('购物车ID不能为空')
        }

        // 添加订单金额
        orderData.totalAmount = orderData.totalAmount || 0

        console.log('创建订单请求数据:', orderData)
        const response = await request.post('/api/order/create', orderData)
        console.log('创建订单响应:', response)

        if (response.code === 200 && response.data) {
          // 创建订单成功后清空购物车
          commit('setCartItems', [])
          
          // 确保返回的订单数据包含必要字段
          const orderResult = {
            ...response.data,
            id: response.data.id || response.data.orderId,
            orderNo: response.data.orderNo || String(response.data.id || response.data.orderId),
            totalAmount: Number(response.data.totalAmount || orderData.totalAmount || 0)
          }
          
          console.log('处理后的订单数据:', orderResult)
          return orderResult
        } else {
          throw new Error(response.message || '创建订单失败')
        }
      } catch (error) {
        console.error('创建订单失败:', error)
        throw error
      }
    },

    async fetchOrder({ state, commit }, orderId) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        if (!orderId) {
          throw new Error('订单ID不能为空')
        }

        // 确保orderId是数字类型
        const id = parseInt(orderId)
        if (isNaN(id)) {
          throw new Error('无效的订单ID')
        }

        const response = await request.get(`/api/order/detail/${id}`, {
          params: {
            userId: state.user.id || state.user.userId
          }
        })

        if (response.code === 200) {
          const orderData = {
            ...response.data,
            totalAmount: Number(response.data.totalAmount || 0),
            orderNo: response.data.orderNo || String(id)
          }
          commit('setCurrentOrder', orderData)
          return orderData
        } else {
          throw new Error(response.message || '获取订单详情失败')
        }
      } catch (error) {
        console.error('获取订单详情失败:', error)
        throw error
      }
    },

    async payOrder({ state }, { orderId, payType, amount }) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        if (!orderId) {
          throw new Error('订单ID不能为空')
        }

        // 使用PUT方法和正确的URL格式
        const response = await request.put(`/api/order/${orderId}/pay`, null, {
          params: {
            userId: state.user.id || state.user.userId,
            payType,
            amount: Number(amount)
          }
        })

        console.log('支付响应:', response)

        if (response.code === 200) {
          return response.data
        } else {
          throw new Error(response.message || '支付失败')
        }
      } catch (error) {
        console.error('支付失败:', error)
        throw error
      }
    },

    async fetchBanners() {
      const banners = await request.get('/api/banner/list')
      return banners
    },

    async fetchOrders({ state, commit }, params = {}) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        // 确保有用户ID
        const queryParams = {
          ...params,
          userId: state.user.id || state.user.userId
        }

        console.log('获取订单列表参数:', queryParams)
        const response = await request.get('/api/order/list', {
          params: queryParams
        })
        console.log('获取订单列表响应:', response)

        if (response.code === 200) {
          const orders = response.data || {
            records: [],
            total: 0,
            size: params.size || 10,
            current: params.current || 1
          }
          commit('setOrders', orders.records)
          return orders
        } else {
          throw new Error(response.message || '获取订单列表失败')
        }
      } catch (error) {
        console.error('获取订单列表失败:', error)
        throw error
      }
    },

    async cancelOrder({ state }, orderId) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        const response = await request.post(`/api/order/cancel`, {
          orderId,
          userId: state.user.id || state.user.userId
        })

        if (response.code === 200) {
          return response.data
        } else {
          throw new Error(response.message || '取消订单失败')
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        throw error
      }
    },

    async confirmOrder({ state }, orderId) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        const response = await request.post(`/api/order/confirm`, {
          orderId,
          userId: state.user.id || state.user.userId
        })

        if (response.code === 200) {
          return response.data
        } else {
          throw new Error(response.message || '确认收货失败')
        }
      } catch (error) {
        console.error('确认收货失败:', error)
        throw error
      }
    },

    async fetchLogistics(_, orderId) {
      const logistics = await request.get(`/api/order/${orderId}/logistics`)
      return logistics
    },

    async uploadImage(_, formData) {
      const response = await request.post('/api/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      return response
    },

    async submitReviews(_, reviews) {
      await request.post('/api/review/submit', reviews)
    },

    async fetchFavorites({ commit }, params) {
      const favorites = await request.get('/api/favorite/list', { params })
      commit('setFavorites', favorites)
      return favorites
    },

    async addFavorite(_, productId) {
      await request.post('/api/favorite/add', { productId })
    },

    async cancelFavorite(_, favoriteId) {
      await request.delete(`/api/favorite/${favoriteId}`)
    },

    async updateCartSelected({ commit, state }, { productId, selected }) {
      try {
        if (!state.user) {
          throw new Error('请先登录')
        }

        const response = await request.put('/api/cart/selected', null, {
          params: {
            userId: state.user.id || state.user.userId,
            productId,
            selected
          }
        })

        if (response.code === 200) {
          // 不在这里更新本地状态，而是重新获取购物车数据
          return response.data
        } else {
          throw new Error(response.message || '更新选中状态失败')
        }
      } catch (error) {
        console.error('更新购物车商品选中状态失败:', error)
        throw error
      }
    }
  }
}) 