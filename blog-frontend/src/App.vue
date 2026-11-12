<template>
  <div class="site">
    <header class="site-header">
      <div class="container header-inner">
        <router-link to="/" class="brand">
          <img v-if="config.blogLogo" :src="config.blogLogo" class="brand-logo" alt="logo" />
          <span class="brand-title">{{ config.blogTitle || '我的博客' }}</span>
        </router-link>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/about" class="nav-link">关于我</router-link>
        </nav>
      </div>
    </header>

    <main class="container main">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="container">
        © {{ year }} {{ config.blogTitle || '我的博客' }} · Powered by Vue3 + Spring Boot
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useSiteStore } from './stores/site'

const siteStore = useSiteStore()
const config = computed(() => siteStore.config)
const year = new Date().getFullYear()

onMounted(() => {
  siteStore.loadConfig().catch(() => {})
})
</script>

<style scoped>
.site {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.site-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(6px);
  border-bottom: 1px solid #ebeef5;
}
.header-inner {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}
.brand-logo {
  width: 30px;
  height: 30px;
  border-radius: 6px;
  object-fit: cover;
}
.nav {
  display: flex;
  gap: 24px;
}
.nav-link {
  font-size: 15px;
  color: #5c6b7a;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  transition: color 0.2s;
}
.nav-link:hover {
  color: #409eff;
}
.nav-link.router-link-active {
  color: #409eff;
  border-bottom-color: #409eff;
}
.main {
  flex: 1;
  padding-top: 24px;
  padding-bottom: 40px;
}
.site-footer {
  border-top: 1px solid #ebeef5;
  background: #fff;
  padding: 20px 0;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
</style>
