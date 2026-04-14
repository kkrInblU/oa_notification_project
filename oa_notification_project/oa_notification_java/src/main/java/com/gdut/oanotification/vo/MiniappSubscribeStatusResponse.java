package com.gdut.oanotification.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MiniappSubscribeStatusResponse {

    private String userEmail;
    private Boolean miniappEnabled;
    private Boolean serverConfigured;
    private String templateId;
    private String subscribePage;
    private Boolean hasBoundUser;
    private Boolean hasWechatOpenid;
    private String wechatOpenidMasked;
    private Boolean sessionBound;
}
