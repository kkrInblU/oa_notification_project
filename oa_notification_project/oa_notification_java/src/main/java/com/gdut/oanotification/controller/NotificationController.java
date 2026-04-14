package com.gdut.oanotification.controller;

import com.gdut.oanotification.common.api.ResultBody;
import com.gdut.oanotification.service.NotificationService;
import com.gdut.oanotification.vo.NotificationDetailResponse;
import com.gdut.oanotification.vo.NotificationListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public ResultBody<NotificationListResponse> getNotifications(
        @RequestParam(value = "limit", defaultValue = "20") Integer limit
    ) {
        return ResultBody.success(notificationService.getLatestNotifications(limit));
    }

    @GetMapping("/notifications/undergraduate")
    public ResultBody<NotificationListResponse> getUndergraduateNotifications(
        @RequestParam(value = "limit", defaultValue = "20") Integer limit
    ) {
        return ResultBody.success(notificationService.getAudienceNotifications("undergraduate", limit));
    }

    @GetMapping("/notifications/graduate")
    public ResultBody<NotificationListResponse> getGraduateNotifications(
        @RequestParam(value = "limit", defaultValue = "20") Integer limit
    ) {
        return ResultBody.success(notificationService.getAudienceNotifications("graduate", limit));
    }

    @GetMapping("/notifications/staff")
    public ResultBody<NotificationListResponse> getStaffNotifications(
        @RequestParam(value = "limit", defaultValue = "20") Integer limit
    ) {
        return ResultBody.success(notificationService.getAudienceNotifications("staff", limit));
    }

    @GetMapping("/notification-detail")
    public ResultBody<NotificationDetailResponse> getNotificationDetail(
        @RequestParam("newsId") String newsId
    ) {
        return ResultBody.success(notificationService.getNotificationDetail(newsId));
    }
}
