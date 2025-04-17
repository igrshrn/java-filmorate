package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

public abstract class AbstractControllerTest {
    @Autowired
    protected WebApplicationContext context;
    protected MockMvc mockMvc;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected String createJson(Map<String, Object> fields) {
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании JSON", e);
        }
    }

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

}
