package com.resume.marketplace.service.impl;

import com.resume.marketplace.common.BusinessException;
import com.resume.marketplace.entity.Favorite;
import com.resume.marketplace.mapper.FavoriteMapper;
import com.resume.marketplace.mapper.ProductMapper;
import com.resume.marketplace.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收藏服务实现。
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper, ProductMapper productMapper) {
        this.favoriteMapper = favoriteMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        // 商品必须存在
        if (productMapper.findById(productId) == null) {
            throw new BusinessException(404, "商品不存在");
        }
        // 幂等：已收藏则直接成功返回
        if (favoriteMapper.findByUserAndProduct(userId, productId) != null) {
            return;
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setProductId(productId);
        try {
            favoriteMapper.insert(f);
        } catch (DuplicateKeyException ignored) {
            // 同一用户并发点击收藏时，唯一键冲突仍视为幂等成功。
        }
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        // 幂等：未收藏时删除返回 0，也视为成功
        favoriteMapper.delete(userId, productId);
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteMapper.findByUserAndProduct(userId, productId) != null;
    }
}
