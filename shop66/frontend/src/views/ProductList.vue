<template>
  <div class="product-list-page">
    <!-- 导航栏 -->
    <van-nav-bar
      :title="pageTitle"
      left-arrow
      @click-left="$router.back()"
    />
    
    <!-- 筛选栏 -->
    <van-dropdown-menu>
      <van-dropdown-item v-model="sortBy" :options="sortOptions" />
      <van-dropdown-item v-model="priceRange" :options="priceOptions" />
    </van-dropdown-menu>

    <!-- 商品列表 -->
    <div class="product-list" v-if="products.length">
      <div 
        class="product-item" 
        v-for="product in products" 
        :key="product.id"
        @click="goToDetail(product.id)"
      >
        <van-image 
          :src="getImageUrl(product.mainImage)" 
          width="100%" 
          height="120"
          fit="cover"
          @error="onImageError"
        />
        <div class="product-info">
          <div class="product-name">{{ product.name }}</div>
          <div class="product-category">{{ product.categoryName }}</div>
          <div class="product-price">¥{{ product.price }}</div>
          <div class="product-tags">
            <van-tag type="danger" v-if="product.sales > 0">已售{{ product.sales }}件</van-tag>
            <van-tag type="warning" v-if="isNewProduct(product)">新品</van-tag>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <van-empty v-else description="暂无相关商品" />

    <!-- 加载更多 -->
    <van-loading v-if="loading" type="spinner" vertical>加载中...</van-loading>
    <van-divider v-if="finished" content-position="center">没有更多了</van-divider>
  </div>
</template>

<script>
import { IMAGE_BASE_URL } from '@/utils/config'

export default {
  name: 'ProductList',
  data() {
    return {
      loading: false,
      finished: false,
      current: 1,
      products: [],
      sortBy: 'default',
      priceRange: 'all',
      sortOptions: [
        { text: '默认排序', value: 'default' },
        { text: '销量优先', value: 'sales' },
        { text: '最新上架', value: 'createTime' },
        { text: '价格从低到高', value: 'priceAsc' },
        { text: '价格从高到低', value: 'priceDesc' }
      ],
      priceOptions: [
        { text: '全部价格', value: 'all' },
        { text: '0-1000元', value: '0-1000' },
        { text: '1000-3000元', value: '1000-3000' },
        { text: '3000-5000元', value: '3000-5000' },
        { text: '5000元以上', value: '5000-' }
      ]
    }
  },
  computed: {
    pageTitle() {
      const { keyword, categoryId } = this.$route.query
      if (keyword) return `搜索：${keyword}`
      if (categoryId) {
        const category = this.$store.state.categories.find(c => c.id === parseInt(categoryId))
        return category ? category.name : '商品列表'
      }
      return '商品列表'
    },
    queryParams() {
      const params = {
        current: this.current,
        size: 10,
        status: 1
      }

      // 添加搜索关键词
      if (this.$route.query.keyword) {
        params.keyword = this.$route.query.keyword
      }

      // 添加分类ID
      if (this.$route.query.categoryId) {
        params.categoryId = parseInt(this.$route.query.categoryId)
      }

      // 添加排序参数
      if (this.sortBy !== 'default') {
        params.sort = this.sortBy
      }

      // 添加价格区间
      if (this.priceRange !== 'all') {
        const [min, max] = this.priceRange.split('-')
        params.minPrice = parseInt(min)
        if (max) params.maxPrice = parseInt(max)
      }

      return params
    }
  },
  watch: {
    // 监听筛选条件变化，重新加载数据
    sortBy() {
      this.resetAndLoad()
    },
    priceRange() {
      this.resetAndLoad()
    },
    // 监听路由参数变化，重新加载数据
    '$route.query': {
      handler() {
        this.resetAndLoad()
      },
      immediate: true
    }
  },
  methods: {
    resetAndLoad() {
      this.current = 1
      this.finished = false
      this.products = []
      this.loadProducts()
    },
    async loadProducts() {
      if (this.loading || this.finished) return
      this.loading = true

      try {
        const response = await this.$store.dispatch('fetchProducts', this.queryParams)
        
        const { records, current, pages } = response
        
        this.products.push(...records)
        this.current = current + 1
        this.finished = current >= pages
      } catch (error) {
        console.error('获取商品列表失败:', error)
        this.$toast.fail('获取商品列表失败')
      } finally {
        this.loading = false
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
    goToDetail(id) {
      if (!id || isNaN(id)) {
        this.$toast('商品ID无效')
        return
      }
      this.$router.push(`/product/detail/${id}`)
    },
    isNewProduct(product) {
      const thirtyDaysAgo = new Date()
      thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
      return new Date(product.createTime) > thirtyDaysAgo
    }
  }
}
</script>

<style scoped>
.product-list-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 50px;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 10px;
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
</style> 