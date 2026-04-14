package com.gdut.oanotification.service;

import com.gdut.oanotification.vo.NotificationDetailResponse;
import com.gdut.oanotification.vo.NotificationListResponse;

public interface NotificationService {

    NotificationListResponse getLatestNotifications(int limit);

    NotificationListResponse getAudienceNotifications(String audienceType, int limit);

    NotificationDetailResponse getNotificationDetail(String newsId);
}
