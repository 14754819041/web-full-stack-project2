<template>
  <div class="category">
    <van-nav-bar title="商品分类"/>
    
    <div class="category-content">
      <!-- 左侧分类菜单 -->
      <div class="category-nav">
        <div 
          v-for="item in parentCategories" 
          :key="item.id"
          :class="['nav-item', { active: activeId === item.id }]"
          @click="activeId = item.id"
        >
          {{ item.name }}
        </div>
      </div>

      <!-- 右侧子分类 -->
      <div class="category-list">
        <div class="category-title">{{ activeCategory ? activeCategory.name : '全部分类' }}</div>
        <div class="sub-list">
          <div 
            v-for="item in subCategories" 
            :key="item.id"
            class="sub-item"
            @click="goToList(item.id)"
          >
            <van-image
              width="60"
              height="60"
              :src="getImageUrl(item)"
              @error="onImageError"
            />
            <div class="sub-name">{{ item.name }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { IMAGE_BASE_URL } from '@/utils/config'

export default {
  name: 'Category',
  data() {
    return {
      activeId: null,
      categories: []
    }
  },
  computed: {
    // 一级分类 (parentId为null的分类)
    parentCategories() {
      return this.categories.filter(item => !item.parentId && item.level === 1)
    },
    // 当前选中的一级分类
    activeCategory() {
      return this.categories.find(item => item.id === this.activeId)
    },
    // 当前选中分类的子分类
    subCategories() {
      return this.categories.filter(item => item.parentId === this.activeId)
    }
  },
  created() {
    this.fetchCategories()
  },
  methods: {
    async fetchCategories() {
      try {
        const categories = await this.$store.dispatch('fetchCategories')
        console.log('分类数据:', categories)
        
        // 过滤掉重复的分类
        const uniqueCategories = categories.reduce((acc, curr) => {
          const exists = acc.find(item => 
            item.name === curr.name && 
            item.level === curr.level && 
            item.parentId === curr.parentId
          )
          if (!exists) {
            acc.push(curr)
          }
          return acc
        }, [])
        
        this.categories = uniqueCategories
        
        // 默认选中第一个一级分类
        if (this.categories.length > 0) {
          const firstParent = this.categories.find(item => !item.parentId && item.level === 1)
          if (firstParent) {
            this.activeId = firstParent.id
          }
        }
      } catch (error) {
        console.error('获取分类失败:', error)
        this.$toast.fail('获取分类失败')
      }
    },
    getImageUrl(category) {
      // 根据分类生成默认图片
      const defaultImages = {
        '手机': '/images/category/phone.png',
        '手机数码': '/images/category/digital.png',
        '电脑': '/images/category/computer.png',
        '电脑办公': '/images/category/computer.png',
        '家电': '/images/category/appliance.png',
        '家用电器': '/images/category/appliance.png',
        '数码配件': '/images/category/digital.png',
        '平板电脑': '/images/category/tablet.png',
        '笔记本': '/images/category/laptop.png',
        '台式机': '/images/category/desktop.png',
        '办公设备': '/images/category/office.png',
        '冰箱': '/images/category/fridge.png',
        '洗衣机': '/images/category/washer.png',
        '空调': '/images/category/ac.png'
      }
      
      // 如果有图片路径，使用实际图片
      if (category.image) {
        return `${IMAGE_BASE_URL}${category.image}`
      }
      
      // 否则使用默认图片
      return defaultImages[category.name] || '/images/default.jpg'
    },
    onImageError(event) {
      event.target.src = '/images/default.jpg'
      event.target.onerror = null
    },
    goToList(categoryId) {
      if (!categoryId || isNaN(categoryId)) {
        this.$toast('分类ID无效')
        return
      }
      this.$router.push(`/product/list?categoryId=${categoryId}`)
    }
  }
}
</script>

<style scoped>
.category {
  min-height: 100vh;
  background: #f7f8fa;
}

.category-content {
  display: flex;
  position: fixed;
  top: 46px;
  bottom: 0;
  width: 100%;
}

.category-nav {
  width: 85px;
  height: 100%;
  background: #fff;
  overflow-y: auto;
}

.nav-item {
  height: 45px;
  line-height: 45px;
  text-align: center;
  font-size: 14px;
  color: #333;
  border-left: 3px solid transparent;
}

.nav-item.active {
  color: #1989fa;
  background: #f7f8fa;
  border-left-color: #1989fa;
}

.category-list {
  flex: 1;
  height: 100%;
  padding: 15px;
  background: #f7f8fa;
  overflow-y: auto;
}

.category-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

.sub-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.sub-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  padding: 10px;
  border-radius: 8px;
}

.sub-name {
  margin-top: 8px;
  font-size: 12px;
  color: #333;
}
</style> 