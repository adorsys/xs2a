package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.spi.service.FundsConfirmationSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FundsConfirmationService {
    private FundsConfirmationSpi fundsConfirmationSpi;
    private FundMapper fundMapper;

    @Autowired
    public FundsConfirmationService(FundsConfirmationSpi fundsConfirmationSpi, FundMapper fundMapper) {
        this.fundsConfirmationSpi = fundsConfirmationSpi;
        this.fundMapper = fundMapper;
    }

    public boolean fundsConfirmation(FundsConfirmationRequest request){
        return fundsConfirmationSpi.fundsConfirmation(fundMapper.toModel(request));
    }
}
