package com.gdut.oanotification.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserSettingsRequest {

    @NotBlank(message = "missing userEmail")
    @Email(message = "invalid userEmail")
    private String userEmail;

    private Integer refreshIntervalMinutes;
    private Boolean emailEnabled;
    private Boolean miniappEnabled;
}
