package com.gzy.controller;

import com.gzy.crawler.StatisticsCrawler;
import com.gzy.entity.Statistics;
import com.gzy.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsCrawler steamdtCrawler;
    private final StatisticsService statisticsService;

    /**
     * 获取最新的统计数据
     */
    @GetMapping("/latest")
    public ResponseEntity<Statistics> getLatest() {
        return statisticsService.getLatest()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 分页获取统计数据
     */
    @GetMapping("/list")
    public ResponseEntity<Page<Statistics>> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Statistics> summaries = statisticsService.findAll(page, size);
        return ResponseEntity.ok(summaries);
    }

    /**
     * 根据时间范围获取统计数据
     */
    @GetMapping("/range")
    public ResponseEntity<List<Statistics>> getByTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {

        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);

            List<Statistics> summaries = statisticsService.findByTimeRange(start, end);
            return ResponseEntity.ok(summaries);

        } catch (Exception e) {
            log.error("解析时间参数失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取数据库中的记录总数
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getCount() {
        long count = statisticsService.count();
        return ResponseEntity.ok(count);
    }

    /**
     * 手动触发数据抓取
     */
    @PostMapping("/crawl")
    public ResponseEntity<String> manualCrawl() {
        try {
            steamdtCrawler.manualCrawl();
            return ResponseEntity.ok("数据抓取已触发");
        } catch (Exception e) {
            log.error("手动触发抓取失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("抓取失败: " + e.getMessage());
        }
    }

    /**
     * 获取专业统计数据（趋势数据包）- 专用接口，减轻前端压力
     */
    @GetMapping("/pro-stats/{days}")
    public ResponseEntity<Map<String, Object>> getProStatistics(@PathVariable int days) {
        try {
            Map<String, Object> proStatsData = statisticsService.getProStatistics(days);
            return ResponseEntity.ok(proStatsData);
        } catch (Exception e) {
            log.error("获取专业统计数据失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取专业统计数据 - 时间段内的汇总分析（适用于图表分析）
     */
    @GetMapping("/pro-stats/period")
    public ResponseEntity<Map<String, Object>> getProStatisticsByPeriod(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "hourly") String interval) {
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;
            
            Map<String, Object> periodData = statisticsService.getStatisticsByPeriod(start, end, interval);
            return ResponseEntity.ok(periodData);
        } catch (Exception e) {
            log.error("获取周期统计数据失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除所有数据（谨慎使用）
     */
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAll() {
        long count = statisticsService.count();
        statisticsService.deleteAll();
        return ResponseEntity.ok("已删除 " + count + " 条记录");
    }
}