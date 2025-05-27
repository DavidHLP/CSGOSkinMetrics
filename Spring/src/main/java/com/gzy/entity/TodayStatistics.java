package com.gzy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayStatistics {
    // 新增数量
    private String addNum;

    // 新增估值
    private Double addValuation;

    // 交易数量
    private String tradeNum;

    // 成交额
    private Double turnover;

    // 新增数量比率
    private Double addNumRatio;

    // 新增金额比率
    private Double addAmountRatio;

    // 交易量比率
    private Double tradeVolumeRatio;

    // 交易金额比率
    private Double tradeAmountRatio;
}
