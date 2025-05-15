package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.filmorate.utils.HttpMethodEnum;
import ru.yandex.practicum.filmorate.utils.RandomUtils;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public abstract class AbstractControllerTest {
    @Autowired
    protected WebApplicationContext context;
    protected MockMvc mockMvc;
    protected final ObjectMapper objectMapper;
    protected RandomUtils randomUtils;

    protected AbstractControllerTest() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

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
        randomUtils = new RandomUtils();
    }

    protected ResultActions performRequest(
            HttpMethodEnum method,
            String url,
            String json,
            MultiValueMap<String, String> params,
            Object... uriVars
    ) throws Exception {
        if (params != null && !params.isEmpty()) {
            url = UriComponentsBuilder.fromPath(url).queryParams(params).build().toUriString();
        }

        RequestBuilder requestBuilder = switch (method) {
            case POST -> post(url, uriVars)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json);
            case PUT -> put(url, uriVars)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json);
            case GET -> get(url, uriVars)
                    .contentType(MediaType.APPLICATION_JSON);
            case DELETE -> delete(url, uriVars)
                    .contentType(MediaType.APPLICATION_JSON);
        };

        return mockMvc.perform(requestBuilder);
    }

    protected ResultActions performRequest(
            HttpMethodEnum method,
            String url,
            MultiValueMap<String, String> params,
            Object... uriVars
    ) throws Exception {
        return performRequest(method, url, "", params, uriVars);
    }

    protected ResultActions performRequest(
            HttpMethodEnum method,
            String url,
            MultiValueMap<String, String> params
    ) throws Exception {
        return performRequest(method, url, "", params, new Object[0]);
    }

    protected ResultActions performRequest(
            HttpMethodEnum method,
            String url,
            String json,
            Object... uriVars) throws Exception {
        return performRequest(method, url, json, null, uriVars);
    }

    protected ResultActions performRequest(
            HttpMethodEnum method,
            String url,
            Object... uriVars
    ) throws Exception {
        return performRequest(method, url, "", new LinkedMultiValueMap<>(), uriVars);
    }

    protected ResultActions performRequest(
            HttpMethodEnum method,
            String url
    ) throws Exception {
        return performRequest(method, url, "", null, new Object[0]);
    }


}
