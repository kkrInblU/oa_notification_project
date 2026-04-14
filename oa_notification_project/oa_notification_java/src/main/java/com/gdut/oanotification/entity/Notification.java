package com.gdut.oanotification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("notifications")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String newsId;
    private String title;
    private String category;
    private String fragmentId;
    private LocalDateTime publishTime;
    private String publishDepartment;
    private String contentHtml;
    private String contentText;
    private String detailUrl;
    private Integer audienceUndergraduate;
    private String audienceUndergraduateRuleVersion;
    private String audienceUndergraduateRuleDetail;
    private Integer audienceGraduate;
    private String audienceGraduateRuleVersion;
    private String audienceGraduateRuleDetail;
    private Integer audienceStaff;
    private String audienceStaffRuleVersion;
    private String audienceStaffRuleDetail;
    private Integer viewCount;
    private LocalDateTime firstSeenTime;
    private LocalDateTime lastSeenTime;
    private LocalDateTime crawlTime;
}
