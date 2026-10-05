package com.resume.marketplace.mapper;

import com.resume.marketplace.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单 Mapper。
 */
public interface OrderMapper {

    /** 新增订单（id 回填） */
    int insert(Order order);

    /** 按主键查询 */
    Order findById(@Param("id") Long id);

    /**
     * 更新订单状态（带乐观条件：仅当 DB 中当前状态 == expectedFrom 时才更新，
     * 防止并发下脏写；返回受影响行数，0 表示已被其它请求抢先变更）。
     */
    int updateStatus(@Param("id") Long id,
                     @Param("expectedFrom") String expectedFrom,
                     @Param("to") String to);

    /** 买家视角：查询我买到的订单 */
    List<Order> findByBuyer(@Param("buyerId") Long buyerId);

    /** 卖家视角：查询我卖出的订单 */
    List<Order> findBySeller(@Param("sellerId") Long sellerId);
}