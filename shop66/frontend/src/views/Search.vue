<template>
  <div class="search">
    <van-nav-bar title="搜索" left-arrow @click-left="$router.back()"/>
    
    <van-search
      v-model="keyword"
      show-action
      placeholder="请输入搜索关键词"
      @search="onSearch"
      @cancel="onCancel"
    >
      <template #action>
        <div @click="onSearch">搜索</div>
      </template>
    </van-search>

    <!-- 搜索历史 -->
    <div class="search-history" v-if="history.length && !keyword">
      <div class="history-header">
        <span>搜索历史</span>
        <van-icon name="delete-o" @click="clearHistory"/>
      </div>
      <div class="history-list">
        <van-tag
          v-for="item in history"
          :key="item"
          plain
          type="primary"
          size="medium"
          @click="onHistoryClick(item)"
        >
          {{ item }}
        </van-tag>
      </div>
    </div>

    <!-- 热门搜索 -->
    <div class="hot-search">
      <div class="hot-header">热门搜索</div>
      <div class="hot-list">
        <van-tag
          v-for="item in hotKeywords"
          :key="item"
          plain
          type="danger"
          size="medium"
          @click="onHistoryClick(item)"
        >
          {{ item }}
        </van-tag>
      </div>
    </div>
  </div>
</template>

<script>
const HISTORY_KEY = 'search_history'
const MAX_HISTORY = 10

export default {
  name: 'Search',
  data() {
    return {
      keyword: '',
      history: [],
      hotKeywords: ['手机', '电脑', '耳机', '平板', '相机', '手表']
    }
  },
  created() {
    // 从localStorage加载搜索历史
    const history = localStorage.getItem(HISTORY_KEY)
    if (history) {
      this.history = JSON.parse(history)
    }
  },
  methods: {
    onSearch() {
      if (!this.keyword.trim()) return
      
      // 保存到搜索历史
      this.saveHistory(this.keyword)
      
      // 跳转到搜索结果页
      this.$router.push({
        path: '/product/list',
        query: { keyword: this.keyword }
      })
    },
    onCancel() {
      this.$router.back()
    },
    onHistoryClick(keyword) {
      this.keyword = keyword
      this.onSearch()
    },
    saveHistory(keyword) {
      const index = this.history.indexOf(keyword)
      if (index !== -1) {
        this.history.splice(index, 1)
      }
      this.history.unshift(keyword)
      
      // 限制历史记录数量
      if (this.history.length > MAX_HISTORY) {
        this.history.pop()
      }
      
      // 保存到localStorage
      localStorage.setItem(HISTORY_KEY, JSON.stringify(this.history))
    },
    clearHistory() {
      this.$dialog.confirm({
        title: '提示',
        message: '确定要清空搜索历史吗？'
      }).then(() => {
        this.history = []
        localStorage.removeItem(HISTORY_KEY)
      })
    }
  }
}
</script>

<style scoped>
.search {
  min-height: 100vh;
  background: #f7f8fa;
}

.search-history,
.hot-search {
  background: #fff;
  margin-top: 10px;
  padding: 15px;
}

.history-header,
.hot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  color: #323233;
  font-size: 14px;
}

.history-list,
.hot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.van-tag {
  cursor: pointer;
}
</style> 