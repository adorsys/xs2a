package de.adorsys.aspsp.xs2a.integtest.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
public class CustomDateDeserializer<T> extends StdDeserializer<T> {
    private final Class<T> vc;

    public CustomDateDeserializer(Class<T> vc) {
        super(vc);
        this.vc = vc;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        try {
            Method now = Optional.ofNullable(vc.getMethod("now"))
                             .orElseThrow(() -> new IllegalArgumentException("Can't find method now"));

            return (T)now.invoke(vc.getDeclaredClasses());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Can't invoke method 'now'");
        }
        return null;
    }
}
