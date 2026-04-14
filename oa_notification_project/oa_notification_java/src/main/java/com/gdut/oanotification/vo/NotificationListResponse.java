package com.gdut.oanotification.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationListResponse {

    private List<NotificationItem> items;
    private Integer totalCount;
    private Integer unreadCount;
    private String lastSync;
}
