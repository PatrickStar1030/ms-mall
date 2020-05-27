package com.dilemma.common.exception;

import com.dilemma.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 通用异常定义，通过枚举定义具体返回的错误信息
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MsException extends RuntimeException {
    private ExceptionEnum exceptionEnum;
}
