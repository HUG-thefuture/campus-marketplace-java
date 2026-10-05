package com.resume.marketplace.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类：负责 token 的签发与解析。
 *
 * <p><b>JWT 鉴权核心流程：</b></p>
 * <ol>
 *   <li>登录成功后，服务端根据用户信息签发 token（这里放入 userId 与 role）。</li>
 *   <li>前端把 token 放到请求头 <code>Authorization: Bearer &lt;token&gt;</code>。</li>
 *   <li>{@link JwtAuthFilter} 拦截请求，解析并校验签名与过期时间。</li>
 *   <li>校验通过后把用户信息写入上下文，接口即可拿到「当前登录人」。</li>
 * </ol>
 *
 * <p><b>为什么用 JWT？</b> 无状态：服务端不存 Session，任何节点都能独立校验，
 * 方便水平扩展；同时兼顾了「谁在操作」与「什么角色」两个信息。</p>
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /** 签名密钥（HS256），由配置注入 */
    private final SecretKey key;

    /** token 有效期（毫秒） */
    private final long expireMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire}") long expireMs) {
        // 2026-09 安全修复：仓库不再存放任何默认密钥（旧版把 secret 写死在
        // application.yml，拿到源码即可自签 ADMIN token）。未配置 JWT_SECRET
        // 时每次启动随机生成——重启后旧 token 全部失效，对演示/测试无损，
        // 生产环境必须显式注入。
        if (secret == null || secret.isBlank()) {
            byte[] rnd = new byte[48];
            new SecureRandom().nextBytes(rnd);
            secret = Base64.getEncoder().encodeToString(rnd);
            log.warn("未配置 JWT_SECRET，已生成本次进程随机密钥（重启后旧 token 失效）。"
                     + "生产环境请通过环境变量注入固定密钥。");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // 预校验长度：HS256 要求密钥至少 32 字节，过短时给出可读报错，
        // 而不是让 Keys.hmacShaKeyFor 抛出难定位的 WeakKeyException
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET 长度不足：HS256 至少需要 32 字节，当前 " + keyBytes.length
                    + " 字节。请更换更长的密钥（可用 openssl rand -base64 48 生成）。");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expireMs = expireMs;
    }

    /**
     * 签发 token。
     *
     * @param userId 用户 ID
     * @param role   角色（USER/ADMIN）
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMs);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);   // 把角色放进 claims，鉴权时直接读取，无需再查库
        return Jwts.builder()
                .subject(String.valueOf(userId))  // subject 存用户 ID
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)                     // 使用 HS256 签名
                .compact();
    }

    /**
     * 解析 token，返回 claims；签名错误或已过期会抛异常。
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 token 解析出用户 ID（subject） */
    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    /** 校验 token 是否有效（能正常解析且未过期即有效） */
    public boolean validate(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}