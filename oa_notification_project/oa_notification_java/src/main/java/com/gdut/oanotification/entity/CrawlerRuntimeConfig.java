package com.gdut.oanotification.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("crawler_runtime_config")
public class CrawlerRuntimeConfig {

    @TableId
    private String configKey;
    private String configValue;
    private String configType;
    private String description;
    private LocalDateTime updatedAt;
}
