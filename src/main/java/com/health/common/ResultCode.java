package com.health.common;

/**
 * 统一返回状态码枚举
 */
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),
    /** 未认证 */
    UNAUTHORIZED(401, "未认证，请先登录"),
    /** 服务器内部错误 */
    ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
