package com.resume.marketplace.config;

import com.resume.marketplace.entity.User;
import com.resume.marketplace.enums.RoleEnum;
import com.resume.marketplace.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 示例账号初始化器。
 *
 * <p>应用启动时检查 user01 / admin 是否存在，不存在则用 BCrypt 现场生成
 * 正确密码哈希后创建，<b>避免在 SQL 里硬编码密码哈希</b>。</p>
 *
 * <p>这也是真实项目里「启动时初始化管理员账号」的常见做法，是一个加分学习点。</p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createIfAbsent("user01", "小明", "13800000001", RoleEnum.USER.value());
        createIfAbsent("admin", "管理员", "13800000002", RoleEnum.ADMIN.value());
    }

    private void createIfAbsent(String username, String nickname, String phone, String role) {
        if (userMapper.findByUsername(username) != null) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        // 关键：这里用 passwordEncoder 现场生成正确的 BCrypt 哈希
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setRole(role);
        userMapper.insert(user);
        log.info("已初始化示例账号: {}（默认密码见 README 示例账号一节）", username);
    }
}