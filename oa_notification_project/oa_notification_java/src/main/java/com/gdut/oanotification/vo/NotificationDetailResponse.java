package com.gdut.oanotification.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDetailResponse {

    private Integer id;
    private String newsId;
    private String title;
    private String department;
    private String publishTime;
    private String category;
    private String contentText;
    private String contentHtml;
    private String miniappContentHtml;
    private String detailUrl;
    private Integer viewCount;
    private List<String> images;
    private List<AttachmentItem> attachments;
}
