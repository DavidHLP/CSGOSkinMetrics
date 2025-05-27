package com.gzy.repository;

import com.gzy.entity.Statistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatisticsRepository extends MongoRepository<Statistics, String> {
    
    /**
     * 根据创建时间范围查询
     */
    List<Statistics> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 根据创建时间倒序查询最新的几条记录
     */
    List<Statistics> findTopNByOrderByCreateTimeDesc(int limit);
    
    /**
     * 查询最新的一条记录
     */
    Statistics findFirstByOrderByCreateTimeDesc();
}
