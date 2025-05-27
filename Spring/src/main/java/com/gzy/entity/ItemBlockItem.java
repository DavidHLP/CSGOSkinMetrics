package com.gzy.entity;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemBlockItem {
    
    // 类型 ("HOT" 或 "ITEM_TYPE")
    private String type;
    
    // 名称
    private String name;
    
    // 级别 (0, 1, 2, 3)
    private Integer level;
    
    // 类型值
    private String typeVal;
    
    // 指数
    private Double index;
    
    // 涨跌率
    private Double riseFallRate;
    
    // 涨跌差值
    private Double riseFallDiff;
} 