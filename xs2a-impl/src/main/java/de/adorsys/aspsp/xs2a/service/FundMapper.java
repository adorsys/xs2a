package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationRequest.builder;

@Component
public class FundMapper {
    private AccountMapper accountMapper;

    @Autowired
    public FundMapper(AccountMapper accountMapper){
        this.accountMapper = accountMapper;
    }

    public SpiFundsConfirmationRequest toModel(FundsConfirmationRequest request){
        return builder()
        .cardNumber(request.getCardNumber())
        .payee(request.getPayee())
        .psuAccount(accountMapper.toModel(request.getPsuAccount()))
        .instructedAmount(accountMapper.toModel(request.getInstructedAmount()))
        .build();
    }
}
