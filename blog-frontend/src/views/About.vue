<template>
  <div class="about">
    <div class="profile-card">
      <img v-if="config.blogLogo" :src="config.blogLogo" class="avatar" alt="avatar" />
      <div v-else class="avatar avatar-placeholder">{{ (config.blogTitle || '我').charAt(0) }}</div>
      <div class="profile-info">
        <h1 class="blog-title">{{ config.blogTitle || '我的博客' }}</h1>
        <p class="blog-subtitle">{{ config.blogSubtitle || '记录与分享' }}</p>
      </div>
    </div>

    <div class="about-card">
      <h2 class="section-title">关于我</h2>
      <div v-if="aboutHtml" class="markdown-body" v-html="aboutHtml"></div>
      <el-empty v-else description="博主还没有填写介绍" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useSiteStore } from '../stores/site'
import { renderMarkdown } from '../utils/markdown'

const siteStore = useSiteStore()
const config = computed(() => siteStore.config)
const aboutHtml = computed(() => renderMarkdown(config.value.aboutMe))

onMounted(async () => {
  await siteStore.loadConfig().catch(() => {})
  document.title = `关于我 - ${config.value.blogTitle || '我的博客'}`
})
</script>

<style scoped>
.about {
  max-width: 820px;
  margin: 0 auto;
}
.profile-card {
  background: #fff;
  border-radius: 10px;
  padding: 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;
}
.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  flex: none;
}
.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  color: #fff;
  font-size: 28px;
  font-weight: 600;
}
.blog-title {
  margin: 0 0 6px;
  font-size: 24px;
  color: #1f2d3d;
}
.blog-subtitle {
  margin: 0;
  color: #7b8794;
  font-size: 14px;
}
.about-card {
  background: #fff;
  border-radius: 10px;
  padding: 28px;
}
.section-title {
  margin: 0 0 18px;
  font-size: 18px;
  color: #1f2d3d;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}
@media (max-width: 900px) {
  .profile-card {
    flex-direction: column;
    text-align: center;
  }
  .about-card,
  .profile-card {
    padding: 20px;
  }
}
</style>
