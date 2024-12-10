<template>
  <div class="home">
    <!-- 搜索栏 -->
    <search-bar />
    
    <!-- 轮播图 -->
    <van-swipe class="banner" :autoplay="3000" indicator-color="white">
      <van-swipe-item 
        v-for="product in recommendProducts" 
        :key="product.id"
        @click="goToDetail(product.id)"
      >
        <img 
          :src="getImageUrl(product.mainImage)" 
          class="banner-img"
          @error="onImageError"
        />
        <div class="banner-info">
          <div class="banner-title">{{ product.name }}</div>
          <div class="banner-desc">
            <span class="price">¥{{ product.price }}</span>
            <van-tag type="danger" v-if="product.categoryName">{{ product.categoryName }}</van-tag>
          </div>
        </div>
      </van-swipe-item>
    </van-swipe>

    <!-- 功能按钮区 -->
    <van-grid :column-num="4" :border="false" class="nav-grid">
      <van-grid-item icon="apps-o" text="分类" to="/category"/>
      <van-grid-item icon="phone-o" text="手机数码" to="/product/list?categoryId=1"/>
      <van-grid-item icon="tv-o" text="家用电器" to="/product/list?categoryId=2"/>
      <van-grid-item icon="desktop-o" text="电脑办公" to="/product/list?categoryId=3"/>
    </van-grid>

    <!-- 每周上新 -->
    <div class="section">
      <div class="section-title">
        <span>每周上新</span>
        <van-button plain hairline size="mini" to="/product/list?sort=createTime">
          更多
          <van-icon name="arrow" />
        </van-button>
      </div>
      <div class="product-list" v-if="newProducts && newProducts.length">
        <div 
          class="product-item" 
          v-for="product in newProducts" 
          :key="product.id"
          @click="goToDetail(product.id)"
        >
          <van-image 
            :src="getImageUrl(product.mainImage)" 
            width="100%" 
            height="120"
            fit="cover"
          />
          <div class="product-info">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-category">{{ product.categoryName }}</div>
            <div class="product-price">¥{{ product.price }}</div>
            <div class="product-stock">库存: {{ product.stock }}件</div>
          </div>
        </div>
      </div>
      <div v-else class="empty-tip">
        暂无新品
      </div>
    </div>

    <!-- 人气推荐 -->
    <div class="section">
      <div class="section-title">
        <span>人气推荐</span>
        <van-button plain hairline size="mini" to="/product/list?sort=sales">
          更多
          <van-icon name="arrow" />
        </van-button>
      </div>
      <div class="product-list" v-if="hotProducts && hotProducts.length">
        <div 
          class="product-item" 
          v-for="product in hotProducts" 
          :key="product.id"
          @click="goToDetail(product.id)"
        >
          <van-image 
            :src="getImageUrl(product.mainImage)" 
            width="100%" 
            height="120"
            fit="cover"
          />
          <div class="product-info">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-category">{{ product.categoryName }}</div>
            <div class="product-price">¥{{ product.price }}</div>
            <div class="product-tags">
              <van-tag type="danger" v-if="product.sales > 0">已售{{ product.sales }}件</van-tag>
              <van-tag type="warning" v-if="isNewProduct(product)">新品</van-tag>
            </div>
            <van-button 
              size="mini" 
              type="danger" 
              @click.stop="addToCart(product)"
              :disabled="product.stock <= 0"
            >
              {{ product.stock > 0 ? '加入购物车' : '已售罄' }}
            </van-button>
          </div>
        </div>
      </div>
      <div v-else class="empty-tip">
        暂无推荐商品
      </div>
    </div>
  </div>
</template>

<script>
import SearchBar from '@/components/SearchBar.vue'
import { IMAGE_BASE_URL } from '@/utils/config'

export default {
  name: 'Home',
  components: {
    SearchBar
  },
  data() {
    return {
      recommendProducts: [],
      newProducts: [],
      hotProducts: [],
      loading: false
    }
  },
  created() {
    this.fetchHomeData()
  },
  methods: {
    async fetchHomeData() {
      if (this.loading) return
      this.loading = true

      try {
        // 获取所有商品列表
        const response = await this.$store.dispatch('fetchProducts', {
          current: 1,     // 当前页码
          size: 20,       // 获取前20条数据
          status: 1       // 商品状态：1-上架
        })

        console.log('商品列表数据:', response)
        const products = response.records || []
        
        if (products.length > 0) {
          // 按销量排序，取前3个作为轮播推荐
          this.recommendProducts = [...products]
            .filter(p => p.mainImage)  // 确保有主图
            .sort((a, b) => (b.sales || 0) - (a.sales || 0))
            .slice(0, 3)
          
          // 按创建时间排序，取前6个作为新品
          this.newProducts = [...products]
            .sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))
            .slice(0, 6)

          // 按销量排序，取前4个作为热销商品
          this.hotProducts = [...products]
            .sort((a, b) => (b.sales || 0) - (a.sales || 0))
            .slice(0, 4)
        } else {
          console.log('没有获取到商品数据')
          this.$toast('暂无商品数据')
        }
      } catch (error) {
        console.error('获取首页数据失败:', error)
        this.$toast.fail('获取数据失败')
      } finally {
        this.loading = false
      }
    },
    isNewProduct(product) {
      const thirtyDaysAgo = new Date()
      thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
      return new Date(product.createTime) > thirtyDaysAgo
    },
    goToDetail(id) {
      if (!id || isNaN(id)) {
        this.$toast('商品ID无效')
        return
      }
      this.$router.push(`/product/detail/${id}`)
    },
    async addToCart(product) {
      if (product.stock <= 0) {
        this.$toast('商品已售罄')
        return
      }
      
      try {
        await this.$store.dispatch('addToCart', {
          productId: product.id,
          quantity: 1
        })
        this.$toast.success('添加成功')
      } catch (error) {
        if (error.message === '请先登录') {
          // 保存当前路径，登录后返回
          this.$router.push({
            path: '/login',
            query: { redirect: this.$route.fullPath }
          })
        } else {
          this.$toast.fail(error.message || '添加失败')
        }
      }
    },
    getImageUrl(path) {
      if (!path) return '/images/default.jpg'
      if (path.startsWith('http')) return path
      return `${IMAGE_BASE_URL}${path}`
    },
    onImageError(event) {
      // 使用默认商品图片
      event.target.src = '/images/default.jpg'
      // 添加onerror处理，防止死循环
      event.target.onerror = null
    }
  }
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: #f7f8fa;
}

.banner {
  height: 180px;
  background: #fff;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
}

.banner-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 4px;
}

.banner-desc {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.banner-desc .price {
  font-size: 18px;
  color: #ff6b81;
}

.nav-grid {
  background: #fff;
  margin: 10px 0;
}

.section {
  background: #fff;
  margin: 10px 0;
  padding: 15px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 0 10px;
}

.product-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.product-info {
  padding: 10px;
}

.product-name {
  font-size: 14px;
  color: #323233;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.product-category {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.product-price {
  color: #f44;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 6px;
}

.product-tags {
  margin-bottom: 8px;
}

.product-tags .van-tag {
  margin-right: 4px;
}

.product-stock {
  font-size: 12px;
  color: #969799;
  margin-bottom: 6px;
}

.empty-tip {
  text-align: center;
  color: #969799;
  padding: 30px 0;
}

.van-button--mini {
  width: 100%;
}
</style> 