package com.gdut.oanotification.service.impl;

import com.gdut.oanotification.common.exception.BizException;
import com.gdut.oanotification.config.OaProperties;
import com.gdut.oanotification.entity.CrawlJobLog;
import com.gdut.oanotification.mapper.CrawlJobLogMapper;
import com.gdut.oanotification.mapper.NotificationDeliveryLogMapper;
import com.gdut.oanotification.mapper.NotificationMapper;
import com.gdut.oanotification.mapper.UserMapper;
import com.gdut.oanotification.service.DeliverySchedulerService;
import com.gdut.oanotification.service.MiniappService;
import com.gdut.oanotification.util.TimeFormatUtils;
import com.gdut.oanotification.vo.DueUserRow;
import com.gdut.oanotification.vo.ManualDeliveryResponse;
import com.gdut.oanotification.vo.PendingEmailDeliveryRow;
import com.gdut.oanotification.vo.PendingMiniappDeliveryRow;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliverySchedulerServiceImpl implements DeliverySchedulerService {

    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final NotificationDeliveryLogMapper notificationDeliveryLogMapper;
    private final CrawlJobLogMapper crawlJobLogMapper;
    private final JavaMailSender javaMailSender;
    private final MiniappService miniappService;
    private final OaProperties oaProperties;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    @Scheduled(fixedDelay = 60000)
    public void runDeliveryCycle() {
        if (!running.compareAndSet(false, true)) {
            log.info("Delivery scheduler skipped because previous cycle is still running");
            return;
        }
        CrawlJobLog jobLog = createJobLog("scheduled");
        try {
            DeliverySummary summary = prepareDeliveryQueue(jobLog.getId(), false);
            int emailSuccess = sendPendingEmailDeliveries();
            int miniappSuccess = sendPendingMiniappDeliveries();
            int emailFailed = countFailedByDelta(summary.emailCreated, emailSuccess);
            int miniappFailed = countFailedByDelta(summary.miniappCreated, miniappSuccess);
            completeJobLog(jobLog, summary, emailSuccess, emailFailed, miniappSuccess, miniappFailed);
        } catch (Exception ex) {
            failJobLog(jobLog, ex);
        } finally {
            running.set(false);
        }
    }

    @Override
    public ManualDeliveryResponse runManualDelivery() {
        if (!running.compareAndSet(false, true)) {
            throw new BizException(409, "delivery task already in progress");
        }
        CrawlJobLog jobLog = createJobLog("single");
        try {
            DeliverySummary summary = prepareDeliveryQueue(jobLog.getId(), true);
            int emailSuccess = sendPendingEmailDeliveries();
            int miniappSuccess = sendPendingMiniappDeliveries();
            int emailFailed = countFailedByDelta(summary.emailCreated, emailSuccess);
            int miniappFailed = countFailedByDelta(summary.miniappCreated, miniappSuccess);
            completeJobLog(jobLog, summary, emailSuccess, emailFailed, miniappSuccess, miniappFailed);
            return ManualDeliveryResponse.builder()
                .jobId(jobLog.getId())
                .checkedUsers(summary.checkedUsers)
                .matchedNotifications(summary.notificationsMatched)
                .createdEmailDeliveryRecords(summary.emailCreated)
                .createdMiniappDeliveryRecords(summary.miniappCreated)
                .successfulEmailDeliveries(emailSuccess)
                .failedEmailDeliveries(emailFailed)
                .successfulMiniappDeliveries(miniappSuccess)
                .failedMiniappDeliveries(miniappFailed)
                .successfulDeliveries(emailSuccess + miniappSuccess)
                .failedDeliveries(emailFailed + miniappFailed)
                .build();
        } catch (Exception ex) {
            failJobLog(jobLog, ex);
            throw ex;
        } finally {
            running.set(false);
        }
    }

    @Transactional
    protected DeliverySummary prepareDeliveryQueue(Long jobId, boolean ignorePeriod) {
        List<DueUserRow> dueUsers = ignorePeriod
            ? userMapper.selectActiveUsersForManualDelivery()
            : userMapper.selectDueUsersForNotificationCheck();
        DeliverySummary summary = new DeliverySummary();
        for (DueUserRow user : dueUsers) {
            Object sinceTime = user.getLastNotificationCheckAt() != null ? user.getLastNotificationCheckAt() : user.getCreatedAt();
            int notificationsCount = defaultInt(notificationMapper.countUserNewNotificationsSince(user.getId(), sinceTime));
            int emailCreated = notificationDeliveryLogMapper.insertDueEmailDeliveryRecords(jobId, user.getId(), sinceTime);
            int miniappCreated = notificationDeliveryLogMapper.insertDueMiniappDeliveryRecords(jobId, user.getId(), sinceTime);
            var entity = new com.gdut.oanotification.entity.User();
            entity.setId(user.getId());
            entity.setLastNotificationCheckAt(LocalDateTime.now());
            userMapper.updateById(entity);

            summary.checkedUsers++;
            summary.notificationsMatched += notificationsCount;
            summary.emailCreated += emailCreated;
            summary.miniappCreated += miniappCreated;
        }
        return summary;
    }

    protected int sendPendingEmailDeliveries() {
        List<PendingEmailDeliveryRow> pendingRows = notificationDeliveryLogMapper.selectPendingEmailDeliveries();
        if (pendingRows.isEmpty()) {
            return 0;
        }
        Map<String, List<PendingEmailDeliveryRow>> grouped = new LinkedHashMap<>();
        for (PendingEmailDeliveryRow row : pendingRows) {
            grouped.computeIfAbsent(row.getRecipient(), key -> new ArrayList<>()).add(row);
        }
        int successCount = 0;
        for (List<PendingEmailDeliveryRow> rows : grouped.values()) {
            List<Long> deliveryIds = rows.stream().map(PendingEmailDeliveryRow::getDeliveryId).toList();
            try {
                if (sendNotificationEmail(rows.get(0).getRecipient(), rows)) {
                    notificationDeliveryLogMapper.markDeliverySuccess(deliveryIds, null);
                } else {
                    notificationDeliveryLogMapper.markDeliverySuccess(deliveryIds, null);
                }
                successCount += deliveryIds.size();
            } catch (Exception ex) {
                notificationDeliveryLogMapper.markDeliveryFailed(deliveryIds, trimError(ex.getMessage()));
            }
        }
        return successCount;
    }

    protected int sendPendingMiniappDeliveries() {
        List<PendingMiniappDeliveryRow> pendingRows = notificationDeliveryLogMapper.selectPendingMiniappDeliveries();
        if (pendingRows.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        for (PendingMiniappDeliveryRow row : pendingRows) {
            try {
                if (!StringUtils.hasText(row.getWechatOpenid())) {
                    notificationDeliveryLogMapper.markDeliveryFailed(List.of(row.getDeliveryId()), "user wechat_openid not bound");
                    continue;
                }
                Map<String, Object> response = miniappService.sendSubscribeMessage(
                    row.getWechatOpenid(),
                    Map.of(
                        "newsId", row.getNewsId(),
                        "title", defaultString(row.getTitle()),
                        "category", defaultString(row.getCategory()),
                        "publishDepartment", defaultString(row.getPublishDepartment()),
                        "publishTime", TimeFormatUtils.formatMinute(row.getPublishTime()),
                        "detailUrl", defaultString(row.getDetailUrl()),
                        "contentText", defaultString(row.getContentText())
                    ),
                    "pages/detail/detail?newsId=" + row.getNewsId()
                );
                notificationDeliveryLogMapper.markDeliverySuccess(
                    List.of(row.getDeliveryId()),
                    String.valueOf(response.getOrDefault("msgid", ""))
                );
                successCount++;
            } catch (Exception ex) {
                notificationDeliveryLogMapper.markDeliveryFailed(List.of(row.getDeliveryId()), trimError(ex.getMessage()));
            }
        }
        return successCount;
    }

    private CrawlJobLog createJobLog(String triggerMode) {
        CrawlJobLog jobLog = new CrawlJobLog();
        jobLog.setJobType("delivery_only");
        jobLog.setTriggerMode(triggerMode);
        jobLog.setStatus("running");
        jobLog.setIncrementalMode(0);
        jobLog.setSchedulerEnabled(0);
        jobLog.setStartedAt(LocalDateTime.now());
        jobLog.setMessage("User-based notification delivery task started");
        crawlJobLogMapper.insert(jobLog);
        return jobLog;
    }

    private void completeJobLog(
        CrawlJobLog jobLog,
        DeliverySummary summary,
        int emailSuccess,
        int emailFailed,
        int miniappSuccess,
        int miniappFailed
    ) {
        jobLog.setStatus(emailFailed == 0 && miniappFailed == 0 ? "success" : "partial_success");
        jobLog.setFinishedAt(LocalDateTime.now());
        jobLog.setNotificationsCount(summary.notificationsMatched);
        jobLog.setDbNotificationsCount(summary.emailCreated);
        jobLog.setDbAttachmentsCount(summary.miniappCreated);
        jobLog.setMessage(
            "Delivery task completed; "
                + "checked_users=" + summary.checkedUsers + "; "
                + "matched_notifications=" + summary.notificationsMatched + "; "
                + "email_records=" + summary.emailCreated + "; "
                + "miniapp_records=" + summary.miniappCreated + "; "
                + "email_delivered=" + emailSuccess + "; "
                + "email_failed=" + emailFailed + "; "
                + "miniapp_delivered=" + miniappSuccess + "; "
                + "miniapp_failed=" + miniappFailed
        );
        jobLog.setDurationSeconds((int) java.time.Duration.between(jobLog.getStartedAt(), jobLog.getFinishedAt()).getSeconds());
        crawlJobLogMapper.updateById(jobLog);
    }

    private void failJobLog(CrawlJobLog jobLog, Exception ex) {
        log.error("Delivery scheduler failed", ex);
        jobLog.setStatus("failed");
        jobLog.setFinishedAt(LocalDateTime.now());
        jobLog.setErrorMessage(ex.getMessage());
        jobLog.setMessage("User-based notification delivery task failed");
        jobLog.setDurationSeconds((int) java.time.Duration.between(jobLog.getStartedAt(), jobLog.getFinishedAt()).getSeconds());
        crawlJobLogMapper.updateById(jobLog);
    }

    private boolean sendNotificationEmail(String recipient, List<PendingEmailDeliveryRow> rows) throws Exception {
        if (!StringUtils.hasText(recipient)) {
            return false;
        }
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        String from = StringUtils.hasText(oaProperties.getDelivery().getDefaultFromEmail())
            ? oaProperties.getDelivery().getDefaultFromEmail()
            : null;
        if (StringUtils.hasText(from)) {
            helper.setFrom(from);
        }
        helper.setTo(recipient);
        helper.setSubject(buildMailSubject(rows));
        helper.setText(buildMailBody(rows), false);
        javaMailSender.send(message);
        return true;
    }

    private String buildMailSubject(List<PendingEmailDeliveryRow> rows) {
        return "[Campus Notification] Found " + rows.size() + " new notifications";
    }

    private String buildMailBody(List<PendingEmailDeliveryRow> rows) {
        StringBuilder builder = new StringBuilder("You have new campus notifications:\n\n");
        rows.stream()
            .sorted(Comparator.comparing(PendingEmailDeliveryRow::getPublishTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .forEach(row -> builder.append("- ")
                .append(defaultString(row.getTitle()))
                .append(" | ")
                .append(defaultString(row.getPublishDepartment()))
                .append(" | ")
                .append(TimeFormatUtils.formatMinute(row.getPublishTime()))
                .append("\n")
                .append(defaultString(row.getDetailUrl()))
                .append("\n\n"));
        return builder.toString();
    }

    private int countFailedByDelta(int createdCount, int successCount) {
        return Math.max(createdCount - successCount, 0);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String trimError(String message) {
        if (!StringUtils.hasText(message)) {
            return "delivery failed";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }

    private static class DeliverySummary {
        private int checkedUsers;
        private int notificationsMatched;
        private int emailCreated;
        private int miniappCreated;
    }
}
