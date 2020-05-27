package com.dilemma.common.advice;


import com.dilemma.common.exception.MsException;
import com.dilemma.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 自动拦截所有的controller
 * 公共异常
 */
@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(MsException.class)
    public ResponseEntity<ExceptionResult> handleException(MsException e){
        return ResponseEntity.status(e.getExceptionEnum().getCode())
                .body(new ExceptionResult(e.getExceptionEnum()));
    }
}
