package com.resume.marketplace.service.impl;

import com.resume.marketplace.common.BusinessException;
import com.resume.marketplace.dto.LoginRequest;
import com.resume.marketplace.dto.LoginResponse;
import com.resume.marketplace.dto.RegisterRequest;
import com.resume.marketplace.dto.UserVO;
import com.resume.marketplace.entity.User;
import com.resume.marketplace.enums.RoleEnum;
import com.resume.marketplace.mapper.UserMapper;
import com.resume.marketplace.security.JwtUtil;
import com.resume.marketplace.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现。
 *
 * <p>密码安全性是本类的核心：注册用 BCrypt 加密存储，登录用 matches 比对，
 * 全程不出现明文密码的持久化。</p>
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 用户名唯一性校验（依赖唯一索引 + 应用层二次确认）
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // 关键：密码加密后再入库
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setRole(RoleEnum.USER.value());   // 注册默认普通用户；管理员由初始化数据产生

        try {
            userMapper.insert(user);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发注册同名用户：上方查重通过后、插入前，另一请求可能已占用用户名，
            // 撞 uk_user_username 唯一键。转为 400 而不是把 500 抛给用户。
            throw new BusinessException("用户名已存在");
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        // 用户不存在或密码不匹配，统一返回同一句提示（避免枚举用户名是否存在的安全风险）
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 签发 JWT：把 userId 与 role 封进 token
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getRole());
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return toVO(user);
    }

    /** 实体转 VO：刻意不复制 password 字段 */
    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        return vo;
    }
}