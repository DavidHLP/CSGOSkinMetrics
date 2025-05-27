package com.gzy.service;

import com.gzy.entity.Statistics;
import com.gzy.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    /**
     * 保存统计数据
     */
    public Statistics save(Statistics summary) {
        if (summary.getCreateTime() == null) {
            summary.setCreateTime(LocalDateTime.now());
        }
        return statisticsRepository.save(summary);
    }

    /**
     * 获取最新的统计数据
     */
    public Optional<Statistics> getLatest() {
        Statistics latest = statisticsRepository.findFirstByOrderByCreateTimeDesc();
        return Optional.ofNullable(latest);
    }

    /**
     * 分页查询统计数据
     */
    public Page<Statistics> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createTime"));
        return statisticsRepository.findAll(pageRequest);
    }

    /**
     * 根据时间范围查询
     */
    public List<Statistics> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        return statisticsRepository.findByCreateTimeBetween(start, end);
    }

    /**
     * 获取总记录数
     */
    public long count() {
        return statisticsRepository.count();
    }

    /**
     * 根据ID查询
     */
    public Optional<Statistics> findById(String id) {
        return statisticsRepository.findById(id);
    }

    /**
     * 删除指定ID的记录
     */
    public void deleteById(String id) {
        statisticsRepository.deleteById(id);
    }

    /**
     * 删除所有记录
     */
    public void deleteAll() {
        statisticsRepository.deleteAll();
    }

    /**
     * 批量保存统计数据
     */
    public List<Statistics> saveAll(List<Statistics> statisticsList) {
        return statisticsRepository.saveAll(statisticsList);
    }

    /**
     * 获取专业统计数据（趋势数据包）
     *
     * @param days 查询天数
     * @return 专业统计数据
     */
    public Map<String, Object> getProStatistics(int days) {
        // 限制查询天数，防止过大查询
        int limitedDays = Math.min(days, 90);
        PageRequest pageRequest = PageRequest.of(0, limitedDays,
                Sort.by(Sort.Direction.ASC, "createTime"));
        Page<Statistics> statisticsPage = statisticsRepository.findAll(pageRequest);
        List<Statistics> statisticsList = statisticsPage.getContent();

        Map<String, Object> proStatsData = new HashMap<>();

        // 市场指数趋势数据
        List<Map<String, Object>> marketIndexTrend = new ArrayList<>();
        // 成交额数据
        List<Map<String, Object>> turnoverData = new ArrayList<>();
        // 新增数量数据
        List<Map<String, Object>> addNumData = new ArrayList<>();
        // 按天聚合的成交额数据
        Map<String, Map<String, Double>> dailyTurnoverData = new HashMap<>();

        for (Statistics stats : statisticsList) {
            // 日期格式化为 YYYY-MM-DD HH:MM:SS
            String formattedDate = stats.getCreateTime().toString();
            // 日期（仅日期部分，不含时间）- 用于按天聚合
            String dailyDate = stats.getCreateTime().toLocalDate().toString();

            // 市场指数趋势
            Map<String, Object> indexPoint = new HashMap<>();
            indexPoint.put("date", formattedDate);
            indexPoint.put("index", stats.getBroadMarketIndex());
            indexPoint.put("diffRatio", stats.getDiffYesterdayRatio());
            marketIndexTrend.add(indexPoint);

            // 成交额数据
            Map<String, Object> turnoverPoint = new HashMap<>();
            turnoverPoint.put("date", formattedDate);
            turnoverPoint.put("today",
                    stats.getTodayStatistics() != null ? stats.getTodayStatistics().getTurnover() : 0.0);
            turnoverPoint.put("yesterday",
                    stats.getYesterdayStatistics() != null ? stats.getYesterdayStatistics().getTurnover() : 0.0);
            turnoverData.add(turnoverPoint);

            // 新增数量数据
            Map<String, Object> addNumPoint = new HashMap<>();
            addNumPoint.put("date", formattedDate);
            addNumPoint.put("today",
                    stats.getTodayStatistics() != null ? stats.getTodayStatistics().getAddNum() : "0");
            addNumPoint.put("yesterday",
                    stats.getYesterdayStatistics() != null ? stats.getYesterdayStatistics().getAddNum() : "0");
            addNumData.add(addNumPoint);

            // 按天聚合成交额数据
            if (!dailyTurnoverData.containsKey(dailyDate)) {
                dailyTurnoverData.put(dailyDate, new HashMap<>());
                dailyTurnoverData.get(dailyDate).put("today", 0.0);
                dailyTurnoverData.get(dailyDate).put("yesterday", 0.0);
                dailyTurnoverData.get(dailyDate).put("count", 0.0);
            }

            // 累加该天的成交额
            Map<String, Double> dailyData = dailyTurnoverData.get(dailyDate);
            double todayTurnover = stats.getTodayStatistics() != null ? stats.getTodayStatistics().getTurnover() : 0.0;
            double yesterdayTurnover = stats.getYesterdayStatistics() != null
                    ? stats.getYesterdayStatistics().getTurnover()
                    : 0.0;

            dailyData.put("today", dailyData.get("today") + todayTurnover);
            dailyData.put("yesterday", dailyData.get("yesterday") + yesterdayTurnover);
            dailyData.put("count", dailyData.get("count") + 1);
        }

        // 将每日聚合数据转换为列表
        List<Map<String, Object>> dailyTurnoverList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : dailyTurnoverData.entrySet()) {
            Map<String, Object> dailyPoint = new HashMap<>();
            dailyPoint.put("date", entry.getKey());

            // 如果有多个记录，计算平均值
            double count = entry.getValue().get("count");
            if (count > 0) {
                dailyPoint.put("today", entry.getValue().get("today") / count);
                dailyPoint.put("yesterday", entry.getValue().get("yesterday") / count);
            } else {
                dailyPoint.put("today", 0.0);
                dailyPoint.put("yesterday", 0.0);
            }

            dailyTurnoverList.add(dailyPoint);
        }

        // 按日期排序
        dailyTurnoverList.sort((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")));

        proStatsData.put("marketIndexTrend", marketIndexTrend);
        proStatsData.put("turnoverData", turnoverData);
        proStatsData.put("dailyTurnoverData", dailyTurnoverList);
        proStatsData.put("addNumData", addNumData);

        // 统计概览数据
        Map<String, Object> overview = new HashMap<>();
        if (!statisticsList.isEmpty()) {
            Statistics latest = statisticsList.get(statisticsList.size() - 1);
            overview.put("latestIndex", latest.getBroadMarketIndex());
            overview.put("surviveNum", latest.getSurviveNum());
            overview.put("holdersNum", latest.getHoldersNum());
            overview.put("riseFallType", latest.getRiseFallType());
            overview.put("riseFallDays", latest.getRiseFallDays());

            // 计算总成交额
            double totalTurnover = statisticsList.stream()
                    .filter(stats -> stats.getTodayStatistics() != null)
                    .mapToDouble(stats -> stats.getTodayStatistics().getTurnover())
                    .sum();
            overview.put("totalTurnover", totalTurnover);
        }

        proStatsData.put("overview", overview);

        return proStatsData;
    }

    /**
     * 获取时间段内的统计数据分析
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param interval  时间间隔类型
     * @return 时间段内的统计分析数据
     */
    public Map<String, Object> getStatisticsByPeriod(LocalDateTime startTime, LocalDateTime endTime, String interval) {
        // 如果未提供开始时间，默认为7天前
        LocalDateTime start = startTime != null ? startTime : LocalDateTime.now().minusDays(7);
        // 如果未提供结束时间，默认为当前时间
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();

        List<Statistics> statisticsList = statisticsRepository.findByCreateTimeBetween(start, end);

        Map<String, Object> periodData = new HashMap<>();

        // 分析市场指数变化
        double maxIndex = statisticsList.stream()
                .mapToDouble(Statistics::getBroadMarketIndex)
                .max().orElse(0);
        double minIndex = statisticsList.stream()
                .mapToDouble(Statistics::getBroadMarketIndex)
                .min().orElse(0);
        double avgIndex = statisticsList.stream()
                .mapToDouble(Statistics::getBroadMarketIndex)
                .average().orElse(0);

        Map<String, Object> indexAnalysis = new HashMap<>();
        indexAnalysis.put("max", maxIndex);
        indexAnalysis.put("min", minIndex);
        indexAnalysis.put("avg", avgIndex);
        indexAnalysis.put("change", maxIndex - minIndex);
        indexAnalysis.put("changeRatio", minIndex > 0 ? (maxIndex - minIndex) / minIndex * 100 : 0);

        periodData.put("indexAnalysis", indexAnalysis);

        // 成交额分析
        double totalTurnover = statisticsList.stream()
                .filter(stats -> stats.getTodayStatistics() != null)
                .mapToDouble(stats -> stats.getTodayStatistics().getTurnover())
                .sum();
        double avgTurnover = statisticsList.stream()
                .filter(stats -> stats.getTodayStatistics() != null)
                .mapToDouble(stats -> stats.getTodayStatistics().getTurnover())
                .average().orElse(0);

        Map<String, Object> turnoverAnalysis = new HashMap<>();
        turnoverAnalysis.put("total", totalTurnover);
        turnoverAnalysis.put("avg", avgTurnover);

        periodData.put("turnoverAnalysis", turnoverAnalysis);

        return periodData;
    }
}