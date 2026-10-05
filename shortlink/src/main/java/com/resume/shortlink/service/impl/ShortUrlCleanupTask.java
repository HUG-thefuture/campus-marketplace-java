package com.resume.shortlink.service.impl;

import com.resume.shortlink.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 过期短链定时清理任务。
 *
 * <p>每小时整点批量删除已过期的短链；跳转时的过期校验是「读时判断」，
 * 这里的定时任务是「存时清理」，两层配合保证过期短链既不可访问也不长期占存储。</p>
 */
@Component
public class ShortUrlCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(ShortUrlCleanupTask.class);

    private final ShortUrlRepository repository;

    public ShortUrlCleanupTask(ShortUrlRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpired() {
        int deleted = repository.deleteExpired(LocalDateTime.now());
        if (deleted > 0) {
            log.info("定时清理：删除过期短链 {} 条", deleted);
        }
    }
}
