package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets messageCodeTextAisSpecific
 */
public enum MessageCodeTextAisSpecific {

    CONSENT_INVALID("CONSENT_INVALID"),

    SESSIONS_NOT_SUPPORTED("SESSIONS_NOT_SUPPORTED"),

    ACCESS_EXCEEDED("ACCESS_EXCEEDED"),

    REQUESTED_FORMATS_INVALID("REQUESTED_FORMATS_INVALID");

    private String value;

    MessageCodeTextAisSpecific(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MessageCodeTextAisSpecific fromValue(String text) {
        for (MessageCodeTextAisSpecific b : MessageCodeTextAisSpecific.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}

