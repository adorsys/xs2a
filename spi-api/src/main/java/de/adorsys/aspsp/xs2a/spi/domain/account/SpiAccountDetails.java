package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;

import java.util.Currency;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpiAccountDetails {
    @Id
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
    private SpiBalances balances;
}
