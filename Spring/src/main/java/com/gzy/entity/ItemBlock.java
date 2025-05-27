package com.gzy.entity;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "item_blocks")
public class ItemBlock {

    @Id
    private String id;

    // 创建时间
    private LocalDateTime createTime;

    // 结构化的数据内容
    private ItemBlockData data;

    // 成功状态
    private Boolean success;

    // 错误代码
    private Integer errorCode;

    // 错误信息
    private String errorMsg;

    // 错误数据
    private Object errorData;

    // 错误代码字符串
    private String errorCodeStr;
}