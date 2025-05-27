export interface ItemBlockResponse {
  id: string;
  createTime: string;
  data: any;
  success: boolean;
  errorCode?: number;
  errorMsg?: string;
  errorData?: any;
  errorCodeStr?: string;
}

export interface StatisticsResponse {
  id: string;
  createTime: string;
  broadMarketIndex: number;
  diffYesterday: number;
  diffYesterdayRatio: number;
  historyMarketIndexList: [number, number][];
  todayStatistics: any;
  yesterdayStatistics: any;
  surviveNum: string;
  holdersNum: string;
  riseFallType: string;
  riseFallDays: number;
}

export interface HomeDataResponse {
  itemBlocks: ItemBlockResponse[];
  statistics: StatisticsResponse;
}
