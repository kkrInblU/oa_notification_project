package com.gdut.oanotification.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReminderDeliveryRow {

    private Long deliveryId;
    private String newsId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String title;
    private String publishDepartment;
    private LocalDateTime publishTime;
    private String detailUrl;
    private String contentText;
}
