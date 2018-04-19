package de.adorsys.aspsp.xs2a.spi.domain.payment;

import lombok.*;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.Id;

@Value
@AllArgsConstructor
public class SpiAddress {
    @Id
    @Setter
    @NonFinal
    private String id;
    private String street;
    private String buildingNumber;
    private String city;
    private String postalCode;
    private String country;
}
