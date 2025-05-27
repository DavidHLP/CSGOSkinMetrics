package com.gzy.entity;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "steam_statistics")
public class Statistics {

    @Id
    private String id;

    // 创建时间
    private LocalDateTime createTime;

    // 市场指数
    private Double broadMarketIndex;

    // 昨日差值
    private Double diffYesterday;

    // 昨日差值比率
    private Double diffYesterdayRatio;

    // 历史市场指数列表 [[时间戳, 指数值], ...]
    private List<List<Double>> historyMarketIndexList;

    // 今日统计
    private TodayStatistics todayStatistics;

    // 昨日统计
    private YesterdayStatistics yesterdayStatistics;

    // 存活数量
    private String surviveNum;

    // 持有者数量
    private String holdersNum;

    // 涨跌类型
    private String riseFallType;

    // 涨跌天数
    private Integer riseFallDays;
}
