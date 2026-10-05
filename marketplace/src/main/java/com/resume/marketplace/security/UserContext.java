package com.resume.marketplace.security;

/**
 * 当前登录用户上下文（ThreadLocal）。
 *
 * <p>过滤器解析 token 后，把用户信息存进 ThreadLocal；Service/Controller 通过
 * {@link #getUserId()} 拿「当前登录人」，用于越权判断（非本人不能操作他人数据）。
 * 请求结束后由过滤器清理，避免线程复用导致的数据串号。</p>
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId, String role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getRole() {
        return ROLE.get();
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}