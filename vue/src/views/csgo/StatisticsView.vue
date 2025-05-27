<template>
  <div class="statistics-container">
    <!-- 头部统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon market-icon">📈</div>
        <div class="stat-info">
          <div class="stat-value">{{ overview.latestIndex?.toFixed(2) || '--' }}</div>
          <div class="stat-label">最新市场指数</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon holders-icon">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ latestData?.holdersNum || '--' }}</div>
          <div class="stat-label">持有者数量</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon survive-icon">💎</div>
        <div class="stat-info">
          <div class="stat-value">{{ latestData?.surviveNum || '--' }}</div>
          <div class="stat-label">存活数量</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon turnover-icon">💰</div>
        <div class="stat-info">
          <div class="stat-value">{{ formatMoney(overview.totalTurnover) }}</div>
          <div class="stat-label">总成交额</div>
        </div>
      </div>
    </div>

    <!-- 控制栏 -->
    <div class="controls">
      <div class="time-controls">
        <button
          v-for="option in timeOptions"
          :key="option.value"
          :class="['time-btn', { active: selectedDays === option.value }]"
          @click="selectedDays = option.value"
        >
          {{ option.label }}
        </button>
      </div>

      <button class="refresh-btn" @click="refreshData" :disabled="loading">
        <span class="refresh-icon">🔄</span>
        {{ loading ? '刷新中...' : '手动抓取' }}
      </button>
    </div>

    <!-- 图表容器 -->
    <div class="charts-grid">
      <!-- 市场指数趋势图 -->
      <div class="chart-container">
        <h3>市场指数趋势</h3>
        <div ref="marketIndexChart" class="chart"></div>
      </div>

      <!-- 成交额对比图 -->
      <div class="chart-container">
        <h3>成交额对比</h3>
        <div ref="turnoverChart" class="chart"></div>
      </div>

      <!-- 新增数量统计 -->
      <div class="chart-container">
        <h3>新增数量统计</h3>
        <div ref="addNumChart" class="chart"></div>
      </div>


    </div>

    <!-- 数据表格 -->
    <div class="data-table">
      <h3>最新数据记录</h3>
      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>市场指数</th>
              <th>昨日差值</th>
              <th>成交额</th>
              <th>涨跌状态</th>
              <th>涨跌天数</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in tableData" :key="item.id">
              <td>{{ formatDate(item.createTime) }}</td>
              <td class="number">{{ item.broadMarketIndex?.toFixed(2) }}</td>
              <td :class="['number', item.diffYesterday >= 0 ? 'positive' : 'negative']">
                {{ item.diffYesterday >= 0 ? '+' : '' }}{{ item.diffYesterday?.toFixed(2) }}
                ({{ item.diffYesterdayRatio >= 0 ? '+' : '' }}{{ item.diffYesterdayRatio?.toFixed(2) }}%)
              </td>
              <td class="number">{{ formatMoney(item.todayStatistics?.turnover) }}</td>
              <td :class="['status', item.riseFallType === '上涨' ? 'rise' : 'fall']">
                {{ item.riseFallType }}
              </td>
              <td class="number">{{ item.riseFallDays }}天</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  getLatestStatistics,
  getStatisticsList,
  getProStatistics,
  triggerCrawl
} from '@/api/statistics'
import type { Statistics } from '@/api/statistics.d'

// 定义类型接口
interface OverviewData {
  totalRecords?: number
  latestIndex: number
  avgIndex?: number
  totalTurnover: number
  surviveNum?: string
  holdersNum?: string
  riseFallType?: string
  riseFallDays?: number
}

// 趋势数据接口
interface ProStatisticsData {
  marketIndexTrend: Array<{date: string, index: number, diffRatio: number}>
  turnoverData: Array<{date: string, today: number, yesterday: number}>
  addNumData: Array<{date: string, today: string, yesterday: string}>
  dailyTurnoverData: Array<{date: string, today: number, yesterday: number}>
  overview: {
    latestIndex: number
    surviveNum: string
    holdersNum: string
    riseFallType: string
    riseFallDays: number
    totalTurnover: number
  }
}

