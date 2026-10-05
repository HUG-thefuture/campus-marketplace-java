package com.resume.marketplace.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体，对应表 `product`。
 */
public class Product {

    private Long id;
    private String title;
    private String description;
    private String category;
    private BigDecimal price;
    private String images;      // JSON 数组字符串，如 ["/img/a.jpg"]，演示简单存字符串即可
    private String status;      // ON_SALE / OFF_SHELF / SOLD
    private Long sellerId;      // 卖家用户 ID（越权判断的关键字段）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}