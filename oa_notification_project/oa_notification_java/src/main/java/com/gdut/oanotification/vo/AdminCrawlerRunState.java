package com.gdut.oanotification.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminCrawlerRunState {

    private Boolean running;
    private String lastStartedAt;
    private String lastFinishedAt;
    private String lastStatus;
    private String lastMessage;
}
