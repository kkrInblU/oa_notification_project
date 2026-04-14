package com.gdut.oanotification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdut.oanotification.entity.NotificationDeliveryLog;
import com.gdut.oanotification.vo.PendingEmailDeliveryRow;
import com.gdut.oanotification.vo.PendingMiniappDeliveryRow;
import com.gdut.oanotification.vo.ReminderDeliveryRow;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface NotificationDeliveryLogMapper extends BaseMapper<NotificationDeliveryLog> {

    @Select("""
        SELECT
            d.id AS delivery_id,
            d.news_id,
            d.status,
            d.created_at,
            d.updated_at,
            n.title,
            n.publish_department,
            n.publish_time,
            n.detail_url,
            n.content_text
        FROM notification_delivery_log d
        INNER JOIN users u
            ON u.id = d.user_id
        INNER JOIN notifications n
            ON n.news_id = d.news_id
        WHERE d.channel = 'miniapp'
          AND u.email = #{email}
        ORDER BY
            CASE WHEN n.publish_time IS NULL THEN 1 ELSE 0 END,
            n.publish_time DESC,
            d.id DESC
        LIMIT #{limit}
        """)
    List<ReminderDeliveryRow> selectMiniappReminderRows(@Param("email") String email, @Param("limit") int limit);

    @Update({
        "<script>",
        "UPDATE notification_delivery_log d",
        "INNER JOIN users u ON u.id = d.user_id",
        "SET d.status = 'read',",
        "    d.last_attempt_at = CURRENT_TIMESTAMP,",
        "    d.updated_at = CURRENT_TIMESTAMP",
        "WHERE d.channel = 'miniapp'",
        "  AND u.email = #{email}",
        "  AND d.id IN",
        "<foreach collection='deliveryIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    int markMiniappDeliveriesRead(@Param("email") String email, @Param("deliveryIds") List<Long> deliveryIds);

    @Insert("""
        INSERT IGNORE INTO notification_delivery_log (
            news_id,
            user_id,
            subscription_id,
            job_id,
            channel,
            recipient,
            status
        )
        SELECT
            n.news_id,
            u.id,
            s.id,
            #{jobId},
            'email',
            u.email,
            'pending'
        FROM users u
        INNER JOIN subscriptions s
            ON s.user_id = u.id
           AND s.target_type = 'department'
           AND s.status = 1
           AND u.email_notifications_enabled = 1
        INNER JOIN notifications n
            ON n.publish_department = s.target_value
        WHERE u.id = #{userId}
          AND u.status = 1
          AND u.email IS NOT NULL
          AND u.email <> ''
          AND COALESCE(n.first_seen_time, n.crawl_time, n.publish_time) > #{sinceTime}
        """)
    int insertDueEmailDeliveryRecords(@Param("jobId") Long jobId, @Param("userId") Integer userId, @Param("sinceTime") Object sinceTime);

    @Insert("""
        INSERT IGNORE INTO notification_delivery_log (
            news_id,
            user_id,
            subscription_id,
            job_id,
            channel,
            recipient,
            status
        )
        SELECT
            n.news_id,
            u.id,
            s.id,
            #{jobId},
            'miniapp',
            COALESCE(NULLIF(u.email, ''), u.username, CAST(u.id AS CHAR)),
            'pending'
        FROM users u
        INNER JOIN subscriptions s
            ON s.user_id = u.id
           AND s.target_type = 'department'
           AND s.status = 1
           AND u.miniapp_notifications_enabled = 1
        INNER JOIN notifications n
            ON n.publish_department = s.target_value
        WHERE u.id = #{userId}
          AND u.status = 1
          AND COALESCE(n.first_seen_time, n.crawl_time, n.publish_time) > #{sinceTime}
        """)
    int insertDueMiniappDeliveryRecords(@Param("jobId") Long jobId, @Param("userId") Integer userId, @Param("sinceTime") Object sinceTime);

    @Select("""
        SELECT
            d.id AS delivery_id,
            d.news_id,
            d.user_id,
            d.subscription_id,
            d.job_id,
            d.recipient,
            d.status,
            u.username,
            n.title,
            n.category,
            n.fragment_id,
            n.publish_time,
            n.publish_department,
            n.detail_url,
            n.content_text
        FROM notification_delivery_log d
        INNER JOIN users u
            ON u.id = d.user_id
           AND u.email_notifications_enabled = 1
        INNER JOIN notifications n
            ON n.news_id = d.news_id
        LEFT JOIN subscriptions s
            ON s.id = d.subscription_id
        WHERE d.channel = 'email'
          AND d.status = 'pending'
          AND (s.id IS NULL OR s.status = 1)
        ORDER BY d.user_id ASC, n.publish_time ASC, d.id ASC
        """)
    List<PendingEmailDeliveryRow> selectPendingEmailDeliveries();

    @Select("""
        SELECT
            d.id AS delivery_id,
            d.news_id,
            d.user_id,
            d.subscription_id,
            d.job_id,
            d.recipient,
            d.status,
            u.username,
            u.email,
            u.wechat_openid,
            n.title,
            n.category,
            n.fragment_id,
            n.publish_time,
            n.publish_department,
            n.detail_url,
            n.content_text
        FROM notification_delivery_log d
        INNER JOIN users u
            ON u.id = d.user_id
           AND u.miniapp_notifications_enabled = 1
        INNER JOIN notifications n
            ON n.news_id = d.news_id
        LEFT JOIN subscriptions s
            ON s.id = d.subscription_id
        WHERE d.channel = 'miniapp'
          AND d.status = 'pending'
          AND (s.id IS NULL OR s.status = 1)
        ORDER BY d.user_id ASC, n.publish_time ASC, d.id ASC
        """)
    List<PendingMiniappDeliveryRow> selectPendingMiniappDeliveries();

    @Update({
        "<script>",
        "UPDATE notification_delivery_log",
        "SET status = 'success',",
        "    sent_at = CURRENT_TIMESTAMP,",
        "    last_attempt_at = CURRENT_TIMESTAMP,",
        "    provider_message_id = #{providerMessageId},",
        "    error_msg = NULL",
        "WHERE id IN",
        "<foreach collection='deliveryIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    int markDeliverySuccess(@Param("deliveryIds") List<Long> deliveryIds, @Param("providerMessageId") String providerMessageId);

    @Update({
        "<script>",
        "UPDATE notification_delivery_log",
        "SET status = 'failed',",
        "    retry_count = retry_count + 1,",
        "    last_attempt_at = CURRENT_TIMESTAMP,",
        "    error_msg = #{errorMessage}",
        "WHERE id IN",
        "<foreach collection='deliveryIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    int markDeliveryFailed(@Param("deliveryIds") List<Long> deliveryIds, @Param("errorMessage") String errorMessage);

    @Insert("""
        INSERT IGNORE INTO notification_delivery_log (
            news_id,
            user_id,
            subscription_id,
            job_id,
            channel,
            recipient,
            status
        )
        SELECT
            n.news_id,
            u.id,
            s.id,
            NULL,
            'miniapp',
            COALESCE(NULLIF(u.email, ''), u.username, CAST(u.id AS CHAR)),
            'pending'
        FROM notifications n
        INNER JOIN subscriptions s
            ON s.target_type = 'department'
           AND s.target_value = n.publish_department
           AND s.status = 1
        INNER JOIN users u
            ON u.id = s.user_id
           AND u.status = 1
           AND u.miniapp_notifications_enabled = 1
        WHERE u.email = #{email}
          AND n.news_id IN (
              SELECT news_id FROM (
                  SELECT news_id
                  FROM notifications
                  ORDER BY
                      CASE WHEN publish_time IS NULL THEN 1 ELSE 0 END,
                      publish_time DESC,
                      id DESC
                  LIMIT #{limit}
              ) latest_notifications
          )
        """)
    int ensureMiniappDeliveryRecordsForUser(@Param("email") String email, @Param("limit") int limit);
}
