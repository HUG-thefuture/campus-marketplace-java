package com.resume.marketplace.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单状态机单元测试。
 *
 * <p>不依赖数据库、不依赖 Spring，纯粹验证 {@link OrderStatus} 的流转规则，
 * 帮助理解「状态机如何防非法流转」。</p>
 */
class OrderStatusTest {

    @Test
    void pending_can_flow_to_paid() {
        assertTrue(OrderStatus.PENDING.canTransitTo(OrderStatus.PAID));
    }

    @Test
    void pending_can_flow_to_canceled() {
        assertTrue(OrderStatus.PENDING.canTransitTo(OrderStatus.CANCELED));
    }

    @Test
    void pending_cannot_skip_to_completed() {
        // 不允许 PENDING 直接跳到 COMPLETED，必须经过 PAID
        assertFalse(OrderStatus.PENDING.canTransitTo(OrderStatus.COMPLETED));
    }

    @Test
    void paid_can_flow_to_completed_and_canceled() {
        assertTrue(OrderStatus.PAID.canTransitTo(OrderStatus.COMPLETED));
        assertTrue(OrderStatus.PAID.canTransitTo(OrderStatus.CANCELED));
    }

    @Test
    void terminal_states_cannot_flow_anywhere() {
        assertFalse(OrderStatus.COMPLETED.canTransitTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.COMPLETED.canTransitTo(OrderStatus.CANCELED));
        assertFalse(OrderStatus.CANCELED.canTransitTo(OrderStatus.PAID));
    }

    @Test
    void from_parses_case_insensitively() {
        assertEquals(OrderStatus.PAID, OrderStatus.from("paid"));
        assertEquals(OrderStatus.CANCELED, OrderStatus.from("CANCELED"));
    }

    @Test
    void from_rejects_illegal_value() {
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.from("NOT_A_STATUS"));
    }
}