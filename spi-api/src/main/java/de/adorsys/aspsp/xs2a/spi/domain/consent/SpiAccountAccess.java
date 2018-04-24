package de.adorsys.aspsp.xs2a.spi.domain.consent;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpiAccountAccess {
    private List<SpiAccountReference> accounts;
    private List<SpiAccountReference> balances;
    private List<SpiAccountReference> transactions;
    private SpiAccountAccessType availableAccounts;
    private SpiAccountAccessType allPsd2;
}
