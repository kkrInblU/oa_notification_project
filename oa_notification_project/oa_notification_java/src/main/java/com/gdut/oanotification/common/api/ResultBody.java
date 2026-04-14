package com.gdut.oanotification.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultBody<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> ResultBody<T> success(T data) {
        return new ResultBody<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ResultBody<T> success(String message, T data) {
        return new ResultBody<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> ResultBody<T> error(ResultCode resultCode) {
        return new ResultBody<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> ResultBody<T> error(ResultCode resultCode, String message) {
        return new ResultBody<>(resultCode.getCode(), message, null);
    }

    public static <T> ResultBody<T> error(int code, String message) {
        return new ResultBody<>(code, message, null);
    }

    public static <T> ResultBody<T> failure(int code, String message) {
        return error(code, message);
    }
}
