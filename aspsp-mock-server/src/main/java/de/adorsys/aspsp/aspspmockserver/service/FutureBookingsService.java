/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        return accountService.getAccountById(accountId)
            .flatMap(this::updateAccountBalance);
    }

    private Optional<SpiAccountDetails> updateAccountBalance(SpiAccountDetails account) {
        return calculateNewBalance(account)
            .map(bal -> saveUpdatedAccount(account, bal))
            .orElse(Optional.empty());
    }

    private Optional<SpiAccountDetails> saveUpdatedAccount(SpiAccountDetails account, SpiBalances balance) {
        account.updateFirstBalance(balance);
        return accountService.updateAccount(account);
    }

    private Optional<SpiBalances> calculateNewBalance(SpiAccountDetails account) {
        return account.getFirstBalance().map(bal -> getNewBalance(account, bal));
    }

    private SpiBalances getNewBalance(SpiAccountDetails account, SpiBalances balance) {
        SpiAccountBalance newAccountBalance = new SpiAccountBalance();
        newAccountBalance.setSpiAmount(getNewAmount(account, balance));
        newAccountBalance.setLastActionDateTime(new Date());
        newAccountBalance.setDate(new Date());
        balance.setInterimAvailable(newAccountBalance);
        return balance;
    }

    private SpiAmount getNewAmount(SpiAccountDetails account, SpiBalances b) {
        return new SpiAmount(Currency.getInstance("EUR"), String.valueOf(getNewBalanceAmount(account, b)));
    }

    private double getNewBalanceAmount(SpiAccountDetails account, SpiBalances balance) {
        double oldBalanceAmount = balance.getInterimAvailable().getSpiAmount().getDoubleContent();
        return oldBalanceAmount - paymentService.calculateAmountToBeCharged(account.getId());
    }
}
