package com.resume.marketplace.service.impl;

import com.resume.marketplace.common.BusinessException;
import com.resume.marketplace.dto.ReportVO;
import com.resume.marketplace.entity.Product;
import com.resume.marketplace.entity.Report;
import com.resume.marketplace.enums.ReportStatus;
import com.resume.marketplace.mapper.ProductMapper;
import com.resume.marketplace.mapper.ReportMapper;
import com.resume.marketplace.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 举报服务实现。
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final ProductMapper productMapper;

    public ReportServiceImpl(ReportMapper reportMapper, ProductMapper productMapper) {
        this.reportMapper = reportMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public void report(Long reporterId, Long productId, String reason) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        // 不能举报自己发布的商品（演示一种业务约束）
        if (product.getSellerId().equals(reporterId)) {
            throw new BusinessException("不能举报自己发布的商品");
        }
        Report r = new Report();
        r.setProductId(productId);
        r.setReporterId(reporterId);
        r.setReason(reason);
        r.setStatus(ReportStatus.PENDING.value());
        reportMapper.insert(r);
    }

    @Override
    public List<ReportVO> listByReporter(Long reporterId) {
        return toVOList(reportMapper.findByReporter(reporterId));
    }

    @Override
    public List<ReportVO> listAll() {
        return toVOList(reportMapper.findAll());
    }

    @Override
    @Transactional
    public void handle(Long reportId, String status) {
        Report report = reportMapper.findById(reportId);
        if (report == null) {
            throw new BusinessException(404, "举报不存在");
        }
        // 校验状态值合法（大小写不敏感），并统一入库为大写；
        // 非法值转成业务异常返回 400（IllegalArgumentException 若不接住会落到兜底 500）
        ReportStatus target;
        try {
            target = ReportStatus.from(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, e.getMessage());
        }
        reportMapper.updateStatus(reportId, target.value());
    }

    private List<ReportVO> toVOList(List<Report> reports) {
        List<ReportVO> vos = new ArrayList<>();
        for (Report r : reports) {
            ReportVO vo = new ReportVO();
            vo.setId(r.getId());
            vo.setProductId(r.getProductId());
            vo.setReporterId(r.getReporterId());
            vo.setReason(r.getReason());
            vo.setStatus(r.getStatus());
            vo.setCreatedAt(r.getCreatedAt());
            vos.add(vo);
        }
        return vos;
    }
}