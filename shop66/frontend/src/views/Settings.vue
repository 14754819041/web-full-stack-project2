<template>
  <div class="settings">
    <van-nav-bar
      title="设置"
      left-arrow
      @click-left="$router.back()"
    />
    
    <!-- 账号设置 -->
    <van-cell-group title="账号设置">
      <van-cell title="修改密码" is-link @click="onChangePassword"/>
      <van-cell title="绑定手机" is-link @click="onBindPhone"/>
      <van-cell title="实名认证" is-link @click="onVerifyIdentity"/>
    </van-cell-group>

    <!-- 消息设置 -->
    <van-cell-group title="消息设置">
      <van-cell title="推送通知">
        <template #right-icon>
          <van-switch v-model="settings.pushNotification" size="24"/>
        </template>
      </van-cell>
      <van-cell title="订单状态通知">
        <template #right-icon>
          <van-switch v-model="settings.orderNotification" size="24"/>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 其他设置 -->
    <van-cell-group title="其他设置">
      <van-cell title="清除缓存" is-link @click="onClearCache"/>
      <van-cell title="关于我们" is-link @click="onAbout"/>
      <van-cell title="用户协议" is-link @click="onUserAgreement"/>
      <van-cell title="隐私政策" is-link @click="onPrivacyPolicy"/>
    </van-cell-group>

    <!-- 退出登录按钮 -->
    <div class="logout-button">
      <van-button block type="danger" @click="onLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Settings',
  data() {
    return {
      settings: {
        pushNotification: true,
        orderNotification: true
      }
    }
  },
  methods: {
    onChangePassword() {
      this.$toast('修改密码功能开发中')
    },
    onBindPhone() {
      this.$toast('绑定手机功能开发中')
    },
    onVerifyIdentity() {
      this.$toast('实名认证功能开发中')
    },
    async onClearCache() {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确定要清除缓存吗？'
        })
        // 清除缓存的逻辑
        localStorage.clear()
        this.$toast.success('清除成功')
      } catch {
        // 用户取消了操作
      }
    },
    onAbout() {
      this.$router.push('/about')
    },
    onUserAgreement() {
      this.$router.push('/agreement')
    },
    onPrivacyPolicy() {
      this.$router.push('/privacy')
    },
    async onLogout() {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确定要退出登录吗？'
        })
        await this.$store.dispatch('logout')
        this.$toast.success('退出成功')
        this.$router.replace('/login')
      } catch (error) {
        if (error) this.$toast.fail('退出失败')
      }
    }
  }
}
</script>

<style scoped>
.settings {
  min-height: 100vh;
  background: #f7f8fa;
}

.logout-button {
  margin: 20px 16px;
}
</style> 