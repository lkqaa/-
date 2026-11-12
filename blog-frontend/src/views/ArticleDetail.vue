<template>
  <div v-loading="loading" class="detail">
    <template v-if="article">
      <article class="post">
        <header class="post-header">
          <h1 class="post-title">{{ article.title }}</h1>
          <div class="post-meta">
            <span>{{ formatDate(article.publishedAt || article.createdAt) }}</span>
            <span v-if="article.categoryName" class="category">{{ article.categoryName }}</span>
            <span>{{ article.views ?? 0 }} 阅读</span>
            <span>{{ article.commentCount ?? 0 }} 评论</span>
          </div>
          <div v-if="article.tags?.length" class="post-tags">
            <el-tag
              v-for="tag in article.tags"
              :key="tag.id"
              size="small"
              :color="tag.color || undefined"
              :style="tag.color ? 'border:none;color:#fff' : ''"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </header>

        <div class="markdown-body" v-html="contentHtml"></div>
      </article>

      <nav class="neighbors">
        <div class="neighbor" :class="{ disabled: !article.prev }" @click="goArticle(article.prev)">
          <span class="label">上一篇</span>
          <span class="text">{{ article.prev?.title || '没有了' }}</span>
        </div>
        <div class="neighbor right" :class="{ disabled: !article.next }" @click="goArticle(article.next)">
          <span class="label">下一篇</span>
          <span class="text">{{ article.next?.title || '没有了' }}</span>
        </div>
      </nav>

      <section class="comments">
        <h3 class="section-title">评论（{{ commentsTotal }}）</h3>

        <el-empty v-if="!comments.length" description="还没有评论，来抢沙发吧" :image-size="70" />

        <div v-for="item in comments" :key="item.id" class="comment-item">
          <el-avatar :size="36" class="c-avatar">{{ (item.nickname || '匿').charAt(0) }}</el-avatar>
          <div class="c-main">
            <div class="c-head">
              <span class="c-name">{{ item.nickname }}</span>
              <a
                v-if="item.website"
                :href="item.website"
                target="_blank"
                rel="noopener noreferrer"
                class="c-site"
              >主页</a>
              <span class="c-time">{{ formatDateTime(item.createdAt) }}</span>
            </div>
            <div class="markdown-body c-content" v-html="renderMarkdown(item.content)"></div>
          </div>
        </div>

        <div v-if="commentsTotal > commentSize" class="pager">
          <el-pagination
            v-model:current-page="commentPage"
            :page-size="commentSize"
            :total="commentsTotal"
            layout="prev, pager, next"
            background
            small
            @current-change="loadComments"
          />
        </div>

        <div class="comment-form">
          <h4 class="form-title">发表评论</h4>
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-row :gutter="12">
              <el-col :xs="24" :sm="8">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="form.nickname" maxlength="64" placeholder="怎么称呼你" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-form-item label="邮箱（不公开）" prop="email">
                  <el-input v-model="form.email" maxlength="128" placeholder="选填" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-form-item label="网站" prop="website">
                  <el-input v-model="form.website" maxlength="255" placeholder="选填" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="内容（支持 Markdown）" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="4"
                maxlength="1000"
                show-word-limit
                placeholder="友善发言，共同进步～"
              />
            </el-form-item>
          </el-form>
          <el-button type="primary" :loading="submitting" @click="submitComment">提交评论</el-button>
        </div>
      </section>
    </template>

    <el-empty v-else-if="!loading" description="文章不存在或已下架">
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { articleApi } from '../api'
import { useSiteStore } from '../stores/site'
import { renderMarkdown } from '../utils/markdown'

const route = useRoute()
const router = useRouter()
const siteStore = useSiteStore()

const article = ref(null)
const loading = ref(false)
const comments = ref([])
const commentsTotal = ref(0)
const commentPage = ref(1)
const commentSize = 10
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({ nickname: '', email: '', website: '', content: '' })
const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  content: [{ required: true, message: '请输入评论内容', trigger: 'blur' }]
}

