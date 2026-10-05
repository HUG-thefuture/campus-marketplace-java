package com.resume.marketplace.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * 订单状态枚举 + 状态机流转规则。
 *
 * <p><b>为什么把状态机规则放在这里？</b>领域规则应内聚在领域对象中，
 * 而不是散落在各个 Service 的 if-else 里。所有状态变更都调用
 * {@link #canTransitTo(OrderStatus)} 先做合法性校验，非法跳转直接拒绝，
 * 从根源上杜绝脏数据（例如怕出现 PENDING 直接跳到 COMPLETED）。</p>
 *
 * <p>合法流转：</p>
 * <pre>
 * PENDING   -> PAID -> COMPLETED
 * PENDING   -> CANCELED
 * PAID      -> CANCELED   （付款后退款取消）
 * </pre>
 */
public enum OrderStatus {

    /** 待支付 */
    PENDING,
    /** 已支付 */
    PAID,
    /** 已完成（交易关闭的终态） */
    COMPLETED,
    /** 已取消（终态） */
    CANCELED;

    /**
     * 判断当前状态能否流转到目标状态。
     *
     * @param target 目标状态
     * @return true=允许；false=非法流转
     */
    public boolean canTransitTo(OrderStatus target) {
        return allowedTransitions().contains(target);
    }

    /** 每个状态允许跳转到的目标状态集合 */
    private Set<OrderStatus> allowedTransitions() {
        switch (this) {
            case PENDING:
                return EnumSet.of(PAID, CANCELED);
            case PAID:
                return EnumSet.of(COMPLETED, CANCELED);
            case COMPLETED:
            case CANCELED:
            default:
                // 终态不允许再流转
                return Collections.emptySet();
        }
    }

    /** 解析字符串为枚举，非法值抛异常（配合全局异常处理器返回 400） */
    public static OrderStatus from(String value) {
        for (OrderStatus s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("非法订单状态: " + value);
    }
}