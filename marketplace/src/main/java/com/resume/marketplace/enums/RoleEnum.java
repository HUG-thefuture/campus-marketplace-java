package com.resume.marketplace.enums;

/**
 * 用户角色枚举。
 *
 * <p>用字符串常量代替魔法数字，配合 {@link com.resume.marketplace.annotation.RequireRole}
 * 注解做接口级角色校验。</p>
 */
public enum RoleEnum {

    /** 普通用户 */
    USER("USER"),
    /** 管理员 */
    ADMIN("ADMIN");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}