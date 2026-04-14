package com.gdut.oanotification.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManualDeliveryResponse {

    private Long jobId;
    private Integer checkedUsers;
    private Integer matchedNotifications;
    private Integer createdEmailDeliveryRecords;
    private Integer createdMiniappDeliveryRecords;
    private Integer successfulEmailDeliveries;
    private Integer failedEmailDeliveries;
    private Integer successfulMiniappDeliveries;
    private Integer failedMiniappDeliveries;
    private Integer successfulDeliveries;
    private Integer failedDeliveries;
}
