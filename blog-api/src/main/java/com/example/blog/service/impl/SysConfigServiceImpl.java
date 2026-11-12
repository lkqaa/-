package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.entity.SysConfig;
import com.example.blog.mapper.SysConfigMapper;
import com.example.blog.service.SysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    private static final Logger log = LoggerFactory.getLogger(SysConfigServiceImpl.class);
    private static final String CACHE_KEY = "blog:site:config";

    private final SysConfigMapper sysConfigMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${blog.cache.config-ttl-seconds:600}")
    private long configTtlSeconds;

    public SysConfigServiceImpl(SysConfigMapper sysConfigMapper,
                               RedisTemplate<String, Object> redisTemplate) {
        this.sysConfigMapper = sysConfigMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> publicConfig() {
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached instanceof Map) {
                log.debug("[cache] HIT site config");
                return (Map<String, String>) cached;
            }
        } catch (Exception e) {
            log.warn("[cache] site config read failed, fallback to DB: {}", e.getMessage());
        }

        Map<String, String> fresh = computePublicConfig();

        try {
            redisTemplate.opsForValue().set(CACHE_KEY, fresh, Duration.ofSeconds(configTtlSeconds));
            log.debug("[cache] PUT site config, ttl={}s", configTtlSeconds);
        } catch (Exception e) {
            log.warn("[cache] site config write ignored: {}", e.getMessage());
        }
        return fresh;
    }

    private Map<String, String> computePublicConfig() {
        Map<String, String> raw = sysConfigMapper.selectList(null).stream()
                .collect(java.util.stream.Collectors.toMap(
                        SysConfig::getConfigKey,
                        c -> c.getConfigValue() == null ? "" : c.getConfigValue(),
                        (a, b) -> b));

        Map<String, String> result = new LinkedHashMap<>();
        result.put("blogTitle", raw.getOrDefault("blog_title", ""));
        result.put("blogSubtitle", raw.getOrDefault("blog_subtitle", ""));
        result.put("blogLogo", raw.getOrDefault("blog_logo", ""));
        result.put("aboutMe", raw.getOrDefault("about_me", ""));
        return result;
    }

    @Override
    public List<SysConfig> listAll() {
        return sysConfigMapper.selectList(new LambdaQueryWrapper<SysConfig>().orderByAsc(SysConfig::getId));
    }

    @Override
    @Transactional
    public void updateBatch(Map<String, String> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        items.forEach((key, value) -> {
            SysConfig existing = sysConfigMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
            if (existing == null) {
                SysConfig config = new SysConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                sysConfigMapper.insert(config);
            } else {
                existing.setConfigValue(value);
                sysConfigMapper.updateById(existing);
            }
        });
        evictConfig();
    }

    private void evictConfig() {
        try {
            redisTemplate.delete(CACHE_KEY);
            log.debug("[cache] EVICT site config");
        } catch (Exception e) {
            log.warn("[cache] site config evict ignored: {}", e.getMessage());
        }
    }
}
