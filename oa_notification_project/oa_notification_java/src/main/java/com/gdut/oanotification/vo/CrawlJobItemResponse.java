package com.gdut.oanotification.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrawlJobItemResponse {

    private Long id;
    private String jobType;
    private String triggerMode;
    private String status;
    private String startedAt;
    private String finishedAt;
    private Integer durationSeconds;
    private Integer notificationsCount;
    private Integer attachmentsCount;
    private Integer dbNotificationsCount;
    private Integer dbAttachmentsCount;
    private String message;
    private String errorMessage;
}
