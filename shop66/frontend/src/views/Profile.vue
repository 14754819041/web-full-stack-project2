<template>
  <div class="profile">
    <van-nav-bar title="我的"/>
    
    <!-- 用户信息 -->
    <div v-if="user" class="user-info">
      <van-image
        round
        width="64"
        height="64"
        :src="user.avatar || 'https://img.yzcdn.cn/vant/cat.jpeg'"
      />
      <div class="user-name">{{ user.username }}</div>
      <van-button size="small" @click="logout">退出登录</van-button>
    </div>
    <div v-else class="user-placeholder">
      <van-image
        round
        width="64"
        height="64"
        src="https://img.yzcdn.cn/vant/cat.jpeg"
      />
      <div class="login-buttons">
        <van-button plain type="info" size="small" @click="$router.push('/login')">登录</van-button>
        <van-button plain type="info" size="small" @click="$router.push('/register')">注册</van-button>
      </div>
    </div>

    <!-- 订单入口 -->
    <van-cell-group title="我的订单">
      <van-cell title="全部订单" is-link to="/order/list"/>
      <van-grid :column-num="4" :border="false">
        <van-grid-item
          icon="pending-payment"
          text="待付款"
          to="/order/list?status=UNPAID"
        />
        <van-grid-item
          icon="logistics"
          text="待发货"
          to="/order/list?status=PAID"
        />
        <van-grid-item
          icon="logistics"
          text="待收货"
          to="/order/list?status=SHIPPED"
        />
        <van-grid-item
          icon="comment-o"
          text="待评价"
          to="/order/list?status=RECEIVED"
        />
      </van-grid>
    </van-cell-group>

    <!-- 其他功能 -->
    <van-cell-group title="我的服务">
      <van-grid :column-num="4" :border="false">
        <van-grid-item icon="star" text="我的收藏" to="/favorite"/>
        <van-grid-item icon="location-o" text="收货地址" to="/address/list"/>
        <van-grid-item icon="service-o" text="联系客服"/>
        <van-grid-item icon="setting-o" text="设置" to="/settings"/>
      </van-grid>
    </van-cell-group>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'Profile',
  computed: {
    ...mapState(['user'])
  },
  methods: {
    async logout() {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确认退出登录吗？'
        })

        // 调用退出登录action
        await this.$store.dispatch('logout')
        
        this.$toast.success('退出成功')
        
        // 跳转到登录页
        this.$router.replace({
          path: '/login',
          query: { redirect: '/' }
        })
      } catch (error) {
        if (error.message !== '取消') {
          this.$toast.fail('退出失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.profile {
  min-height: 100vh;
  background: #f7f8fa;
}

.user-info,
.user-placeholder {
  text-align: center;
  padding: 30px 0;
  background: #fff;
}

.user-name {
  margin: 10px 0;
  font-size: 16px;
}

.login-buttons {
  margin-top: 15px;
}

.login-buttons .van-button {
  margin: 0 5px;
}
</style> 