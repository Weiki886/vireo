package com.weiki.vireobackend.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置。
 * 雪花 ID 超过 JavaScript Number.MAX_SAFE_INTEGER，统一按字符串返回，避免前端精度丢失。
 */
@Configuration
public class JacksonConfig {

    /**
     * 定制 Jackson：把所有 Long（含 long 包装类型）序列化为字符串，
     * 避免雪花 ID 超过 JS 安全整数范围导致前端精度丢失。
     *
     * @return Jackson 构建器定制器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder.serializerByType(Long.class, ToStringSerializer.instance);
    }
}
