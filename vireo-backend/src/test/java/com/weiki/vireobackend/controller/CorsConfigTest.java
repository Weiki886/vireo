package com.weiki.vireobackend.controller;

import com.weiki.vireobackend.config.CorsConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import(CorsConfig.class)
class CorsConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowConfiguredLocalFrontendOrigin() throws Exception {
        mockMvc.perform(options("/health/")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void shouldRejectUnknownOrigin() throws Exception {
        String allowOrigin = mockMvc.perform(options("/health/")
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andReturn()
                .getResponse()
                .getHeader("Access-Control-Allow-Origin");

        assertThat(allowOrigin).isNull();
    }
}
