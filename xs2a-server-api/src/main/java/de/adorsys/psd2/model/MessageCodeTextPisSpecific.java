package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets messageCodeTextPisSpecific
 */
public enum MessageCodeTextPisSpecific {
    PRODUCT_INVALID("PRODUCT_INVALID"),
    PRODUCT_UNKNOWN("PRODUCT_UNKNOWN"),
    PAYMENT_FAILED("PAYMENT_FAILED"),
    REQUIRED_KID_MISSING("REQUIRED_KID_MISSING"),
    EXECUTION_DATE_INVALID("EXECUTION_DATE_INVALID");

    private String value;

    MessageCodeTextPisSpecific(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MessageCodeTextPisSpecific fromValue(String text) {
        for (MessageCodeTextPisSpecific b : MessageCodeTextPisSpecific.values()) {
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
