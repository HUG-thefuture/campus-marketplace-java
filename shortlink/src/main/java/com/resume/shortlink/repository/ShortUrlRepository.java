package com.resume.shortlink.repository;

import com.resume.shortlink.entity.ShortUrl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 短链接数据访问层（JdbcTemplate）。
 *
 * <p>用 Spring JDBC 而非 MyBatis，是为了演示另一种轻量持久化方式；
 * 核心做 5 件事：插入、按短码查、原子自增计数、按过期时间批量删除、按短码判存。</p>
 */
@Repository
public class ShortUrlRepository {

    private final JdbcTemplate jdbcTemplate;

    public ShortUrlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ShortUrl> ROW_MAPPER = (rs, rowNum) -> {
        ShortUrl s = new ShortUrl();
        s.setId(rs.getLong("id"));
        s.setShortCode(rs.getString("short_code"));
        s.setOriginalUrl(rs.getString("original_url"));
        Timestamp exp = rs.getTimestamp("expire_at");
        s.setExpireAt(exp == null ? null : exp.toLocalDateTime());
        s.setAccessCount(rs.getLong("access_count"));
        Timestamp created = rs.getTimestamp("created_at");
        s.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return s;
    };

    /** 插入短链，回填自增 id */
    public ShortUrl insert(ShortUrl s) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO short_url (short_code, original_url, expire_at, access_count) VALUES (?, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, s.getShortCode());
            ps.setString(2, s.getOriginalUrl());
            ps.setTimestamp(3, Timestamp.valueOf(s.getExpireAt()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            s.setId(key.longValue());
        }
        return s;
    }

    /** 按短码查询 */
    public ShortUrl findByCode(String shortCode) {
        return jdbcTemplate.query(
                "SELECT id, short_code, original_url, expire_at, access_count, created_at FROM short_url WHERE short_code = ?",
                ROW_MAPPER, shortCode)
                .stream().findFirst().orElse(null);
    }

    /** 原子自增访问次数（并发安全，无需先读再写） */
    public void incrementAccessCount(Long id) {
        jdbcTemplate.update("UPDATE short_url SET access_count = access_count + 1 WHERE id = ?", id);
    }

    /** 判断短码是否已被占用 */
    public boolean existsByCode(String shortCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM short_url WHERE short_code = ?", Integer.class, shortCode);
        return count != null && count > 0;
    }

    /** 批量删除已过期的短链（配合定时任务清理），返回删除行数 */
    public int deleteExpired(LocalDateTime now) {
        return jdbcTemplate.update("DELETE FROM short_url WHERE expire_at < ?", Timestamp.valueOf(now));
    }
}