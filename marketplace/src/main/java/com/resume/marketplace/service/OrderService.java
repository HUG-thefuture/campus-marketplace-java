package com.resume.marketplace.service;

import com.resume.marketplace.dto.OrderVO;

import java.util.List;

/**
 * 订单服务接口。
 */
public interface OrderService {

    /** 创建订单（买家下单，校验商品在售、不能买自己的商品） */
    OrderVO create(Long buyerId, Long productId);

    /** 状态流转（校验越权 + 状态机合法性） */
    OrderVO transit(Long operatorId, Long orderId, String targetStatus);

    /** 订单详情（越权校验：仅买/卖家可看） */
    OrderVO detail(Long operatorId, Long orderId);

    /** 买家视角订单列表 */
    List<OrderVO> listByBuyer(Long buyerId);

    /** 卖家视角订单列表 */
    List<OrderVO> listBySeller(Long sellerId);
}