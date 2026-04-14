package com.gdut.oanotification.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentSubscriptionItem {

    private String department;
    private Integer status;
}
