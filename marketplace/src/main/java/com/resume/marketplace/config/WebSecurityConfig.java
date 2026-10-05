package com.resume.marketplace.config;

import com.resume.marketplace.security.JwtAuthFilter;
import com.resume.marketplace.security.JwtUtil;
import com.resume.marketplace.security.RoleInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web / 安全相关配置。
 *
 * <p>说明：本项目未引入 spring-security 全家桶，而是用「JWT 过滤器 + 角色拦截器」
 * 自己实现轻量鉴权，更直观、更利于学习理解。这里仅复用 spring-security-crypto 的
 * BCrypt 密码加密（pom 已显式声明该依赖，starter-web 不会传递引入它）。</p>
 */
@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;

    public WebSecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 密码加密器：BCrypt。
     *
     * <p><b>为什么不能存明文密码？</b>数据库一旦泄露，明文会让所有用户的密码直接暴露。
     * BCrypt 自带盐值、可配置计算成本，是业界通用的密码哈希方案。校验用 matches()，不反向解密。</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 声明 JwtAuthFilter 为 bean（本身不标注 @Component，避免被 Boot 自动注册两次）。
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtUtil);
    }

    /**
     * 注册 JWT 过滤器，并显式指定 URL 匹配规则（只拦 /api/*）。
     *
     * <p>关键点：JwtAuthFilter 不标注 @Component，而是由这里以 @Bean 声明，
     * 再用 FilterRegistrationBean 交给 Servlet 容器管理。这样既能精准控制拦截路径，
     * 又避免被 Spring Boot 自动扫描注册导致同一过滤器执行两次（常见新手坑）。</p>
     *
     * <p><b>注意（踩坑记录）</b>：Servlet 过滤器的 URL 匹配遵循 Servlet 规范，必须写
     * <code>/api/*</code>；写 Spring 风格的 <code>/api/**</code> 会被 Tomcat 以
     * "Suspicious URL pattern" 警告并<b>静默忽略</b>，导致过滤器压根不执行。</p>
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>(jwtAuthFilter);
        // 拦截所有 API 请求。过滤器设计为「透传」：未带有效 token 时不会在此中断，
        // 而是由角色拦截器 / 各业务接口决定是否需要鉴权（登录、注册、公开商品检索无需登录）。
        bean.addUrlPatterns("/api/*");
        return bean;
    }

    /**
     * 注册角色校验拦截器。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RoleInterceptor()).addPathPatterns("/api/**");
    }
}