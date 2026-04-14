package com.gdut.oanotification.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSettingsResponse {

    private String userEmail;
    private Boolean emailEnabled;
    private Boolean miniappEnabled;
    private Integer refreshIntervalMinutes;
    private String lastNotificationCheckAt;
    private Boolean hasUser;
}
