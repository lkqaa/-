<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索标题或摘要"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 150px">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="toolbar-right">
        <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">
          批量删除
        </el-button>
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建文章
        </el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="list" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="46" />
      <el-table-column label="标题" min-width="220">
        <template #default="{ row }">
          <el-tag v-if="row.isTop === 1" size="small" type="danger" effect="plain" class="mr4">置顶</el-tag>
          <span class="title">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="110">
        <template #default="{ row }">{{ row.categoryName || '-' }}</template>
      </el-table-column>
      <el-table-column label="标签" min-width="150">
        <template #default="{ row }">
          <el-tag
            v-for="tag in row.tags"
            :key="tag.id"
            size="small"
            class="mr4"
            :color="tag.color || undefined"
            :style="tag.color ? 'border:none;color:#fff' : ''"
          >
            {{ tag.name }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="views" label="阅读" width="80" align="center" />
      <el-table-column prop="commentCount" label="评论" width="80" align="center" />
      <el-table-column label="创建时间" width="150">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '撤回' : '发布' }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="load"
        @size-change="handleSearch"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑文章' : '新建文章'"
      width="900px"
      top="5vh"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" v-loading="formLoading">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="列表页展示的简介" maxlength="500" show-word-limit />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="分类">
              <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="标签">
              <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width: 100%">
                <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="slug">
              <el-input v-model="form.slug" placeholder="URL 别名，如 hello-world（可空）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面">
              <el-input v-model="form.cover" placeholder="封面图 URL（可空）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="正文" prop="content">
          <MdEditor v-model="form.content" :toolbars="toolbars" style="height: 380px" previewTheme="github" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="0">草稿</el-radio>
                <el-radio :value="1">发布</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="置顶">
              <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { articleApi, categoryApi, tagApi } from '../api/admin'

const toolbars = [
  'bold', 'italic', 'strikeThrough', 'title', 'quote', 'unorderedList', 'orderedList',
  'code', 'link', 'image', 'table', '=',
  'preview', 'fullscreen'
]

const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const tags = ref([])
const selectedIds = ref([])

const query = reactive({ page: 1, size: 10, keyword: '', categoryId: null, status: null })

const dialogVisible = ref(false)
const formLoading = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const emptyForm = () => ({
  title: '',
  summary: '',
  content: '',
  cover: '',
  categoryId: null,
  tagIds: [],
  slug: '',
  status: 0,
  isTop: 0
})
const form = reactive(emptyForm())

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }]
}

function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.keyword) delete params.keyword
    if (params.categoryId == null) delete params.categoryId
    if (params.status == null) delete params.status
    const data = await articleApi.list(params)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const [cats, tagList] = await Promise.all([categoryApi.list(), tagApi.list()])
  categories.value = cats
  tags.value = tagList
}

function handleSearch() {
  query.page = 1
  load()
}

function handleReset() {
  query.keyword = ''
  query.categoryId = null
  query.status = null
  handleSearch()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, emptyForm())
  dialogVisible.value = true
  formLoading.value = true
  try {
    const detail = await articleApi.detail(row.id)
    Object.assign(form, {
      title: detail.title,
      summary: detail.summary || '',
      content: detail.content,
      cover: detail.cover || '',
      categoryId: detail.categoryId,
      tagIds: (detail.tags || []).map(t => t.id),
      slug: detail.slug || '',
      status: detail.status,
      isTop: detail.isTop ?? 0
    })
  } finally {
    formLoading.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await articleApi.update(editingId.value, { ...form })
      ElMessage.success('保存成功')
    } else {
      await articleApi.create({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  await articleApi.updateStatus(row.id, next)
  ElMessage.success(next === 1 ? '已发布' : '已撤回为草稿')
  await load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除文章「${row.title}」吗？`, '提示', { type: 'warning' })
  await articleApi.remove(row.id)
  ElMessage.success('删除成功')
  if (list.value.length === 1 && query.page > 1) query.page--
  await load()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 篇文章吗？`, '提示', { type: 'warning' })
  await articleApi.batchRemove(selectedIds.value)
  ElMessage.success('删除成功')
  await load()
}

onMounted(() => {
  load()
  loadOptions()
})
</script>

<style scoped>
.toolbar-right { margin-left: auto; display: flex; gap: 8px; }
.mr4 { margin-right: 4px; }
.title { font-weight: 500; }
</style>
