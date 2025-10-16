package com.ziwan.ziwanpicturebackend.manager;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class LocalRedisCacheManager {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 本地缓存：优先级最高
     */
    private final Cache<String, String> localCache = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    /**
     * 从缓存获取数据，优先本地 -> redis
     *
     * @param key
     * @param clazz
     * @return
     */
    public <T> T getCache(String key, Class<T> clazz) {
        // 1. 本地缓存
        String cacheValue = localCache.getIfPresent(key);
        if (StrUtil.isNotBlank(cacheValue)) {
            return JSONUtil.toBean(cacheValue, clazz);
        }

        // 2. Redis 缓存
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        cacheValue = opsForValue.get(key);
        if (StrUtil.isNotBlank(cacheValue)) {
            // 写回本地缓存
            localCache.put(key, cacheValue);
            return JSONUtil.toBean(cacheValue, clazz);
        }

        return null;
    }

    public <T> T getCache(String key, TypeReference<T> typeRef) {
        String cacheValue = localCache.getIfPresent(key);
        if (StrUtil.isNotBlank(cacheValue)) {
            return JSONUtil.toBean(cacheValue, typeRef, false);
        }

        cacheValue = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(cacheValue)) {
            localCache.put(key, cacheValue);
            return JSONUtil.toBean(cacheValue, typeRef, false);
        }
        return null;
    }



    /**
     * 写入缓存（同时写本地和Redis，随机过期防止雪崩）
     */
    public void setCache(String key, Object value) {
        String cachedValue = JSONUtil.toJsonStr(value);

        // 写本地缓存
        localCache.put(key, cachedValue);

        // 设置过期时间 5-10 分钟
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);

        // 写 Redis
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        opsForValue.set(key, cachedValue, cacheExpireTime, TimeUnit.SECONDS);
    }

    /**
     * 删除缓存
     */
    public void removeCache(String key) {
        localCache.invalidate(key);
        stringRedisTemplate.delete(key);
    }
}




