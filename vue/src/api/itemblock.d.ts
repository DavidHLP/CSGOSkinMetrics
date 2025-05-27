export interface ItemBlockItem {
  type: string
  name: string
  level: number
  typeVal: string
  index: number
  riseFallRate: number
  riseFallDiff: number
}

export interface ItemBlockCategory {
  defaultList: ItemBlockItem[]
  topList: ItemBlockItem[]
  bottomList: ItemBlockItem[]
}

export interface ItemBlockData {
  hot: ItemBlockCategory
  itemTypeLevel1: ItemBlockCategory
  itemTypeLevel2: ItemBlockCategory
  itemTypeLevel3: ItemBlockCategory
}

export interface ItemBlock {
  id: string
  createTime: string
  data: ItemBlockData
  success: boolean
  errorCode: number
  errorMsg: string
  errorData?: any
  errorCodeStr?: string
}

// 数据分析接口定义
export interface ItemBlockOverview {
  createTime: string
  hotItemsCount: number
  level1ItemsCount: number
  level2ItemsCount: number
  level3ItemsCount: number
  totalItemsCount: number
  topRisingItem?: ItemBlockItem
  topFallingItem?: ItemBlockItem
}

export interface HotItemsRiseFallAnalysis {
  topListAvgRate: number
  topListMaxRate: number
  topItemsCount: number
  bottomListAvgRate: number
  bottomListMinRate: number
  bottomItemsCount: number
}

export interface ItemTypeRiseFallAnalysis {
  nameToRateMap: Record<string, number>
  avgRiseFallRate: number
  risingItemsCount: number
  fallingItemsCount: number
  totalItemsCount: number
}

export interface IndexAnalysis {
  nameToIndexMap: Record<string, number>
  avgIndex: number
  maxIndex: number
  minIndex: number
  totalItemsCount: number
}

export interface ItemPriceTrend {
  itemName: string
  timeLabels: string[]
  indexValues: number[]
  riseFallRates: number[]
}
