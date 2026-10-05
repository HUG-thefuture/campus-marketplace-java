package com.resume.shortlink.service.impl;

import com.resume.shortlink.common.BusinessException;
import com.resume.shortlink.dto.CreateShortUrlRequest;
import com.resume.shortlink.dto.ShortUrlResponse;
import com.resume.shortlink.dto.ShortUrlStatsResponse;
import com.resume.shortlink.entity.ShortUrl;
import com.resume.shortlink.repository.ShortUrlRepository;
import com.resume.shortlink.service.ShortUrlService;
import com.resume.shortlink.util.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 短链接服务实现。
 *
 * <p><b>短码生成策略：</b>用 SecureRandom 生成随机数，Base62 编码得到短码；
 * 由于 base62 唯一索引兜底，唯一性由「随机 + 唯一索引 + 冲突重试」保证。
 * 这种「哈希/随机 + 进制编码」是短链最常见的做法，简单且无需全局发号器。</p>
 */
@Service
public class ShortUrlServiceImpl implements ShortUrlService {

    private final ShortUrlRepository repository;
    private final int codeLength;
    private final long defaultExpireMs;
    private final String domain;

    private final SecureRandom random = new SecureRandom();

    public ShortUrlServiceImpl(ShortUrlRepository repository,
                               @Value("${shortlink.code-length}") int codeLength,
                               @Value("${shortlink.default-expire-ms}") long defaultExpireMs,
                               @Value("${shortlink.domain}") String domain) {
        this.repository = repository;
        this.codeLength = codeLength;
        this.defaultExpireMs = defaultExpireMs;
        this.domain = domain;
    }

    @Override
    @Transactional
    public ShortUrlResponse create(CreateShortUrlRequest request) {
        // 简单 URL 校验：只接受 http/https 协议
        String originalUrl = request.getOriginalUrl().trim();
        try {
            URI parsed = new URI(originalUrl);
            if (!("http".equalsIgnoreCase(parsed.getScheme()) || "https".equalsIgnoreCase(parsed.getScheme()))
                    || parsed.getHost() == null) {
                throw new BusinessException("原始链接必须是含有效主机的 http/https URL");
            }
        } catch (URISyntaxException e) {
            throw new BusinessException("原始链接格式非法");
        }

        String shortCode;
        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            // 用户自定义短码：需校验唯一性
            shortCode = request.getCustomCode().trim();
            if (!shortCode.matches("[A-Za-z0-9]{1,16}") || shortCode.equals("error") || shortCode.equals("api")) {
                throw new BusinessException("自定义短码仅允许 1–16 位字母或数字，不能使用保留路径");
            }
            if (repository.existsByCode(shortCode)) {
                throw new BusinessException("自定义短码已被占用");
            }
        } else {
            // 自动生成：随机 -> Base62 -> 冲突重试
            shortCode = generateUniqueCode();
        }

        long expireMs = (request.getExpireMs() != null && request.getExpireMs() > 0)
                ? request.getExpireMs() : defaultExpireMs;
        // 用 Duration 把毫秒转成可加的时间量，避免数值溢出，语义也更清晰
        LocalDateTime expireAt = LocalDateTime.now().plus(Duration.ofMillis(expireMs));

        ShortUrl entity = new ShortUrl();
        entity.setShortCode(shortCode);
        entity.setOriginalUrl(originalUrl);
        entity.setExpireAt(expireAt);
        // 先查询只能提供友好提示；真正的并发冲突仍必须以唯一索引为准。
        for (int attempt = 0; ; attempt++) {
            try {
                repository.insert(entity);
                break;
            } catch (DuplicateKeyException e) {
                if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
                    throw new BusinessException(409, "自定义短码已被占用");
                }
                if (attempt >= 4) throw new BusinessException(409, "短码生成冲突，请重试");
                shortCode = generateUniqueCode();
                entity.setShortCode(shortCode);
            }
        }

        return new ShortUrlResponse(
                shortCode,
                buildShortUrl(shortCode),
                originalUrl,
                expireAt.toString());
    }

    @Override
    public String resolve(String shortCode) {
        ShortUrl entity = requireUrl(shortCode);
        // 过期校验：到期即失效，返回 410 语义（用业务异常表达）
        if (entity.getExpireAt() != null && entity.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(410, "短链接已过期");
        }
        // 原子自增访问次数
        repository.incrementAccessCount(entity.getId());
        return entity.getOriginalUrl();
    }

    @Override
    public ShortUrlStatsResponse stats(String shortCode) {
        ShortUrl entity = requireUrl(shortCode);
        return new ShortUrlStatsResponse(
                entity.getShortCode(),
                buildShortUrl(entity.getShortCode()),
                entity.getOriginalUrl(),
                entity.getAccessCount(),
                entity.getExpireAt() == null ? null : entity.getExpireAt().toString(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString());
    }

    /** 按短码查询，不存在抛 404（跳转与统计共用） */
    private ShortUrl requireUrl(String shortCode) {
        ShortUrl entity = repository.findByCode(shortCode);
        if (entity == null) {
            throw new BusinessException(404, "短链接不存在");
        }
        return entity;
    }

    /** 生成一个不冲突的唯一短码（最多重试 5 次，极小概率冲突） */
    private String generateUniqueCode() {
        for (int i = 0; i < 5; i++) {
            // 位掩码取非负随机数：Math.abs 在 Long.MIN_VALUE 上会溢出为负，
            // 而 & Long.MAX_VALUE 恒非负，且不影响随机性
            long num = random.nextLong() & Long.MAX_VALUE;
            String code = Base62.encode(num);
            // 编码结果可能长度不足，补足到 codeLength；过长则截取
            code = padOrTruncate(code, codeLength);
            if (!repository.existsByCode(code)) {
                return code;
            }
        }
        throw new BusinessException("短码生成冲突，请重试");
    }

    private String padOrTruncate(String code, int length) {
        if (code.length() >= length) {
            return code.substring(0, length);
        }
        // 用 '0' 补前导，保持固定长度（'0' 是 Base62 表的第 0 位）
        StringBuilder sb = new StringBuilder();
        for (int i = code.length(); i < length; i++) {
            sb.append('0');
        }
        sb.append(code);
        return sb.toString();
    }

    private String buildShortUrl(String shortCode) {
        String base = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        return base + "/" + shortCode;
    }
}
