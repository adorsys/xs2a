package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This is the overall lifecycle status of the consent.  Valid values are:   - 'received': The consent data have been
 * received and are technically correct.     The data is not authorised yet.   - 'rejected': The consent data have
 * been rejected e.g. since no successful authorisation has taken place.   - 'valid': The consent is accepted and
 * valid for GET account data calls and others as specified in the consent object.   - 'revokedByPsu': The consent
 * has been revoked by the PSU towards the ASPSP.   - 'expired': The consent expired.   - 'terminatedByTpp': The
 * corresponding TPP has terminated the consent by applying the DELETE method to the consent resource.  The ASPSP
 * might add further codes. These codes then shall be contained in the ASPSP's documentation of the XS2A interface
 * and has to be added to this API definition as well.
 */
public enum ConsentStatus {
    RECEIVED("received"), REJECTED("rejected"), VALID("valid"), REVOKEDBYPSU("revokedByPsu"), EXPIRED("expired"),
    TERMINATEDBYTPP("terminatedByTpp");
    private String value;

    ConsentStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ConsentStatus fromValue(String text) {
        for (ConsentStatus b : ConsentStatus.values()) {
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

