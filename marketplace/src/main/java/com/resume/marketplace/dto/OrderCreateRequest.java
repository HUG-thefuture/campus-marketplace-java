package com.resume.marketplace.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建订单请求 DTO。
 */
public class OrderCreateRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
}