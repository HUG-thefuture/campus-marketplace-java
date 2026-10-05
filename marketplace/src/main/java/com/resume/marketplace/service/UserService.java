package com.resume.marketplace.service;

import com.resume.marketplace.dto.LoginRequest;
import com.resume.marketplace.dto.LoginResponse;
import com.resume.marketplace.dto.RegisterRequest;
import com.resume.marketplace.dto.UserVO;

/**
 * 用户服务接口：注册、登录、查询。
 */
public interface UserService {

    /** 注册：校验用户名唯一、密码 BCrypt 加密后落库 */
    void register(RegisterRequest request);

    /** 登录：校验密码，签发 JWT */
    LoginResponse login(LoginRequest request);

    /** 查询用户信息（外发 VO，不含密码） */
    UserVO getUserById(Long userId);
}