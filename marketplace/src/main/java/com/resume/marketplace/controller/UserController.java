package com.resume.marketplace.controller;

import com.resume.marketplace.common.Result;
import com.resume.marketplace.dto.LoginRequest;
import com.resume.marketplace.dto.LoginResponse;
import com.resume.marketplace.dto.RegisterRequest;
import com.resume.marketplace.dto.UserVO;
import com.resume.marketplace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口：注册、登录、查询当前用户信息。
 */
@Tag(name = "用户模块", description = "注册 / 登录 / 用户信息")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @Operation(summary = "用户登录，返回 JWT")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "查询当前登录用户信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        Long userId = com.resume.marketplace.security.UserContext.getUserId();
        if (userId == null) {
            // 2026-09 修复：原先返回 HTTP 200 + body code 401，违反自家
            // "code 与 HTTP 状态码一致"的约定；统一走异常处理器映射 401。
            throw new com.resume.marketplace.common.BusinessException(401, "未登录");
        }
        return Result.success(userService.getUserById(userId));
    }
}