// 今日统计
export interface TodayStatistics {
  addNum: string
  addValuation: number
  tradeNum: string
  turnover: number
  addNumRatio: number
  addAmountRatio: number
  tradeVolumeRatio: number
  tradeAmountRatio: number
}

// 昨日统计
export interface YesterdayStatistics {
  addNum: string
  addValuation: number
  tradeNum: string
  turnover: number
}

// 统计数据主体
export interface Statistics {
  id: string
  createTime: string
  broadMarketIndex: number
  diffYesterday: number
  diffYesterdayRatio: number
  historyMarketIndexList: Array<[number, number]>
  todayStatistics: TodayStatistics
  yesterdayStatistics: YesterdayStatistics
  surviveNum: string
  holdersNum: string
  riseFallType: string
  riseFallDays: number
}

// 分页数据
export interface PageData<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
}

// 图表数据
export interface ChartData {
  dates: string[]
  marketIndexes: number[]
  turnovers: number[]
  addNums: number[]
}

// API响应
export interface ApiResponse<T = any> {
  code: number
  data: T
  message: string
  success: boolean
}
