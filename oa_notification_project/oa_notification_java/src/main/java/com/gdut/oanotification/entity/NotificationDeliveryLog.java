package com.gdut.oanotification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("notification_delivery_log")
public class NotificationDeliveryLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String newsId;
    private Integer userId;
    private Long subscriptionId;
    private Long jobId;
    private String channel;
    private String recipient;
    private String status;
    private Integer retryCount;
    private String errorMsg;
    private String providerMessageId;
    private LocalDateTime sentAt;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
