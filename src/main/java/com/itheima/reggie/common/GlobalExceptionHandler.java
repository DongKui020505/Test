package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException e) {
        log.error(e.getMessage());
        return R.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<String> exceptionHandler(Exception e) {
        log.error(e.getMessage());
        return R.error("未知异常！");
    }
}
