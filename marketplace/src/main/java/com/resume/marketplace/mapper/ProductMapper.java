package com.resume.marketplace.mapper;

import com.resume.marketplace.entity.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品 Mapper。分页检索用「先 count 再 limit」两步。
 */
public interface ProductMapper {

    /** 新增商品（id 回填） */
    int insert(Product product);

    /** 按主键查询 */
    Product findById(@Param("id") Long id);

    /** 更新商品（动态 SET，只更新非空字段） */
    int update(Product product);

    /**
     * 分页检索商品。
     *
     * @param keyword  标题关键字（可空，LIKE 模糊匹配；调用方需先转义 % _ \）
     * @param category 分类（可空）
     * @param status   状态（可空，默认只查 ON_SALE 在售商品）
     * @param offset   偏移量 = (page-1)*size，用 long 防止大页码 int 溢出
     * @param size     每页条数
     */
    List<Product> search(@Param("keyword") String keyword,
                         @Param("category") String category,
                         @Param("status") String status,
                         @Param("offset") long offset,
                         @Param("size") int size);

    /** 统计检索结果总数（配合 search 做分页） */
    long count(@Param("keyword") String keyword,
               @Param("category") String category,
               @Param("status") String status);

    /** 卖家视角：查询某用户发布的所有商品 */
    List<Product> findBySeller(@Param("sellerId") Long sellerId);

    /**
     * 原子售罄：仅当商品仍为 ON_SALE 时置为 SOLD（乐观锁防并发超卖）。
     *
     * @return 影响行数：0 表示商品已被他人下单或状态已变化
     */
    int markSoldIfOnSale(@Param("id") Long id);

    /** 取消订单后回补：仅当商品为 SOLD 时恢复为 ON_SALE（配合订单取消流程） */
    int restoreOnSaleIfSold(@Param("id") Long id);

    /**
     * 原子下架：仅当商品仍为 ON_SALE 时置为 OFF_SHELF。
     *
     * @return 影响行数：0 表示状态已被并发修改（如刚被下单置为 SOLD）
     */
    int offShelfIfOnSale(@Param("id") Long id);
}