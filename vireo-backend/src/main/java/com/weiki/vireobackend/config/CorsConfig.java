package com.weiki.vireobackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置。仅放行本地前端开发服务器，并允许携带 Cookie（Session 登录态依赖它）。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 注册全局 CORS 规则。
     *
     * @param registry CORS 注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（只允许本地前端）
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
