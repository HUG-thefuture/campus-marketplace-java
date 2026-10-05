package com.resume.shortlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建短链请求 DTO。
 */
public class CreateShortUrlRequest {

    @NotBlank(message = "原始链接不能为空")
    @Size(max = 512, message = "链接长度不能超过 512")
    private String originalUrl;

    /** 可选：自定义短码（不填则自动生成） */
    @Size(max = 16, message = "短码长度不能超过 16")
    private String customCode;

    /** 可选：有效期（毫秒），不填用默认 30 天 */
    private Long expireMs;

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getCustomCode() { return customCode; }
    public void setCustomCode(String customCode) { this.customCode = customCode; }

    public Long getExpireMs() { return expireMs; }
    public void setExpireMs(Long expireMs) { this.expireMs = expireMs; }
}