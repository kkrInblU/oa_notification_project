package com.gdut.oanotification.vo;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiniappSendTestResponse {

    private String userEmail;
    private String newsId;
    private String openidMasked;
    private Map<String, Object> wechatResponse;
}
