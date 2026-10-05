package com.resume.marketplace.controller;

import com.resume.marketplace.common.BusinessException;
import com.resume.marketplace.common.Result;
import com.resume.marketplace.dto.OrderCreateRequest;
import com.resume.marketplace.dto.OrderStatusRequest;
import com.resume.marketplace.dto.OrderVO;
import com.resume.marketplace.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口：创建、状态流转、详情、列表。
 */
@Tag(name = "订单模块", description = "下单 / 状态流转 / 查询")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "创建订单（买家下单）")
    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.success(orderService.create(requireLogin(), request.getProductId()));
    }

    @Operation(summary = "订单状态流转（需为买/卖家或管理员）")
    @PostMapping("/{orderId}/status")
    public Result<OrderVO> transit(@PathVariable Long orderId,
                                   @Valid @RequestBody OrderStatusRequest request) {
        return Result.success(orderService.transit(requireLogin(), orderId, request.getStatus()));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderId}")
    public Result<OrderVO> detail(@PathVariable Long orderId) {
        return Result.success(orderService.detail(requireLogin(), orderId));
    }

    @Operation(summary = "我买到的订单")
    @GetMapping("/bought")
    public Result<List<OrderVO>> bought() {
        return Result.success(orderService.listByBuyer(requireLogin()));
    }

    @Operation(summary = "我卖出的订单")
    @GetMapping("/sold")
    public Result<List<OrderVO>> sold() {
        return Result.success(orderService.listBySeller(requireLogin()));
    }

    private Long requireLogin() {
        Long userId = com.resume.marketplace.security.UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }
}