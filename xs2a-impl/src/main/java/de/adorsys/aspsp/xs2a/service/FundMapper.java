package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FundMapper {
    private AccountMapper accountMapper;

    @Autowired
    public FundMapper(AccountMapper accountMapper){
        this.accountMapper = accountMapper;
    }

    public SpiFundsConfirmationRequest toModel(FundsConfirmationRequest request){
        return new SpiFundsConfirmationRequest(request.getCardNumber(),
        accountMapper.toSpi(request.getPsuAccount()),
        request.getPayee(),
        accountMapper.toSpi(request.getInstructedAmount()));
    }
}
