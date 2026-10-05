package com.resume.marketplace.security;

import com.resume.marketplace.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 角色校验拦截器：检查接口上的 {@link RequireRole} 注解，校验当前用户角色。
 *
 * <p>放在 JWT 过滤器之后执行；此时 {@link UserContext} 已持有当前用户信息。
 * 区分「未登录（401）」与「已登录但角色不够（403）」两种情况。</p>
 */
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非控制器方法（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 优先读方法上的注解，其次读类上的注解
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return true; // 无需角色校验，放行
        }

        // 未登录
        if (UserContext.getUserId() == null) {
            writeError(response, 401, "未登录或登录已过期");
            return false;
        }

        // 角色不匹配
        String currentRole = UserContext.getRole();
        if (!requireRole.value().value().equals(currentRole)) {
            writeError(response, 403, "无权限访问该资源");
            return false;
        }
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            "{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
    }
}