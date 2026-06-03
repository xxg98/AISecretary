package com.kailei.aisecretary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类（SpringBoot 4 最终无警告版）
 * 官方推荐：使用 RedisSerializer.json() 替代所有废弃序列化器
 * 兼容 JDK17 + SpringBoot4，无乱码、支持对象存储
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Key / HashKey 序列化：String（标准无乱码）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);

        // ===================== 核心修复 =====================
        // Value / HashValue 序列化：官方最终推荐 RedisSerializer.json()
        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        // ===================================================

        // 初始化
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}