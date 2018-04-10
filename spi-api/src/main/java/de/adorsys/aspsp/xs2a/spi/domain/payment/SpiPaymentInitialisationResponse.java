package de.adorsys.aspsp.xs2a.spi.domain.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import lombok.Data;

@Data
public class SpiPaymentInitialisationResponse {
    @JsonProperty("transaction_status")
    private SpiTransactionStatus transactionStatus;
    private String paymentId;
    private SpiAmount spiTransactionFees;
    private boolean spiTransactionFeeIndicator;
    private String[] scaMethods;
    private String psuMessage;
    private String[] tppMessages;
    private boolean tppRedirectPreferred;
}