// 响应式数据
const loading = ref(false)
const latestData = ref<Statistics | null>(null)
const tableData = ref<Statistics[]>([])
const proStatsData = ref<ProStatisticsData | null>(null)
const overview = ref<OverviewData>({
  latestIndex: 0,
  totalTurnover: 0
})

const selectedDays = ref(1)
const timeOptions = [
  { label: '1小时', value: 0.04167 }, // 1/24 天
  { label: '6小时', value: 0.25 },   // 6/24 天
  { label: '1天', value: 1 },
  { label: '3天', value: 3 },
  { label: '7天', value: 7 }
]

// 图表引用
const marketIndexChart = ref<HTMLElement>()
const turnoverChart = ref<HTMLElement>()
const addNumChart = ref<HTMLElement>()

// 图表实例
let marketIndexChartInstance: echarts.ECharts | null = null
let turnoverChartInstance: echarts.ECharts | null = null
let addNumChartInstance: echarts.ECharts | null = null

// 格式化金额
const formatMoney = (value: number | undefined): string => {
  if (!value) return '--'
  if (value >= 100000000) {
    return (value / 100000000).toFixed(1) + '亿'
  } else if (value >= 10000) {
    return (value / 10000).toFixed(1) + '万'
  }
  return value.toFixed(2)
}

// 格式化日期 - 显示完整时间
const formatDate = (dateStr: string): string => {
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化图表时间轴 - 根据时间范围选择合适的格式
const formatChartTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  if (selectedDays.value <= 0.25) { // 6小时内显示时:分:秒
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } else if (selectedDays.value <= 1) { // 1天内显示月-日 时:分
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } else if (selectedDays.value <= 7) { // 7天内显示月-日 时:分
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } else { // 更长时间显示日期
    return date.toLocaleDateString('zh-CN')
  }
}

// 初始化图表
const initCharts = async () => {
  await nextTick()

  if (marketIndexChart.value) {
    marketIndexChartInstance = echarts.init(marketIndexChart.value)
  }
  if (turnoverChart.value) {
    turnoverChartInstance = echarts.init(turnoverChart.value)
  }
  if (addNumChart.value) {
    addNumChartInstance = echarts.init(addNumChart.value)
  }

}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true

    // 使用专业统计接口获取所有需要的数据
    const days = Math.ceil(selectedDays.value * 24) // 计算查询的小时数
    const proStats = await getProStatistics(Math.max(days, 1)) // 至少查询1小时的数据
    proStatsData.value = proStats

    // 更新概览数据
    if (proStats.overview) {
      overview.value = {
        latestIndex: proStats.overview.latestIndex,
        totalTurnover: proStats.overview.totalTurnover,
        surviveNum: proStats.overview.surviveNum,
        holdersNum: proStats.overview.holdersNum,
        riseFallType: proStats.overview.riseFallType,
        riseFallDays: proStats.overview.riseFallDays
      }
    }

    // 获取表格数据
    const [latestRes, listRes] = await Promise.all([
      getLatestStatistics(),
      getStatisticsList(0, Math.max(days, 1))
    ])

    latestData.value = latestRes
    tableData.value = listRes.content

    // 更新图表
    updateCharts()

  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 更新所有图表
const updateCharts = () => {
  if (!proStatsData.value) return

  // 市场指数趋势图
  updateMarketIndexChartWithProData()

  // 成交额图表
  updateTurnoverChartWithProData()

  // 新增数量图表
  updateAddNumChartWithProData()
}

// 使用专业数据更新市场指数图表
const updateMarketIndexChartWithProData = () => {
  if (!marketIndexChartInstance || !proStatsData.value) return

  const trend = proStatsData.value.marketIndexTrend
  const dates = trend.map(item => formatChartTime(item.date))
  const indexes = trend.map(item => item.index)
  const rawDates = trend.map(item => item.date)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const dataIndex = params[0].dataIndex
        const originalTime = formatDate(rawDates[dataIndex])
        return `${originalTime}<br/>市场指数: ${params[0].value}`
      }
    },
    grid: { top: 40, right: 40, bottom: 80, left: 60 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45,
        interval: Math.max(0, Math.floor(dates.length / 10)) // 自动调整标签间隔
      }
    },
    yAxis: {
      type: 'value',
      name: '指数'
    },
    series: [{
      name: '市场指数',
      type: 'line',
      smooth: true,
      data: indexes,
      lineStyle: { color: '#5470c6', width: 3 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(84, 112, 198, 0.3)' },
          { offset: 1, color: 'rgba(84, 112, 198, 0.1)' }
        ])
      }
    }]
  }

  marketIndexChartInstance.setOption(option)
}

