package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.service.mapper.FundMapper;
import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.spi.service.FundsConfirmationSpi;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FundsConfirmationService {
    private final FundsConfirmationSpi fundsConfirmationSpi;
    private final FundMapper fundMapper;

    public ResponseObject<FundsConfirmationResponse> fundsConfirmation(FundsConfirmationRequest request) {

        SpiFundsConfirmationRequest spiRequest = fundMapper.mapToSpiFundsConfirmationRequest(request);
        Boolean areSufficientFunds = fundsConfirmationSpi.fundsConfirmation(spiRequest);

        return ResponseObject.builder()
               .body(new FundsConfirmationResponse(areSufficientFunds)).build();
    }
}
