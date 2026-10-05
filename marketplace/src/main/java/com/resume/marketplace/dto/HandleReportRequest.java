package com.resume.marketplace.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理员处理举报请求 DTO。
 *
 * <p>status 取值：PENDING / RESOLVED / REJECTED（大小写不敏感），
 * 非法值由 {@code ReportStatus.from} 解析失败后转 400。</p>
 */
public class HandleReportRequest {

    @NotBlank(message = "处理状态不能为空")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