// 使用专业数据更新成交额图表
const updateTurnoverChartWithProData = () => {
  if (!turnoverChartInstance || !proStatsData.value) return

  // 使用按天聚合的成交额数据
  const dailyTurnoverData = proStatsData.value.dailyTurnoverData
  const dates = dailyTurnoverData.map(item => item.date) // 直接使用日期，不需要格式化
  const todayTurnovers = dailyTurnoverData.map(item => item.today)
  const yesterdayTurnovers = dailyTurnoverData.map(item => item.yesterday)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const dataIndex = params[0].dataIndex
        const date = dates[dataIndex]
        let result = `${date}<br/>`
        params.forEach((param: any) => {
          result += `${param.seriesName}: ${formatMoney(param.value)}<br/>`
        })
        return result
      }
    },
    legend: { data: ['今日成交额', '昨日成交额'] },
    grid: { top: 60, right: 40, bottom: 80, left: 80 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45,
        interval: 0 // 显示所有日期标签
      }
    },
    yAxis: {
      type: 'value',
      name: '成交额',
      axisLabel: { formatter: (value: number) => formatMoney(value) }
    },
    series: [
      {
        name: '今日成交额',
        type: 'bar',
        data: todayTurnovers,
        itemStyle: { color: '#91cc75' }
      },
      {
        name: '昨日成交额',
        type: 'bar',
        data: yesterdayTurnovers,
        itemStyle: { color: '#fac858' }
      }
    ]
  }

  turnoverChartInstance.setOption(option)
}

// 使用专业数据更新新增数量图表
const updateAddNumChartWithProData = () => {
  if (!addNumChartInstance || !proStatsData.value) return

  const addNumData = proStatsData.value.addNumData
  const dates = addNumData.map(item => formatChartTime(item.date))
  const todayAddNums = addNumData.map(item => parseInt(item.today.replace(/[^0-9]/g, '') || '0'))
  const yesterdayAddNums = addNumData.map(item => parseInt(item.yesterday.replace(/[^0-9]/g, '') || '0'))
  const rawDates = addNumData.map(item => item.date)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const dataIndex = params[0].dataIndex
        const originalTime = formatDate(rawDates[dataIndex])
        let result = `${originalTime}<br/>`
        params.forEach((param: any) => {
          result += `${param.seriesName}: ${param.value}<br/>`
        })
        return result
      }
    },
    legend: { data: ['今日新增', '昨日新增'] },
    grid: { top: 60, right: 40, bottom: 80, left: 60 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45,
        interval: Math.max(0, Math.floor(dates.length / 10))
      }
    },
    yAxis: {
      type: 'value',
      name: '数量'
    },
    series: [
      {
        name: '今日新增',
        type: 'line',
        data: todayAddNums,
        lineStyle: { color: '#ee6666' }
      },
      {
        name: '昨日新增',
        type: 'line',
        data: yesterdayAddNums,
        lineStyle: { color: '#73c0de' }
      }
    ]
  }

  addNumChartInstance.setOption(option)
}

// 刷新数据
const refreshData = async () => {
  try {
    loading.value = true
    await triggerCrawl()
    setTimeout(loadData, 2000) // 等待抓取完成后重新加载
  } catch (error) {
    console.error('触发抓取失败:', error)
    loading.value = false
  }
}

// 监听天数变化
watch(selectedDays, loadData)

// 页面挂载
onMounted(async () => {
  await initCharts()
  await loadData()

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    marketIndexChartInstance?.resize()
    turnoverChartInstance?.resize()
    addNumChartInstance?.resize()
  })
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
  background: var(--background-light);
  min-height: 100vh;
  max-width: 1400px;
  margin: 0 auto;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(270px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
  animation: fadeIn 0.5s ease-out;
}

