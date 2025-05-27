package com.gzy.repository;

import com.gzy.entity.ItemBlock;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemBlockRepository extends MongoRepository<ItemBlock, String> {
    
    /**
     * 根据创建时间范围查询
     */
    List<ItemBlock> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 根据创建时间倒序查询最新的几条记录
     */
    List<ItemBlock> findTopNByOrderByCreateTimeDesc(int limit);
    
    /**
     * 查询最新的一条记录
     */
    ItemBlock findFirstByOrderByCreateTimeDesc();
    
    /**
     * 根据成功状态查询
     */
    List<ItemBlock> findBySuccess(Boolean success);
} 