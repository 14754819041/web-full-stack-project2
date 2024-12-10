<template>
  <div class="order-list">
    <van-nav-bar
      title="我的订单"
      left-arrow
      @click-left="$router.back()"
    />

    <!-- 订单状态切换 -->
    <van-tabs v-model="activeTab" sticky>
      <van-tab title="全部" name="all" />
      <van-tab title="待付款" name="unpaid" />
      <van-tab title="待发货" name="unshipped" />
      <van-tab title="待收货" name="shipped" />
      <van-tab title="已完成" name="completed" />
    </van-tabs>

    <!-- 订单列表 -->
    <div class="order-content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div class="order-item" v-for="order in orders" :key="order.id">
            <div class="order-header">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-status">{{ getStatusText(order.status) }}</span>
            </div>

            <div class="order-content">
              <div class="product-list">
                <div 
                  class="product-item" 
                  v-for="item in order.items" 
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

              <div class="order-footer">
                <div class="total">
                  共{{ order.items ? order.items.length : 0 }}件商品，
                  实付：<span class="price">¥{{ Number(order.totalAmount || 0).toFixed(2) }}</span>
                </div>

                <div class="actions">
                  <template v-if="order.status === 0">
                    <van-button 
                      size="small" 
                      type="danger"
                      @click="goPay(order)"
                    >
                      去支付
                    </van-button>
                    <van-button 
                      size="small" 
                      @click="cancelOrder(order.id)"
                    >
                      取消订单
                    </van-button>
                  </template>

                  <template v-if="order.status === 2">
                    <van-button 
                      size="small" 
                      type="primary"
                      @click="confirmOrder(order.id)"
                    >
                      确认收货
                    </van-button>
                    <van-button 
                      size="small"
                      @click="viewLogistics(order.id)"
                    >
                      查看物流
                    </van-button>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { IMAGE_BASE_URL } from '@/utils/config'

export default {
  name: 'OrderList',
  data() {
    return {
      activeTab: 'all',
      loading: false,
      refreshing: false,
      finished: false,
      current: 1,
      size: 10,
      orders: []
    }
  },
  computed: {
    ...mapState(['user']),
    status() {
      const statusMap = {
        all: '',
        unpaid: '0',
        unshipped: '1',
        shipped: '2',
        completed: '3'
      }
      return statusMap[this.activeTab]
    }
  },
  watch: {
    activeTab() {
      this.reset()
      this.onLoad()
    }
  },
  created() {
    if (!this.user) {
      this.$toast('请先登录')
      this.$router.push({
        path: '/login',
        query: { redirect: this.$route.fullPath }
      })
      return
    }

    this.onLoad()
  },
  methods: {
    getImageUrl(path) {
      if (!path) return '/images/default.jpg'
      if (path.startsWith('http')) return path
      return `${IMAGE_BASE_URL}${path}`
    },
    getStatusText(status) {
      const statusMap = {
        0: '待付款',
        1: '待发货',
        2: '待收货',
        3: '已完成',
        4: '已取消'
      }
      return statusMap[status] || '未知状态'
    },
    reset() {
      this.finished = false
      this.current = 1
      this.orders = []
    },
    async onLoad() {
      if (this.loading) return

      try {
        this.loading = true
        const params = {
          current: this.current,
          size: this.size,
          userId: this.user.id || this.user.userId
        }
        if (this.status) {
          params.status = this.status
        }

        console.log('请求订单列表参数:', params)
        const result = await this.$store.dispatch('fetchOrders', params)
        console.log('订单列表响应:', result)

        if (result && result.records) {
          const orders = result.records.map(order => ({
            ...order,
            totalAmount: Number(order.totalAmount || 0),
            items: (order.items || []).map(item => ({
              ...item,
              price: Number(item.price || 0),
              quantity: Number(item.quantity || 1)
            }))
          }))

          if (this.refreshing) {
            this.orders = orders
          } else {
            this.orders.push(...orders)
          }

          this.current++
          this.finished = orders.length < this.size
        } else {
          this.finished = true
        }
      } catch (error) {
        console.error('获取订单列表失败:', error)
        this.$toast.fail(error.message || '获取订单列表失败')
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    async onRefresh() {
      this.reset()
      await this.onLoad()
    },
    goPay(order) {
      this.$router.push({
        name: 'payment',
        params: { 
          id: order.id,
          amount: order.totalAmount
        }
      })
    },
    async cancelOrder(orderId) {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确认取消订单吗？'
        })

        await this.$store.dispatch('cancelOrder', orderId)
        this.$toast.success('取消成功')
        this.onRefresh()
      } catch (error) {
        if (error.message !== '取消') {
          this.$toast.fail(error.message || '取消订单失败')
        }
      }
    },
    async confirmOrder(orderId) {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确认收到商品了吗？'
        })

        await this.$store.dispatch('confirmOrder', orderId)
        this.$toast.success('确认收货成功')
        this.onRefresh()
      } catch (error) {
        if (error.message !== '取消') {
          this.$toast.fail(error.message || '确认收货失败')
        }
      }
    },
    viewLogistics(orderId) {
      this.$router.push(`/order/logistics/${orderId}`)
    }
  }
}
</script>

<style scoped>
.order-list {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 100px;
  display: flex;
  flex-direction: column;
}

.order-item {
  background: #fff;
  margin: 10px;
  border-radius: 8px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding: 10px 15px;
  border-bottom: 1px solid #eee;
}

.order-no {
  color: #666;
  font-size: 14px;
}

.order-status {
  color: #f44;
}

.product-list {
  padding: 10px;
}

.product-item {
  margin-bottom: 10px;
}

.product-item:last-child {
  margin-bottom: 0;
}

.order-footer {
  padding: 10px 15px;
  border-top: 1px solid #eee;
}

.total {
  text-align: right;
  margin-bottom: 10px;
}

.price {
  color: #f44;
  font-weight: bold;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.actions .van-button {
  margin-left: 10px;
}

.order-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 50px;
}

.order-list::after {
  content: '';
  display: block;
  height: 50px;
}

.order-item:last-child {
  margin-bottom: 60px;
}

.van-tabs {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
}

.van-list {
  min-height: calc(100vh - 100px);
  padding-bottom: 60px;
}
</style> 