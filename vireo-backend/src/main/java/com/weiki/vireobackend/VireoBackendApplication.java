package com.weiki.vireobackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Vireo 后端启动类。
 * <ul>
 *   <li>{@link MapperScan}：扫描 Mapper 接口并注册为 Bean；</li>
 *   <li>{@link EnableAspectJAutoProxy}：开启 AOP 支持（exposeProxy 暴露代理对象）；</li>
 *   <li>{@link SpringBootApplication}：Spring Boot 自动配置入口。</li>
 * </ul>
 */
@MapperScan("com.weiki.vireobackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class VireoBackendApplication {

    /**
     * 程序入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(VireoBackendApplication.class, args);
    }

}
