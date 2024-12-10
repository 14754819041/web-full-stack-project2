<template>
  <div class="address-list">
    <van-nav-bar
      title="收货地址"
      left-arrow
      @click-left="$router.back()"
    />
    
    <van-address-list
      v-model="selectedAddressId"
      :list="formattedAddresses"
      default-tag-text="默认"
      @add="onAdd"
      @edit="onEdit"
      @select="onSelect"
    />
  </div>
</template>

<script>
export default {
  name: 'AddressList',
  data() {
    return {
      selectedAddressId: '',
      addresses: []
    }
  },
  computed: {
    formattedAddresses() {
      return this.addresses.map(addr => ({
        id: addr.id,
        name: addr.name,
        tel: addr.phone,
        address: `${addr.province}${addr.city}${addr.district}${addr.detail}`,
        isDefault: addr.isDefault
      }))
    }
  },
  created() {
    this.fetchAddresses()
  },
  methods: {
    async fetchAddresses() {
      try {
        const addresses = await this.$store.dispatch('fetchAddresses')
        this.addresses = addresses
        const defaultAddress = addresses.find(addr => addr.isDefault)
        if (defaultAddress) {
          this.selectedAddressId = defaultAddress.id
        }
      } catch (error) {
        this.$toast.fail('获取地址列表失败')
      }
    },
    onAdd() {
      this.$router.push('/address/edit')
    },
    onEdit(address) {
      this.$router.push(`/address/edit/${address.id}`)
    },
    onSelect(address) {
      // 如果是从订单确认页面跳转来的，选择地址后返回
      if (this.$route.query.from === 'order') {
        this.$store.commit('setSelectedAddress', {
          id: address.id,
          name: address.name,
          phone: address.tel,
          fullAddress: address.address
        })
        this.$router.back()
      }
    }
  }
}
</script>

<style scoped>
.address-list {
  min-height: 100vh;
  background: #f7f8fa;
}
</style> 