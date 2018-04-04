package de.adorsys.aspsp.xs2a.spi.domain.common;

import lombok.Data;

@Data
public class SpiAddress {
    private final String street;
    private final String buildingNumber;
    private final String city;
    private final String postalCode;
    private final String country;
}
