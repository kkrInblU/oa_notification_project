package com.gdut.oanotification.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
public class BatchSubscriptionsRequest {

    @NotBlank(message = "missing userEmail")
    @Email(message = "invalid userEmail")
    private String userEmail;

    private List<BatchSubscriptionItemRequest> subscriptions;
}
