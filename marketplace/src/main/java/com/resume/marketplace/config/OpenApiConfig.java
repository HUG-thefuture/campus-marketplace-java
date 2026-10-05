package com.resume.marketplace.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI（Swagger）文档配置。
 *
 * <p>通过 springdoc-openapi 自动扫描 Controller 生成接口文档，访问：
 * <code>http://localhost:8080/swagger-ui.html</code></p>
 *
 * <p>这里额外声明了 JWT 的鉴权方式，调试时可在 Swagger UI 右上角填入 token，
 * 后续请求自动携带 <code>Authorization: Bearer &lt;token&gt;</code>。</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI marketplaceOpenAPI() {
        // 声明一个名为 bearerAuth 的 HTTP Bearer 安全方案
        SecurityScheme scheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("校园二手交易平台 API")
                        .description("用户 / 商品 / 收藏 / 订单 / 举报 接口文档")
                        .version("1.0.0"))
                .components(new Components().addSecuritySchemes("bearerAuth", scheme))
                // 全局默认需要鉴权（具体接口是否强制取决于业务逻辑）
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}