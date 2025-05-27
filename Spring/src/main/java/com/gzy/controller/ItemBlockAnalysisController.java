package com.gzy.controller;

import com.gzy.service.ItemBlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/item-block/analysis")
@RequiredArgsConstructor
public class ItemBlockAnalysisController {

    private final ItemBlockService itemBlockService;

    /**
     * 获取数据总览信息
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> overview = itemBlockService.getItemBlockOverview();
        return ResponseEntity.ok(overview);
    }

    /**
     * 获取热门物品涨跌幅分析
     */
    @GetMapping("/hot-rise-fall")
    public ResponseEntity<Map<String, Object>> getHotItemsRiseFall() {
        Map<String, Object> analysis = itemBlockService.analyzeHotItemsRiseFall();
        return ResponseEntity.ok(analysis);
    }

    /**
     * 获取物品类型涨跌幅分析
     */
    @GetMapping("/item-type-rise-fall/{level}")
    public ResponseEntity<Map<String, Object>> getItemTypeRiseFall(@PathVariable int level) {
        if (level < 1 || level > 3) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> analysis = itemBlockService.analyzeItemTypeRiseFall(level);
        return ResponseEntity.ok(analysis);
    }

    /**
     * 获取指数分析
     */
    @GetMapping("/index-analysis/{category}")
    public ResponseEntity<Map<String, Object>> getIndexAnalysis(@PathVariable String category) {
        Map<String, Object> analysis = itemBlockService.analyzeItemIndex(category);

        if (analysis.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(analysis);
    }

    /**
     * 获取物品价格趋势数据（最近7天）
     */
    @GetMapping("/trend/{itemName}")
    public ResponseEntity<Map<String, Object>> getItemPriceTrend(@PathVariable String itemName) {
        Map<String, Object> trendData = itemBlockService.getItemPriceTrend(itemName);

        if (trendData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(trendData);
    }
}