package com.gdut.oanotification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("crawl_job_log")
public class CrawlJobLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String jobType;
    private String triggerMode;
    private String status;
    private Integer incrementalMode;
    private Integer schedulerEnabled;
    private Integer intervalHours;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer durationSeconds;
    private Integer notificationsCount;
    private Integer attachmentsCount;
    private Integer dbNotificationsCount;
    private Integer dbAttachmentsCount;
    private String message;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
