package com.resume.marketplace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园二手交易平台后端 —— 应用启动入口。
 *
 * <p>@MapperScan 扫描 mapper 接口包，MyBatis 会自动为这些接口生成代理实现，
 * 无需为每个接口手动写 @Mapper 注解。</p>
 */
@SpringBootApplication
@MapperScan("com.resume.marketplace.mapper")
public class MarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApplication.class, args);
    }
}