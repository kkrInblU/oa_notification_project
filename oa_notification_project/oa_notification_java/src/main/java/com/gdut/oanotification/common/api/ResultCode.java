package com.gdut.oanotification.common.api;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(0, "success"),
    FAILED(500, "failed"),
    VALIDATE_FAILED(400, "validation failed"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