.stat-card {
  background: var(--card-bg);
  border-radius: var(--border-radius);
  padding: 24px;
  box-shadow: var(--box-shadow);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.3s, box-shadow 0.3s;
  overflow: hidden;
  position: relative;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 5px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 48px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: rgba(102, 192, 244, 0.1);
}

.market-icon {
  background: rgba(102, 192, 244, 0.1);
}

.holders-icon {
  background: rgba(102, 244, 166, 0.1);
}

.survive-icon {
  background: rgba(244, 102, 166, 0.1);
}

.turnover-icon {
  background: rgba(244, 198, 102, 0.1);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--text-dark);
  margin-bottom: 4px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: var(--card-bg);
  padding: 15px 20px;
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  animation: fadeIn 0.5s ease-out;
  animation-delay: 0.2s;
  animation-fill-mode: both;
}

.time-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.time-btn {
  padding: 10px 16px;
  border: 1px solid #d1d5db;
  border-radius: var(--border-radius);
  background: var(--card-bg);
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.time-btn:hover {
  border-color: var(--secondary-color);
}

.time-btn.active {
  background: var(--secondary-color);
  color: white;
  border-color: var(--secondary-color);
  box-shadow: 0 2px 5px rgba(102, 192, 244, 0.3);
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--secondary-color);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 5px rgba(102, 192, 244, 0.3);
}

.refresh-btn:hover:not(:disabled) {
  background: #4faee8;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 192, 244, 0.4);
}

.refresh-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.refresh-icon {
  animation: rotate 2s linear infinite;
  display: inline-block;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.chart-container {
  background: var(--card-bg);
  border-radius: var(--border-radius);
  padding: 20px;
  box-shadow: var(--box-shadow);
  transition: transform 0.3s, box-shadow 0.3s;
  animation: fadeIn 0.5s ease-out;
  animation-delay: 0.3s;
  animation-fill-mode: both;
}

.chart-container:nth-child(2) {
  animation-delay: 0.4s;
}

.chart-container:nth-child(3) {
  animation-delay: 0.5s;
}

.chart-container:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
}

.chart-container h3 {
  margin: 0 0 20px 0;
  color: var(--text-dark);
  font-size: 18px;
  font-weight: 600;
  position: relative;
  padding-bottom: 10px;
}

.chart-container h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 3px;
  background: var(--secondary-color);
}

.chart {
  height: 300px;
  transition: height 0.3s;
}

.data-table {
  background: var(--card-bg);
  border-radius: var(--border-radius);
  padding: 20px;
  box-shadow: var(--box-shadow);
  animation: fadeIn 0.5s ease-out;
  animation-delay: 0.6s;
  animation-fill-mode: both;
}

.data-table h3 {
  margin: 0 0 20px 0;
  color: var(--text-dark);
  font-size: 18px;
  font-weight: 600;
  position: relative;
  padding-bottom: 10px;
}

.data-table h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 3px;
  background: var(--secondary-color);
}

.table-container {
  max-height: 400px;
  overflow-y: auto;
  border-radius: var(--border-radius);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

th {
  background: #f9fafb;
  font-weight: 600;
  color: var(--text-dark);
  position: sticky;
  top: 0;
  z-index: 10;
}

tr:hover {
  background-color: #f9fafb;
}

.number {
  text-align: right;
  font-family: monospace;
  font-weight: 500;
}

.positive {
  color: var(--success-color);
  font-weight: 600;
}

.negative {
  color: var(--error-color);
  font-weight: 600;
}

.status.rise {
  color: var(--success-color);
  font-weight: 600;
}

.status.fall {
  color: var(--error-color);
  font-weight: 600;
}

/* 动画 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式设计增强 */
@media (max-width: 768px) {
  .statistics-container {
    padding: 15px;
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }

  .controls {
    flex-direction: column;
    gap: 15px;
  }

  .time-controls {
    width: 100%;
    justify-content: center;
  }

  .time-btn {
    padding: 8px 12px;
    font-size: 12px;
  }

  .refresh-btn {
    width: 100%;
    justify-content: center;
  }

  .chart {
    height: 250px;
  }

  .table-container th:first-child,
  .table-container td:first-child {
    min-width: 140px;
  }

  .stat-value {
    font-size: 28px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
