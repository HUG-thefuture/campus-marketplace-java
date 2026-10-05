package com.resume.marketplace.mapper;

import com.resume.marketplace.entity.Report;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 举报 Mapper。
 */
public interface ReportMapper {

    /** 新增举报 */
    int insert(Report report);

    /** 按主键查询 */
    Report findById(@Param("id") Long id);

    /** 按举报人查询其发起的所有举报 */
    List<Report> findByReporter(@Param("reporterId") Long reporterId);

    /** 查询全部举报（管理员视角，通常分页） */
    List<Report> findAll();

    /** 更新处理状态（管理员处理用） */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}