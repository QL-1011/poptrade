package com.poptrade.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一后端返回结果。
 * <p>所有 Controller 返回值必须包裹在此类型中，前端根据 code 判断请求状态。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 请求成功 */
    public static final int CODE_SUCCESS = 200;
    /** 业务异常 */
    public static final int CODE_BUSINESS_ERROR = 400;
    /** 未登录 / Token 过期 */
    public static final int CODE_UNAUTHORIZED = 401;
    /** 无权限 */
    public static final int CODE_FORBIDDEN = 403;
    /** 资源不存在 */
    public static final int CODE_NOT_FOUND = 404;
    /** 系统异常 */
    public static final int CODE_SYSTEM_ERROR = 500;

    private static final String MSG_SUCCESS = "操作成功";
    private static final String MSG_SYSTEM_ERROR = "系统异常，请联系管理员";

    private Integer code;
    private String message;
    private T data;

    // ---- 成功 ----

    public static <T> Result<T> success() {
        return new Result<>(CODE_SUCCESS, MSG_SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_SUCCESS, MSG_SUCCESS, data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(CODE_SUCCESS, message, data);
    }

    // ---- 业务异常 ----

    public static <T> Result<T> error(String message) {
        return new Result<>(CODE_BUSINESS_ERROR, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // ---- 系统异常 ----

    public static <T> Result<T> fail() {
        return new Result<>(CODE_SYSTEM_ERROR, MSG_SYSTEM_ERROR, null);
    }
}
