package com.resume.marketplace.service.impl;

import com.resume.marketplace.common.BusinessException;
import com.resume.marketplace.dto.OrderVO;
import com.resume.marketplace.entity.Order;
import com.resume.marketplace.entity.Product;
import com.resume.marketplace.enums.OrderStatus;
import com.resume.marketplace.enums.ProductStatus;
import com.resume.marketplace.mapper.OrderMapper;
import com.resume.marketplace.mapper.ProductMapper;
import com.resume.marketplace.service.OrderService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务实现。
 *
 * <p><b>状态机如何防非法流转？</b>任何状态变更必须先经 {@link OrderStatus#canTransitTo}
 * 校验「当前状态 -> 目标状态」是否在白名单内；再通过带 expectedFrom 条件的 UPDATE 做
 * 乐观锁兜底，从「业务规则 + 数据库」两层同时保证不会出现脏流转。</p>
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    public OrderServiceImpl(OrderMapper orderMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public OrderVO create(Long buyerId, Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        // 只有「在售」商品可被购买
        if (!ProductStatus.ON_SALE.value().equals(product.getStatus())) {
            throw new BusinessException("商品已下架或已售出，无法下单");
        }
        // 不能购买自己发布的商品
        if (product.getSellerId().equals(buyerId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }

        // 原子售罄：带旧状态条件的 UPDATE（乐观锁），并发下单只有一个请求能成功，
        // 从根源上保证「一件在售商品最多存在一笔未取消订单」。
        int rows = productMapper.markSoldIfOnSale(productId);
        if (rows == 0) {
            throw new BusinessException(409, "手慢了，商品已被他人下单");
        }

        Order order = new Order();
        order.setProductId(productId);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setAmount(product.getPrice());   // 下单时快照金额，防止卖家改价影响历史单
        order.setStatus(OrderStatus.PENDING.name());
        // 2026-09 修复：秒级时间戳+6位随机数在并发下可能撞 order_no 唯一键，
        // 撞号时换号重试（最多 3 次）而不是把 500 抛给用户；生产建议换雪花 ID。
        // 注意：该重试依赖 MySQL/InnoDB「重复键不中止事务」的行为（本项目的目标库），
        // 换 H2/PostgreSQL 等会把事务标记为 aborted 的库时，重试需外移到事务之外。
        DuplicateKeyException dup = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            order.setOrderNo(generateOrderNo());
            try {
                orderMapper.insert(order);
                return toVO(order);
            } catch (DuplicateKeyException e) {
                dup = e;
            }
        }
        throw dup;
    }

    /** 生成唯一订单号：时间戳 + 6 位随机数（演示用途，生产可用雪花 ID 或发号器） */
    private String generateOrderNo() {
        String ts = LocalDateTime.now().format(ORDER_NO_FMT);
        int rand = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "MP" + ts + rand;
    }

    @Override
    @Transactional
    public OrderVO transit(Long operatorId, Long orderId, String targetStatusStr) {
        Order order = requireOrder(orderId);
        // 越权：支付/取消一般由买家发起，完成/取消由卖家发起；
        // 这里简化为「买/卖家任一都可以推进」，但管理员可处理任何订单。
        String role = com.resume.marketplace.security.UserContext.getRole();
        boolean isBuyer = order.getBuyerId().equals(operatorId);
        boolean isSeller = order.getSellerId().equals(operatorId);
        boolean isAdmin = "ADMIN".equals(role);
        if (!isBuyer && !isSeller && !isAdmin) {
            throw new BusinessException(403, "无权操作他人订单");
        }

        OrderStatus from;
        OrderStatus target;
        try {
            from = OrderStatus.from(order.getStatus());
            target = OrderStatus.from(targetStatusStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("非法订单状态");
        }

        // 核心：状态机合法性校验
        if (!from.canTransitTo(target)) {
            throw new BusinessException("非法状态流转：" + from + " -> " + target);
        }

        // 乐观锁更新：仅当 DB 中仍为 expectedFrom 时才成功
        int rows = orderMapper.updateStatus(orderId, from.name(), target.name());
        if (rows == 0) {
            throw new BusinessException("订单状态已变更，请刷新后重试");
        }

        // 取消订单 = 释放占用：把已售出的商品回补为在售，卖家无需手动重新上架
        if (target == OrderStatus.CANCELED) {
            productMapper.restoreOnSaleIfSold(order.getProductId());
        }

        // 读取最新状态返回
        return toVO(requireOrder(orderId));
    }

    @Override
    public OrderVO detail(Long operatorId, Long orderId) {
        Order order = requireOrder(orderId);
        String role = com.resume.marketplace.security.UserContext.getRole();
        boolean isBuyer = order.getBuyerId().equals(operatorId);
        boolean isSeller = order.getSellerId().equals(operatorId);
        boolean isAdmin = "ADMIN".equals(role);
        if (!isBuyer && !isSeller && !isAdmin) {
            throw new BusinessException(403, "无权查看他人订单");
        }
        return toVO(order);
    }

    @Override
    public List<OrderVO> listByBuyer(Long buyerId) {
        List<Order> orders = orderMapper.findByBuyer(buyerId);
        List<OrderVO> vos = new ArrayList<>();
        for (Order o : orders) {
            vos.add(toVO(o));
        }
        return vos;
    }

    @Override
    public List<OrderVO> listBySeller(Long sellerId) {
        List<Order> orders = orderMapper.findBySeller(sellerId);
        List<OrderVO> vos = new ArrayList<>();
        for (Order o : orders) {
            vos.add(toVO(o));
        }
        return vos;
    }

    private Order requireOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    private OrderVO toVO(Order o) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setProductId(o.getProductId());
        vo.setBuyerId(o.getBuyerId());
        vo.setSellerId(o.getSellerId());
        vo.setAmount(o.getAmount());
        vo.setStatus(o.getStatus());
        vo.setCreatedAt(o.getCreatedAt());
        vo.setUpdatedAt(o.getUpdatedAt());
        return vo;
    }
}