package com.kanako.chatbaseonnetty.base.exception;

import com.kanako.chatbaseonnetty.base.response.JsonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = RuntimeException.class)
    public JsonResult handleRuntimeException(RuntimeException e) {
        return new JsonResult(201, e.getMessage(),null);
    }
}
