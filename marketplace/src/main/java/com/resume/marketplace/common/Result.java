package com.resume.marketplace.common;

/**
 * 统一响应结果封装。
 *
 * <p>所有接口都返回这个结构，前端只需处理一种格式：</p>
 * <pre>{ code: 200, message: "success", data: {...} }</pre>
 *
 * <p>code 约定：200 成功；400 参数/业务错误；401 未登录/token 失效；403 无权限；500 服务器错误。</p>
 */
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功，无数据 */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /** 成功，携带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /** 失败：自定义 code 与 message */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}