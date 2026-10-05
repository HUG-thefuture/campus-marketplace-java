package com.resume.shortlink.dto;

/**
 * 短链访问统计响应。
 */
public class ShortUrlStatsResponse {

    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private long accessCount;
    private String expireAt;
    private String createdAt;

    public ShortUrlStatsResponse(String shortCode, String shortUrl, String originalUrl,
                                 long accessCount, String expireAt, String createdAt) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.accessCount = accessCount;
        this.expireAt = expireAt;
        this.createdAt = createdAt;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public long getAccessCount() {
        return accessCount;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
