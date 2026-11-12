<template>
  <div>
    <el-row :gutter="12">
      <el-col v-for="card in cards" :key="card.label" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
          <div v-if="card.extra" class="stat-extra">{{ card.extra }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="row-gap">
      <el-col :xs="24" :md="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>发布与访问趋势</span>
              <el-radio-group v-model="days" size="small" @change="loadTrend">
                <el-radio-button :value="7">近 7 天</el-radio-button>
                <el-radio-button :value="30">近 30 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="chartRef" class="chart" v-loading="trendLoading" />
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card shadow="never">
          <template #header><span>热门文章 Top 10</span></template>
          <el-empty v-if="!hot.length" description="暂无数据" :image-size="60" />
          <div v-else class="hot-list">
            <div v-for="(item, index) in hot" :key="item.id" class="hot-item">
              <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="hot-title" :title="item.title">{{ item.title }}</span>
              <span class="hot-views">{{ item.views }} 次</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { statsApi } from '../api/admin'

const overview = ref({})
const hot = ref([])
const days = ref(7)
const trendLoading = ref(false)
const chartRef = ref(null)
let chart = null

const cards = computed(() => {
  const o = overview.value
  return [
    { label: '文章总数', value: o.articleCount ?? 0, extra: `已发布 ${o.publishedCount ?? 0} / 草稿 ${o.draftCount ?? 0}` },
    { label: '分类数', value: o.categoryCount ?? 0 },
    { label: '标签数', value: o.tagCount ?? 0 },
    { label: '评论数', value: o.commentCount ?? 0 },
    { label: '总阅读量', value: o.totalViews ?? 0 },
    { label: '今日访问量', value: o.todayViews ?? 0 }
  ]
})

async function loadOverview() {
  overview.value = await statsApi.overview()
}

async function loadTrend() {
  trendLoading.value = true
  try {
    const list = await statsApi.trend(days.value)
    renderChart(list || [])
  } finally {
    trendLoading.value = false
  }
}

function renderChart(list) {
  if (!chart) return
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增文章', '访问量'], right: 0 },
    grid: { left: 48, right: 48, top: 48, bottom: 32 },
    xAxis: { type: 'category', data: list.map(i => i.date) },
    yAxis: [
      { type: 'value', name: '篇', minInterval: 1 },
      { type: 'value', name: '次' }
    ],
    series: [
      {
        name: '新增文章',
        type: 'bar',
        barMaxWidth: 24,
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
        data: list.map(i => i.articleCount)
      },
      {
        name: '访问量',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        itemStyle: { color: '#67c23a' },
        areaStyle: { opacity: 0.12 },
        data: list.map(i => i.views)
      }
    ]
  })
}

function handleResize() {
  chart?.resize()
}

onMounted(async () => {
  chart = echarts.init(chartRef.value)
  handleResize()
  window.addEventListener('resize', handleResize)
  await Promise.all([loadOverview(), loadTrend()])
  hot.value = await statsApi.hotArticles(10)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.stat-card { margin-bottom: 12px; }
.stat-label { color: #909399; font-size: 13px; }
.stat-value { font-size: 26px; font-weight: 600; margin-top: 6px; color: #303133; }
.stat-extra { margin-top: 6px; font-size: 12px; color: #909399; }
.row-gap { margin-top: 2px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.chart { height: 340px; }
.hot-list { display: flex; flex-direction: column; gap: 10px; max-height: 340px; overflow-y: auto; }
.hot-item { display: flex; align-items: center; gap: 10px; }
.rank {
  width: 20px; height: 20px; flex: none; border-radius: 4px;
  background: #f0f2f5; color: #909399; font-size: 12px;
  display: flex; align-items: center; justify-content: center;
}
.rank.top { background: #f56c6c; color: #fff; }
.hot-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hot-views { color: #909399; font-size: 12px; }
</style>
