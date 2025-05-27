<script setup lang="ts">
import { ref, onMounted, computed, watch, type Ref } from 'vue';
import { fetchHomeData } from '@/api/home';
import type { HomeDataResponse, ItemBlockResponse, StatisticsResponse } from '@/api/home.d';
import * as echarts from 'echarts';

// 数据加载状态
const loading = ref(true);
// 错误消息
const errorMessage = ref('');
// 主页数据
const homeData = ref<HomeDataResponse | null>(null);
// 选中的数据类型
const selectedDataType = ref('default');
// 选中的数据类别
const selectedCategory = ref('hot');
// 物品数据加载状态
const itemDataLoading = ref(false);
// 物品数据错误信息
const itemDataError = ref('');

// 物品数据类型定义
interface ItemData {
  type: string;
  name: string;
  level: number;
  typeVal: string;
  index: number;
  riseFallRate: number;
  riseFallDiff: number;
  [key: string]: any; // 允许其他任意属性
}

// 数据类别选项
const categoryOptions = [
  { value: 'hot', label: '热门物品' },
  { value: 'itemTypeLevel1', label: '一级类型' },
  { value: 'itemTypeLevel2', label: '二级类型' },
  { value: 'itemTypeLevel3', label: '三级类型' }
];

// 加载数据
const loadData = async () => {
  try {
    loading.value = true;
    itemDataLoading.value = true;
    itemDataError.value = '';

    homeData.value = await fetchHomeData();

    // 调试输出数据结构
    console.log('获取到的主页数据:', homeData.value);
    if (homeData.value?.itemBlocks && homeData.value.itemBlocks.length > 0) {
      console.log('最新物品数据:', homeData.value.itemBlocks[0]);
      if (homeData.value.itemBlocks[0].data) {
        console.log('物品数据内容:', homeData.value.itemBlocks[0].data);
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error);
    errorMessage.value = '加载数据失败，请稍后重试';
    itemDataError.value = '数据加载失败';
  } finally {
    loading.value = false;
    itemDataLoading.value = false;
  }
};

// 计算属性
const statistics: Ref<StatisticsResponse> = computed(() => homeData.value?.statistics || {} as StatisticsResponse);
const itemBlocks: Ref<ItemBlockResponse[]> = computed(() => homeData.value?.itemBlocks || []);

// 获取最新的物品数据
const latestItemBlock = computed(() => {
  if (itemBlocks.value.length === 0) return null;
  return itemBlocks.value[0]; // 只获取第一条数据(最新的)
});

// 计算当前显示的项目数据
const currentItemData: Ref<ItemData[]> = computed(() => {
  if (!latestItemBlock.value?.data) {
    console.log('无物品数据或数据结构不正确', latestItemBlock.value);
    return [];
  }

  try {
    const firstItemData = latestItemBlock.value.data;
    console.log('物品数据结构:', firstItemData);

    // 根据选择的类别和数据类型获取对应列表
    const category = firstItemData[selectedCategory.value];
    if (!category) {
      console.log(`未找到类别: ${selectedCategory.value}`);
      return [];
    }

    // 根据数据类型选择列表
    const listType = selectedDataType.value === 'default' ? 'defaultList' :
                     selectedDataType.value === 'top' ? 'topList' : 'bottomList';

    if (Array.isArray(category[listType])) {
      console.log(`使用 ${selectedCategory.value}.${listType} 获取数据`);
      return category[listType];
    }

    console.log(`${selectedCategory.value}.${listType} 不是有效的数组`);
    return [];
  } catch (error) {
    console.error('解析物品数据失败:', error);
    return [];
  }
});

// 格式化大数字
const formatLargeNumber = (num: number | undefined): string => {
  if (num === undefined || (num !== 0 && !num)) return '0.00';

  if (num >= 100000) {
    return (num / 10000).toFixed(2) + '万';
  }
  return num.toFixed(2);
};

// 初始化图表
const initMarketChart = () => {
  if (!statistics.value?.historyMarketIndexList) return;

  const chartDom = document.getElementById('market-chart');
  if (!chartDom) return;

  const myChart = echarts.init(chartDom);

  const option = {
    title: {
      text: '市场指数走势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'time',
      axisLabel: {
        formatter: '{MM}-{dd}'
      }
    },
    yAxis: {
      type: 'value',
      name: '指数值'
    },
    series: [{
      name: '市场指数',
      type: 'line',
      smooth: true,
      data: statistics.value.historyMarketIndexList.map((item: [number, number]) => ({
        value: [new Date(item[0]), item[1]]
      })),
      areaStyle: {
        opacity: 0.3
      }
    }]
  };

  myChart.setOption(option);

  // 响应窗口大小变化
  window.addEventListener('resize', () => {
    myChart.resize();
  });
};

// 获取状态类名
const getStatusClass = (success: boolean) => {
  return success ? 'success-status' : 'error-status';
};

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '';
  const date = new Date(time);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 格式化百分比
const formatPercent = (value: number) => {
  return `${(value * 100).toFixed(2)}%`;
};

// 组件挂载后执行
onMounted(() => {
  loadData().then(() => {
    // 初始化图表
    setTimeout(() => {
      initMarketChart();
    }, 100);
  });
});
</script>

<template>
  <main class="home-view">
    <!-- 加载中显示 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <div>加载中...</div>
    </div>

    <!-- 错误信息显示 -->
    <div v-else-if="errorMessage" class="error-container">
      <div class="error-icon">!</div>
      <div>{{ errorMessage }}</div>
      <button @click="loadData" class="retry-button">重试</button>
    </div>

    <!-- 数据大屏内容 -->
    <div v-else class="dashboard-container">
      <!-- 统计数据卡片 -->
      <div class="statistics-card" v-if="statistics">
        <div class="card-header">
          <h2>市场概览</h2>
          <span class="update-time">更新时间: {{ formatTime(statistics.createTime) }}</span>
        </div>

        <div class="market-index-section">
          <div class="index-value">
            <div class="main-value">{{ statistics.broadMarketIndex.toFixed(2) }}</div>
            <div class="diff-value" :class="statistics.diffYesterday >= 0 ? 'positive' : 'negative'">
              {{ statistics.diffYesterday >= 0 ? '+' : '' }}{{ statistics.diffYesterday.toFixed(2) }}
              ({{ formatPercent(statistics.diffYesterdayRatio) }})
            </div>
          </div>

          <div class="index-details">
            <div class="detail-item">
              <span class="label">涨跌类型:</span>
              <span class="value">{{ statistics.riseFallType }}</span>
            </div>
            <div class="detail-item">
              <span class="label">涨跌天数:</span>
              <span class="value">{{ statistics.riseFallDays }}</span>
            </div>
            <div class="detail-item">
              <span class="label">存活数量:</span>
              <span class="value">{{ statistics.surviveNum }}</span>
            </div>
            <div class="detail-item">
              <span class="label">持有者数量:</span>
              <span class="value">{{ statistics.holdersNum }}</span>
            </div>
          </div>
        </div>

        <!-- 今日统计和昨日统计 -->
        <div class="statistics-comparison">
          <div class="stats-column">
            <h3>今日统计</h3>
            <div class="stats-row">
              <div class="stat-item">
                <div class="stat-label">新增数量</div>
                <div class="stat-value">{{ statistics.todayStatistics.addNum }}</div>
                <div class="stat-ratio" :class="statistics.todayStatistics.addNumRatio >= 0 ? 'positive' : 'negative'">
                  {{ statistics.todayStatistics.addNumRatio >= 0 ? '+' : '' }}{{ formatPercent(statistics.todayStatistics.addNumRatio) }}
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-label">新增估值</div>
                <div class="stat-value">{{ statistics.todayStatistics.addValuation.toFixed(2) }}</div>
                <div class="stat-ratio" :class="statistics.todayStatistics.addAmountRatio >= 0 ? 'positive' : 'negative'">
                  {{ statistics.todayStatistics.addAmountRatio >= 0 ? '+' : '' }}{{ formatPercent(statistics.todayStatistics.addAmountRatio) }}
                </div>
              </div>
            </div>
            <div class="stats-row">
              <div class="stat-item">
                <div class="stat-label">交易数量</div>
                <div class="stat-value">{{ statistics.todayStatistics.tradeNum }}</div>
                <div class="stat-ratio" :class="statistics.todayStatistics.tradeVolumeRatio >= 0 ? 'positive' : 'negative'">
                  {{ statistics.todayStatistics.tradeVolumeRatio >= 0 ? '+' : '' }}{{ formatPercent(statistics.todayStatistics.tradeVolumeRatio) }}
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-label">成交额</div>
                <div class="stat-value">{{ statistics.todayStatistics.turnover.toFixed(2) }}</div>
                <div class="stat-ratio" :class="statistics.todayStatistics.tradeAmountRatio >= 0 ? 'positive' : 'negative'">
                  {{ statistics.todayStatistics.tradeAmountRatio >= 0 ? '+' : '' }}{{ formatPercent(statistics.todayStatistics.tradeAmountRatio) }}
                </div>
              </div>
            </div>
          </div>

          <div class="stats-column">
            <h3>昨日统计</h3>
            <div class="stats-row">
              <div class="stat-item">
                <div class="stat-label">新增数量</div>
                <div class="stat-value">{{ statistics.yesterdayStatistics.addNum }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">新增估值</div>
                <div class="stat-value">{{ statistics.yesterdayStatistics.addValuation.toFixed(2) }}</div>
              </div>
            </div>
            <div class="stats-row">
              <div class="stat-item">
                <div class="stat-label">交易数量</div>
                <div class="stat-value">{{ statistics.yesterdayStatistics.tradeNum }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">成交额</div>
                <div class="stat-value">{{ statistics.yesterdayStatistics.turnover.toFixed(2) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 市场指数图表 -->
        <div class="chart-container">
          <div id="market-chart" class="chart"></div>
        </div>
      </div>

      <!-- ItemBlock 数据列表 -->
      <div class="itemblocks-card">
        <div class="card-header">
          <h2>物品指数分析</h2>
          <div class="controls-container">
            <div class="category-selector">
              <button
                v-for="option in categoryOptions"
                :key="option.value"
                class="category-btn"
                :class="{ active: selectedCategory === option.value }"
                @click="selectedCategory = option.value"
                :disabled="itemDataLoading"
              >
                {{ option.label }}
              </button>
            </div>
            <div class="data-type-selector">
              <button
                class="data-type-btn"
                :class="{ active: selectedDataType === 'default' }"
                @click="selectedDataType = 'default'"
                :disabled="itemDataLoading"
              >
                综合列表
              </button>
              <button
                class="data-type-btn"
                :class="{ active: selectedDataType === 'top' }"
                @click="selectedDataType = 'top'"
                :disabled="itemDataLoading"
              >
                涨幅榜
              </button>
              <button
                class="data-type-btn"
                :class="{ active: selectedDataType === 'bottom' }"
                @click="selectedDataType = 'bottom'"
                :disabled="itemDataLoading"
              >
                跌幅榜
              </button>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="itemDataLoading" class="loading-items">
          <div class="loading-items-spinner"></div>
          <div>加载物品数据中...</div>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="itemDataError" class="error-info">
          <div class="error-detail">
            <span class="error-label">错误信息:</span>
            <span class="error-value">{{ itemDataError }}</span>
          </div>
          <button @click="loadData" class="retry-button small">重试</button>
        </div>

        <!-- 空数据状态 -->
        <div v-else-if="!latestItemBlock" class="no-data-message">
          暂无数据
        </div>

        <!-- API错误状态 -->
        <div v-else-if="!latestItemBlock.success" class="error-info">
          <div class="error-detail">
            <span class="error-label">错误代码:</span>
            <span class="error-value">{{ latestItemBlock.errorCode }} ({{ latestItemBlock.errorCodeStr }})</span>
          </div>
          <div class="error-detail">
            <span class="error-label">错误信息:</span>
            <span class="error-value">{{ latestItemBlock.errorMsg }}</span>
          </div>
        </div>

        <!-- 空列表状态 -->
        <div v-else-if="currentItemData.length === 0" class="no-data-message">
          暂无{{ selectedDataType === 'default' ? '综合' : selectedDataType === 'top' ? '涨幅' : '跌幅' }}数据
        </div>

        <!-- 数据列表 -->
        <div v-else class="item-data-grid">
          <div v-for="(item, index) in currentItemData"
               :key="`${item.type || 'unknown'}-${item.name || 'unnamed'}-${index}`"
               class="item-data-card"
               :class="{ 'rise': item.riseFallRate > 0, 'fall': item.riseFallRate < 0 }">
            <div class="item-rank">#{{ index + 1 }}</div>
            <div class="item-name">{{ item.name || '未命名物品' }}</div>
            <div class="item-metrics">
              <div class="metric">
                <span class="metric-label">指数值</span>
                <span class="metric-value">{{ formatLargeNumber(item.index) }}</span>
              </div>
              <div class="metric">
                <span class="metric-label">涨跌幅</span>
                <span class="metric-value" :class="(item.riseFallRate || 0) >= 0 ? 'positive' : 'negative'">
                  {{ (item.riseFallRate || 0) >= 0 ? '+' : '' }}{{ (item.riseFallRate || 0).toFixed(2) }}%
                </span>
              </div>
              <div class="metric">
                <span class="metric-label">差值</span>
                <span class="metric-value" :class="(item.riseFallDiff || 0) >= 0 ? 'positive' : 'negative'">
                  {{ (item.riseFallDiff || 0) >= 0 ? '+' : '' }}{{ (item.riseFallDiff || 0).toFixed(2) }}
                </span>
              </div>
            </div>
            <div class="item-type">
              <span class="type-badge">{{ item.type || 'UNKNOWN' }}</span>
              <span v-if="(item.level || 0) > 0" class="level-badge">L{{ item.level }}</span>
            </div>
          </div>
        </div>

        <div v-if="latestItemBlock && latestItemBlock.success" class="data-update-time">
          更新时间: {{ formatTime(latestItemBlock.createTime) }}
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.home-view {
  padding: 20px;
  background-color: var(--background-light);
  min-height: 100vh;
  max-width: 1400px;
  margin: 0 auto;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
}

.loading-spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid var(--secondary-color);
  width: 50px;
  height: 50px;
  animation: spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: var(--error-color);
}

.error-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: var(--error-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin-bottom: 20px;
  box-shadow: 0 5px 15px rgba(239, 83, 80, 0.3);
}

.retry-button {
  margin-top: 20px;
  padding: 10px 24px;
  background-color: var(--secondary-color);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 5px rgba(102, 192, 244, 0.3);
}

.retry-button:hover {
  background-color: #4faee8;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 192, 244, 0.4);
}

.retry-button.small {
  margin-top: 10px;
  padding: 6px 12px;
  font-size: 0.9rem;
}

.dashboard-container {
  display: grid;
  grid-template-columns: 1fr;
  gap: 25px;
  animation: fadeIn 0.5s ease-out;
}

.statistics-card, .itemblocks-card {
  background-color: var(--card-bg);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  padding: 25px;
  transition: transform 0.3s, box-shadow 0.3s;
  overflow: hidden;
  position: relative;
}

.statistics-card:hover, .itemblocks-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
}

.statistics-card::before, .itemblocks-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 5px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  flex-wrap: wrap;
  gap: 15px;
}

.card-header h2 {
  margin: 0;
  font-size: 1.6rem;
  color: var(--primary-color);
  font-weight: 600;
}

.update-time {
  color: #888;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 5px;
}

.update-time::before {
  content: '🕒';
  font-size: 1rem;
}

.market-index-section {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  margin-bottom: 30px;
  gap: 20px;
}

.index-value {
  display: flex;
  flex-direction: column;
  min-width: 200px;
}

.main-value {
  font-size: 3rem;
  font-weight: bold;
  color: var(--primary-color);
  margin-bottom: 5px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.diff-value {
  font-size: 1.3rem;
  font-weight: 500;
}

.positive {
  color: var(--success-color);
}

.negative {
  color: var(--error-color);
}

.index-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 15px;
  background: rgba(248, 249, 250, 0.7);
  border-radius: var(--border-radius);
  min-width: 300px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.label {
  color: #888;
  font-size: 0.9rem;
}

.value {
  font-weight: bold;
  font-size: 1.2rem;
  color: var(--primary-color);
}

.statistics-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;
  margin-bottom: 30px;
}

.stats-column h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: var(--primary-color);
  font-size: 1.3rem;
  font-weight: 600;
  position: relative;
  padding-bottom: 10px;
}

.stats-column h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 3px;
  background: var(--secondary-color);
}

