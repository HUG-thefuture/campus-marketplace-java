package com.resume.marketplace.mapper;

import com.resume.marketplace.entity.Favorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏 Mapper。
 */
public interface FavoriteMapper {

    /** 新增收藏 */
    int insert(Favorite favorite);

    /** 按用户+商品查询是否已收藏 */
    Favorite findByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    /** 取消收藏 */
    int delete(@Param("userId") Long userId, @Param("productId") Long productId);

    /** 查询某用户收藏的商品ID列表（用于“我的收藏”列表） */
    List<Favorite> findByUser(@Param("userId") Long userId);
}