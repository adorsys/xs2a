package de.adorsys.aspsp.xs2a.spi.domain.payment;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import lombok.Data;

@Data
public class SpiCreatePaymentRequest {
    private final SpiAmount spiTransactionFees;
    private final boolean spiTransactionFeeIndicator;
    private final String[] scaMethods;
    private final String psuMessage;
    private final String[] tppMessages;
}