.stats-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 20px;
}

.stat-item {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: var(--border-radius);
  transition: transform 0.3s;
}

.stat-item:hover {
  transform: translateY(-3px);
  background-color: #f0f4f8;
}

.stat-label {
  color: #888;
  font-size: 0.9rem;
}

.stat-value {
  font-weight: bold;
  font-size: 1.3rem;
  margin: 8px 0;
  color: var(--primary-color);
}

.stat-ratio {
  font-size: 0.9rem;
  font-weight: 500;
}

.chart-container {
  margin-top: 30px;
  border-radius: var(--border-radius);
  overflow: hidden;
  background: #f9f9f9;
  padding: 15px;
}

.chart {
  width: 100%;
  height: 400px;
  border-radius: var(--border-radius);
}

/* 数据类型选择器 */
.data-type-selector {
  display: flex;
  gap: 5px;
}

.data-type-btn {
  padding: 8px 15px;
  border: 1px solid #e0e0e0;
  background-color: white;
  border-radius: var(--border-radius);
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.data-type-btn:hover {
  border-color: var(--secondary-color);
}

.data-type-btn.active {
  background-color: var(--secondary-color);
  color: white;
  border-color: var(--secondary-color);
}

/* 物品数据网格 */
.item-data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.item-data-card {
  background-color: white;
  border-radius: var(--border-radius);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  padding: 20px;
  position: relative;
  transition: all 0.3s;
  overflow: hidden;
  border-left: 4px solid transparent;
}

.item-data-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.item-data-card.rise {
  border-left-color: var(--success-color);
}

.item-data-card.fall {
  border-left-color: var(--error-color);
}

.item-rank {
  position: absolute;
  top: 15px;
  right: 15px;
  background-color: rgba(0, 0, 0, 0.05);
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: var(--primary-color);
}

.item-name {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 15px;
  padding-right: 35px;
}

.item-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 10px;
  margin-bottom: 15px;
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.metric-label {
  font-size: 0.8rem;
  color: #888;
}

.metric-value {
  font-size: 1.1rem;
  font-weight: 600;
}

.item-type {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.type-badge, .level-badge {
  padding: 4px 8px;
  border-radius: 20px;
  font-size: 0.8rem;
  background-color: rgba(0, 0, 0, 0.05);
}

.data-update-time {
  text-align: right;
  color: #888;
  font-size: 0.9rem;
  margin-top: 20px;
}

.no-data-message {
  text-align: center;
  padding: 30px;
  color: #888;
  font-size: 1.1rem;
}

.error-info {
  background-color: rgba(231, 76, 60, 0.05);
  padding: 15px;
  border-radius: var(--border-radius);
  border-left: 3px solid var(--error-color);
}

.error-detail {
  margin-bottom: 10px;
}

.error-label {
  color: #888;
  margin-right: 8px;
  font-weight: 500;
}

.error-value {
  color: var(--error-color);
  font-family: monospace;
}

/* 动画 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式布局增强 */
@media (min-width: 1024px) {
  .dashboard-container {
    grid-template-columns: 2fr 1fr;
  }

  .statistics-card {
    grid-column: 1 / 2;
    grid-row: 1 / 2;
  }

  .itemblocks-card {
    grid-column: 2 / 3;
    grid-row: 1 / 2;
  }
}

@media (max-width: 768px) {
  .statistics-comparison {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .stats-row {
    grid-template-columns: 1fr;
  }

  .index-details {
    min-width: 100%;
  }

  .market-index-section {
    flex-direction: column;
  }

  .chart {
    height: 300px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .update-time {
    width: 100%;
  }

  .item-data-grid {
    grid-template-columns: 1fr;
  }

  .data-type-selector {
    width: 100%;
    justify-content: space-between;
  }

  .data-type-btn {
    flex: 1;
    padding: 8px 5px;
    font-size: 0.8rem;
    text-align: center;
  }

  .category-selector,
  .data-type-selector {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }

  .category-btn,
  .data-type-btn {
    flex: 1;
    min-width: 70px;
    padding: 8px 5px;
    font-size: 0.8rem;
    text-align: center;
  }
}

/* 加载状态 */
.loading-items {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

.loading-items-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: var(--secondary-color);
  animation: spin 1s linear infinite;
  margin-right: 10px;
}

/* 控制器容器 */
.controls-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 类别选择器 */
.category-selector {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.category-btn {
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  background-color: white;
  border-radius: var(--border-radius);
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.category-btn:hover {
  border-color: var(--primary-color);
}

.category-btn.active {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}
</style>
