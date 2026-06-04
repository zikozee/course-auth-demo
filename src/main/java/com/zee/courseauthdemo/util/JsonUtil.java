package com.zee.courseauthdemo.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.CollectionType;

import java.util.ArrayList;
import java.util.List;

/**
 * @dev : Ezekiel Eromosei
 * @date : 04 Jun, 2026
 */

@Slf4j
@Component
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public JsonUtil(@Qualifier("customObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T fromJSON(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JacksonException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public <T> List<T> jsonToList(String json, Class<T> clazz) {
        try {
            CollectionType listType =
                    objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
            return objectMapper.readValue(json, listType);
        } catch (JacksonException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public <T> String toJSON(T data, boolean prettify) {
        try {
            return prettify ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data) : objectMapper.writeValueAsString(data);
        } catch (JacksonException ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }
    public <T> T fromJSON(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JacksonException _) {
            log.error("Not able to deserialize string");
            return null;
        }
    }
}
