package com.gzy.service;

import com.gzy.entity.ItemBlock;
import com.gzy.entity.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeViewService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<ItemBlock> findRecentItemBlocks() {
        Query query = new Query().limit(10);
        return mongoTemplate.find(query, ItemBlock.class);
    }

    public Statistics findLatestStatistics() {
        Query query = new Query().limit(1);
        return mongoTemplate.findOne(query, Statistics.class);
    }
}
