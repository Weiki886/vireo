package com.weiki.vireobackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.weiki.vireobackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class VireoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VireoBackendApplication.class, args);
    }

}
