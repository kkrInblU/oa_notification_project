package com.gdut.oanotification.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReminderItem {

    private Long deliveryId;
    private String newsId;
    private String title;
    private String department;
    private String publishTime;
    private String detailUrl;
    private String summary;
    private Boolean isRead;
    private String status;
}
