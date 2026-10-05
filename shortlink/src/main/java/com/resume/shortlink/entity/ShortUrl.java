package com.resume.shortlink.entity;

import java.time.LocalDateTime;

/**
 * 短链接实体，对应表 short_url。
 */
public class ShortUrl {

    private Long id;
    private String shortCode;
    private String originalUrl;
    private LocalDateTime expireAt;   // 失效时间
    private long accessCount;         // 访问次数
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }

    public long getAccessCount() { return accessCount; }
    public void setAccessCount(long accessCount) { this.accessCount = accessCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}