package com.resume.marketplace.controller;

import com.resume.marketplace.common.PageResult;
import com.resume.marketplace.common.Result;
import com.resume.marketplace.dto.ProductRequest;
import com.resume.marketplace.dto.ProductVO;
import com.resume.marketplace.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口：发布、下架、详情、分页检索。
 */
@Tag(name = "商品模块", description = "发布 / 下架 / 检索")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "发布商品（需登录）")
    @PostMapping
    public Result<ProductVO> publish(@Valid @RequestBody ProductRequest request) {
        Long userId = requireLogin();
        return Result.success(productService.publish(userId, request));
    }

    @Operation(summary = "下架商品（仅卖家本人）")
    @PostMapping("/{productId}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long productId) {
        Long userId = requireLogin();
        productService.offShelf(userId, productId);
        return Result.success();
    }

    @Operation(summary = "商品详情（公开）")
    @GetMapping("/{productId}")
    public Result<ProductVO> detail(@PathVariable Long productId) {
        return Result.success(productService.detail(productId));
    }

    @Operation(summary = "分页检索商品（公开，按关键字/分类）")
    @GetMapping("/search")
    public Result<PageResult<ProductVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(productService.search(keyword, category, page, size));
    }

    @Operation(summary = "我的商品（需登录）")
    @GetMapping("/mine")
    public Result<List<ProductVO>> mine() {
        Long userId = requireLogin();
        return Result.success(productService.listBySeller(userId));
    }

    private Long requireLogin() {
        Long userId = com.resume.marketplace.security.UserContext.getUserId();
        if (userId == null) {
            throw new com.resume.marketplace.common.BusinessException(401, "未登录");
        }
        return userId;
    }
}