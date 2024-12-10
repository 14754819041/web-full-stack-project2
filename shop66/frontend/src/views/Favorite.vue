<template>
  <div class="favorite">
    <van-nav-bar
      title="我的收藏"
      left-arrow
      @click-left="$router.back()"
    />
    
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div class="product-list">
          <van-card
            v-for="item in favorites"
            :key="item.id"
            :price="item.price"
            :title="item.name"
            :thumb="item.image"
            @click="goToDetail(item)"
          >
            <template #footer>
              <van-button size="mini" @click.stop="cancelFavorite(item)">取消收藏</van-button>
              <van-button size="mini" type="danger" @click.stop="addToCart(item)">加入购物车</van-button>
            </template>
          </van-card>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script>
export default {
  name: 'Favorite',
  data() {
    return {
      loading: false,
      finished: false,
      refreshing: false,
      page: 1,
      favorites: []
    }
  },
  methods: {
    async onLoad() {
      if (this.refreshing) {
        this.favorites = []
        this.refreshing = false
      }
      
      try {
        const params = { page: this.page }
        const response = await this.$store.dispatch('fetchFavorites', params)
        
        if (response.length < 10) {
          this.finished = true
        }
        this.favorites.push(...response)
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
    goToDetail(item) {
      this.$router.push(`/product/${item.productId}`)
    },
    async cancelFavorite(item) {
      try {
        await this.$store.dispatch('cancelFavorite', item.id)
        this.$toast.success('取消成功')
        this.favorites = this.favorites.filter(f => f.id !== item.id)
      } catch (error) {
        this.$toast.fail('取消失败')
      }
    },
    async addToCart(item) {
      try {
        await this.$store.dispatch('addToCart', {
          productId: item.productId,
          quantity: 1
        })
        this.$toast.success('添加成功')
      } catch (error) {
        this.$toast.fail('添加失败')
      }
    }
  }
}
</script>

<style scoped>
.favorite {
  min-height: 100vh;
  background: #f7f8fa;
}

.product-list {
  padding: 10px;
}

.van-card {
  margin-bottom: 10px;
  background: #fff;
}

.van-card__footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 