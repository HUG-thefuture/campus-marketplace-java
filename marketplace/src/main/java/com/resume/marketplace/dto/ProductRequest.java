package com.resume.marketplace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 商品发布请求 DTO。
 */
public class ProductRequest {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 128, message = "标题长度不能超过 128")
    private String title;

    @Size(max = 2000, message = "描述不能超过 2000 字")
    private String description;

    @NotBlank(message = "商品分类不能为空")
    @Size(max = 32, message = "分类长度不能超过 32")
    private String category;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    /** 图片 JSON 数组字符串，非必填 */
    private String images;

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
}