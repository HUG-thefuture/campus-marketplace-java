package com.resume.marketplace.service;

import com.resume.marketplace.dto.ReportVO;

import java.util.List;

/**
 * 举报服务接口。
 */
public interface ReportService {

    /** 举报商品 */
    void report(Long reporterId, Long productId, String reason);

    /** 我发起的举报列表 */
    List<ReportVO> listByReporter(Long reporterId);

    /** 管理员：查看全部举报 */
    List<ReportVO> listAll();

    /** 管理员：处理举报（更新状态） */
    void handle(Long reportId, String status);
}