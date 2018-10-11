package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets messageCodeTextUnspecific
 */
public enum MessageCodeTextUnspecific {

    CERTIFICATE_INVALID("CERTIFICATE_INVALID"),

    CERTIFICATE_EXPIRED("CERTIFICATE_EXPIRED"),

    CERTIFICATE_BLOCKED("CERTIFICATE_BLOCKED"),

    CERTIFICATE_REVOKED("CERTIFICATE_REVOKED"),

    CERTIFICATE_MISSING("CERTIFICATE_MISSING"),

    SIGNATURE_INVALID("SIGNATURE_INVALID"),

    SIGNATURE_MISSING("SIGNATURE_MISSING"),

    FORMAT_ERROR("FORMAT_ERROR"),

    PARAMETER_NOT_SUPPORTED("PARAMETER_NOT_SUPPORTED"),

    PSU_CREDENTIALS_INVALID("PSU_CREDENTIALS_INVALID"),

    SERVICE_INVALID("SERVICE_INVALID"),

    SERVICE_BLOCKED("SERVICE_BLOCKED"),

    CORPORATE_ID_INVALID("CORPORATE_ID_INVALID"),

    CONSENT_UNKNOWN("CONSENT_UNKNOWN"),

    CONSENT_INVALID("CONSENT_INVALID"),

    CONSENT_EXPIRED("CONSENT_EXPIRED"),

    TOKEN_UNKNOWN("TOKEN_UNKNOWN"),

    TOKEN_INVALID("TOKEN_INVALID"),

    TOKEN_EXPIRED("TOKEN_EXPIRED"),

    RESOURCE_UNKNOWN("RESOURCE_UNKNOWN"),

    RESOURCE_EXPIRED("RESOURCE_EXPIRED"),

    TIMESTAMP_INVALID("TIMESTAMP_INVALID"),

    PERIOD_INVALID("PERIOD_INVALID"),

    SCA_METHOD_UNKNOWN("SCA_METHOD_UNKNOWN");

    private String value;

    MessageCodeTextUnspecific(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MessageCodeTextUnspecific fromValue(String text) {
        for (MessageCodeTextUnspecific b : MessageCodeTextUnspecific.values()) {
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

