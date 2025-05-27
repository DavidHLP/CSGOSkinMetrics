package com.gzy.controller;

import com.gzy.entity.ItemBlock;
import com.gzy.entity.Statistics;
import com.gzy.service.HomeViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeViewController {

    @Autowired
    private HomeViewService homeViewService;

    @GetMapping("/home")
    public ResponseEntity<?> getHomeData() {
        List<ItemBlock> itemBlocks = homeViewService.findRecentItemBlocks();
        Statistics statistics = homeViewService.findLatestStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("itemBlocks", itemBlocks);
        response.put("statistics", statistics);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/itemblocks")
    public ResponseEntity<?> getItemBlocks() {
        List<ItemBlock> itemBlocks = homeViewService.findRecentItemBlocks();
        return ResponseEntity.ok(itemBlocks);
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        Statistics statistics = homeViewService.findLatestStatistics();
        return ResponseEntity.ok(statistics);
    }
}
