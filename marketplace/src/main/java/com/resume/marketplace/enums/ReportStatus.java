package com.resume.marketplace.enums;

/**
 * 举报处理状态枚举。
 */
public enum ReportStatus {

    /** 待处理 */
    PENDING("PENDING"),
    /** 已处理（举报成立） */
    RESOLVED("RESOLVED"),
    /** 已驳回 */
    REJECTED("REJECTED");

    private final String value;

    ReportStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /** 解析字符串为枚举（大小写不敏感），非法值抛异常（配合全局异常处理器返回 400） */
    public static ReportStatus from(String value) {
        for (ReportStatus s : values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("非法举报处理状态: " + value);
    }
}