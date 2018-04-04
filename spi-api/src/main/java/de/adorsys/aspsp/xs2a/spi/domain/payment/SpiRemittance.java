package de.adorsys.aspsp.xs2a.spi.domain.payment;

import lombok.Data;

@Data
public class SpiRemittance {
    private final String reference;
    private final String referenceType;
    private final String referenceIssuer;
}
