<template>
  <div class="address-edit">
    <van-nav-bar
      :title="isEdit ? '编辑地址' : '新增地址'"
      left-arrow
      @click-left="$router.back()"
    />
    
    <van-address-edit
      :area-list="areaList"
      :address-info="addressInfo"
      :show-delete="isEdit"
      show-set-default
      :area-columns-placeholder="['选择省份', '选择城市', '选择区县']"
      @save="onSave"
      @delete="onDelete"
    />
  </div>
</template>

<script>
import { areaList } from '@vant/area-data'

export default {
  name: 'AddressEdit',
  data() {
    return {
      areaList,
      addressInfo: {
        name: '',
        tel: '',
        province: '',
        city: '',
        county: '',
        addressDetail: '',
        areaCode: '',
        isDefault: false
      }
    }
  },
  computed: {
    isEdit() {
      return !!this.$route.params.id
    }
  },
  created() {
    if (this.isEdit) {
      this.fetchAddress()
    }
  },
  methods: {
    async fetchAddress() {
      try {
        const address = await this.$store.dispatch('fetchAddress', this.$route.params.id)
        this.addressInfo = {
          id: address.id,
          name: address.name,
          tel: address.phone,
          province: address.province,
          city: address.city,
          county: address.district,
          addressDetail: address.detail,
          areaCode: address.areaCode,
          isDefault: address.isDefault
        }
      } catch (error) {
        this.$toast.fail('获取地址信息失败')
      }
    },
    async onSave(content) {
      try {
        const addressData = {
          id: this.isEdit ? this.$route.params.id : null,
          name: content.name,
          phone: content.tel,
          province: content.province,
          city: content.city,
          district: content.county,
          detail: content.addressDetail,
          areaCode: content.areaCode,
          isDefault: content.isDefault
        }
        await this.$store.dispatch('saveAddress', addressData)
        this.$toast.success('保存成功')
        this.$router.back()
      } catch (error) {
        this.$toast.fail('保存失败')
      }
    },
    async onDelete() {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确定要删除此地址吗？'
        })
        await this.$store.dispatch('deleteAddress', this.$route.params.id)
        this.$toast.success('删除成功')
        this.$router.back()
      } catch (error) {
        if (error) {
          this.$toast.fail('删除失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.address-edit {
  min-height: 100vh;
  background: #f7f8fa;
}
</style> 