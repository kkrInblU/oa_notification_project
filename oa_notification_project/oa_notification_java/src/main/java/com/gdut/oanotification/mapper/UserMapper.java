package com.gdut.oanotification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdut.oanotification.entity.User;
import com.gdut.oanotification.vo.DueUserRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("""
        SELECT
            u.id,
            u.username,
            u.email,
            u.wechat_openid,
            u.email_notifications_enabled,
            u.miniapp_notifications_enabled,
            u.notification_refresh_interval_minutes,
            u.last_notification_check_at,
            u.created_at
        FROM users u
        WHERE u.status = 1
          AND (
              u.email_notifications_enabled = 1
              OR u.miniapp_notifications_enabled = 1
          )
          AND EXISTS (
              SELECT 1
              FROM subscriptions s
              WHERE s.user_id = u.id
                AND s.status = 1
          )
          AND (
              u.last_notification_check_at IS NULL
              OR TIMESTAMPDIFF(
                  MINUTE,
                  u.last_notification_check_at,
                  CURRENT_TIMESTAMP
              ) >= u.notification_refresh_interval_minutes
          )
        ORDER BY u.id ASC
        """)
    List<DueUserRow> selectDueUsersForNotificationCheck();

    @Select("""
        SELECT
            u.id,
            u.username,
            u.email,
            u.wechat_openid,
            u.email_notifications_enabled,
            u.miniapp_notifications_enabled,
            u.notification_refresh_interval_minutes,
            u.last_notification_check_at,
            u.created_at
        FROM users u
        WHERE u.status = 1
          AND (
              u.email_notifications_enabled = 1
              OR u.miniapp_notifications_enabled = 1
          )
          AND EXISTS (
              SELECT 1
              FROM subscriptions s
              WHERE s.user_id = u.id
                AND s.status = 1
          )
        ORDER BY u.id ASC
        """)
    List<DueUserRow> selectActiveUsersForManualDelivery();
}
