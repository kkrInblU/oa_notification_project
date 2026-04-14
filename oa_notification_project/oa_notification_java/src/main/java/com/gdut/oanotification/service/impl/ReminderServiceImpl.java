package com.gdut.oanotification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdut.oanotification.common.exception.BizException;
import com.gdut.oanotification.entity.User;
import com.gdut.oanotification.mapper.NotificationDeliveryLogMapper;
import com.gdut.oanotification.mapper.UserMapper;
import com.gdut.oanotification.service.ReminderService;
import com.gdut.oanotification.util.TimeFormatUtils;
import com.gdut.oanotification.vo.ReadRemindersResponse;
import com.gdut.oanotification.vo.ReminderDeliveryRow;
import com.gdut.oanotification.vo.ReminderItem;
import com.gdut.oanotification.vo.RemindersResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final NotificationDeliveryLogMapper notificationDeliveryLogMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public RemindersResponse getReminders(String userEmail, int limit) {
        if (!StringUtils.hasText(userEmail)) {
            throw BizException.badRequest("missing userEmail");
        }
        ensureUser(userEmail.trim());
        int safeLimit = Math.max(limit, 20);
        notificationDeliveryLogMapper.ensureMiniappDeliveryRecordsForUser(userEmail.trim(), safeLimit);
        List<ReminderDeliveryRow> rows = notificationDeliveryLogMapper.selectMiniappReminderRows(userEmail.trim(), Math.min(Math.max(limit, 1), 50));
        List<ReminderItem> items = rows.stream().map(row -> ReminderItem.builder()
            .deliveryId(row.getDeliveryId())
            .newsId(row.getNewsId())
            .title(defaultString(row.getTitle()))
            .department(defaultString(row.getPublishDepartment()))
            .publishTime(TimeFormatUtils.formatMinute(row.getPublishTime()))
            .detailUrl(defaultString(row.getDetailUrl()))
            .summary(TimeFormatUtils.buildSummary(row.getContentText(), 56))
            .isRead("read".equalsIgnoreCase(defaultString(row.getStatus())))
            .status(defaultString(row.getStatus()))
            .build()).toList();
        int unreadCount = (int) items.stream().filter(item -> !Boolean.TRUE.equals(item.getIsRead())).count();
        return RemindersResponse.builder()
            .items(items)
            .totalCount(items.size())
            .unreadCount(unreadCount)
            .lastSync(TimeFormatUtils.nowHourMinute())
            .userEmail(userEmail.trim())
            .build();
    }

    @Override
    @Transactional
    public ReadRemindersResponse markRead(String userEmail, List<Long> deliveryIds) {
        if (!StringUtils.hasText(userEmail)) {
            throw BizException.badRequest("missing userEmail");
        }
        int updated = deliveryIds == null || deliveryIds.isEmpty()
            ? 0
            : notificationDeliveryLogMapper.markMiniappDeliveriesRead(userEmail.trim(), deliveryIds);
        return new ReadRemindersResponse(updated);
    }

    private User ensureUser(String email) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email).last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        User user = new User();
        user.setUsername(resolveUsername(email));
        user.setEmail(email);
        user.setEmailNotificationsEnabled(1);
        user.setMiniappNotificationsEnabled(1);
        user.setNotificationRefreshIntervalMinutes(60);
        user.setLastNotificationCheckAt(LocalDateTime.now());
        user.setStatus(1);
        userMapper.insert(user);
        return userMapper.selectById(user.getId());
    }

    private String resolveUsername(String email) {
        int index = email.indexOf('@');
        return index > 0 ? email.substring(0, index) : email;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
