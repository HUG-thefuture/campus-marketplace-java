package com.resume.marketplace.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色校验注解：标注在 Controller 方法上，声明该接口需要的角色。
 *
 * <p>例如管理员接口标注 <code>@RequireRole(RoleEnum.ADMIN)</code>，
 * {@link com.resume.marketplace.security.RoleInterceptor} 会校验当前登录人角色，
 * 不满足则返回 403。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 需要的角色 */
    com.resume.marketplace.enums.RoleEnum value();
}