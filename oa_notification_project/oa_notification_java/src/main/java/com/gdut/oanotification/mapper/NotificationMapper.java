package com.gdut.oanotification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdut.oanotification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("""
        SELECT COUNT(DISTINCT n.news_id)
        FROM notifications n
        INNER JOIN users u
            ON u.id = #{userId}
           AND u.status = 1
        INNER JOIN subscriptions s
            ON s.user_id = u.id
           AND s.target_type = 'department'
           AND s.target_value = n.publish_department
           AND s.status = 1
        WHERE COALESCE(n.first_seen_time, n.crawl_time, n.publish_time) > #{sinceTime}
        """)
    Integer countUserNewNotificationsSince(@Param("userId") Integer userId, @Param("sinceTime") Object sinceTime);
}
