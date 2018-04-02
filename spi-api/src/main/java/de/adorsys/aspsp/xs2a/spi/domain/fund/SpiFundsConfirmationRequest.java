package de.adorsys.aspsp.xs2a.spi.domain.fund;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SpiFundsConfirmationRequest {
    private final String cardNumber;
    private final SpiAccountReference psuAccount;
    private final String payee;
    private final SpiAmount instructedAmount;
}
