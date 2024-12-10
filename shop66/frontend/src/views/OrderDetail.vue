<template>
  <div class="order-detail">
    <van-nav-bar
      title="订单详情"
      left-arrow
      @click-left="$router.back()"
    />
    
    <!-- 订单状态 -->
    <div class="status-card">
      <div class="status-text">{{ getStatusText(order.status) }}</div>
      <div class="status-desc">{{ getStatusDesc(order.status) }}</div>
    </div>

    <!-- 收货地址 -->
    <van-cell-group title="收货信息">
      <van-cell>
        <template #title>
          <div class="address-info">
            <div class="contact">
              <span>{{ order.address.name }}</span>
              <span>{{ order.address.phone }}</span>
            </div>
            <div class="address">{{ order.address.fullAddress }}</div>
          </div>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 商品信息 -->
    <van-cell-group title="商品信息">
      <van-card
        v-for="item in order.items"
        :key="item.id"
        :num="item.quantity"
        :price="item.price"
        :title="item.name"
        :thumb="item.image"
      />
    </van-cell-group>

    <!-- 订单信息 -->
    <van-cell-group title="订单信息">
      <van-cell title="订单编号" :value="order.orderNo"/>
      <van-cell title="创建时间" :value="formatDate(order.createTime)"/>
      <van-cell title="支付方式" :value="getPaymentMethod(order.payMethod)"/>
      <van-cell title="订单备注" :value="order.remark || '无'"/>
    </van-cell-group>

    <!-- 金额信息 -->
    <van-cell-group title="金额信息">
      <van-cell title="商品总额" :value="`¥${order.totalAmount}`"/>
      <van-cell title="运费" value="¥0.00"/>
      <van-cell title="实付金额" :value="`¥${order.totalAmount}`" class="total-amount"/>
    </van-cell-group>

    <!-- 底部按钮 -->
    <div class="bottom-buttons">
      <template v-if="order.status === 'UNPAID'">
        <van-button type="danger" @click="onPay">立即付款</van-button>
        <van-button plain @click="onCancel">取消订单</van-button>
      </template>
      <template v-else-if="order.status === 'SHIPPED'">
        <van-button type="primary" @click="onConfirm">确认收货</van-button>
        <van-button plain @click="onViewLogistics">查看物流</van-button>
      </template>
      <template v-else-if="order.status === 'RECEIVED'">
        <van-button type="primary" @click="onReview">评价订单</van-button>
      </template>
      <van-button v-if="order.status === 'COMPLETED'" plain @click="onBuyAgain">再次购买</van-button>
    </div>
  </div>
</template>

<script>
import { formatDate } from '@/utils/date'

export default {
  name: 'OrderDetail',
  data() {
    return {
      order: {
        status: '',
        address: {},
        items: [],
        orderNo: '',
        createTime: '',
        payMethod: '',
        remark: '',
        totalAmount: 0
      }
    }
  },
  created() {
    this.fetchOrderDetail()
  },
  methods: {
    formatDate,
    async fetchOrderDetail() {
      try {
        const order = await this.$store.dispatch('fetchOrder', this.$route.params.id)
        this.order = order
      } catch (error) {
        this.$toast.fail('获取订单详情失败')
      }
    },
    getStatusText(status) {
      const statusMap = {
        UNPAID: '待付款',
        PAID: '待发货',
        SHIPPED: '待收货',
        RECEIVED: '待评价',
        COMPLETED: '已完成',
        CANCELLED: '已取消'
      }
      return statusMap[status] || status
    },
    getStatusDesc(status) {
      const descMap = {
        UNPAID: '请尽快完成支付',
        PAID: '商家正在处理您的订单',
        SHIPPED: '商品正在配送中',
        RECEIVED: '期待您的评价',
        COMPLETED: '交易已完成',
        CANCELLED: '订单已取消'
      }
      return descMap[status] || ''
    },
    getPaymentMethod(method) {
      const methodMap = {
        ALIPAY: '支付宝',
        WECHAT: '微信支付'
      }
      return methodMap[method] || method
    },
    onPay() {
      this.$router.push(`/payment/${this.order.id}`)
    },
    async onCancel() {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确定要取消此订单吗？'
        })
        await this.$store.dispatch('cancelOrder', this.order.id)
        this.$toast.success('取消成功')
        this.fetchOrderDetail()
      } catch (error) {
        if (error) this.$toast.fail('取消失败')
      }
    },
    async onConfirm() {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确认已收到商品？'
        })
        await this.$store.dispatch('confirmOrder', this.order.id)
        this.$toast.success('确认成功')
        this.fetchOrderDetail()
      } catch (error) {
        if (error) this.$toast.fail('确认失败')
      }
    },
    onViewLogistics() {
      this.$router.push(`/order/logistics/${this.order.id}`)
    },
    onReview() {
      this.$router.push(`/order/review/${this.order.id}`)
    },
    onBuyAgain() {
      // 将商品重新加入购物车
      this.order.items.forEach(item => {
        this.$store.dispatch('addToCart', {
          productId: item.productId,
          quantity: item.quantity
        })
      })
      this.$router.push('/cart')
    }
  }
}
</script>

<style scoped>
.order-detail {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 50px;
}

.status-card {
  background: #ee0a24;
  color: #fff;
  padding: 20px;
}

.status-text {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.status-desc {
  font-size: 14px;
  opacity: 0.8;
}

.address-info {
  padding: 5px 0;
}

.contact {
  margin-bottom: 5px;
}

.contact span {
  margin-right: 10px;
}

.address {
  color: #666;
  font-size: 14px;
}

.total-amount {
  color: #ee0a24;
  font-weight: bold;
}

.bottom-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px;
  background: #fff;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
</style> 