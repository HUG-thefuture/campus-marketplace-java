package com.resume.shortlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 短链接服务 —— 应用启动入口。
 *
 * <p>功能：长链接转短码、短码跳转（302 redirect）、过期校验、访问计数、
 * 过期短链定时清理。技术：Spring Boot 3 + Spring JDBC（JdbcTemplate）+ MySQL。</p>
 */
@SpringBootApplication
@EnableScheduling
public class ShortlinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortlinkApplication.class, args);
    }
}