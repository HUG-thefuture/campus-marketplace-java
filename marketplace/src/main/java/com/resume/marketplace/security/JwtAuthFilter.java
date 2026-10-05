package com.resume.marketplace.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器：在进入 Controller 前解析并校验 token。
 *
 * <p><b>鉴权流程</b>：从 <code>Authorization</code> 请求头取出 token（去掉 "Bearer " 前缀），
 * 解析成功则把 userId + role 写入 {@link UserContext}；失败则返回 401 并中断请求。</p>
 *
 * <p>注意：本类<b>不标注 @Component</b>。它由 {@link com.resume.marketplace.config.WebSecurityConfig}
 * 以 {@code @Bean} 方式注册进 FilterRegistrationBean，避免被 Spring Boot 自动注册导致过滤器执行两次。</p>
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        log.debug("JWT filter: uri={}, token={}",
                request.getRequestURI(), token == null ? "<none>" : "<present>");
        if (StringUtils.hasText(token) && jwtUtil.validate(token)) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long userId = Long.valueOf(claims.getSubject());
                String role = claims.get("role", String.class);
                UserContext.set(userId, role);
            } catch (Exception e) {
                // 解析异常按无效 token 处理
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // 请求结束务必清理，避免 ThreadLocal 内存泄漏 / 数据串号
            UserContext.clear();
        }
    }

    /** 从请求头提取 token（去掉前缀） */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(header);
        if (StringUtils.hasText(bearer) && bearer.startsWith(prefix)) {
            return bearer.substring(prefix.length());
        }
        return null;
    }
}