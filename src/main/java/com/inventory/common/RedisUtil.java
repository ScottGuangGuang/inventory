package com.inventory.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 存入对象（序列化为 JSON 字符串）
     */
    public void set(String key, Object value) {
        set(key, value, 30L, TimeUnit.MINUTES);
    }

    public void set(String key, Object value, Long timeout, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, timeout, unit);
        } catch (Exception e) {
            throw new RuntimeException("Redis 序列化失败", e);
        }
    }

    /**
     * 获取对象（反序列化为指定类型）
     */
    public <T> T get(String key, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Redis 反序列化失败", e);
        }
    }

    /**
     * 获取对象（支持泛型，如 List<PageResult>）
     */
    public <T> T get(String key, TypeReference<T> typeRef) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Redis 反序列化失败", e);
        }
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public void deleteKeys(String pattern) {
        // 使用 SCAN 替代 KEYS（生产环境更安全）
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}