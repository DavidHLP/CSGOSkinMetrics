package com.gzy.crawler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gzy.entity.ItemBlock;
import com.gzy.entity.ItemBlockData;
import com.gzy.entity.ItemBlockCategory;
import com.gzy.entity.ItemBlockItem;
import com.gzy.repository.ItemBlockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemBlockCrawler {

    private final WebClient webClient;
    private final ItemBlockRepository itemBlockRepository;

    private static final String API_URL = "https://sdt-api.ok-skins.com/index/item-block/v1/summary";
    private static final int TIMEOUT_SECONDS = 30;

    @Scheduled(fixedRate = 30000) // 每30秒执行一次
    public void crawlItemBlockData() {
        log.info("开始抓取ItemBlock数据...");

        try {
            long timestamp = System.currentTimeMillis();

            Mono<String> responseMono = webClient.get()
                    .uri(API_URL + "?timestamp=" + timestamp)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .doOnError(WebClientResponseException.class,
                            ex -> log.error("HTTP错误: 状态码={}, 响应体={}", ex.getStatusCode(), ex.getResponseBodyAsString()))
                    .doOnError(Exception.class, ex -> log.error("请求异常: {}", ex.getMessage()));

            String response = responseMono.block();

            if (response == null || response.isEmpty()) {
                log.error("从API收到空响应");
                return;
            }

            JSONObject jsonResponse = JSON.parseObject(response);

            // 检查响应状态
            Boolean success = jsonResponse.getBoolean("success");
            Integer errorCode = jsonResponse.getInteger("errorCode");
            String errorMsg = jsonResponse.getString("errorMsg");

            // 验证API响应是否成功
            if (success == null || !success || (errorCode != null && errorCode != 0)) {
                log.error("API返回失败状态: success={}, errorCode={}, errorMsg={}",
                        success, errorCode, errorMsg);
                // 仍然保存失败的响应数据用于调试
                saveItemBlockData(jsonResponse, success, errorCode, errorMsg, null);
                return;
            }

            JSONObject dataObject = jsonResponse.getJSONObject("data");
            ItemBlockData parsedData = null;

            if (dataObject != null) {
                log.info("开始解析ItemBlock数据...");
                parsedData = parseItemBlockData(dataObject);
                log.info("成功解析ItemBlock数据，包含 {} 个分类",
                        parsedData != null ? "多个" : "0");
            }

            // 保存数据
            saveItemBlockData(jsonResponse, success, errorCode, errorMsg, parsedData);

            log.info("成功获取并处理ItemBlock数据");

        } catch (Exception e) {
            log.error("抓取ItemBlock数据时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析ItemBlock数据
     */
    private ItemBlockData parseItemBlockData(JSONObject dataObject) {
        try {
            return ItemBlockData.builder()
                    .hot(parseCategory(dataObject.getJSONObject("hot")))
                    .itemTypeLevel1(parseCategory(dataObject.getJSONObject("itemTypeLevel1")))
                    .itemTypeLevel2(parseCategory(dataObject.getJSONObject("itemTypeLevel2")))
                    .itemTypeLevel3(parseCategory(dataObject.getJSONObject("itemTypeLevel3")))
                    .build();
        } catch (Exception e) {
            log.error("解析ItemBlock数据时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析分类数据
     */
    private ItemBlockCategory parseCategory(JSONObject categoryObj) {
        if (categoryObj == null) {
            return null;
        }

        return ItemBlockCategory.builder()
                .defaultList(parseItemList(categoryObj.getJSONArray("defaultList")))
                .topList(parseItemList(categoryObj.getJSONArray("topList")))
                .bottomList(parseItemList(categoryObj.getJSONArray("bottomList")))
                .build();
    }

    /**
     * 解析项目列表
     */
    private List<ItemBlockItem> parseItemList(com.alibaba.fastjson2.JSONArray jsonArray) {
        if (jsonArray == null) {
            return new ArrayList<>();
        }

        List<ItemBlockItem> items = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject itemObj = jsonArray.getJSONObject(i);
            if (itemObj != null) {
                ItemBlockItem item = ItemBlockItem.builder()
                        .type(itemObj.getString("type"))
                        .name(itemObj.getString("name"))
                        .level(itemObj.getInteger("level"))
                        .typeVal(itemObj.getString("typeVal"))
                        .index(itemObj.getDouble("index"))
                        .riseFallRate(itemObj.getDouble("riseFallRate"))
                        .riseFallDiff(itemObj.getDouble("riseFallDiff"))
                        .build();
                items.add(item);
            }
        }
        return items;
    }

    /**
     * 保存ItemBlock数据
     */
    private void saveItemBlockData(JSONObject jsonResponse, Boolean success, Integer errorCode,
            String errorMsg, ItemBlockData data) {
        try {
            ItemBlock itemBlock = ItemBlock.builder()
                    .createTime(LocalDateTime.now())
                    .success(success)
                    .errorCode(errorCode)
                    .errorMsg(errorMsg)
                    .errorData(jsonResponse.get("errorData"))
                    .errorCodeStr(jsonResponse.getString("errorCodeStr"))
                    .data(data)
                    .build();

            ItemBlock savedItemBlock = itemBlockRepository.save(itemBlock);

            int totalItems = 0;
            if (data != null) {
                totalItems += countItemsInCategory(data.getHot());
                totalItems += countItemsInCategory(data.getItemTypeLevel1());
                totalItems += countItemsInCategory(data.getItemTypeLevel2());
                totalItems += countItemsInCategory(data.getItemTypeLevel3());
            }

            log.info("成功保存ItemBlock数据到MongoDB，ID: {}, 包含 {} 个items",
                    savedItemBlock.getId(), totalItems);

        } catch (Exception e) {
            log.error("保存ItemBlock数据时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 统计分类中的项目数量
     */
    private int countItemsInCategory(ItemBlockCategory category) {
        if (category == null) {
            return 0;
        }
        int count = 0;
        if (category.getDefaultList() != null)
            count += category.getDefaultList().size();
        if (category.getTopList() != null)
            count += category.getTopList().size();
        if (category.getBottomList() != null)
            count += category.getBottomList().size();
        return count;
    }

    /**
     * 手动触发数据抓取（用于测试）
     */
    public void manualCrawl() {
        log.info("手动触发ItemBlock数据抓取");
        crawlItemBlockData();
    }
}
