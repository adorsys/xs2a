package de.adorsys.aspsp.xs2a.integtest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.adorsys.aspsp.xs2a.integtest.config.HttpStatusDeserializer;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class Response<T> {
    @JsonProperty("code")
    @JsonDeserialize(using = HttpStatusDeserializer.class)
    private HttpStatus httpStatus;
    private Map<String, String> header;
    private T body;
}
