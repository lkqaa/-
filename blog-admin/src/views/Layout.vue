<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="22"><Reading /></el-icon>
        <span>博客管理后台</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        class="menu"
      >
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon>仪表盘</el-menu-item>
        <el-menu-item index="/articles"><el-icon><Document /></el-icon>文章管理</el-menu-item>
        <el-menu-item index="/categories"><el-icon><Folder /></el-icon>分类管理</el-menu-item>
        <el-menu-item index="/tags"><el-icon><PriceTag /></el-icon>标签管理</el-menu-item>
        <el-menu-item index="/comments"><el-icon><ChatDotRound /></el-icon>评论管理</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">{{ $route.meta.title }}</div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user">
              <el-avatar :size="28" :src="userStore.adminInfo?.avatar" class="avatar">
                {{ (userStore.adminInfo?.nickname || 'A').charAt(0) }}
              </el-avatar>
              <span class="nickname">{{ userStore.adminInfo?.nickname || 'Admin' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  if (!userStore.adminInfo) {
    userStore.fetchInfo().catch(() => {})
  }
})

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #304156; overflow-x: hidden; }
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.menu { border-right: none; }
.menu :deep(.el-menu-item.is-active) { background: #1f2d3d; }
.header {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left { font-size: 16px; font-weight: 600; }
.user { display: flex; align-items: center; gap: 8px; cursor: pointer; outline: none; }
.avatar { background: #409eff; }
.nickname { font-size: 14px; }
.main { background: #f5f7fa; padding: 16px; }
</style>
