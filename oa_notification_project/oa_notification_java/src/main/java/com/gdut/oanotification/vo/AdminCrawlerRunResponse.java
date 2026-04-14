package com.gdut.oanotification.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminCrawlerRunResponse {

    private Boolean started;
    private AdminCrawlerRunState runState;
}
