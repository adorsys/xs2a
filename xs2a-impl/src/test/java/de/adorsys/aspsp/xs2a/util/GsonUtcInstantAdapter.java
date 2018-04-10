package de.adorsys.aspsp.xs2a.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Locale;
import java.util.TimeZone;


/**
 * GSON custom adapter for java.util.Date class that enables GSON to parse dates into UTC TimeZone
 * and not local one.
 * Also supports both date and datetime formats for parsing.
 */
public class GsonUtcInstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

    private final DateFormat dateTimeFormat;
    private final DateFormat dateFormat;

    public GsonUtcInstantAdapter() {
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public synchronized JsonElement serialize(Instant date, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(dateTimeFormat.format(date));
    }

    @Override
    public synchronized Instant deserialize(JsonElement jsonElement, Type type,
                                         JsonDeserializationContext jsonDeserializationContext
                                        ) {
        try {
            String asString = jsonElement.getAsString();
            if (asString.matches("[0-9]{2,4}-[0-9]{1,2}-[0-9]{1,2}")) {
                return dateFormat.parse(asString).toInstant();
            }
            return dateTimeFormat.parse(asString).toInstant();
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }
}
