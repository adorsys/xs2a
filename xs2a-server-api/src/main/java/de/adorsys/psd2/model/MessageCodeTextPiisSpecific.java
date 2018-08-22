package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets messageCodeTextPiisSpecific
 */
public enum MessageCodeTextPiisSpecific {
    CARD_INVALID("CARD_INVALID"),
    NO_PIIS_ACTIVATION("NO_PIIS_ACTIVATION");

    private String value;

    MessageCodeTextPiisSpecific(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MessageCodeTextPiisSpecific fromValue(String text) {
        for (MessageCodeTextPiisSpecific b : MessageCodeTextPiisSpecific.values()) {
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
