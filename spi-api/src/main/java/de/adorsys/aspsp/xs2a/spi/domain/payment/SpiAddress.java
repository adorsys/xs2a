package de.adorsys.aspsp.xs2a.spi.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpiAddress {
    private String street;
    private String buildingNumber;
    private String city;
    private String postalCode;
    private String country;
}
