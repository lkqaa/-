<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索昵称或内容"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="垃圾" :value="2" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="toolbar-right">
        <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="list" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="46" />
      <el-table-column prop="articleTitle" label="文章" min-width="180" show-overflow-tooltip />
      <el-table-column prop="nickname" label="昵称" width="120" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" link type="success" @click="changeStatus(row, 1)">通过</el-button>
          <el-button v-if="row.status !== 2" link type="warning" @click="changeStatus(row, 2)">垃圾</el-button>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { commentApi } from '../api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])

const query = reactive({ page: 1, size: 10, keyword: '', status: null })

function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

function statusText(status) {
  return status === 0 ? '待审核' : status === 1 ? '已通过' : '垃圾'
}

function statusType(status) {
  return status === 0 ? 'warning' : status === 1 ? 'success' : 'danger'
}

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.keyword) delete params.keyword
    if (params.status == null) delete params.status
    const data = await commentApi.list(params)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  load()
}

function handleReset() {
  query.keyword = ''
  query.status = null
  handleSearch()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function changeStatus(row, status) {
  await commentApi.updateStatus(row.id, status)
  ElMessage.success('状态已更新')
  await load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该评论吗？', '提示', { type: 'warning' })
  await commentApi.remove(row.id)
  ElMessage.success('删除成功')
  if (list.value.length === 1 && query.page > 1) query.page--
  await load()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条评论吗？`, '提示', { type: 'warning' })
  await commentApi.batchRemove(selectedIds.value)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar-right { margin-left: auto; }
</style>
