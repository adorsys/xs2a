package de.adorsys.aspsp.xs2a.service.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.aspsp.xs2a.component.DateTimeDeserializer;

import java.time.LocalDateTime;

/**
 * Holds the object mapper instance used everywhere in this module to
 * provide for identical configuration.
 *
 * @author fpo
 */
public class ObjectMapperFactory {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.registerModule(getDateTimeDeserializerModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new Jdk8Module()); // add support for Optionals
        objectMapper.registerModule(new JavaTimeModule()); // add support for java.time types
        objectMapper.registerModule(new ParameterNamesModule()); // support for multiargs constructors
        OBJECT_MAPPER = objectMapper;
    }

    public static ObjectMapper instance() {
        return OBJECT_MAPPER;
    }

    private static SimpleModule getDateTimeDeserializerModule() {
        SimpleModule dateTimeModule = new SimpleModule();
        dateTimeModule.addDeserializer(LocalDateTime.class, new DateTimeDeserializer());
        return dateTimeModule;
    }
}
