package com.health.common;

/**
 * 统一返回结果
 *
 * @param <T> 数据类型
 */
public class Result<T> {

    /** 状态码 */
    private int code;
    /** 提示信息 */
    private String msg;
    /** 数据 */
    private T data;

    private Result() {
    }

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应（无返回值）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.ERROR.getCode(), msg, null);
    }

    /**
     * 失败响应（自定义状态码）
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 未认证响应
     */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(ResultCode.UNAUTHORIZED.getCode(), msg, null);
    }

    // getter / setter

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
