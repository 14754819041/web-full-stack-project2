<template>
  <div class="message">
    <van-nav-bar title="消息"/>
    
    <van-tabs v-model="active">
      <van-tab title="店铺消息">
        <van-list
          v-model="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <van-cell v-for="item in shopMessages" :key="item.id">
            <template #title>
              <div class="message-title">{{ item.title }}</div>
              <div class="message-content">{{ item.content }}</div>
              <div class="message-time">{{ item.time }}</div>
            </template>
          </van-cell>
        </van-list>
      </van-tab>
      
      <van-tab title="系统消息">
        <van-list
          v-model="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <van-cell v-for="item in systemMessages" :key="item.id">
            <template #title>
              <div class="message-title">{{ item.title }}</div>
              <div class="message-content">{{ item.content }}</div>
              <div class="message-time">{{ item.time }}</div>
            </template>
          </van-cell>
        </van-list>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script>
export default {
  name: 'Message',
  data() {
    return {
      active: 0,
      loading: false,
      finished: false,
      shopMessages: [],
      systemMessages: []
    }
  },
  methods: {
    onLoad() {
      // 加载消息数据
    }
  }
}
</script>

<style scoped>
.message-title {
  font-weight: bold;
  margin-bottom: 5px;
}
.message-content {
  color: #666;
  font-size: 14px;
}
.message-time {
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}
</style> 