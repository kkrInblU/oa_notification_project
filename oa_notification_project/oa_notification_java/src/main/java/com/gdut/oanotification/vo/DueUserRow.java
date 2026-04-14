package com.gdut.oanotification.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DueUserRow {

    private Integer id;
    private String username;
    private String email;
    private String wechatOpenid;
    private Integer emailNotificationsEnabled;
    private Integer miniappNotificationsEnabled;
    private Integer notificationRefreshIntervalMinutes;
    private LocalDateTime lastNotificationCheckAt;
    private LocalDateTime createdAt;
}
