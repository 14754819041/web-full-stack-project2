<template>
  <div class="login">
    <van-nav-bar 
      title="登录" 
      left-arrow 
      @click-left="$router.back()"
    />
    
    <van-form @submit="onSubmit">
      <van-field
        v-model="form.username"
        name="username"
        label="用户名"
        placeholder="请输入用户名"
        autocomplete="username"
        :rules="[{ required: true, message: '请填写用户名' }]"
      />
      <van-field
        v-model="form.password"
        type="password"
        name="password"
        label="密码"
        placeholder="请输入密码"
        autocomplete="current-password"
        :rules="[{ required: true, message: '请填写密码' }]"
      />
      <div style="margin: 16px;">
        <van-button 
          round 
          block 
          type="info" 
          native-type="submit" 
          :loading="loading"
        >
          登录
        </van-button>
        <van-button
          round
          block
          plain
          type="info"
          @click="$router.push('/register')"
          style="margin-top: 10px;"
        >
          注册账号
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      loading: false
    }
  },
  methods: {
    async onSubmit() {
      if (this.loading) return
      this.loading = true

      try {
        await this.$store.dispatch('login', this.form)
        this.$toast.success('登录成功')
        
        // 获取登录前的路径
        const redirect = this.$route.query.redirect || '/'
        this.$router.replace(redirect)
      } catch (error) {
        this.$toast.fail(error.message || '登录失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login {
  padding: 20px;
}
</style> 