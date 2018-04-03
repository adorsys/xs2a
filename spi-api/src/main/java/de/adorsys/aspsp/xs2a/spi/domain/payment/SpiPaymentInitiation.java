package de.adorsys.aspsp.xs2a.spi.domain.payment;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import lombok.Data;

@Data
public class SpiPaymentInitiation {
    private SpiTransactionStatus spiTransactionStatus;
    private String paymentId;
    private SpiAmount spiTransactionFees;
    private boolean spiTransactionFeeIndicator;
    private String psuMessage;
    private String[] tppMessages;
    private final boolean tppRedirectPreferred;
}
