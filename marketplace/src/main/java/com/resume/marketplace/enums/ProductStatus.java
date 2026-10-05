package com.resume.marketplace.enums;

/**
 * 商品状态枚举。
 */
public enum ProductStatus {

    /** 在售 */
    ON_SALE("ON_SALE"),
    /** 已下架 */
    OFF_SHELF("OFF_SHELF"),
    /** 已售出 */
    SOLD("SOLD");

    private final String value;

    ProductStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}