package com.gzy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YesterdayStatistics {
    // 新增数量
    private String addNum;

    // 新增估值
    private Double addValuation;

    // 交易数量
    private String tradeNum;

    // 成交额
    private Double turnover;
}
