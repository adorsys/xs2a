package de.adorsys.aspsp.xs2a.integtest.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.aspsp.xs2a.integtest.utils.CustomDateDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@Profile("dev")
public class ObjectMapperConfig {
    @Bean
    @Primary
    public ObjectMapper integrationTestMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new Jdk8Module()); // add support for Optionals
        objectMapper.registerModule(new JavaTimeModule()); // add support for java.time types
        objectMapper.registerModule(new ParameterNamesModule()); // support for multiargs constructors
        registerDateDeserializerModule(objectMapper);
        return objectMapper;
    }

    private void registerDateDeserializerModule(ObjectMapper objectMapper) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDateTime.class, new CustomDateDeserializer<>(LocalDateTime.class));
        simpleModule.addDeserializer(LocalDate.class, new CustomDateDeserializer<>(LocalDate.class));
        objectMapper.registerModule(simpleModule);
    }
}
