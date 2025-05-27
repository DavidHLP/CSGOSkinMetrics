import { http } from '@/utils/request';
import type { HomeDataResponse, ItemBlockResponse, StatisticsResponse } from './home.d';

const API_BASE_URL = '/api';

export const fetchHomeData = async (): Promise<HomeDataResponse> => {
  return http.get(`${API_BASE_URL}/home`);
};

export const fetchItemBlocks = async (): Promise<ItemBlockResponse[]> => {
  return http.get(`${API_BASE_URL}/itemblocks`);
};

export const fetchStatistics = async (): Promise<StatisticsResponse> => {
  return http.get(`${API_BASE_URL}/statistics`);
};
