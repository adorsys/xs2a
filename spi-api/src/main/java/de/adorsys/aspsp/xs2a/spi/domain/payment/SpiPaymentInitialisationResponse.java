package de.adorsys.aspsp.xs2a.spi.domain.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SpiPaymentInitialisationResponse {
@JsonProperty("transaction_status")
    private String transactionStatus;
}
