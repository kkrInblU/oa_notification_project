package com.gdut.oanotification.service;

import com.gdut.oanotification.dto.request.BatchSubscriptionsRequest;
import com.gdut.oanotification.vo.DepartmentSubscriptionItem;
import com.gdut.oanotification.vo.DepartmentSubscriptionsResponse;

public interface SubscriptionService {

    DepartmentSubscriptionsResponse getDepartmentSubscriptions(String userEmail);

    DepartmentSubscriptionItem subscribeDepartment(String userEmail, String department);

    DepartmentSubscriptionsResponse saveBatchSubscriptions(BatchSubscriptionsRequest request);
}
