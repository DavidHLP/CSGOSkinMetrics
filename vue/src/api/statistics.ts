import { http } from '@/utils/request'
import type { Statistics, PageData } from './statistics.d'

// 获取最新统计数据
export const getLatestStatistics = () => {
  return http.get<Statistics>('/api/summary/latest')
}

// 分页获取统计数据
export const getStatisticsList = (page: number = 0, size: number = 10) => {
  return http.get<PageData<Statistics>>('/api/summary/list', {
    params: { page, size }
  })
}

// 手动触发数据抓取
export const triggerCrawl = () => {
  return http.post<string>('/api/summary/crawl')
}

// 获取专业统计数据（趋势数据包）
export const getProStatistics = (days: number = 30) => {
  return http.get<{
    marketIndexTrend: Array<{date: string, index: number, diffRatio: number}>,
    turnoverData: Array<{date: string, today: number, yesterday: number}>,
    dailyTurnoverData: Array<{date: string, today: number, yesterday: number}>,
    addNumData: Array<{date: string, today: string, yesterday: string}>,
    overview: {
      latestIndex: number,
      surviveNum: string,
      holdersNum: string,
      riseFallType: string,
      riseFallDays: number,
      totalTurnover: number
    }
  }>(`/api/summary/pro-stats/${days}`)
}

// 获取专业统计数据 - 时间段内的汇总分析
export const getProStatisticsByPeriod = (
  startTime?: string,
  endTime?: string,
  interval: string = 'hourly'
) => {
  return http.get<{
    indexAnalysis: {
      max: number,
      min: number,
      avg: number,
      change: number,
      changeRatio: number
    },
    turnoverAnalysis: {
      total: number,
      avg: number
    }
  }>('/api/summary/pro-stats/period', {
    params: { startTime, endTime, interval }
  })
}
