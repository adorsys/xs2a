package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.*;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.Id;

import java.util.Currency;

@Value
@AllArgsConstructor
public class SpiAccountReference {
    @Id
    @Setter
    @NonFinal
    private String accountId;
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private Currency currency;
}
