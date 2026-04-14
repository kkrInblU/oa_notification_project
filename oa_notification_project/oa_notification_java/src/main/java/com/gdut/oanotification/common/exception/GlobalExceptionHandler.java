package com.gdut.oanotification.common.exception;

import com.gdut.oanotification.common.api.ResultBody;
import com.gdut.oanotification.common.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResultBody<Void> handleBizException(BizException ex) {
        return ResultBody.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultBody<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage() == null ? "invalid request" : error.getDefaultMessage())
            .orElse("invalid request");
        return ResultBody.error(ResultCode.VALIDATE_FAILED, message);
    }

    @ExceptionHandler(BindException.class)
    public ResultBody<Void> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage() == null ? "invalid request" : error.getDefaultMessage())
            .orElse("invalid request");
        return ResultBody.error(ResultCode.VALIDATE_FAILED, message);
    }

    @ExceptionHandler(Exception.class)
    public ResultBody<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResultBody.error(ResultCode.FAILED, ex.getMessage() == null ? "internal server error" : ex.getMessage());
    }
}
