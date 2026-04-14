package com.gdut.oanotification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("subscriptions")
public class Subscription {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private String targetType;
    private String targetValue;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
