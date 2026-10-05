package com.resume.shortlink.dto;

/**
 * 创建短链响应 VO。
 */
public class ShortUrlResponse {

    private String shortCode;
    private String shortUrl;      // 完整短链（域名 + 短码）
    private String originalUrl;
    private String expireAt;

    public ShortUrlResponse(String shortCode, String shortUrl, String originalUrl, String expireAt) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.expireAt = expireAt;
    }

    public String getShortCode() { return shortCode; }
    public String getShortUrl() { return shortUrl; }
    public String getOriginalUrl() { return originalUrl; }
    public String getExpireAt() { return expireAt; }
}