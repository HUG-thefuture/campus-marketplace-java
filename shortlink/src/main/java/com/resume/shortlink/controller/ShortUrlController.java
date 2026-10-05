package com.resume.shortlink.controller;

import com.resume.shortlink.common.BusinessException;
import com.resume.shortlink.common.Result;
import com.resume.shortlink.dto.CreateShortUrlRequest;
import com.resume.shortlink.dto.ShortUrlResponse;
import com.resume.shortlink.dto.ShortUrlStatsResponse;
import com.resume.shortlink.service.ShortUrlService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 短链接接口。
 *
 * <p>三个端点：
 * <ul>
 *   <li>POST /api/shorten —— 长链转短链</li>
 *   <li>GET /{code} —— 短链跳转（302 重定向到原始长链）</li>
 *   <li>GET /api/url/{code}/stats —— 访问次数查询</li>
 * </ul>
 */
@RestController
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping("/api/shorten")
    public Result<ShortUrlResponse> shorten(@Valid @RequestBody CreateShortUrlRequest request) {
        return Result.success(shortUrlService.create(request));
    }

    /**
     * 访问次数查询：返回访问计数、失效时间等统计信息（已过期的短链仍可查询）。
     */
    @GetMapping("/api/url/{code}/stats")
    public Result<ShortUrlStatsResponse> stats(@PathVariable String code) {
        return Result.success(shortUrlService.stats(code));
    }

    /**
     * 短链跳转：找到原始长链后 302 重定向；过期返回 410，不存在返回 404。
     */
    @GetMapping("/{code}")
    public void redirect(@PathVariable String code, HttpServletResponse response) throws IOException {
        try {
            String originalUrl = shortUrlService.resolve(code);
            // 302 临时重定向（短链跳转常用 302，方便统计与后续变更）
            response.sendRedirect(originalUrl);
        } catch (BusinessException e) {
            // 业务异常在重定向场景下不能走普通 JSON 返回，这里直接写状态码
            response.setStatus(e.getCode() == 410 ? HttpStatus.GONE.value()
                    : (e.getCode() == 404 ? HttpStatus.NOT_FOUND.value()
                    : HttpStatus.BAD_REQUEST.value()));
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":" + e.getCode() + ",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}