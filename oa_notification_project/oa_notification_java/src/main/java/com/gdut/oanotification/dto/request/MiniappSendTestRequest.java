package com.gdut.oanotification.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MiniappSendTestRequest {

    @Email(message = "invalid userEmail")
    private String userEmail;

    private String openid;
    private String newsId;
    private String title;
    private String department;
    private String category;
    private String contentText;
    private String detailUrl;
    private String page;
}
