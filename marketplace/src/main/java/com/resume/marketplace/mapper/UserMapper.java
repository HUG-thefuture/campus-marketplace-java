package com.resume.marketplace.mapper;

import com.resume.marketplace.entity.User;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper。
 *
 * <p>SQL 写在 resources/mapper/UserMapper.xml 中（也可用注解，这里统一用 XML 便于管理复杂 SQL）。</p>
 */
public interface UserMapper {

    /** 按用户名查询（登录/注册查重用） */
    User findByUsername(@Param("username") String username);

    /** 按主键查询 */
    User findById(@Param("id") Long id);

    /** 新增用户，返回受影响行数；实体对象的 id 会回填（useGeneratedKeys） */
    int insert(User user);
}