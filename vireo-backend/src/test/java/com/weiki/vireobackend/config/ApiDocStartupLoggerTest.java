package com.weiki.vireobackend.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class ApiDocStartupLoggerTest {

    @Test
    void shouldLogKnife4jApiDocUrlWhenApplicationIsReady(CapturedOutput output) {
        new ApiDocStartupLogger(8123, "/api").onApplicationReady();

        assertThat(output).contains("后端接口文档地址：http://localhost:8123/api/doc.html");
    }
}
