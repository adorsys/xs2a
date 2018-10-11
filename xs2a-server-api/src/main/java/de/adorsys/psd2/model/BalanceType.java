package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The following balance types are defined:   - \"closingBooked\":      Balance of the account at the end of the pre-agreed account reporting period.      It is the sum of the opening booked balance at the beginning of the period and all entries booked      to the account during the pre-agreed account reporting period.          For card-accounts, this is composed of            - invoiced, but not yet paid entries        - \"expected\":     Balance composed of booked entries and pending items known at the time of calculation,      which projects the end of day balance if everything is booked on the account and no other entry is posted.          For card accounts, this is composed of        - invoiced, but not yet paid entries,        - not yet invoiced but already booked entries and       - pending items (not yet booked)        - \"authorised\":     The expected balance together with the value of a pre-approved credit line the ASPSP makes permanently available to the user.          For card-accounts:          \"money to spend with the value of a pre-approved credit limit on the card account\"        - \"openingBooked\":     Book balance of the account at the beginning of the account reporting period.      It always equals the closing book balance from the previous report.   - \"interimAvailable\":     Available balance calculated in the course of the account �servicer�s business day,      at the time specified, and subject to further changes during the business day.      The interim balance is calculated on the basis of booked credit and debit items during the calculation      time/period specified.          For card-accounts, this is composed of       - invoiced, but not yet paid entries,        - not yet invoiced but already booked entries   - \"forwardAvailable\":     Forward available balance of money that is at the disposal of the account owner on the date specified.   - \"nonInvoiced\":      Only for card accounts, to be checked yet. #TODO CARD-ACCOUNTS nonInvoiced New in spec. detailes needed   - \"available\": #TODO MISSING IN SPEC: 'available' New in spec. Detailes needed.
 */
public enum BalanceType {

    CLOSINGBOOKED("closingBooked"),

    EXPECTED("expected"),

    AUTHORISED("authorised"),

    OPENINGBOOKED("openingBooked"),

    INTERIMAVAILABLE("interimAvailable"),

    FORWARDAVAILABLE("forwardAvailable"),

    NONINVOICED("nonInvoiced"),

    AVAILABLE("available");

    private String value;

    BalanceType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static BalanceType fromValue(String text) {
        for (BalanceType b : BalanceType.values()) {
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

