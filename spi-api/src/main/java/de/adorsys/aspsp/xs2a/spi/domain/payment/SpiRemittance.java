package de.adorsys.aspsp.xs2a.spi.domain.payment;

import lombok.Data;

@Data
public class SpiRemittance {
    private String reference;
    private String referenceType;
    private String referenceIssuer;
}
