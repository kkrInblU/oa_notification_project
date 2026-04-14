package com.gdut.oanotification.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentSubscriptionsResponse {

    private String userEmail;
    private List<DepartmentSubscriptionItem> items;
    private List<String> departments;
}
