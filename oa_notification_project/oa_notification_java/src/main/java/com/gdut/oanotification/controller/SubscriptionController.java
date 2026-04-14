package com.gdut.oanotification.controller;

import com.gdut.oanotification.common.api.ResultBody;
import com.gdut.oanotification.dto.request.BatchSubscriptionsRequest;
import com.gdut.oanotification.dto.request.DepartmentSubscriptionRequest;
import com.gdut.oanotification.service.SubscriptionService;
import com.gdut.oanotification.vo.DepartmentSubscriptionItem;
import com.gdut.oanotification.vo.DepartmentSubscriptionsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/departments")
    public ResultBody<DepartmentSubscriptionsResponse> getDepartmentSubscriptions(
        @RequestParam("userEmail") String userEmail
    ) {
        return ResultBody.success(subscriptionService.getDepartmentSubscriptions(userEmail));
    }

    @PostMapping("/department")
    public ResultBody<DepartmentSubscriptionItem> subscribeDepartment(
        @Valid @RequestBody DepartmentSubscriptionRequest request
    ) {
        return ResultBody.success(
            subscriptionService.subscribeDepartment(request.getUserEmail(), request.getDepartment())
        );
    }

    @PostMapping("/batch")
    public ResultBody<DepartmentSubscriptionsResponse> saveBatchSubscriptions(
        @Valid @RequestBody BatchSubscriptionsRequest request
    ) {
        return ResultBody.success(subscriptionService.saveBatchSubscriptions(request));
    }
}
