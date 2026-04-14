package com.gdut.oanotification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("attachments")
public class Attachment {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String newsId;
    private String fileId;
    private String filename;
    private String extension;
    private Long size;
    private LocalDateTime crawlTime;
}
