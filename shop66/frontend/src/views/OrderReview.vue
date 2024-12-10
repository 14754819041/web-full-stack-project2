<template>
  <div class="order-review">
    <van-nav-bar
      title="评价订单"
      left-arrow
      @click-left="$router.back()"
    />
    
    <!-- 商品列表 -->
    <div class="products">
      <div 
        v-for="item in orderItems" 
        :key="item.id"
        class="product-review"
      >
        <van-card
          :num="item.quantity"
          :price="item.price"
          :title="item.name"
          :thumb="item.image"
        />
        
        <!-- 评分 -->
        <div class="rating">
          <div class="rating-title">商品评分</div>
          <van-rate v-model="item.rating" />
        </div>
        
        <!-- 评价内容 -->
        <div class="content">
          <van-field
            v-model="item.content"
            type="textarea"
            placeholder="请输入商品评价"
            rows="3"
            autosize
          />
        </div>
        
        <!-- 图片上传 -->
        <div class="upload-images">
          <div class="upload-title">上传图片</div>
          <van-uploader
            v-model="item.images"
            multiple
            :max-count="5"
            :after-read="afterRead"
          />
        </div>
      </div>
    </div>
    
    <!-- 匿名评价 -->
    <van-cell-group>
      <van-cell title="匿名评价">
        <template #right-icon>
          <van-switch v-model="anonymous" size="24" />
        </template>
      </van-cell>
    </van-cell-group>
    
    <!-- 提交按钮 -->
    <div class="submit-btn">
      <van-button type="primary" block @click="onSubmit" :loading="submitting">
        提交评价
      </van-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderReview',
  data() {
    return {
      orderItems: [],
      anonymous: false,
      submitting: false
    }
  },
  created() {
    this.fetchOrderItems()
  },
  methods: {
    async fetchOrderItems() {
      try {
        const order = await this.$store.dispatch('fetchOrder', this.$route.params.id)
        this.orderItems = order.items.map(item => ({
          ...item,
          rating: 5,
          content: '',
          images: []
        }))
      } catch (error) {
        this.$toast.fail('获取订单信息失败')
      }
    },
    async afterRead(file) {
      // 处理图片上传
      try {
        const formData = new FormData()
        formData.append('file', file.file)
        const response = await this.$store.dispatch('uploadImage', formData)
        file.url = response.url
      } catch (error) {
        this.$toast.fail('图片上传失败')
      }
    },
    async onSubmit() {
      if (this.submitting) return
      
      // 验证评价内容
      for (const item of this.orderItems) {
        if (!item.content.trim()) {
          return this.$toast('请填写商品评价')
        }
      }
      
      this.submitting = true
      try {
        const reviews = this.orderItems.map(item => ({
          orderId: this.$route.params.id,
          productId: item.productId,
          rating: item.rating,
          content: item.content,
          images: item.images.map(img => img.url),
          anonymous: this.anonymous
        }))
        
        await this.$store.dispatch('submitReviews', reviews)
        this.$toast.success('评价成功')
        this.$router.replace('/order/list?status=COMPLETED')
      } catch (error) {
        this.$toast.fail('评价失败')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.order-review {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 50px;
}

.product-review {
  margin: 10px;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
}

.rating {
  margin: 15px 0;
}

.rating-title {
  margin-bottom: 8px;
  color: #323233;
}

.content {
  margin: 15px 0;
}

.upload-images {
  margin: 15px 0;
}

.upload-title {
  margin-bottom: 8px;
  color: #323233;
}

.submit-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 16px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
</style> 