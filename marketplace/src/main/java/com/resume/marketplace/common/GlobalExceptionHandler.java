package com.resume.marketplace.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>统一异常的重要性：</p>
 * <ul>
 *   <li>接口永远返回统一的 {@link Result} 结构，前端处理简单。</li>
 *   <li>把「业务异常 / 参数校验异常 / 系统异常」三类分开处理，语义清晰。</li>
 *   <li>避免把堆栈、SQL 等敏感信息直接暴露给调用方。</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常：body 的 code 与 HTTP 状态码保持一致（400/401/403/404/409），
     * 未识别的业务码一律回退 400。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.status(toHttpStatus(e.getCode()))
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    private HttpStatus toHttpStatus(int code) {
        return switch (code) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    /** @RequestBody 的 @Valid 校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = firstFieldMessage(e.getBindingResult().getFieldErrors());
        return Result.fail(400, msg == null ? "参数校验失败" : msg);
    }

    /** @ModelAttribute / 表单绑定的 @Valid 校验失败 */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBind(BindException e) {
        String msg = firstFieldMessage(e.getBindingResult().getFieldErrors());
        return Result.fail(400, msg == null ? "参数校验失败" : msg);
    }

    /** 请求体不是合法 JSON（2026-09 修复：原先被兜底处理器吞成 500） */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleUnreadable(Exception e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.fail(400, "请求体不是合法 JSON");
    }

    /** 路径参数类型不匹配（如 /api/product/abc 传给 Long 参数），返回 400 而不是 500 */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatch(Exception e) {
        log.warn("路径参数类型错误: {}", e.getMessage());
        return Result.fail(400, "路径参数类型不正确");
    }

    /** 访问不存在的路径（Spring Boot 3.2 起 NoResourceFoundException，原先被吞成 500） */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(Exception e) {
        log.debug("访问不存在的路径: {}", e.getMessage());
        return Result.fail(404, "接口不存在");
    }

    /** 兜底异常：未捕获的运行时异常，返回 500，不泄露细节 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器开小差了，请稍后再试");
    }

    /** 从字段错误列表里取第一条人类可读信息 */
    private String firstFieldMessage(java.util.List<FieldError> errors) {
        if (errors == null || errors.isEmpty()) {
            return null;
        }
        FieldError fe = errors.get(0);
        return fe.getField() + ": " + fe.getDefaultMessage();
    }
}