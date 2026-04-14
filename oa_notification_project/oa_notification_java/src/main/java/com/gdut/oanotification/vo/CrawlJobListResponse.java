package com.gdut.oanotification.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrawlJobListResponse {

    private List<CrawlJobItemResponse> items;
    private Integer page;
    private Integer pageSize;
    private Integer totalCount;
    private Boolean hasMore;
    private AdminCrawlerRunState runState;
}
