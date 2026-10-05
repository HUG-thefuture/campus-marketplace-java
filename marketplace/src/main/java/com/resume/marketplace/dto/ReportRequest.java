package com.resume.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 举报商品请求 DTO。
 */
public class ReportRequest {

    @NotNull(message = "被举报商品ID不能为空")
    private Long productId;

    @NotBlank(message = "举报原因不能为空")
    @Size(max = 255, message = "举报原因不能超过 255 字")
    private String reason;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}