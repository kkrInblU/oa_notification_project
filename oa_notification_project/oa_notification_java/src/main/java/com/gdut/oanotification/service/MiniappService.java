package com.gdut.oanotification.service;

import com.gdut.oanotification.dto.request.MiniappSendTestRequest;
import com.gdut.oanotification.dto.request.MiniappSessionRequest;
import com.gdut.oanotification.vo.MiniappSendTestResponse;
import com.gdut.oanotification.vo.MiniappSubscribeStatusResponse;
import java.util.Map;

public interface MiniappService {

    MiniappSubscribeStatusResponse getSubscribeStatus(String userEmail);

    MiniappSubscribeStatusResponse bindSession(MiniappSessionRequest request);

    Map<String, Object> sendSubscribeMessage(String openid, Map<String, Object> notification, String page);

    MiniappSendTestResponse sendTest(MiniappSendTestRequest request);
}
