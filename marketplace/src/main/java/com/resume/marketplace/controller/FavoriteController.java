package com.resume.marketplace.controller;

import com.resume.marketplace.common.BusinessException;
import com.resume.marketplace.common.Result;
import com.resume.marketplace.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏接口：收藏 / 取消收藏 / 查询是否收藏。
 */
@Tag(name = "收藏模块", description = "收藏 / 取消收藏商品")
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(summary = "收藏商品（需登录）")
    @PostMapping("/{productId}")
    public Result<Void> add(@PathVariable Long productId) {
        favoriteService.addFavorite(requireLogin(), productId);
        return Result.success();
    }

    @Operation(summary = "取消收藏（需登录）")
    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable Long productId) {
        favoriteService.removeFavorite(requireLogin(), productId);
        return Result.success();
    }

    @Operation(summary = "查询是否已收藏")
    @GetMapping("/{productId}/status")
    public Result<Boolean> status(@PathVariable Long productId) {
        return Result.success(favoriteService.isFavorite(requireLogin(), productId));
    }

    private Long requireLogin() {
        Long userId = com.resume.marketplace.security.UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }
}