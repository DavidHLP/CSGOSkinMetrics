package com.gzy.service;

import com.gzy.entity.ItemBlock;
import com.gzy.entity.ItemBlockCategory;
import com.gzy.entity.ItemBlockData;
import com.gzy.entity.ItemBlockItem;
import com.gzy.repository.ItemBlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemBlockService {

    private final ItemBlockRepository itemBlockRepository;

    /**
     * 获取最新的ItemBlock数据
     */
    public ItemBlock getLatestItemBlock() {
        return itemBlockRepository.findFirstByOrderByCreateTimeDesc();
    }

    /**
     * 获取一段时间内的ItemBlock数据
     */
    public List<ItemBlock> getItemBlocksByTimeRange(LocalDateTime start, LocalDateTime end) {
        return itemBlockRepository.findByCreateTimeBetween(start, end);
    }

    /**
     * 获取热门物品的涨跌幅统计
     */
    public Map<String, Object> analyzeHotItemsRiseFall() {
        ItemBlock latest = getLatestItemBlock();
        if (latest == null || latest.getData() == null || latest.getData().getHot() == null) {
            return Collections.emptyMap();
        }

        ItemBlockCategory hotCategory = latest.getData().getHot();

        // 统计涨幅和跌幅分布
        Map<String, Object> result = new HashMap<>();

        // 统计涨幅榜数据
        if (hotCategory.getTopList() != null && !hotCategory.getTopList().isEmpty()) {
            List<ItemBlockItem> topItems = hotCategory.getTopList();

            double avgRiseFallRate = topItems.stream()
                    .mapToDouble(ItemBlockItem::getRiseFallRate)
                    .average()
                    .orElse(0.0);

            double maxRiseFallRate = topItems.stream()
                    .mapToDouble(ItemBlockItem::getRiseFallRate)
                    .max()
                    .orElse(0.0);

            result.put("topListAvgRate", avgRiseFallRate);
            result.put("topListMaxRate", maxRiseFallRate);
            result.put("topItemsCount", topItems.size());
        }

        // 统计跌幅榜数据
        if (hotCategory.getBottomList() != null && !hotCategory.getBottomList().isEmpty()) {
            List<ItemBlockItem> bottomItems = hotCategory.getBottomList();

            double avgRiseFallRate = bottomItems.stream()
                    .mapToDouble(ItemBlockItem::getRiseFallRate)
                    .average()
                    .orElse(0.0);

            double minRiseFallRate = bottomItems.stream()
                    .mapToDouble(ItemBlockItem::getRiseFallRate)
                    .min()
                    .orElse(0.0);

            result.put("bottomListAvgRate", avgRiseFallRate);
            result.put("bottomListMinRate", minRiseFallRate);
            result.put("bottomItemsCount", bottomItems.size());
        }

        return result;
    }

    /**
     * 获取物品类型的涨跌幅统计
     */
    public Map<String, Object> analyzeItemTypeRiseFall(int level) {
        ItemBlock latest = getLatestItemBlock();
        if (latest == null || latest.getData() == null) {
            return Collections.emptyMap();
        }

        ItemBlockData data = latest.getData();
        ItemBlockCategory category = null;

        // 选择对应级别的类型
        switch (level) {
            case 1:
                category = data.getItemTypeLevel1();
                break;
            case 2:
                category = data.getItemTypeLevel2();
                break;
            case 3:
                category = data.getItemTypeLevel3();
                break;
            default:
                return Collections.emptyMap();
        }

        if (category == null || category.getDefaultList() == null || category.getDefaultList().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        List<ItemBlockItem> items = category.getDefaultList();

        // 统计涨跌幅分布
        Map<String, Double> nameToRateMap = items.stream()
                .collect(Collectors.toMap(
                        ItemBlockItem::getName,
                        ItemBlockItem::getRiseFallRate,
                        (rate1, rate2) -> rate1 // 如有重复键，保留第一个
                ));

        // 计算平均涨跌幅
        double avgRiseFallRate = items.stream()
                .mapToDouble(ItemBlockItem::getRiseFallRate)
                .average()
                .orElse(0.0);

        // 统计涨幅和跌幅项目数量
        long risingItemsCount = items.stream()
                .filter(item -> item.getRiseFallRate() >= 0)
                .count();

        long fallingItemsCount = items.stream()
                .filter(item -> item.getRiseFallRate() < 0)
                .count();

        result.put("nameToRateMap", nameToRateMap);
        result.put("avgRiseFallRate", avgRiseFallRate);
        result.put("risingItemsCount", risingItemsCount);
        result.put("fallingItemsCount", fallingItemsCount);
        result.put("totalItemsCount", items.size());

        return result;
    }

    /**
     * 获取指数统计数据
     */
    public Map<String, Object> analyzeItemIndex(String categoryName) {
        ItemBlock latest = getLatestItemBlock();
        if (latest == null || latest.getData() == null) {
            return Collections.emptyMap();
        }

        ItemBlockData data = latest.getData();
        ItemBlockCategory category = null;

        // 选择对应的分类
        switch (categoryName.toLowerCase()) {
            case "hot":
                category = data.getHot();
                break;
            case "level1":
            case "itemtypelevel1":
                category = data.getItemTypeLevel1();
                break;
            case "level2":
            case "itemtypelevel2":
                category = data.getItemTypeLevel2();
                break;
            case "level3":
            case "itemtypelevel3":
                category = data.getItemTypeLevel3();
                break;
            default:
                return Collections.emptyMap();
        }

        if (category == null || category.getDefaultList() == null || category.getDefaultList().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        List<ItemBlockItem> items = category.getDefaultList();

        // 统计指数分布
        Map<String, Double> nameToIndexMap = items.stream()
                .collect(Collectors.toMap(
                        ItemBlockItem::getName,
                        ItemBlockItem::getIndex,
                        (index1, index2) -> index1 // 如有重复键，保留第一个
                ));

        // 计算平均指数
        double avgIndex = items.stream()
                .mapToDouble(ItemBlockItem::getIndex)
                .average()
                .orElse(0.0);

        // 找出最高和最低指数
        OptionalDouble maxIndex = items.stream()
                .mapToDouble(ItemBlockItem::getIndex)
                .max();

        OptionalDouble minIndex = items.stream()
                .mapToDouble(ItemBlockItem::getIndex)
                .min();

        result.put("nameToIndexMap", nameToIndexMap);
        result.put("avgIndex", avgIndex);
        result.put("maxIndex", maxIndex.isPresent() ? maxIndex.getAsDouble() : 0.0);
        result.put("minIndex", minIndex.isPresent() ? minIndex.getAsDouble() : 0.0);
        result.put("totalItemsCount", items.size());

        return result;
    }

    /**
     * 统计数据总览
     */
    public Map<String, Object> getItemBlockOverview() {
        ItemBlock latest = getLatestItemBlock();
        if (latest == null || latest.getData() == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        ItemBlockData data = latest.getData();

        // 记录数据获取时间
        result.put("createTime", latest.getCreateTime());

        // 统计各分类项目数量
        int hotItemsCount = countItemsInCategory(data.getHot());
        int level1ItemsCount = countItemsInCategory(data.getItemTypeLevel1());
        int level2ItemsCount = countItemsInCategory(data.getItemTypeLevel2());
        int level3ItemsCount = countItemsInCategory(data.getItemTypeLevel3());

        result.put("hotItemsCount", hotItemsCount);
        result.put("level1ItemsCount", level1ItemsCount);
        result.put("level2ItemsCount", level2ItemsCount);
        result.put("level3ItemsCount", level3ItemsCount);
        result.put("totalItemsCount", hotItemsCount + level1ItemsCount + level2ItemsCount + level3ItemsCount);

        // 找出涨幅最高的项目
        ItemBlockItem topRisingItem = null;
        if (data.getHot() != null && data.getHot().getTopList() != null && !data.getHot().getTopList().isEmpty()) {
            topRisingItem = data.getHot().getTopList().get(0);
        }

        // 找出跌幅最大的项目
        ItemBlockItem topFallingItem = null;
        if (data.getHot() != null && data.getHot().getBottomList() != null
                && !data.getHot().getBottomList().isEmpty()) {
            topFallingItem = data.getHot().getBottomList().get(0);
        }

        result.put("topRisingItem", topRisingItem);
        result.put("topFallingItem", topFallingItem);

        return result;
    }

    /**
     * 统计分类中的项目数量
     */
    private int countItemsInCategory(ItemBlockCategory category) {
        if (category == null) {
            return 0;
        }

        int count = 0;
        if (category.getDefaultList() != null) {
            count += category.getDefaultList().size();
        }
        if (category.getTopList() != null) {
            count += category.getTopList().size();
        }
        if (category.getBottomList() != null) {
            count += category.getBottomList().size();
        }

        return count;
    }

    /**
     * 获取物品价格趋势数据（最近7天）
     */
    public Map<String, Object> getItemPriceTrend(String itemName) {
        // 获取最近7天的数据
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);

        List<ItemBlock> itemBlocks = itemBlockRepository.findByCreateTimeBetween(start, end);

        if (itemBlocks.isEmpty()) {
            return Collections.emptyMap();
        }

        // 按时间排序
        itemBlocks.sort(Comparator.comparing(ItemBlock::getCreateTime));

        List<String> timeLabels = new ArrayList<>();
        List<Double> indexValues = new ArrayList<>();
        List<Double> riseFallRates = new ArrayList<>();

        // 遍历每个时间点的数据
        for (ItemBlock itemBlock : itemBlocks) {
            if (itemBlock.getData() == null)
                continue;

            // 记录时间
            timeLabels.add(itemBlock.getCreateTime().toString());

            // 查找物品数据
            ItemBlockItem itemData = findItemByName(itemBlock.getData(), itemName);

            if (itemData != null) {
                indexValues.add(itemData.getIndex());
                riseFallRates.add(itemData.getRiseFallRate());
            } else {
                // 如果没找到数据，使用null表示
                indexValues.add(null);
                riseFallRates.add(null);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("itemName", itemName);
        result.put("timeLabels", timeLabels);
        result.put("indexValues", indexValues);
        result.put("riseFallRates", riseFallRates);

        return result;
    }

    /**
     * 在ItemBlockData中查找指定名称的物品
     */
    private ItemBlockItem findItemByName(ItemBlockData data, String itemName) {
        // 在热门分类中查找
        ItemBlockItem item = findItemInCategory(data.getHot(), itemName);
        if (item != null)
            return item;

        // 在一级类型中查找
        item = findItemInCategory(data.getItemTypeLevel1(), itemName);
        if (item != null)
            return item;

        // 在二级类型中查找
        item = findItemInCategory(data.getItemTypeLevel2(), itemName);
        if (item != null)
            return item;

        // 在三级类型中查找
        return findItemInCategory(data.getItemTypeLevel3(), itemName);
    }

    /**
     * 在分类中查找指定名称的物品
     */
    private ItemBlockItem findItemInCategory(ItemBlockCategory category, String itemName) {
        if (category == null)
            return null;

        // 在默认列表中查找
        if (category.getDefaultList() != null) {
            for (ItemBlockItem item : category.getDefaultList()) {
                if (itemName.equals(item.getName())) {
                    return item;
                }
            }
        }

        // 在涨幅榜中查找
        if (category.getTopList() != null) {
            for (ItemBlockItem item : category.getTopList()) {
                if (itemName.equals(item.getName())) {
                    return item;
                }
            }
        }

        // 在跌幅榜中查找
        if (category.getBottomList() != null) {
            for (ItemBlockItem item : category.getBottomList()) {
                if (itemName.equals(item.getName())) {
                    return item;
                }
            }
        }

        return null;
    }
}
