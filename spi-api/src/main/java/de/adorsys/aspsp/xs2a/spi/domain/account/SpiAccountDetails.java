package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.Id;

import java.util.Currency;
import java.util.List;

@Value
@AllArgsConstructor
public class SpiAccountDetails {
    @Id
    @Setter
    @NonFinal
    private String id;
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private Currency currency;
    private String name;
    private String accountType;
    private SpiAccountType cashSpiAccountType;
    private String bic;
    private List<SpiBalances> balances;
}