const contentHtml = computed(() => renderMarkdown(article.value?.content))

function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 10) : ''
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

function setMeta(title, description) {
  document.title = title
  let meta = document.querySelector('meta[name="description"]')
  if (!meta) {
    meta = document.createElement('meta')
    meta.name = 'description'
    document.head.appendChild(meta)
  }
  meta.content = description || ''
}

function plainText(markdown) {
  return (markdown || '').replace(/[#>*`~\-[\]()!]/g, ' ').replace(/\s+/g, ' ').trim()
}

async function loadArticle(id) {
  loading.value = true
  article.value = null
  try {
    const data = await articleApi.detail(id)
    article.value = data
    const siteTitle = siteStore.config.blogTitle || '我的博客'
    setMeta(`${data.title} - ${siteTitle}`, plainText(data.summary || data.content).slice(0, 120))

    articleApi.view(id).then(views => {
      if (article.value && article.value.id === id) article.value.views = views
    }).catch(() => {})
  } catch (e) {
    article.value = null
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  const data = await articleApi.comments(route.params.id, {
    page: commentPage.value,
    size: commentSize
  })
  comments.value = data.records
  commentsTotal.value = data.total
}

async function submitComment() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await articleApi.addComment(route.params.id, { ...form })
    ElMessage.success('评论成功')
    form.content = ''
    commentPage.value = 1
    await loadComments()
  } finally {
    submitting.value = false
  }
}

function goArticle(brief) {
  if (!brief) return
  router.push(`/article/${brief.id}`)
}

async function init(id) {
  commentPage.value = 1
  window.scrollTo({ top: 0 })
  await Promise.all([
    siteStore.loadConfig().catch(() => {}),
    loadArticle(id)
  ])
  await loadComments()
}

watch(
  () => route.params.id,
  id => {
    if (id) init(id)
  }
)

onMounted(() => {
  if (route.params.id) init(route.params.id)
})
</script>

<style scoped>
.detail {
  max-width: 860px;
  margin: 0 auto;
}
.post {
  background: #fff;
  border-radius: 8px;
  padding: 32px 36px;
}
.post-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}
.post-title {
  margin: 0 0 14px;
  font-size: 28px;
  line-height: 1.4;
  color: #1f2d3d;
}
.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: #909399;
  font-size: 13px;
}
.post-meta .category {
  color: #409eff;
}
.post-tags {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
.neighbors {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}
.neighbor {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: color 0.2s;
}
.neighbor:hover {
  color: #409eff;
}
.neighbor.right {
  align-items: flex-end;
  text-align: right;
}
.neighbor.disabled {
  cursor: default;
  color: #c0c4cc;
}
.neighbor .label {
  font-size: 12px;
  color: #909399;
}
.neighbor .text {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
.comments {
  background: #fff;
  border-radius: 8px;
  padding: 24px 28px;
  margin-top: 16px;
}
.section-title {
  margin: 0 0 20px;
  font-size: 18px;
  color: #1f2d3d;
}
.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f5f6f8;
}
.c-avatar {
  flex: none;
  background: #409eff;
}
.c-main {
  flex: 1;
  min-width: 0;
}
.c-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.c-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}
.c-site {
  font-size: 12px;
  color: #409eff;
}
.c-time {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}
.c-content {
  font-size: 14px;
  line-height: 1.7;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
.comment-form {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px dashed #ebeef5;
}
.form-title {
  margin: 0 0 14px;
  font-size: 15px;
  color: #1f2d3d;
}
@media (max-width: 900px) {
  .post {
    padding: 20px;
  }
  .post-title {
    font-size: 22px;
  }
  .neighbors {
    flex-direction: column;
  }
  .neighbor.right {
    align-items: flex-start;
    text-align: left;
  }
  .comments {
    padding: 20px 16px;
  }
}
</style>
