package com.weiki.vireobackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void longIdShouldSerializeAsString() throws Exception {
        Record record = new Record(1893756824288542721L);

        String json = objectMapper.writeValueAsString(record);

        assertThat(json).isEqualTo("{\"id\":\"1893756824288542721\"}");
    }

    private record Record(Long id) {
    }
}
