package com.gdut.oanotification.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PendingEmailDeliveryRow {

    private Long deliveryId;
    private String newsId;
    private Integer userId;
    private Long subscriptionId;
    private Long jobId;
    private String recipient;
    private String status;
    private String username;
    private String title;
    private String category;
    private String fragmentId;
    private LocalDateTime publishTime;
    private String publishDepartment;
    private String detailUrl;
    private String contentText;
}
