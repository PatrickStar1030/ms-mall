package com.dilemma.common.vo;

import com.dilemma.common.enums.ExceptionEnum;
import lombok.Data;

/**
 * 异常结果返回定义类
 */
@Data
public class ExceptionResult {
    private int status;
    private String message;
    private Long timestamp;

    public ExceptionResult(ExceptionEnum em) {
        this.status = em.getCode();
        this.message = em.getMsg();
        this.timestamp = System.currentTimeMillis();
    }
}
