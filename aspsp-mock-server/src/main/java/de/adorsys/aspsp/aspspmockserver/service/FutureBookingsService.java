package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FutureBookingsService {
    private final AccountService accountService;
    private final PaymentService paymentService;

    public Optional<SpiAccountDetails> changeBalances(String accountId) {

        SpiAccountDetails account = accountService.getAccount(accountId).get();
        SpiBalances balance = account.getFirstBalance();
        double oldBalanceAmount = Double.parseDouble(balance.getInterimAvailable().getSpiAmount().getContent());
        double newBalanceAmount = oldBalanceAmount - paymentService.amountToBeCharged(accountId);

        SpiAmount newAmount = new SpiAmount(Currency.getInstance("EUR"),String.valueOf(newBalanceAmount));
        SpiAccountBalance newAccountBalance = new SpiAccountBalance();
        newAccountBalance.setSpiAmount(newAmount);
        newAccountBalance.setLastActionDateTime(new Date());
        newAccountBalance.setDate(new Date());

        balance.setInterimAvailable(newAccountBalance);
        account.updateFirstBalance(balance);

        return Optional.ofNullable(accountService.addAccount(account));
    }
}
