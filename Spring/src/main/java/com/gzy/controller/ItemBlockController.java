package com.gzy.controller;

import com.gzy.crawler.ItemBlockCrawler;
import com.gzy.entity.ItemBlock;
import com.gzy.repository.ItemBlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/item-block")
@RequiredArgsConstructor
public class ItemBlockController {

    private final ItemBlockRepository itemBlockRepository;
    private final ItemBlockCrawler itemBlockCrawler;

    /**
     * 获取最新的ItemBlock数据
     */
    @GetMapping("/latest")
    public ResponseEntity<ItemBlock> getLatest() {
        ItemBlock latest = itemBlockRepository.findFirstByOrderByCreateTimeDesc();
        if (latest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(latest);
    }

    /**
     * 分页获取ItemBlock数据
     */
    @GetMapping("/list")
    public ResponseEntity<Page<ItemBlock>> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ItemBlock> itemBlocks = itemBlockRepository.findAll(pageRequest);

        return ResponseEntity.ok(itemBlocks);
    }

    /**
     * 根据时间范围获取ItemBlock数据
     */
    @GetMapping("/range")
    public ResponseEntity<List<ItemBlock>> getByTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {

        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);

            List<ItemBlock> itemBlocks = itemBlockRepository.findByCreateTimeBetween(start, end);
            return ResponseEntity.ok(itemBlocks);

        } catch (Exception e) {
            log.error("解析时间参数失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据成功状态获取数据
     */
    @GetMapping("/success")
    public ResponseEntity<List<ItemBlock>> getBySuccess(
            @RequestParam(defaultValue = "true") Boolean success) {

        List<ItemBlock> itemBlocks = itemBlockRepository.findBySuccess(success);
        return ResponseEntity.ok(itemBlocks);
    }

    /**
     * 获取数据库中的记录总数
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getCount() {
        long count = itemBlockRepository.count();
        return ResponseEntity.ok(count);
    }

    /**
     * 手动触发数据抓取
     */
    @PostMapping("/crawl")
    public ResponseEntity<String> manualCrawl() {
        try {
            itemBlockCrawler.manualCrawl();
            return ResponseEntity.ok("ItemBlock数据抓取已触发");
        } catch (Exception e) {
            log.error("手动触发ItemBlock抓取失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("抓取失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新数据的特定分类
     */
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<Object> getLatestCategory(@PathVariable String categoryName) {
        ItemBlock latest = itemBlockRepository.findFirstByOrderByCreateTimeDesc();
        if (latest == null || latest.getData() == null) {
            return ResponseEntity.notFound().build();
        }

        Object categoryData = switch (categoryName.toLowerCase()) {
            case "hot" -> latest.getData().getHot();
            case "itemtypelevel1", "level1" -> latest.getData().getItemTypeLevel1();
            case "itemtypelevel2", "level2" -> latest.getData().getItemTypeLevel2();
            case "itemtypelevel3", "level3" -> latest.getData().getItemTypeLevel3();
            default -> null;
        };

        if (categoryData == null) {
            return ResponseEntity.badRequest().body("不支持的分类名称: " + categoryName);
        }

        return ResponseEntity.ok(categoryData);
    }

    /**
     * 获取最新数据的特定分类的特定列表类型
     */
    @GetMapping("/category/{categoryName}/{listType}")
    public ResponseEntity<Object> getLatestCategoryList(
            @PathVariable String categoryName,
            @PathVariable String listType) {

        ItemBlock latest = itemBlockRepository.findFirstByOrderByCreateTimeDesc();
        if (latest == null || latest.getData() == null) {
            return ResponseEntity.notFound().build();
        }

        com.gzy.entity.ItemBlockCategory category = switch (categoryName.toLowerCase()) {
            case "hot" -> latest.getData().getHot();
            case "itemtypelevel1", "level1" -> latest.getData().getItemTypeLevel1();
            case "itemtypelevel2", "level2" -> latest.getData().getItemTypeLevel2();
            case "itemtypelevel3", "level3" -> latest.getData().getItemTypeLevel3();
            default -> null;
        };

        if (category == null) {
            return ResponseEntity.badRequest().body("不支持的分类名称: " + categoryName);
        }

        Object listData = switch (listType.toLowerCase()) {
            case "default", "defaultlist" -> category.getDefaultList();
            case "top", "toplist" -> category.getTopList();
            case "bottom", "bottomlist" -> category.getBottomList();
            default -> null;
        };

        if (listData == null) {
            return ResponseEntity.badRequest().body("不支持的列表类型: " + listType);
        }

        return ResponseEntity.ok(listData);
    }

    /**
     * 删除所有数据（谨慎使用）
     */
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAll() {
        long count = itemBlockRepository.count();
        itemBlockRepository.deleteAll();
        return ResponseEntity.ok("已删除 " + count + " 条ItemBlock记录");
    }
}