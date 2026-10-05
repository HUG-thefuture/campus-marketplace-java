package com.resume.marketplace.controller;

import com.resume.marketplace.annotation.RequireRole;
import com.resume.marketplace.common.BusinessException;
import com.resume.marketplace.common.Result;
import com.resume.marketplace.dto.HandleReportRequest;
import com.resume.marketplace.dto.ReportRequest;
import com.resume.marketplace.dto.ReportVO;
import com.resume.marketplace.enums.RoleEnum;
import com.resume.marketplace.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 举报接口：举报商品（普通用户）、处理举报（仅管理员）。
 */
@Tag(name = "举报模块", description = "举报商品 / 管理员处理举报")
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "举报商品（需登录）")
    @PostMapping
    public Result<Void> report(@Valid @RequestBody ReportRequest request) {
        reportService.report(requireLogin(), request.getProductId(), request.getReason());
        return Result.success();
    }

    @Operation(summary = "我发起的举报")
    @GetMapping("/mine")
    public Result<List<ReportVO>> mine() {
        return Result.success(reportService.listByReporter(requireLogin()));
    }

    @Operation(summary = "全部举报（仅管理员）")
    @RequireRole(RoleEnum.ADMIN)
    @GetMapping
    public Result<List<ReportVO>> all() {
        return Result.success(reportService.listAll());
    }

    @Operation(summary = "处理举报（仅管理员）")
    @RequireRole(RoleEnum.ADMIN)
    @PostMapping("/{reportId}/handle")
    public Result<Void> handle(@PathVariable Long reportId,
                               @Valid @RequestBody HandleReportRequest request) {
        reportService.handle(reportId, request.getStatus());
        return Result.success();
    }

    private Long requireLogin() {
        Long userId = com.resume.marketplace.security.UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }
}