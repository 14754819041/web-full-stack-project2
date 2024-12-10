<template>
  <div class="search-bar">
    <van-search
      v-model="keyword"
      placeholder="请输入搜索关键词"
      shape="round"
      @search="onSearch"
      @focus="onFocus"
    >
      <template #left>
        <van-icon name="location-o" size="18" />
        <span class="location">{{ location }}</span>
      </template>
    </van-search>
  </div>
</template>

<script>
export default {
  name: 'SearchBar',
  data() {
    return {
      keyword: '',
      location: '北京'
    }
  },
  methods: {
    onSearch() {
      this.$router.push({
        path: '/product/list',
        query: { keyword: this.keyword }
      })
    },
    onFocus() {
      this.$router.push('/search')
    }
  }
}
</script>

<style scoped>
.search-bar {
  padding: 8px 0;
  background: #fff;
}

.location {
  font-size: 14px;
  margin-left: 4px;
  color: #666;
}
</style> 