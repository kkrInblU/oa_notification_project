package com.gdut.oanotification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String email;
    private String wechatOpenid;
    private Integer emailNotificationsEnabled;
    private Integer miniappNotificationsEnabled;
    private Integer notificationRefreshIntervalMinutes;
    private LocalDateTime lastNotificationCheckAt;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
