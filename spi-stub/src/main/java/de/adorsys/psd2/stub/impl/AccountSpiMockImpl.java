/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.stub.impl;

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

@Slf4j
@Service
public class AccountSpiMockImpl implements AccountSpi {
    @Override
    public SpiResponse<List<SpiAccountDetails>> requestAccountList(@NotNull SpiContextData contextData, boolean withBalance, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        log.info("AccountSpi#requestAccountList: contextData {}, withBalance {}, accountConsent-id {}, aspspConsent-id {}", contextData, withBalance, accountConsent.getId(), aspspConsentData.getConsentId());
        SpiAccountDetails details = new SpiAccountDetails("11111-11118", "10023-999999999", "DE52500105173911841934",
                                                          "52500105173911841934", "AEYPM5403H", "PM5403H****", null, Currency.getInstance("EUR"), "Müller",
                                                          "SCT", null, null, "DEUTDE8EXXX", null,
                                                          null, null, Collections.singletonList(buildSpiAccountBalance()));

        return SpiResponse.<List<SpiAccountDetails>>builder()
                   .payload(Collections.singletonList(details))
                   .aspspConsentData(aspspConsentData)
                   .success();
    }

    @Override
    public SpiResponse<SpiAccountDetails> requestAccountDetailForAccount(@NotNull SpiContextData contextData, boolean withBalance, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        log.info("AccountSpi#requestAccountDetailForAccount: contextData {}, withBalance {}, accountReference {}, accountConsent-id {}, aspspConsent-id {}", contextData, accountReference, accountConsent.getId(), aspspConsentData.getConsentId());
        SpiAccountDetails accountDetails = new SpiAccountDetails("11111-11118", "10023-999999999", "DE52500105173911841934",
                                                                 null, null, null, null, Currency.getInstance("EUR"), "Müller",
                                                                 "SCT", null, null, "DEUTDE8EXXX", null,
                                                                 null, null, Collections.singletonList(buildSpiAccountBalance()));

        return SpiResponse.<SpiAccountDetails>builder()
                   .payload(accountDetails)
                   .aspspConsentData(aspspConsentData)
                   .success();
    }

    @Override
    public SpiResponse<SpiTransactionReport> requestTransactionsForAccount(@NotNull SpiContextData contextData, String acceptMediaType, boolean withBalance, @NotNull LocalDate dateFrom, @NotNull LocalDate dateTo, @NotNull BookingStatus bookingStatus, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        log.info("AccountSpi#requestTransactionsForAccount: contextData {}, acceptMediaType {}, withBalance {}, dateFrom {}, dateTo {}, bookingStatus {}, accountReference {}, accountConsent-id {}, aspspConsent-id {}", contextData, acceptMediaType, withBalance, dateFrom, dateTo, bookingStatus, accountReference, accountConsent.getId(), aspspConsentData.getConsentId());


        return SpiResponse.<SpiTransactionReport>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(new SpiTransactionReport(buildSpiTransactionList(), Collections.singletonList(buildSpiAccountBalance()), "application/json", null))
                   .success();
    }

    @Override
    public SpiResponse<SpiTransaction> requestTransactionForAccountByTransactionId(@NotNull SpiContextData contextData, @NotNull String transactionId, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        log.info("AccountSpi#requestTransactionForAccountByTransactionId: contextData {}, aspspConsent-id {}, accountReference {}, accountConsent-id {}, aspspConsent-id {}", contextData, transactionId, accountReference, accountConsent.getId(), aspspConsentData.getConsentId());

        return SpiResponse.<SpiTransaction>builder()
                   .payload(buildSpiTransactionById("0001"))
                   .aspspConsentData(aspspConsentData)
                   .success();
    }

    @Override
    public SpiResponse<List<SpiAccountBalance>> requestBalancesForAccount(@NotNull SpiContextData contextData, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        log.info("AccountSpi#requestBalancesForAccount: contextData {}, accountReference {}, accountConsent-id {}, aspspConsent-id {}", contextData, accountReference, accountConsent.getId(), aspspConsentData.getConsentId());

        return SpiResponse.<List<SpiAccountBalance>>builder()
                   .payload(Collections.singletonList(buildSpiAccountBalance()))
                   .aspspConsentData(aspspConsentData)
                   .success();
    }

    private SpiAccountBalance buildSpiAccountBalance() {
        SpiAccountBalance accountBalance = new SpiAccountBalance();
        accountBalance.setSpiBalanceAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(1000)));
        accountBalance.setSpiBalanceType(SpiBalanceType.INTERIM_AVAILABLE);
        accountBalance.setLastCommittedTransaction("abcd");
        accountBalance.setReferenceDate(LocalDate.of(2020, Month.JANUARY, 1));
        accountBalance.setLastChangeDateTime(LocalDateTime.of(2019, Month.FEBRUARY, 15, 10, 0, 0, 0));
        return accountBalance;
    }

    private List<SpiTransaction> buildSpiTransactionList() {
        List<SpiTransaction> transactions = new ArrayList<>();
        transactions.add(buildSpiTransactionById("0001"));
        transactions.add(buildSpiTransactionById("0002"));
        transactions.add(buildSpiTransactionById("0003"));
        return transactions;
    }

    private SpiTransaction buildSpiTransactionById(String transactionId) {
        return new SpiTransaction(transactionId, "", "", "", "", "aspsp", LocalDate.of(2019, Month.JANUARY, 4),
                                  LocalDate.of(2019, Month.JANUARY, 4), new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(200)), Collections.emptyList(),
                                  "Müller", buildSpiAccountReference(), "Müller", "Müller", buildSpiAccountReference(),
                                  "Müller", "", "", "", "", "");
    }

    private SpiAccountReference buildSpiAccountReference() {
        return new SpiAccountReference("11111-11118", "10023-999999999", "DE52500105173911841934",
                                       "52500105173911841934", "AEYPM5403H", "PM5403H****", null, Currency.getInstance("EUR"));
    }
}
