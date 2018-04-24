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
        return accountService.getAccount(accountId)
               .map(account -> {
                   SpiBalances balance = account.getFirstBalance()
                   .map(b -> {
                       double oldBalanceAmount = Double.parseDouble(b.getInterimAvailable().getSpiAmount().getContent());
                       double newBalanceAmount = oldBalanceAmount - paymentService.amountToBeCharged(accountId);
                       SpiAmount newAmount = new SpiAmount(Currency.getInstance("EUR"),String.valueOf(newBalanceAmount));
                       SpiAccountBalance newAccountBalance = new SpiAccountBalance();
                       newAccountBalance.setSpiAmount(newAmount);
                       newAccountBalance.setLastActionDateTime(new Date());
                       newAccountBalance.setDate(new Date());
                       b.setInterimAvailable(newAccountBalance);
                       return b;
                   }).orElse(null);
                   account.updateFirstBalance(balance);
                   return Optional.of(accountService.addAccount(account));
               })
               .orElse(null);
    }
}

