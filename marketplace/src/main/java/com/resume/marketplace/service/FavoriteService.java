package com.resume.marketplace.service;

/**
 * 收藏服务接口。
 */
public interface FavoriteService {

    /** 收藏商品（幂等：重复收藏返回已收藏提示，不报错） */
    void addFavorite(Long userId, Long productId);

    /** 取消收藏（幂等） */
    void removeFavorite(Long userId, Long productId);

    /** 是否已收藏 */
    boolean isFavorite(Long userId, Long productId);
}