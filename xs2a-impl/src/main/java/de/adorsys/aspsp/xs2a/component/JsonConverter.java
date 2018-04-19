package de.adorsys.aspsp.xs2a.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class JsonConverter {
    private final ObjectMapper objectMapper;

    public <T> Optional<String> toJson(final T object){
        try {
            return Optional.ofNullable(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            log.error("Can't convert object to json: {}", e);
        }
        return Optional.empty();
    }

    public <T> Optional<T> toObject(final String json, final Class<T> target){
        try {
            return Optional.ofNullable(objectMapper.readValue(json, target));
        } catch (JsonProcessingException e) {
            log.error("Can't convert json to object: {}", e);
        } catch (IOException e) {
            log.error("Can't convert json to object: {}", e);
        }
        return Optional.empty();
    }
}
