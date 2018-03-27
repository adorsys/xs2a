package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ContentType {

    XML("application/xml"),
    JSON("application/json"),
    TXT("text/plain"),
    EMPTY("*/*");

    private String type;

    @JsonCreator
    ContentType(String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() {
        return type;
    }
}
