<template>
  <div class="home">
    <section class="content">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索文章标题或摘要"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        <el-select
          v-model="categoryId"
          placeholder="全部分类"
          clearable
          style="width: 150px"
          @change="handleSearch"
        >
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>

      <div v-if="activeTag" class="filter-tip">
        当前标签：<el-tag closable size="small" @close="clearTag">{{ activeTag.name }}</el-tag>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated />

      <el-empty v-else-if="!list.length" description="暂无文章" />

      <template v-else>
        <article v-for="item in list" :key="item.id" class="post-card" @click="goDetail(item.id)">
          <div v-if="item.cover" class="post-cover">
            <img :src="item.cover" :alt="item.title" loading="lazy" />
          </div>
          <div class="post-body">
            <h2 class="post-title">{{ item.title }}</h2>
            <p class="post-summary">{{ item.summary || '暂无摘要' }}</p>
            <div class="post-meta">
              <span>{{ formatDate(item.publishedAt || item.createdAt) }}</span>
              <span v-if="item.categoryName" class="category">{{ item.categoryName }}</span>
              <span>{{ item.views ?? 0 }} 阅读</span>
              <span>{{ item.commentCount ?? 0 }} 评论</span>
              <el-tag
                v-for="tag in item.tags"
                :key="tag.id"
                size="small"
                :color="tag.color || undefined"
                :style="tag.color ? 'border:none;color:#fff' : ''"
              >
                {{ tag.name }}
              </el-tag>
            </div>
          </div>
        </article>

        <div class="pager">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            background
            @current-change="handlePageChange"
            @size-change="handleSearch"
          />
        </div>
      </template>
    </section>

    <aside class="sidebar">
      <div class="widget">
        <h3 class="widget-title">分类</h3>
        <ul class="category-list">
          <li :class="{ active: categoryId === null }" @click="selectCategory(null)">
            <span>全部</span>
          </li>
          <li
            v-for="c in categories"
            :key="c.id"
            :class="{ active: categoryId === c.id }"
            @click="selectCategory(c.id)"
          >
            <span>{{ c.name }}</span>
            <span class="count">{{ c.articleCount }}</span>
          </li>
        </ul>
      </div>

      <div class="widget">
        <h3 class="widget-title">标签</h3>
        <div v-if="!tags.length" class="empty-tip">暂无标签</div>
        <div v-else class="tag-cloud">
          <span
            v-for="t in tags"
            :key="t.id"
            class="tag-item"
            :class="{ active: tagId === t.id }"
            :style="t.color ? { '--tag-color': t.color } : {}"
            @click="selectTag(t.id)"
          >
            {{ t.name }} ({{ t.articleCount }})
          </span>
        </div>
      </div>

      <div class="widget">
        <h3 class="widget-title">热门文章</h3>
        <div v-if="!hot.length" class="empty-tip">暂无数据</div>
        <ol v-else class="hot-list">
          <li v-for="(h, index) in hot" :key="h.id" @click="goDetail(h.id)">
            <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
            <span class="hot-title" :title="h.title">{{ h.title }}</span>
            <span class="count">{{ h.views }}</span>
          </li>
        </ol>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi, siteApi } from '../api'
import { useSiteStore } from '../stores/site'

const router = useRouter()
const siteStore = useSiteStore()

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const categoryId = ref(null)
const tagId = ref(null)
const loading = ref(false)

const categories = ref([])
const tags = ref([])
const hot = ref([])

const activeTag = computed(() => tags.value.find(t => t.id === tagId.value) || null)

function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 10) : ''
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    if (categoryId.value != null) params.categoryId = categoryId.value
    if (tagId.value != null) params.tagId = tagId.value
    const data = await articleApi.list(params)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadSidebar() {
  const [cats, tagList, hotPage] = await Promise.all([
    siteApi.categories(),
    siteApi.tags(),
    articleApi.list({ page: 1, size: 5, orderBy: 'views' })
  ])
  categories.value = cats
  tags.value = tagList
  hot.value = hotPage.records
}

function scrollTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handleSearch() {
  page.value = 1
  load()
  scrollTop()
}

function handlePageChange() {
  load()
  scrollTop()
}

function selectCategory(id) {
  categoryId.value = id
  handleSearch()
}

function selectTag(id) {
  tagId.value = tagId.value === id ? null : id
  handleSearch()
}

function clearTag() {
  tagId.value = null
  handleSearch()
}

function goDetail(id) {
  router.push(`/article/${id}`)
}

onMounted(async () => {
  await siteStore.loadConfig().catch(() => {})
  document.title = siteStore.config.blogTitle || '我的博客'
  load()
  loadSidebar()
})
</script>

<style scoped>
.home {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}
.content {
  flex: 1;
  min-width: 0;
}
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.filter-tip {
  margin-bottom: 12px;
  color: #5c6b7a;
  font-size: 14px;
}
.post-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  padding: 18px 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  gap: 16px;
}
.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(31, 45, 61, 0.08);
}
.post-cover {
  width: 180px;
  height: 120px;
  flex: none;
  border-radius: 6px;
  overflow: hidden;
}
.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.post-body {
  flex: 1;
  min-width: 0;
}
.post-title {
  margin: 0 0 8px;
  font-size: 19px;
  color: #1f2d3d;
  line-height: 1.4;
}
.post-card:hover .post-title {
  color: #409eff;
}
.post-summary {
  margin: 0 0 12px;
  color: #7b8794;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.post-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  color: #909399;
  font-size: 13px;
}
.post-meta .category {
  color: #409eff;
}
.pager {
  display: flex;
  justify-content: center;
}
.sidebar {
  width: 280px;
  flex: none;
}
.widget {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}
.widget-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}
.category-list,
.hot-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.category-list li {
  display: flex;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
  color: #5c6b7a;
  font-size: 14px;
}
.category-list li:hover,
.category-list li.active {
  background: #f0f7ff;
  color: #409eff;
}
.count {
  color: #c0c4cc;
  font-size: 12px;
}
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag-item {
  padding: 4px 10px;
  border-radius: 12px;
  background: #f2f3f5;
  color: #5c6b7a;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.tag-item:hover,
.tag-item.active {
  background: var(--tag-color, #409eff);
  color: #fff;
}
.hot-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 0;
  cursor: pointer;
  font-size: 14px;
  color: #5c6b7a;
}
.hot-list li:hover .hot-title {
  color: #409eff;
}
.rank {
  width: 18px;
  height: 18px;
  flex: none;
  border-radius: 4px;
  background: #f2f3f5;
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rank.top {
  background: #f56c6c;
  color: #fff;
}
.hot-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.empty-tip {
  color: #c0c4cc;
  font-size: 13px;
}
@media (max-width: 900px) {
  .home {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
  .post-cover {
    display: none;
  }
}
</style>
