package com.resume.marketplace.common;

/**
 * 通用业务异常。
 *
 * <p>业务代码里遇到「可预期」的错误（如用户名已存在、余额不符、无权限）时，
 * 直接抛出本异常，配合 {@link GlobalExceptionHandler} 统一转换成 4xx 响应，
 * 避免在 Service/Controller 里到处写 try/catch。</p>
 */
public class BusinessException extends RuntimeException {

    /** HTTP 状态码语义：用于返回给前端的 code */
    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}