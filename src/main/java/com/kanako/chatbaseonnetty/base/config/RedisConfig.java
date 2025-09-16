package com.kanako.chatbaseonnetty.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object>  redisTemplate = new RedisTemplate<>();
        // 配置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置key的序列化器 String字符串的序列化器
        // 所有通过RedisTemplate操作redis时，key都是字符串
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // 设置value的序列化器 JSON字符串的序列化器
        // 所有通过RedisTemplate操作redis时，value都是json字符串
        redisTemplate.setValueSerializer(RedisSerializer.json());
        return redisTemplate;
    }
}
