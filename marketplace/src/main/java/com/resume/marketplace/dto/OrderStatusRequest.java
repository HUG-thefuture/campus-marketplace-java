package com.resume.marketplace.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 订单状态流转请求 DTO。目标状态必须是合法枚举值，否则解析阶段抛 400。
 */
public class OrderStatusRequest {

    /** 目标状态：PAID / COMPLETED / CANCELED */
    @NotBlank(message = "目标状态不能为空")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}