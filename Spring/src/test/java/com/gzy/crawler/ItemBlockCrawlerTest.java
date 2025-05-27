package com.gzy.crawler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class ItemBlockCrawlerTest {

    @Test
    void testParseItemBlockResponse() {
        String jsonData = """
        {
            "success": true,
            "data": {
                "hot": {
                    "defaultList": [
                        {
                            "type": "HOT",
                            "name": "武库指数",
                            "level": 0,
                            "typeVal": "1368059381798760448",
                            "index": 30184.66,
                            "riseFallRate": 3.35,
                            "riseFallDiff": 979.12
                        }
                    ],
                    "topList": [],
                    "bottomList": []
                }
            },
            "errorCode": 0,
            "errorMsg": null
        }
        """;

        JSONObject jsonResponse = JSON.parseObject(jsonData);
        log.info("解析成功: {}", jsonResponse.toJSONString());
        
        // 验证基本结构
        assert jsonResponse.getBoolean("success") == true;
        assert jsonResponse.getInteger("errorCode") == 0;
        
        JSONObject data = jsonResponse.getJSONObject("data");
        assert data != null;
        
        JSONObject hot = data.getJSONObject("hot");
        assert hot != null;
        
        log.info("测试通过 - ItemBlock数据结构解析正常");
    }
} 