package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public enum TransactionsArt {
    booked, expected, authorised, opening_booked, closing_booked, interim_available
}
