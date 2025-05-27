package com.gzy.entity;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemBlockData {
    
    // 热门数据
    private ItemBlockCategory hot;
    
    // 一级物品类型数据
    private ItemBlockCategory itemTypeLevel1;
    
    // 二级物品类型数据
    private ItemBlockCategory itemTypeLevel2;
    
    // 三级物品类型数据
    private ItemBlockCategory itemTypeLevel3;
} 