package de.adorsys.aspsp.xs2a.integtest.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalDate;

public class CustomLocalDateDeserializer extends StdDeserializer<LocalDate> {

    public CustomLocalDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDate deserialize(JsonParser jsonparser, DeserializationContext context) {
        return LocalDate.now();
    }
}
