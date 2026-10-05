package com.resume.marketplace.service.impl;

import com.resume.marketplace.common.BusinessException;
import com.resume.marketplace.common.PageResult;
import com.resume.marketplace.dto.ProductRequest;
import com.resume.marketplace.dto.ProductVO;
import com.resume.marketplace.entity.Product;
import com.resume.marketplace.enums.ProductStatus;
import com.resume.marketplace.mapper.ProductMapper;
import com.resume.marketplace.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品服务实现。
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public ProductVO publish(Long sellerId, ProductRequest request) {
        Product p = new Product();
        p.setTitle(request.getTitle());
        p.setDescription(request.getDescription());
        p.setCategory(request.getCategory());
        p.setPrice(request.getPrice());
        p.setImages(request.getImages());
        p.setSellerId(sellerId);
        p.setStatus(ProductStatus.ON_SALE.value());   // 新发布默认在售
        productMapper.insert(p);
        return toVO(p);
    }

    @Override
    @Transactional
    public void offShelf(Long operatorId, Long productId) {
        Product p = requireProduct(productId);
        // 越权校验：只有卖家本人或管理员可下架
        if (!isOwnerOrAdmin(operatorId, p)) {
            throw new BusinessException(403, "无权操作他人商品");
        }
        if (ProductStatus.OFF_SHELF.value().equals(p.getStatus())) {
            throw new BusinessException("商品已是下架状态");
        }
        if (ProductStatus.SOLD.value().equals(p.getStatus())) {
            throw new BusinessException(409, "商品已售出，不能下架；如需处理请先取消对应订单");
        }
        // 原子下架：带 ON_SALE 条件的条件更新。上方两次校验是 check-then-act，
        // 与并发下单的 markSoldIfOnSale 存在竞态窗口；无条件的 update 会把刚售出的
        // SOLD 覆盖回 OFF_SHELF，导致订单与商品状态失配。影响行数为 0 即状态已被并发修改。
        if (productMapper.offShelfIfOnSale(productId) == 0) {
            throw new BusinessException(409, "商品状态已变化（可能刚被下单），请刷新后重试");
        }
    }

    @Override
    public ProductVO detail(Long productId) {
        return toVO(requireProduct(productId));
    }

    @Override
    public PageResult<ProductVO> search(String keyword, String category, int page, int size) {
        // 分页参数兜底：页码最小 1，每页条数限制在 1~100
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);
        // 用 long 计算偏移量：int 相乘在大页码下会溢出为负，导致 MySQL 直接报错
        long offset = (long) (page - 1) * size;

        // LIKE 关键字转义：% _ \ 是 LIKE 的通配符，不转义的话用户输入会扩大扫描面
        if (keyword != null && !keyword.isEmpty()) {
            keyword = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        }

        // 默认只展示在售商品，避免把下架/已售商品推给买家
        String status = ProductStatus.ON_SALE.value();
        long total = productMapper.count(keyword, category, status);
        List<Product> products = productMapper.search(keyword, category, status, offset, size);

        List<ProductVO> vos = new ArrayList<>();
        for (Product p : products) {
            vos.add(toVO(p));
        }
        return new PageResult<>(vos, total, page, size);
    }

    @Override
    public List<ProductVO> listBySeller(Long sellerId) {
        List<Product> products = productMapper.findBySeller(sellerId);
        List<ProductVO> vos = new ArrayList<>();
        for (Product p : products) {
            vos.add(toVO(p));
        }
        return vos;
    }

    /** 查询商品，不存在则抛 404（带状态码的异常） */
    private Product requireProduct(Long productId) {
        Product p = productMapper.findById(productId);
        if (p == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return p;
    }

    /** 判断操作人是否为商品卖家本人（这里把管理员也视为可操作，可按需调整） */
    private boolean isOwnerOrAdmin(Long operatorId, Product p) {
        String role = currentRole();
        return p.getSellerId().equals(operatorId) || "ADMIN".equals(role);
    }

    /**
     * 从上下文取当前角色。为避免 Service 层直接依赖 ThreadLocal 工具类的呆板，
     * 由 Controller 传入 operatorId 已经足够做越权判断主体；这里保留角色判断以便管理员放行。
     */
    private String currentRole() {
        return com.resume.marketplace.security.UserContext.getRole();
    }

    private ProductVO toVO(Product p) {
        ProductVO vo = new ProductVO();
        vo.setId(p.getId());
        vo.setTitle(p.getTitle());
        vo.setDescription(p.getDescription());
        vo.setCategory(p.getCategory());
        vo.setPrice(p.getPrice());
        vo.setImages(p.getImages());
        vo.setStatus(p.getStatus());
        vo.setSellerId(p.getSellerId());
        vo.setCreatedAt(p.getCreatedAt());
        return vo;
    }
}