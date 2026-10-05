package com.resume.shortlink.service;

import com.resume.shortlink.dto.CreateShortUrlRequest;
import com.resume.shortlink.dto.ShortUrlResponse;
import com.resume.shortlink.dto.ShortUrlStatsResponse;

/**
 * 短链接服务接口。
 */
public interface ShortUrlService {

    /** 创建短链：返回短码 + 完整短链 */
    ShortUrlResponse create(CreateShortUrlRequest request);

    /** 按短码返回原始长链接（用于跳转）；过期或不存在抛业务异常 */
    String resolve(String shortCode);

    /** 查询短链访问统计（访问次数查询）；不存在抛 404，已过期仍可查询 */
    ShortUrlStatsResponse stats(String shortCode);
}