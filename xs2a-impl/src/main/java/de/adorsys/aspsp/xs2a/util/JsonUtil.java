package de.adorsys.aspsp.xs2a.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public final class JsonUtil {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtil(){}

    public static <T> String toJson(final T object){
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Can't convert object to json: {}", e);
        }
        return "";
    }

    public static <T> T toObject(final String json, final Class<T> target){
        try {
            return OBJECT_MAPPER.readValue(json, target);
        } catch (JsonProcessingException e) {
            log.error("Can't convert json to object: {}", e);
        } catch (IOException e) {
            log.error("Can't convert json to object: {}", e);
        }
        return null;
    }
}
