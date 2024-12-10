<template>
  <div class="payment">
    <van-nav-bar
      title="订单支付"
      left-arrow
      @click-left="$router.back()"
    />

    <!-- 订单信息 -->
    <div class="order-info">
      <div class="order-no">
        订单号：{{ order ? order.orderNo : '' }}
      </div>
      <div class="amount">
        ¥{{ orderAmount.toFixed(2) }}
      </div>
    </div>

    <!-- 支付方式 -->
    <div class="payment-methods">
      <div class="title">选择支付方式</div>
      <van-radio-group v-model="payType">
        <div class="method-item" @click="payType = 'alipay'">
          <div class="method-info">
            <van-icon name="alipay" color="#1296db" size="24" />
            <span class="name">支付宝支付</span>
          </div>
          <van-radio name="alipay" />
        </div>
        <div class="method-item" @click="payType = 'wechat'">
          <div class="method-info">
            <van-icon name="wechat" color="#67c23a" size="24" />
            <span class="name">微信支付</span>
          </div>
          <van-radio name="wechat" />
        </div>
      </van-radio-group>
    </div>

    <!-- 支付按钮 -->
    <div class="submit-btn">
      <van-button 
        type="danger" 
        block 
        round
        :loading="loading"
        @click="onSubmit"
      >
        立即支付
      </van-button>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'Payment',
  props: {
    id: {
      type: [String, Number],
      required: true
    },
    amount: {
      type: [String, Number],
      default: 0
    }
  },
  data() {
    return {
      loading: false,
      payType: 'alipay',
      order: null,
      initialAmount: 0
    }
  },
  computed: {
    ...mapState(['user']),
    orderId() {
      const rawId = this.id || this.$route.params.id
      console.log('原始订单ID:', rawId)
      const parsedId = rawId ? parseInt(rawId) : null
      console.log('解析后的订单ID:', parsedId)
      return parsedId
    },
    orderAmount() {
      const amount = this.order ? Number(this.order.totalAmount || 0) : 0
      return amount || this.initialAmount
    }
  },
  created() {
    console.log('Payment组件created, props:', this.$props)
    
    this.initialAmount = Number(this.amount || this.$route.params.amount || 0)
    console.log('初始金额:', this.initialAmount)

    if (!this.user) {
      this.$toast('请先登录')
      this.$router.push({
        path: '/login',
        query: { redirect: this.$route.fullPath }
      })
      return
    }

    if (!this.orderId) {
      console.error('订单ID无效:', this.id, this.$route.params.id)
      this.$toast('订单不存在')
      this.$router.replace('/orders')
      return
    }

    this.fetchOrderInfo()
  },
  methods: {
    async fetchOrderInfo() {
      if (this.loading) return
      this.loading = true

      try {
        const order = await this.$store.dispatch('fetchOrder', this.orderId)
        console.log('订单信息:', order)
        
        if (!order) {
          throw new Error('订单不存在')
        }

        this.order = {
          ...order,
          totalAmount: Number(order.totalAmount || this.initialAmount),
          orderNo: order.orderNo || String(this.orderId)
        }

        console.log('处理后的订单信息:', this.order)
      } catch (error) {
        console.error('获取订单信息失败:', error)
        this.$toast.fail(error.message || '获取订单信息失败')
        this.$router.back()
      } finally {
        this.loading = false
      }
    },
    async onSubmit() {
      if (this.loading) return
      if (!this.orderId) {
        this.$toast('订单不存在')
        return
      }

      const amount = Number(this.orderAmount)
      if (!amount || amount <= 0) {
        this.$toast('订单金额不能为0')
        return
      }

      this.loading = true

      try {
        console.log('提交支付:', {
          orderId: this.orderId,
          payType: this.payType,
          amount
        })

        await this.$store.dispatch('payOrder', {
          orderId: this.orderId,
          payType: this.payType,
          amount
        })
        
        this.$toast.success('支付成功')
        
        // 等待一下再跳转
        await this.$nextTick()
        this.$router.replace('/orders')
      } catch (error) {
        console.error('支付失败:', error)
        this.$toast.fail(error.message || '支付失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.payment {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 50px;
}

.order-info {
  background: #fff;
  margin: 10px;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.order-no {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.amount {
  color: #f44;
  font-size: 32px;
  font-weight: bold;
}

.payment-methods {
  background: #fff;
  margin: 10px;
  padding: 15px;
  border-radius: 8px;
}

.title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

.method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.method-item:last-child {
  border-bottom: none;
}

.method-info {
  display: flex;
  align-items: center;
}

.name {
  margin-left: 10px;
  font-size: 16px;
}

.submit-btn {
  margin: 20px 10px;
}
</style> 