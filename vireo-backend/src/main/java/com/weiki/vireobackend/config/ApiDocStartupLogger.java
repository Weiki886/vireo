package com.weiki.vireobackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动完成后输出接口文档地址。
 */
@Slf4j
@Component
public class ApiDocStartupLogger {

    private final int serverPort;

    private final String contextPath;

    public ApiDocStartupLogger(
            @Value("${server.port:8080}") int serverPort,
            @Value("${server.servlet.context-path:}") String contextPath) {
        this.serverPort = serverPort;
        this.contextPath = contextPath;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("后端接口文档地址：http://localhost:{}{}/doc.html", serverPort, contextPath);
    }
}
