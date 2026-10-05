package com.resume.marketplace.service;

import com.resume.marketplace.common.PageResult;
import com.resume.marketplace.dto.ProductRequest;
import com.resume.marketplace.dto.ProductVO;

import java.util.List;

/**
 * 商品服务接口。
 */
public interface ProductService {

    /** 发布商品（当前登录人为卖家） */
    ProductVO publish(Long sellerId, ProductRequest request);

    /** 下架商品（校验越权：只有卖家本人可下架） */
    void offShelf(Long operatorId, Long productId);

    /** 商品详情 */
    ProductVO detail(Long productId);

    /** 分页检索商品（公开，无需登录） */
    PageResult<ProductVO> search(String keyword, String category, int page, int size);

    /** 查询某卖家发布的所有商品（"我的商品"） */
    List<ProductVO> listBySeller(Long sellerId);
}