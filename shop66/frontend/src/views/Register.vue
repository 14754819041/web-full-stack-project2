<template>
  <div class="register">
    <van-nav-bar title="注册" left-arrow @click-left="onClickLeft"/>
    
    <van-form @submit="onSubmit">
      <van-field
        v-model="username"
        name="username"
        label="用户名"
        placeholder="请输入用户名"
        :rules="[{ required: true, message: '请填写用户名' }]"
      />
      <van-field
        v-model="password"
        type="password"
        name="password"
        label="密码"
        placeholder="请输入密码"
        :rules="[{ required: true, message: '请填写密码' }]"
      />
      <van-field
        v-model="confirmPassword"
        type="password"
        name="confirmPassword"
        label="确认密码"
        placeholder="请再次输入密码"
        :rules="[{ required: true, message: '请确认密码' }]"
      />
      <div style="margin: 16px;">
        <van-button round block type="info" native-type="submit">注册</van-button>
      </div>
    </van-form>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    return {
      username: '',
      password: '',
      confirmPassword: ''
    }
  },
  methods: {
    onClickLeft() {
      this.$router.back()
    },
    async onSubmit(values) {
      if (values.password !== this.confirmPassword) {
        return this.$toast.fail('两次输入的密码不一致')
      }
      try {
        await this.$store.dispatch('register', values)
        this.$router.push('/login')
        this.$toast.success('注册成功')
      } catch (error) {
        this.$toast.fail(error.message || '注册失败')
      }
    }
  }
}
</script> 