<template>
  <div class="product">
    <!-- 导航栏 -->
    <van-nav-bar
      title="商品详情"
      left-arrow
      @click-left="$router.back()"
    />
    
    <!-- 商品信息 -->
    <div class="product-content" v-if="product">
      <!-- 商品图片 -->
      <van-swipe class="product-swipe">
        <van-swipe-item>
          <img :src="getImageUrl(product.mainImage)" class="product-image" @error="onImageError">
        </van-swipe-item>
      </van-swipe>

      <!-- 商品信息 -->
      <div class="product-info">
        <div class="product-price">
          <span class="price">¥{{ product.price }}</span>
          <span class="sales">已售 {{ product.sales || 0 }}件</span>
        </div>
        <div class="product-name">{{ product.name }}</div>
        <div class="product-category">{{ product.categoryName }}</div>
      </div>

      <!-- 商品描述 -->
      <div class="product-desc">
        <div class="desc-title">商品描述</div>
        <div class="desc-content">{{ product.description || '暂无描述' }}</div>
      </div>

      <!-- 商品参数 -->
      <div class="product-params">
        <div class="param-item">
          <span class="param-label">库存</span>
          <span class="param-value">{{ product.stock }}件</span>
        </div>
        <div class="param-item">
          <span class="param-label">分类</span>
          <span class="param-value">{{ product.categoryName }}</span>
        </div>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="product-actions">
      <div class="action-icons">
        <div class="action-item" @click="$router.push('/cart')">
          <van-icon name="shopping-cart-o" />
          <span>购物车</span>
          <div class="cart-badge" v-if="cartCount">{{ cartCount }}</div>
        </div>
        <div class="action-item" @click="toggleFavorite">
          <van-icon :name="isFavorite ? 'star' : 'star-o'" :class="{ favorite: isFavorite }" />
          <span>收藏</span>
        </div>
      </div>
      <div class="action-buttons">
        <van-button 
          type="warning" 
          class="action-button"
          @click="addToCart"
          :disabled="!product || product.stock <= 0"
        >
          加入购物车
        </van-button>
        <van-button 
          type="danger" 
          class="action-button"
          @click="buyNow"
          :disabled="!product || product.stock <= 0"
        >
          立即购买
        </van-button>
      </div>
    </div>

    <!-- 商品数量选择弹窗 -->
    <van-sku
      v-model="showSku"
      :sku="skuData"
      :goods="goodsInfo"
      :goods-id="productId"
      :quota="product ? product.stock : 0"
      :hide-stock="false"
      :show-add-cart-btn="true"
      :reset-stepper-on-hide="true"
      :close-on-click-overlay="true"
      @buy-clicked="onBuyClicked"
      @add-cart="onAddCart"
    />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { IMAGE_BASE_URL } from '@/utils/config'

export default {
  name: 'Product',
  data() {
    return {
      product: null,
      showSku: false,
      isFavorite: false,
      skuData: {
        tree: [],
        list: [],
        price: '0',
        stock_num: 0,
        collection_id: 0,
        none_sku: true
      }
    }
  },
  computed: {
    ...mapState(['cartItems']),
    productId() {
      const id = this.$route.params.id
      return id && !isNaN(id) ? parseInt(id) : null
    },
    cartCount() {
      return this.cartItems.length
    },
    goodsInfo() {
      if (!this.product) return {}
      return {
        title: this.product.name,
        picture: this.getImageUrl(this.product.mainImage)
      }
    }
  },
  created() {
    if (!this.productId) {
      this.$toast('商品ID无效')
      this.$router.back()
      return
    }
    this.fetchProductDetail()
  },
  methods: {
    async fetchProductDetail() {
      try {
        const response = await this.$store.dispatch('fetchProductDetail', this.productId)
        console.log('商品详情:', response)
        
        if (!response || !response.data) {
          throw new Error('商品不存在')
        }
        
        this.product = response.data
        
        // 更新SKU数据
        if (this.product) {
          this.skuData.price = this.product.price
          this.skuData.stock_num = this.product.stock
        }
      } catch (error) {
        console.error('获取商品数据失败:', error)
        this.$toast.fail(error.message || '获取商品数据失败')
        
        // 如果商品不存在，返回上一页
        if (error.message === '商品不存在') {
          this.$router.back()
        }
      }
    },
    getImageUrl(path) {
      if (!path) return '/images/default.jpg'
      if (path.startsWith('http')) return path
      return `${IMAGE_BASE_URL}${path}`
    },
    onImageError(event) {
      event.target.src = '/images/default.jpg'
      event.target.onerror = null
    },
    addToCart() {
      this.showSku = true
    },
    buyNow() {
      this.showSku = true
    },
    async toggleFavorite() {
      if (!this.product) return
      
      try {
        if (this.isFavorite) {
          await this.$store.dispatch('cancelFavorite', this.product.id)
          this.$toast.success('取消收藏成功')
        } else {
          await this.$store.dispatch('addFavorite', this.product.id)
          this.$toast.success('收藏成功')
        }
        this.isFavorite = !this.isFavorite
      } catch (error) {
        this.$toast.fail(this.isFavorite ? '取消收藏失败' : '收藏失败')
      }
    },
    async onAddCart(data) {
      try {
        await this.$store.dispatch('addToCart', {
          productId: this.productId,
          quantity: data.selectedNum
        })
        this.$toast.success('添加成功')
        this.showSku = false
      } catch (error) {
        if (error.message === '请先登录') {
          this.$router.push({
            path: '/login',
            query: { redirect: this.$route.fullPath }
          })
        } else {
          this.$toast.fail(error.message || '添加失败')
        }
      }
    },
    async onBuyClicked(data) {
      try {
        await this.$store.dispatch('addToCart', {
          productId: this.productId,
          quantity: data.selectedNum
        })
        this.$router.push('/order/confirm')
      } catch (error) {
        if (error.message === '请先登录') {
          this.$router.push({
            path: '/login',
            query: { redirect: '/order/confirm' }
          })
        } else {
          this.$toast.fail(error.message || '操作失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.product {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 50px;
}

.product-swipe {
  height: 300px;
  background: #fff;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  background: #fff;
  padding: 15px;
  margin-bottom: 10px;
}

.product-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.price {
  font-size: 24px;
  font-weight: bold;
  color: #f44;
}

.sales {
  font-size: 12px;
  color: #999;
}

.product-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
}

.product-category {
  font-size: 12px;
  color: #666;
}

.product-desc {
  background: #fff;
  padding: 15px;
  margin-bottom: 10px;
}

.desc-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
}

.desc-content {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.product-params {
  background: #fff;
  padding: 15px;
}

.param-item {
  display: flex;
  margin-bottom: 10px;
}

.param-label {
  width: 60px;
  color: #999;
}

.product-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 5px 15px;
  background: #fff;
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.1);
}

.action-icons {
  display: flex;
  margin-right: 15px;
}

.action-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20px;
  font-size: 20px;
}

.action-item span {
  font-size: 12px;
  margin-top: 2px;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  background: #f44;
  border-radius: 8px;
  color: #fff;
  font-size: 12px;
  padding: 0 4px;
}

.favorite {
  color: #f44;
}

.action-buttons {
  flex: 1;
  display: flex;
  gap: 10px;
}

.action-button {
  flex: 1;
  height: 40px;
}
</style> 