package com.gzy.entity;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemBlockCategory {
    
    // 默认列表
    private List<ItemBlockItem> defaultList;
    
    // 涨幅榜 (topList)
    private List<ItemBlockItem> topList;
    
    // 跌幅榜 (bottomList)
    private List<ItemBlockItem> bottomList;
} 