package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FundMapper {
    private final AccountMapper accountMapper;

    public SpiFundsConfirmationRequest mapToSpiFundsConfirmationRequest(FundsConfirmationRequest request) {
        return new SpiFundsConfirmationRequest(request.getCardNumber(),
        accountMapper.toSpi(request.getPsuAccount()),
        request.getPayee(),
        accountMapper.toSpi(request.getInstructedAmount()));
    }
}
