package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.spi.service.FundsConfirmationSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.FundsConfirmationMockData;
import org.springframework.stereotype.Service;

@Service
public class FundsConfirmationSpiImpl implements FundsConfirmationSpi {

    @Override
    public boolean fundsConfirmation(SpiFundsConfirmationRequest request) {
        return FundsConfirmationMockData.fundsConfirmation(request);
    }
}
