package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;

public interface FundsConfirmationSpi {
    boolean fundsConfirmation(SpiFundsConfirmationRequest request);
}
