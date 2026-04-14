package com.gdut.oanotification.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrawlJobDetailResponse {

    private Long id;
    private String jobType;
    private String triggerMode;
    private String status;
    private Integer incrementalMode;
    private Boolean schedulerEnabled;
    private Integer intervalHours;
    private String startedAt;
    private String finishedAt;
    private Integer durationSeconds;
    private Integer notificationsCount;
    private Integer attachmentsCount;
    private Integer dbNotificationsCount;
    private Integer dbAttachmentsCount;
    private String message;
    private String errorMessage;
    private String createdAt;
    private String updatedAt;
}
