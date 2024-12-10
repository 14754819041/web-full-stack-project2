<template>
  <div class="order-confirm">
    <van-nav-bar
      title="确认订单"
      left-arrow
      @click-left="$router.back()"
    />

    <!-- 收货地址 -->
    <div class="address-card" @click="chooseAddress">
      <template v-if="selectedAddress">
        <div class="address-info">
          <div class="user-info">
            <span class="name">{{ selectedAddress.name }}</span>
            <span class="phone">{{ selectedAddress.phone }}</span>
          </div>
          <div class="address">{{ selectedAddress.address }}</div>
        </div>
      </template>
      <div v-else class="no-address">
        <van-icon name="add" />
        添加收货地址
      </div>
      <van-icon name="arrow" class="arrow" />
    </div>

    <!-- 商品列表 -->
    <div class="product-list">
      <div 
        class="product-item" 
        v-for="item in orderItems" 
        :key="item.productId"
      >
        <van-card
          :num="item.quantity"
          :price="Number(item.price).toFixed(2)"
          :title="item.name || item.productName"
          :thumb="getImageUrl(item.mainImage || item.productImage)"
        />
      </div>
    </div>

    <!-- 订单信息 -->
    <div class="order-info">
      <div class="info-item">
        <span>商品总额</span>
        <span class="price">¥{{ totalAmount.toFixed(2) }}</span>
      </div>
      <div class="info-item">
        <span>运费</span>
        <span class="price">¥{{ freight.toFixed(2) }}</span>
      </div>
      <div class="info-item total">
        <span>实付金额</span>
        <span class="price">¥{{ totalPrice.toFixed(2) }}</span>
      </div>
    </div>

    <!-- 提交订单 -->
    <div class="submit-bar-wrapper">
      <van-submit-bar
        :price="totalPrice * 100"
        button-text="提交订单"
        @submit="submitOrder"
      />
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { IMAGE_BASE_URL } from '@/utils/config'

export default {
  name: 'OrderConfirm',
  data() {
    return {
      loading: false,
      orderItems: [],
      freight: 0 // 运费
    }
  },
  computed: {
    ...mapState(['user', 'selectedAddress']),
    // 商品总额
    totalAmount() {
      if (!this.orderItems || !this.orderItems.length) return 0

      const total = this.orderItems.reduce((sum, item) => {
        const price = Number(item.price) || 0
        const quantity = Number(item.quantity) || 1
        const subtotal = price * quantity
        
        console.log('计算小计:', {
          name: item.name,
          price,
          quantity,
          subtotal
        })
        
        return sum + subtotal
      }, 0)

      console.log('计算商品总额:', total)
      return total
    },
    // 实付金额 = 商品总额 + 运费
    totalPrice() {
      const total = this.totalAmount + Number(this.freight)
      console.log('计算实付金额:', {
        totalAmount: this.totalAmount,
        freight: this.freight,
        total
      })
      return total
    }
  },
  created() {
    // 检查登录状态
    if (!this.user) {
      this.$toast('请先登录')
      this.$router.push({
        path: '/login',
        query: { redirect: this.$route.fullPath }
      })
      return
    }
    this.fetchOrderData()
  },
  methods: {
    async fetchOrderData() {
      if (this.loading) return
      this.loading = true

      try {
        const itemIds = this.$route.query.items
        if (!itemIds) {
          throw new Error('缺少商品信息')
        }

        // 获取购物车中选中的商品
        const cartItems = await this.$store.dispatch('fetchCartItems')
        console.log('购物车原始数据:', cartItems)

        // 处理商品数据
        this.orderItems = cartItems
          .filter(item => itemIds.split(',').includes(item.productId.toString()))
          .map(item => {
            // 添加详细的日志
            console.log('处理前的商品数据:', {
              id: item.id,
              productId: item.productId,
              name: item.name || item.productName,
              price: item.price,
              productPrice: item.productPrice,
              quantity: item.quantity
            })

            const processedItem = {
              ...item,
              id: item.id || item.cartId,
              productId: item.productId,
              name: item.name || item.productName,
              price: item.price || item.productPrice || 0,
              quantity: item.quantity || 1
            }

            // 确保数值类型
            processedItem.price = Number(processedItem.price)
            processedItem.quantity = Number(processedItem.quantity)

            console.log('处理后的商品数据:', processedItem)
            return processedItem
          })

        console.log('最终订单商品列表:', this.orderItems)
        console.log('商品总额:', this.totalAmount)
        console.log('实付金额:', this.totalPrice)
      } catch (error) {
        console.error('获取订单数据失败:', error)
        this.$toast.fail(error.message || '获取数据失败')
        this.$router.back()
      } finally {
        this.loading = false
      }
    },
    getImageUrl(path) {
      if (!path) return '/images/default.jpg'
      if (path.startsWith('http')) return path
      return `${IMAGE_BASE_URL}${path}`
    },
    chooseAddress() {
      this.$router.push({
        path: '/address/list',
        query: { redirect: this.$route.fullPath }
      })
    },
    async submitOrder() {
      if (!this.orderItems.length) {
        this.$toast('请选择商品')
        return
      }

      try {
        // 收集购物车ID
        const cartIds = this.orderItems.map(item => {
          console.log('订单项:', item)
          if (!item.id) {
            throw new Error(`商品 ${item.productName || item.name} 缺少购物车ID`)
          }
          return item.id
        })

        const orderData = { 
          cartIds,
          userId: this.$store.state.user.id,
          totalAmount: this.totalPrice
        }
        console.log('提交订单数据:', orderData)

        const result = await this.$store.dispatch('createOrder', orderData)
        console.log('创建订单结果:', result)

        if (!result || !result.id) {
          throw new Error('创建订单失败：无效的订单ID')
        }

        // 等待一下，确保订单创建完成
        await this.$nextTick()

        // 使用replace而不是push，避免返回到确认订单页
        this.$router.replace({
          name: 'payment',
          params: { 
            id: String(result.id),
            amount: String(result.totalAmount || this.totalPrice)
          }
        })
      } catch (error) {
        console.error('提交订单失败:', error)
        this.$toast.fail(error.message || '提交订单失败')
      }
    }
  }
}
</script>

<style scoped>
.order-confirm {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 100px;
}

.address-card {
  background: #fff;
  margin: 10px;
  padding: 15px;
  border-radius: 8px;
  position: relative;
}

.address-info {
  padding-right: 20px;
}

.user-info {
  margin-bottom: 8px;
}

.name {
  font-size: 16px;
  font-weight: bold;
  margin-right: 10px;
}

.phone {
  color: #666;
}

.address {
  color: #333;
  line-height: 1.5;
}

.no-address {
  color: #666;
  display: flex;
  align-items: center;
}

.no-address .van-icon {
  margin-right: 5px;
}

.arrow {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.product-list {
  margin: 10px;
}

.product-item {
  background: #fff;
  margin-bottom: 10px;
  border-radius: 8px;
  overflow: hidden;
}

.order-info {
  background: #fff;
  margin: 10px;
  padding: 15px;
  border-radius: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.price {
  color: #f44;
  font-weight: bold;
}

.submit-bar-wrapper {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 50px;
  z-index: 100;
}

.van-submit-bar {
  bottom: 0;
}

.order-confirm::after {
  content: '';
  display: block;
  height: 100px;
}

.total {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.total .price {
  font-size: 18px;
  color: #f44;
}
</style> 