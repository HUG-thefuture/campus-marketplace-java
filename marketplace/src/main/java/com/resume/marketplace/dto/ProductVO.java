package com.resume.marketplace.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品响应 VO（列表与详情可共用）。
 */
public class ProductVO {

    private Long id;
    private String title;
    private String description;
    private String category;
    private BigDecimal price;
    private String images;
    private String status;
    private Long sellerId;
    private LocalDateTime createdAt;

    // getter / setter 省略了无业务逻辑，这里完整给出
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
}