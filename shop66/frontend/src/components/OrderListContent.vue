<template>
  <div class="order-list-content">
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
          
          <div class="order-products">
            <van-card
              v-for="item in order.items"
              :key="item.id"
              :num="item.quantity"
              :price="item.price"
              :title="item.name"
              :thumb="item.image"
            />
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              <span>共{{ getTotalQuantity(order.items) }}件商品</span>
              <span>实付款：</span>
              <span class="price">¥{{ order.payAmount }}</span>
              <span class="origin-price" v-if="order.totalAmount > order.payAmount">
                ¥{{ order.totalAmount }}
              </span>
            </div>
            <div class="order-actions">
              <template v-if="order.status === 0">
                <van-button size="small" type="danger" @click="onPay(order)">立即付款</van-button>
                <van-button size="small" plain @click="onCancel(order)">取消订单</van-button>
              </template>
              <template v-else-if="order.status === 2">
                <van-button size="small" type="primary" @click="onConfirm(order)">确认收货</van-button>
                <van-button size="small" plain @click="onViewLogistics(order)">查看物流</van-button>
              </template>
              <template v-else-if="order.status === 3">
                <van-button size="small" type="primary" @click="onReview(order)">评价订单</van-button>
              </template>
              <van-button size="small" plain @click="onViewDetail(order)">查看详情</van-button>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script>
export default {
  name: 'OrderListContent',
  props: {
    status: [String, Number]
  },
  data() {
    return {
      loading: false,
      finished: false,
      refreshing: false,
      page: 1,
      orders: []
    }
  },
  methods: {
    async onLoad() {
      if (this.refreshing) {
        this.orders = []
        this.refreshing = false
      }
      
      try {
        const params = {
          page: this.page,
          status: this.status
        }
        const response = await this.$store.dispatch('fetchOrders', params)
        
        if (response.length < 10) {
          this.finished = true
        }
        this.orders.push(...response)
        this.page++
      } catch (error) {
        this.$toast.fail('加载失败')
      }
      this.loading = false
    },
    onRefresh() {
      this.finished = false
      this.page = 1
      this.onLoad()
    },
    getStatusText(status) {
      const statusMap = {
        0: '待付款',
        1: '待发货',
        2: '待收货',
        3: '待评价',
        4: '已完成',
        5: '已关闭'
      }
      return statusMap[status] || status
    },
    getTotalQuantity(items) {
      return items.reduce((sum, item) => sum + item.quantity, 0)
    },
    onPay(order) {
      this.$router.push(`/payment/${order.id}`)
    },
    async onCancel(order) {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确定要取消此订单吗？'
        })
        await this.$store.dispatch('cancelOrder', order.id)
        this.$toast.success('取消成功')
        this.onRefresh()
      } catch (error) {
        if (error) this.$toast.fail('取消失败')
      }
    },
    async onConfirm(order) {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确认已收到商品？'
        })
        await this.$store.dispatch('confirmOrder', order.id)
        this.$toast.success('确认成功')
        this.onRefresh()
      } catch (error) {
        if (error) this.$toast.fail('确认失败')
      }
    },
    onViewLogistics(order) {
      this.$router.push(`/order/logistics/${order.id}`)
    },
    onReview(order) {
      this.$router.push(`/order/review/${order.id}`)
    },
    onViewDetail(order) {
      this.$router.push(`/order/detail/${order.id}`)
    }
  }
}
</script>

<style scoped>
.order-list-content {
  padding-bottom: 50px;
}

.order-item {
  margin: 10px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.order-header {
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.order-no {
  color: #666;
  font-size: 14px;
}

.order-status {
  color: #f44;
  font-size: 14px;
}

.order-products {
  padding: 10px;
}

.order-footer {
  padding: 10px;
  border-top: 1px solid #eee;
}

.order-total {
  text-align: right;
  margin-bottom: 10px;
  color: #666;
}

.order-total .price {
  color: #f44;
  font-size: 16px;
  font-weight: bold;
  margin: 0 4px;
}

.order-total .origin-price {
  color: #999;
  font-size: 12px;
  text-decoration: line-through;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 