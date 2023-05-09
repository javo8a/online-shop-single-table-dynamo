package com.onlineshop.infrastructure.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helper to deserialize test_data from json files
 *
 * @param <T>
 */
public class JsonResourceObjectMapper {

    private ObjectMapper objectMapper;

    public JsonResourceObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T loadFromJsonFile(Class<T> model, String fileName) {
        ClassLoader classLoader = this.getClass()
                .getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        try {
            return objectMapper.readValue(inputStream, model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
