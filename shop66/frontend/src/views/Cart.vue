<template>
  <div class="cart">
    <van-nav-bar title="购物车"/>
    
    <!-- 加载状态 -->
    <van-loading v-if="loading" type="spinner" vertical class="loading">
      加载中...
    </van-loading>

    <!-- 购物车列表 -->
    <template v-else-if="cartItems && cartItems.length">
      <div class="cart-content">
        <div 
          class="cart-item" 
          v-for="item in cartItems" 
          :key="item.productId"
        >
          <van-checkbox 
            :name="item.productId"
            :value="item.selected"
            @click="onItemSelect(item)"
          />
          <van-card
            :price="item.price"
            :title="item.name || item.productName"
            :thumb="getImageUrl(item.mainImage || item.productImage)"
          >
            <template #num>
              <van-stepper 
                v-model="item.quantity" 
                :min="1" 
                :max="item.stock"
                :disabled="loading"
                @change="onQuantityChange(item)"
              />
            </template>
            <template #footer>
              <van-button 
                size="mini" 
                @click="removeFromCart(item)"
              >
                删除
              </van-button>
            </template>
          </van-card>
        </div>

        <!-- 底部结算栏 -->
        <van-submit-bar
          :price="totalPrice * 100"
          button-text="提交订单"
          @submit="onSubmit"
        >
          <van-checkbox 
            :value="isAllSelected"
            @click="toggleAll"
          >
            全选
          </van-checkbox>
        </van-submit-bar>
      </div>
    </template>

    <!-- 空购物车提示 -->
    <van-empty v-else description="购物车还是空的">
      <van-button round type="danger" class="bottom-button" to="/">
        去逛逛
      </van-button>
    </van-empty>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { IMAGE_BASE_URL } from '@/utils/config'

export default {
  name: 'Cart',
  data() {
    return {
      loading: false
    }
  },
  computed: {
    ...mapState(['user']),
    cartItems() {
      return this.$store.state.cartItems.map(item => {
        console.log('购物车商品原始数据:', item)
        return {
          ...item,
          price: item.price || item.productPrice || 0,
          quantity: parseInt(item.quantity || 1),
          selected: item.selected === true || item.selected === 'true' || item.selected === 1
        }
      })
    },
    isAllSelected() {
      return this.cartItems && 
             this.cartItems.length > 0 && 
             this.cartItems.every(item => item.selected)
    },
    totalPrice() {
      if (!this.cartItems || !this.cartItems.length) return 0
      
      // 添加日志输出
      console.log('计算总价:', this.cartItems.map(item => ({
        id: item.productId,
        name: item.name || item.productName,
        selected: item.selected,
        price: item.price,
        quantity: item.quantity,
        subtotal: item.selected ? (item.price * item.quantity) : 0
      })))

      return this.cartItems.reduce((total, item) => {
        if (item.selected) {
          const price = parseFloat(item.price || item.productPrice || 0)
          const quantity = parseInt(item.quantity || 1)
          const subtotal = price * quantity
          console.log('商品小计:', {
            id: item.productId,
            name: item.name || item.productName,
            price,
            quantity,
            subtotal
          })
          return total + subtotal
        }
        return total
      }, 0)
    }
  },
  watch: {
    isAllSelected: {
      immediate: true,
      handler(val) {
        this.allSelected = val
      }
    }
  },
  created() {
    // 检查登录状态
    if (!this.user) {
      this.$toast('请先登录')
      this.$router.push({
        path: '/login',
        query: { redirect: '/cart' }
      })
      return
    }
    this.fetchCartItems()
  },
  methods: {
    async fetchCartItems() {
      if (this.loading) return
      this.loading = true

      try {
        await this.$store.dispatch('fetchCartItems')
      } catch (error) {
        if (error.message === '请先登录') {
          this.$router.push({
            path: '/login',
            query: { redirect: '/cart' }
          })
        } else {
          this.$toast.fail(error.message || '获取购物车失败')
        }
      } finally {
        this.loading = false
      }
    },
    getImageUrl(path) {
      if (!path) return '/images/default.jpg'
      if (path.startsWith('http')) return path
      return `${IMAGE_BASE_URL}${path}`
    },
    async onQuantityChange(item) {
      if (!item) return
      
      try {
        await this.$store.dispatch('updateCartItem', {
          productId: item.productId,
          quantity: item.quantity
        })
      } catch (error) {
        // 如果更新失败，恢复原来的数量
        this.$toast.fail(error.message || '更新数量失败')
        await this.fetchCartItems()
      }
    },
    async removeFromCart(item) {
      if (!item || !item.productId) return
      
      try {
        await this.$store.dispatch('removeFromCart', item.productId)
        this.$toast.success('删除成功')
      } catch (error) {
        if (error.message === '请先登录') {
          this.$router.push({
            path: '/login',
            query: { redirect: '/cart' }
          })
        } else {
          this.$toast.fail(error.message || '删除失败')
        }
      }
    },
    async onItemSelect(item) {
      try {
        const newSelected = !item.selected
        console.log('选中状态变化:', item.productId, newSelected)

        await this.$store.dispatch('updateCartSelected', {
          productId: item.productId,
          selected: newSelected ? 'true' : 'false'
        })
        
        // 更新成功后刷新数据
        await this.fetchCartItems()
      } catch (error) {
        if (error.message === '请先登录') {
          this.$router.push({
            path: '/login',
            query: { redirect: '/cart' }
          })
        } else {
          this.$toast.fail(error.message || '更新选中状态失败')
          await this.fetchCartItems()
        }
      }
    },
    async toggleAll() {
      if (!this.cartItems || !this.cartItems.length) return
      
      const selected = !this.isAllSelected
      
      try {
        console.log('全选状态变化:', selected)

        await Promise.all(
          this.cartItems.map(item => 
            this.$store.dispatch('updateCartSelected', {
              productId: item.productId,
              selected: selected ? 'true' : 'false'
            })
          )
        )
        
        // 更新成功后刷新数据
        await this.fetchCartItems()
      } catch (error) {
        this.$toast.fail('更新选中状态失败')
        await this.fetchCartItems()
      }
    },
    async onSubmit() {
      const selectedItems = this.cartItems.filter(item => item.selected)
      if (!selectedItems.length) {
        this.$toast('请选择商品')
        return
      }

      // 跳转到订单确认页面，并传递选中的商品ID
      this.$router.push({
        path: '/order/confirm',
        query: { items: selectedItems.map(item => item.productId).join(',') }
      })
    }
  }
}
</script>

<style scoped>
.cart {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 50px;
}

.cart-content {
  padding: 10px;
}

.cart-item {
  display: flex;
  align-items: center;
  background: #fff;
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 8px;
}

.cart-item .van-checkbox {
  margin-right: 10px;
}

.cart-item .van-card {
  background: #fff;
  width: 100%;
}

.bottom-button {
  width: 160px;
  height: 40px;
}

.van-submit-bar {
  bottom: 50px;
}

.van-submit-bar .van-checkbox {
  margin-left: 10px;
}

.loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style> 