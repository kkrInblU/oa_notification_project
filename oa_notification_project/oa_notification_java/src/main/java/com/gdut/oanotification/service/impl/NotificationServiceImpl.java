package com.gdut.oanotification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdut.oanotification.common.exception.BizException;
import com.gdut.oanotification.entity.Attachment;
import com.gdut.oanotification.entity.Notification;
import com.gdut.oanotification.mapper.AttachmentMapper;
import com.gdut.oanotification.mapper.NotificationMapper;
import com.gdut.oanotification.service.NotificationService;
import com.gdut.oanotification.util.HtmlContentUtils;
import com.gdut.oanotification.util.TimeFormatUtils;
import com.gdut.oanotification.vo.AttachmentItem;
import com.gdut.oanotification.vo.NotificationDetailResponse;
import com.gdut.oanotification.vo.NotificationItem;
import com.gdut.oanotification.vo.NotificationListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final NotificationMapper notificationMapper;
    private final AttachmentMapper attachmentMapper;

    @Override
    public NotificationListResponse getLatestNotifications(int limit) {
        int safeLimit = normalizeLimit(limit);
        List<Notification> rows = notificationMapper.selectList(
            new LambdaQueryWrapper<Notification>()
                .orderByDesc(Notification::getPublishTime)
                .orderByDesc(Notification::getId)
                .last("LIMIT " + safeLimit)
        );

        List<NotificationItem> items = rows.stream().map(this::toNotificationItem).toList();
        Long totalCount = notificationMapper.selectCount(new LambdaQueryWrapper<>());
        return NotificationListResponse.builder()
            .items(items)
            .totalCount(totalCount == null ? 0 : totalCount.intValue())
            .unreadCount(0)
            .lastSync(TimeFormatUtils.nowHourMinute())
            .build();
    }

    @Override
    public NotificationListResponse getAudienceNotifications(String audienceType, int limit) {
        int safeLimit = normalizeLimit(limit);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        switch (audienceType) {
            case "undergraduate" -> wrapper.eq(Notification::getAudienceUndergraduate, 1);
            case "graduate" -> wrapper.eq(Notification::getAudienceGraduate, 1);
            case "staff" -> wrapper.eq(Notification::getAudienceStaff, 1);
            default -> throw BizException.badRequest("unsupported audienceType");
        }

        List<Notification> rows = notificationMapper.selectList(
            wrapper.orderByDesc(Notification::getPublishTime)
                .orderByDesc(Notification::getId)
                .last("LIMIT " + safeLimit)
        );

        List<NotificationItem> items = rows.stream()
            .map(row -> toAudienceNotificationItem(row, audienceType))
            .toList();

        return NotificationListResponse.builder()
            .items(items)
            .totalCount(items.size())
            .unreadCount(0)
            .lastSync(TimeFormatUtils.nowHourMinute())
            .build();
    }

    @Override
    public NotificationDetailResponse getNotificationDetail(String newsId) {
        if (!StringUtils.hasText(newsId)) {
            throw BizException.badRequest("missing newsId");
        }
        Notification notification = notificationMapper.selectOne(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getNewsId, newsId)
                .last("LIMIT 1")
        );
        if (notification == null) {
            throw BizException.notFound("notification not found");
        }

        List<AttachmentItem> attachments = attachmentMapper.selectList(
            new LambdaQueryWrapper<Attachment>()
                .eq(Attachment::getNewsId, newsId)
                .orderByAsc(Attachment::getId)
        ).stream().map(item -> AttachmentItem.builder()
            .fileId(item.getFileId())
            .filename(StringUtils.hasText(item.getFilename()) ? item.getFilename() : "Unnamed Attachment")
            .extension(defaultString(item.getExtension()))
            .size(item.getSize() == null ? 0L : item.getSize())
            .build()
        ).toList();

        return NotificationDetailResponse.builder()
            .id(notification.getId())
            .newsId(notification.getNewsId())
            .title(defaultString(notification.getTitle()))
            .department(defaultDepartment(notification.getPublishDepartment()))
            .publishTime(TimeFormatUtils.formatMinute(notification.getPublishTime()))
            .category(defaultString(notification.getCategory()))
            .contentText(defaultString(notification.getContentText()))
            .contentHtml(defaultString(notification.getContentHtml()))
            .miniappContentHtml(HtmlContentUtils.buildMiniappContentHtml(notification.getContentHtml(), ""))
            .detailUrl(defaultString(notification.getDetailUrl()))
            .viewCount(notification.getViewCount() == null ? 0 : notification.getViewCount())
            .images(HtmlContentUtils.extractImageUrls(notification.getContentHtml(), ""))
            .attachments(attachments)
            .build();
    }

    private NotificationItem toNotificationItem(Notification row) {
        return NotificationItem.builder()
            .id(row.getId())
            .newsId(row.getNewsId())
            .title(defaultString(row.getTitle()))
            .department(defaultDepartment(row.getPublishDepartment()))
            .publishTime(TimeFormatUtils.formatMinute(row.getPublishTime()))
            .summary(TimeFormatUtils.buildSummary(row.getContentText(), 56))
            .detailUrl(defaultString(row.getDetailUrl()))
            .viewCount(row.getViewCount() == null ? 0 : row.getViewCount())
            .unread(false)
            .build();
    }

    private NotificationItem toAudienceNotificationItem(Notification row, String audienceType) {
        NotificationItem.NotificationItemBuilder builder = NotificationItem.builder()
            .id(row.getId())
            .newsId(row.getNewsId())
            .title(defaultString(row.getTitle()))
            .department(defaultDepartment(row.getPublishDepartment()))
            .publishTime(TimeFormatUtils.formatMinute(row.getPublishTime()))
            .category(defaultString(row.getCategory()))
            .summary(TimeFormatUtils.buildSummary(row.getContentText(), 56))
            .detailUrl(defaultString(row.getDetailUrl()));
        switch (audienceType) {
            case "undergraduate" -> {
                builder.ruleVersion(defaultString(row.getAudienceUndergraduateRuleVersion()));
                builder.ruleDetail(defaultString(row.getAudienceUndergraduateRuleDetail()));
            }
            case "graduate" -> {
                builder.ruleVersion(defaultString(row.getAudienceGraduateRuleVersion()));
                builder.ruleDetail(defaultString(row.getAudienceGraduateRuleDetail()));
            }
            case "staff" -> {
                builder.ruleVersion(defaultString(row.getAudienceStaffRuleVersion()));
                builder.ruleDetail(defaultString(row.getAudienceStaffRuleDetail()));
            }
            default -> {
            }
        }
        return builder.build();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String defaultDepartment(String value) {
        return StringUtils.hasText(value) ? value : "Unknown Department";
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
