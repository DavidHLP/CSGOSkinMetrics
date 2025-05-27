package com.gzy.crawler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gzy.entity.Statistics;
import com.gzy.entity.TodayStatistics;
import com.gzy.entity.YesterdayStatistics;
import com.gzy.repository.StatisticsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsCrawler {

    private final WebClient webClient;
    private final StatisticsRepository summaryRepository;

    private static final String API_URL = "https://sdt-api.ok-skins.com/index/statistics/v1/summary";
    private static final int TIMEOUT_SECONDS = 30;

    @Scheduled(fixedRate = 30000) // 每30秒执行一次
    public void crawlSteamStatistics() {
        log.info("开始抓取Steam统计数据...");

        try {
            long timestamp = System.currentTimeMillis();

            Mono<String> responseMono = webClient.get()
                    .uri(API_URL + "?timestamp=" + timestamp)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .doOnError(WebClientResponseException.class,
                            ex -> log.error("HTTP错误: 状态码={}, 响应体={}", ex.getStatusCode(), ex.getResponseBodyAsString()))
                    .doOnError(Exception.class, ex -> log.error("请求异常: {}", ex.getMessage()));

            String response = responseMono.block();

            if (response == null || response.isEmpty()) {
                log.error("从API收到空响应");
                return;
            }

            JSONObject jsonResponse = JSON.parseObject(response);

            // 检查响应状态
            Boolean success = jsonResponse.getBoolean("success");
            Integer errorCode = jsonResponse.getInteger("errorCode");
            String errorMsg = jsonResponse.getString("errorMsg");
            // 验证API响应是否成功
            if (success == null || !success || (errorCode != null && errorCode != 0)) {
                log.error("API返回失败状态: success={}, errorCode={}, errorMsg={}",
                        success, errorCode, errorMsg);
                return;
            }

            JSONObject data = jsonResponse.getJSONObject("data");
            if (data == null) {
                log.warn("响应中没有找到数据");
                return;
            }

            // 处理统计数据
            processStatisticsData(data);

            log.info("成功获取并处理Steam统计数据");

        } catch (Exception e) {
            log.error("抓取Steam统计数据时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理统计数据
     * 
     * @param data 从API获取的数据
     */
    private void processStatisticsData(JSONObject data) {
        try {

            Statistics summary = Statistics.builder()
                    .createTime(LocalDateTime.now())
                    .broadMarketIndex(data.getDouble("broadMarketIndex"))
                    .diffYesterday(data.getDouble("diffYesterday"))
                    .diffYesterdayRatio(data.getDouble("diffYesterdayRatio"))
                    .surviveNum(data.getString("surviveNum"))
                    .holdersNum(data.getString("holdersNum"))
                    .riseFallType(data.getString("riseFallType"))
                    .riseFallDays(data.getInteger("riseFallDays"))
                    .historyMarketIndexList(parseHistoryMarketIndexList(data))
                    .todayStatistics(parseStatistics(data, "todayStatistics", this::buildTodayStatistics))
                    .yesterdayStatistics(parseStatistics(data, "yesterdayStatistics", this::buildYesterdayStatistics))
                    .build();

            Statistics savedSummary = summaryRepository.save(summary);
            log.info("成功保存数据到MongoDB，ID: {}", savedSummary.getId());

        } catch (Exception e) {
            log.error("处理统计数据时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析历史市场指数列表
     */
    @SuppressWarnings("unchecked")
    private List<List<Double>> parseHistoryMarketIndexList(JSONObject data) {
        return Optional.ofNullable(data.get("historyMarketIndexList"))
                .filter(List.class::isInstance)
                .map(rawList -> ((List<List<Object>>) rawList).stream()
                        .map(item -> item.stream()
                                .filter(Number.class::isInstance)
                                .map(obj -> ((Number) obj).doubleValue())
                                .toList())
                        .toList())
                .orElse(new ArrayList<>());
    }

    /**
     * 通用统计数据解析函数
     */
    private <T> T parseStatistics(JSONObject data, String key, Function<JSONObject, T> builder) {
        return Optional.ofNullable(data.getJSONObject(key))
                .map(builder)
                .orElse(null);
    }

    /**
     * 构建今日统计对象
     */
    private TodayStatistics buildTodayStatistics(JSONObject stats) {
        return TodayStatistics.builder()
                .addNum(stats.getString("addNum"))
                .addValuation(stats.getDouble("addValuation"))
                .tradeNum(stats.getString("tradeNum"))
                .turnover(stats.getDouble("turnover"))
                .addNumRatio(stats.getDouble("addNumRatio"))
                .addAmountRatio(stats.getDouble("addAmountRatio"))
                .tradeVolumeRatio(stats.getDouble("tradeVolumeRatio"))
                .tradeAmountRatio(stats.getDouble("tradeAmountRatio"))
                .build();
    }

    /**
     * 构建昨日统计对象
     */
    private YesterdayStatistics buildYesterdayStatistics(JSONObject stats) {
        return YesterdayStatistics.builder()
                .addNum(stats.getString("addNum"))
                .addValuation(stats.getDouble("addValuation"))
                .tradeNum(stats.getString("tradeNum"))
                .turnover(stats.getDouble("turnover"))
                .build();
    }

    /**
     * 手动触发数据抓取（用于测试）
     */
    public void manualCrawl() {
        log.info("手动触发Steam统计数据抓取");
        crawlSteamStatistics();
    }
}