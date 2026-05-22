package com.poptrade.common.exception;

import com.poptrade.common.result.Result;
import lombok.Getter;

/**
 * 业务异常，抛出后由 {@link GlobalExceptionHandler} 统一捕获并返回前端。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = Result.CODE_BUSINESS_ERROR;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
