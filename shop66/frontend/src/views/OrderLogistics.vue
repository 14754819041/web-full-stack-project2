<template>
  <div class="logistics">
    <van-nav-bar
      title="物流信息"
      left-arrow
      @click-left="$router.back()"
    />
    
    <!-- 物流状态卡片 -->
    <div class="status-card">
      <van-cell-group>
        <van-cell>
          <template #title>
            <div class="delivery-info">
              <div class="company">{{ logistics.company }}</div>
              <div class="tracking-no">运单号：{{ logistics.trackingNo }}</div>
              <div class="status">{{ logistics.status }}</div>
            </div>
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <!-- 物流轨迹 -->
    <div class="timeline">
      <van-steps
        direction="vertical"
        :active="logistics.traces.length - 1"
      >
        <van-step
          v-for="(trace, index) in logistics.traces"
          :key="index"
        >
          <h3>{{ trace.content }}</h3>
          <p>{{ formatDate(trace.time) }}</p>
        </van-step>
      </van-steps>
    </div>

    <!-- 收货信息 -->
    <van-cell-group title="收货信息">
      <van-cell>
        <template #title>
          <div class="address-info">
            <div class="contact">
              <span>{{ logistics.address.name }}</span>
              <span>{{ logistics.address.phone }}</span>
            </div>
            <div class="address">{{ logistics.address.fullAddress }}</div>
          </div>
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script>
import { formatDate } from '@/utils/date'

export default {
  name: 'OrderLogistics',
  data() {
    return {
      logistics: {
        company: '',
        trackingNo: '',
        status: '',
        address: {},
        traces: []
      }
    }
  },
  created() {
    this.fetchLogistics()
  },
  methods: {
    formatDate,
    async fetchLogistics() {
      try {
        const logistics = await this.$store.dispatch('fetchLogistics', this.$route.params.id)
        this.logistics = logistics
      } catch (error) {
        this.$toast.fail('获取物流信息失败')
      }
    }
  }
}
</script>

<style scoped>
.logistics {
  min-height: 100vh;
  background: #f7f8fa;
}

.status-card {
  margin: 10px;
  border-radius: 8px;
  overflow: hidden;
}

.delivery-info {
  padding: 5px 0;
}

.company {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.tracking-no {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.status {
  color: #ee0a24;
  font-size: 14px;
}

.timeline {
  margin: 15px;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
}

.address-info {
  padding: 5px 0;
}

.contact {
  margin-bottom: 5px;
}

.contact span {
  margin-right: 10px;
}

.address {
  color: #666;
  font-size: 14px;
}

.timeline :deep(.van-step) .van-step__title {
  margin-bottom: 10px;
}

.timeline :deep(.van-step) h3 {
  font-size: 14px;
  margin: 0;
  color: #323233;
}

.timeline :deep(.van-step) p {
  margin: 0;
  font-size: 12px;
  color: #969799;
}
</style> 