package com.gdut.oanotification.service;

import com.gdut.oanotification.dto.request.UpdateCrawlerConfigRequest;
import com.gdut.oanotification.vo.AdminCrawlerConfigResponse;
import com.gdut.oanotification.vo.AdminCrawlerRunResponse;
import com.gdut.oanotification.vo.CrawlJobDetailResponse;
import com.gdut.oanotification.vo.CrawlJobListResponse;

public interface AdminCrawlerService {

    AdminCrawlerConfigResponse getCrawlerConfig();

    AdminCrawlerConfigResponse updateCrawlerConfig(UpdateCrawlerConfigRequest request);

    CrawlJobListResponse getCrawlerJobs(int limit, int page);

    CrawlJobDetailResponse getCrawlerJobDetail(long jobId);

    AdminCrawlerRunResponse triggerCrawlerRun();
}
