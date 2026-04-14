package com.gdut.oanotification.service;

import com.gdut.oanotification.vo.ManualDeliveryResponse;

public interface DeliverySchedulerService {

    void runDeliveryCycle();

    ManualDeliveryResponse runManualDelivery();
}
