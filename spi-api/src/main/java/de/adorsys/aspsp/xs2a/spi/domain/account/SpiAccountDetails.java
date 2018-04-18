package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.Id;

import java.beans.ConstructorProperties;
import java.util.Currency;
import java.util.List;

@Value
@AllArgsConstructor(onConstructor = @__({@ConstructorProperties({"id", "iban", "bban", "pan", "maskedPan",
                                                                 "msisdn", "currency", "name", "accountType", "cashSpiAccountType",
                                                                 "bic", "balances"})}))
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

/*

    public SpiAccountDetails(String id, String iban, String bban, String pan, String maskedPan,
                             String msisdn,
                             Currency currency,
                             String name,
                             String accountType,
                             SpiAccountType cashSpiAccountType,
                             String bic,
                             List<SpiBalances> balances
                            ) {
        this.id = id;
        this.iban = iban;
        this.bban = bban;
        this.pan = pan;
        this.maskedPan = maskedPan;
        this.msisdn = msisdn;
        this.currency = currency;
        this.name = name;
        this.accountType = accountType;
        this.cashSpiAccountType = cashSpiAccountType;
        this.bic = bic;
        this.balances = balances;
    }
*/
}
