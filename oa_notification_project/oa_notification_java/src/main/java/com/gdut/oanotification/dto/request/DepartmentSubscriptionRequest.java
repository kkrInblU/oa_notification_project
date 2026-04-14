package com.gdut.oanotification.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentSubscriptionRequest {

    @NotBlank(message = "missing userEmail")
    @Email(message = "invalid userEmail")
    private String userEmail;

    @NotBlank(message = "missing department")
    private String department;
}
