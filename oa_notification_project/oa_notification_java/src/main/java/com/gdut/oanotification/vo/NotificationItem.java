package com.gdut.oanotification.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationItem {

    private Integer id;
    private String newsId;
    private String title;
    private String department;
    private String publishTime;
    private String summary;
    private String detailUrl;
    private Integer viewCount;
    private Boolean unread;
    private String category;
    private String ruleVersion;
    private String ruleDetail;
}
