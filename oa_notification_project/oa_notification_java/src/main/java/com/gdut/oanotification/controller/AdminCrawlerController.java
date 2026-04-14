package com.gdut.oanotification.controller;

import com.gdut.oanotification.common.api.ResultBody;
import com.gdut.oanotification.dto.request.UpdateCrawlerConfigRequest;
import com.gdut.oanotification.service.AdminCrawlerService;
import com.gdut.oanotification.service.DeliverySchedulerService;
import com.gdut.oanotification.vo.AdminCrawlerConfigResponse;
import com.gdut.oanotification.vo.AdminCrawlerRunResponse;
import com.gdut.oanotification.vo.CrawlJobDetailResponse;
import com.gdut.oanotification.vo.CrawlJobListResponse;
import com.gdut.oanotification.vo.ManualDeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/crawler")
@RequiredArgsConstructor
public class AdminCrawlerController {

    private final AdminCrawlerService adminCrawlerService;
    private final DeliverySchedulerService deliverySchedulerService;

    @GetMapping("/config")
    public ResultBody<AdminCrawlerConfigResponse> getCrawlerConfig() {
        return ResultBody.success(adminCrawlerService.getCrawlerConfig());
    }

    @PostMapping("/config")
    public ResultBody<AdminCrawlerConfigResponse> updateCrawlerConfig(@RequestBody UpdateCrawlerConfigRequest request) {
        return ResultBody.success(adminCrawlerService.updateCrawlerConfig(request));
    }

    @GetMapping("/jobs")
    public ResultBody<CrawlJobListResponse> getCrawlerJobs(
        @RequestParam(value = "limit", defaultValue = "20") Integer limit,
        @RequestParam(value = "page", defaultValue = "1") Integer page
    ) {
        return ResultBody.success(adminCrawlerService.getCrawlerJobs(limit, page));
    }

    @GetMapping("/job-detail")
    public ResultBody<CrawlJobDetailResponse> getCrawlerJobDetail(@RequestParam("jobId") Long jobId) {
        return ResultBody.success(adminCrawlerService.getCrawlerJobDetail(jobId));
    }

    @PostMapping("/run")
    public ResultBody<AdminCrawlerRunResponse> triggerCrawlerRun() {
        return ResultBody.success(adminCrawlerService.triggerCrawlerRun());
    }

    @PostMapping("/manual-delivery")
    public ResultBody<ManualDeliveryResponse> triggerManualDelivery() {
        return ResultBody.success(deliverySchedulerService.runManualDelivery());
    }
}
