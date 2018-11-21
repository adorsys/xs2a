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

import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountBalance;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FutureBookingsService {
    private final AccountService accountService;
    private final PaymentService paymentService;

    /**
     * Find all stored payments with current iban and update balance in psu account according payments
     *
     * @param iban     Iban for searching payments
     * @param currency Currency for searching payments
     * @return sca approach method which is stored in profile
     */
    public Optional<AspspAccountDetails> changeBalances(String iban, String currency) {
        return accountService.getAccountsByIban(iban).stream()
                   .filter(acc -> areCurrenciesEqual(acc.getCurrency(), currency))
                   .findFirst()
                   .flatMap(this::updateAccountBalance);
    }

    private Optional<AspspAccountDetails> updateAccountBalance(AspspAccountDetails account) {
        return calculateNewBalance(account)
                   .flatMap(bal -> saveNewBalanceToAccount(account, bal));
    }

    private Optional<AspspAccountDetails> saveNewBalanceToAccount(AspspAccountDetails account, AspspAccountBalance balance) {
        account.updateFirstBalance(balance);
        return accountService.updateAccount(account);
    }

    private Optional<AspspAccountBalance> calculateNewBalance(AspspAccountDetails account) {
        return account.getFirstBalance()
                   .map(bal -> getNewBalance(account, bal));
    }

    private AspspAccountBalance getNewBalance(AspspAccountDetails account, AspspAccountBalance balance) {
        AspspAccountBalance newAccountBalance = new AspspAccountBalance();
        newAccountBalance.setSpiBalanceAmount(getNewAmount(account, balance));
        newAccountBalance.setLastChangeDateTime(LocalDateTime.now());
        newAccountBalance.setReferenceDate(LocalDate.now());
        return balance;
    }

    private AspspAmount getNewAmount(AspspAccountDetails account, AspspAccountBalance b) {
        return new AspspAmount(Currency.getInstance("EUR"), getNewBalanceAmount(account, b));
    }

    private BigDecimal getNewBalanceAmount(AspspAccountDetails account, AspspAccountBalance balance) {
        BigDecimal oldBalanceAmount = balance.getSpiBalanceAmount().getAmount();
        return oldBalanceAmount.subtract(paymentService.calculateAmountToBeCharged(account.getResourceId()));
    }

    private boolean areCurrenciesEqual(Currency accountCurrency, String givenCurrency) {
        return Optional.ofNullable(accountCurrency)
                   .map(curr -> curr.getCurrencyCode().equals(givenCurrency))
                   .orElse(false);
    }
}
