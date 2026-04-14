package com.gdut.oanotification.dto.request;

import lombok.Data;

@Data
public class BatchSubscriptionItemRequest {

    private String department;
    private Boolean subscribed;
}
