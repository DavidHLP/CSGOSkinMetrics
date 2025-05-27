import { http } from '@/utils/request'
import type {
  ItemBlock,
  ItemBlockOverview,
  HotItemsRiseFallAnalysis,
  ItemTypeRiseFallAnalysis,
  IndexAnalysis,
  ItemBlockItem,
  ItemPriceTrend
} from './itemblock.d'

/**
 * 获取最新的ItemBlock数据
 */
export async function getLatestItemBlock(): Promise<ItemBlock> {
  return http.get('/item-block/latest')
}

/**
 * 获取指定页数的ItemBlock数据列表
 */
export async function getItemBlockList(page: number = 0, size: number = 10) {
  return http.get('/item-block/list', {
    params: { page, size }
  })
}

/**
 * 获取特定分类的数据
 */
export async function getCategoryData(categoryName: string) {
  return http.get(`/item-block/category/${categoryName}`)
}

/**
 * 获取特定分类的特定列表类型数据
 */
export async function getCategoryListData(categoryName: string, listType: string): Promise<ItemBlockItem[]> {
  return http.get(`/item-block/category/${categoryName}/${listType}`)
}

/**
 * 获取数据分析总览
 */
export async function getItemBlockOverview(): Promise<ItemBlockOverview> {
  return http.get('/item-block/analysis/overview')
}

/**
 * 获取热门物品涨跌幅分析
 */
export async function getHotItemsRiseFallAnalysis(): Promise<HotItemsRiseFallAnalysis> {
  return http.get('/item-block/analysis/hot-rise-fall')
}

/**
 * 获取物品类型涨跌幅分析
 */
export async function getItemTypeRiseFallAnalysis(level: number): Promise<ItemTypeRiseFallAnalysis> {
  if (level < 1 || level > 3) {
    throw new Error('Level must be between 1 and 3')
  }

  return http.get(`/item-block/analysis/item-type-rise-fall/${level}`)
}

/**
 * 获取指数分析
 */
export async function getIndexAnalysis(category: string): Promise<IndexAnalysis> {
  return http.get(`/item-block/analysis/index-analysis/${category}`)
}

/**
 * 手动触发数据抓取 (仅开发测试用)
 */
export async function triggerCrawlManually() {
  return http.post('/item-block/crawl')
}

/**
 * 获取物品价格趋势数据（最近7天）
 */
export async function getItemPriceTrend(itemName: string): Promise<ItemPriceTrend> {
  return http.get(`/item-block/analysis/trend/${encodeURIComponent(itemName)}`)
}
