/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.stub.impl;

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CardAccountSpi;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

@Slf4j
@Service
public class CardAccountSpiMockImpl implements CardAccountSpi {
    private static final LocalDate DATE = LocalDate.of(2019, Month.JANUARY, 4);
    private static final String NAME = "Müller";

    @Override
    public SpiResponse<List<SpiCardAccountDetails>> requestCardAccountList(@NotNull SpiContextData contextData, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("CardAccountSpi#requestCardAccountList: contextData {}, accountConsent-id {}, aspspConsentData {}", contextData, accountConsent.getId(), aspspConsentDataProvider.loadAspspConsentData());

        SpiCardAccountDetails details = new SpiCardAccountDetails("11111-11118", "10023-999999999", "525412******3241", Currency.getInstance("EUR"), NAME,
                                                                  NAME, "SCT", null, null, null,
                                                                  null, null, Collections.singletonList(buildSpiAccountBalance()), null, false);

        return SpiResponse.<List<SpiCardAccountDetails>>builder()
                   .payload(Collections.singletonList(details))
                   .build();
    }

    @Override
    public SpiResponse<SpiCardAccountDetails> requestCardAccountDetailsForAccount(@NotNull SpiContextData contextData, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("CardAccountSpi#requestCardAccountDetailForAccount: contextData {}, accountReference {}, accountConsent-id {}, aspspConsentData {}", contextData, accountReference, accountConsent.getId(), aspspConsentDataProvider.loadAspspConsentData());

        SpiCardAccountDetails accountDetails = new SpiCardAccountDetails("11111-11118", "10023-999999999", "525412******3241", Currency.getInstance("EUR"), NAME,
                                                                         NAME, "SCT", null, null, null,
                                                                         null, null, Collections.singletonList(buildSpiAccountBalance()), null, false);

        return SpiResponse.<SpiCardAccountDetails>builder()
                   .payload(accountDetails)
                   .build();
    }

    @Override
    public SpiResponse<SpiCardTransactionReport> requestCardTransactionsForAccount(@NotNull SpiContextData contextData, @NotNull SpiTransactionReportParameters spiCardTransactionReportParameters, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("CardAccountSpi#requestCardTransactionsForAccount: contextData {}, acceptMediaType {}, dateFrom {}, dateTo {}, bookingStatus {}, accountReference {}, accountConsent-id {}, aspspConsentData {}", contextData, spiCardTransactionReportParameters.getAcceptMediaType(), spiCardTransactionReportParameters.getDateFrom(), spiCardTransactionReportParameters.getDateTo(), spiCardTransactionReportParameters.getBookingStatus(), accountReference, accountConsent.getId(), aspspConsentDataProvider.loadAspspConsentData());

        List<SpiCardTransaction> transactions = BookingStatus.INFORMATION == spiCardTransactionReportParameters.getBookingStatus() ?
                                                    buildSpiInformationTransactionList() :
                                                    buildSpiTransactionList();

        return SpiResponse.<SpiCardTransactionReport>builder()
                   .payload(new SpiCardTransactionReport("dGVzdA==", transactions, Collections.singletonList(buildSpiAccountBalance()), "application/json", null))
                   .build();
    }

    @Override
    public SpiResponse<List<SpiAccountBalance>> requestCardBalancesForAccount(@NotNull SpiContextData contextData, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("CardAccountSpi#requestCardBalancesForAccount: contextData {}, accountReference {}, accountConsent-id {}, aspspConsentData {}", contextData, accountReference, accountConsent.getId(), aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<List<SpiAccountBalance>>builder()
                   .payload(Collections.singletonList(buildSpiAccountBalance()))
                   .build();
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

    private List<SpiCardTransaction> buildSpiInformationTransactionList() {
        List<SpiCardTransaction> transactions = new ArrayList<>();
        transactions.add(buildInformationSpiCardTransaction());
        return transactions;
    }

    private List<SpiCardTransaction> buildSpiTransactionList() {
        List<SpiCardTransaction> transactions = new ArrayList<>();
        transactions.add(buildSpiCardTransactionById("0001"));
        transactions.add(buildSpiCardTransactionById("0002"));
        transactions.add(buildSpiCardTransactionById("0003"));
        return transactions;
    }

    private SpiCardTransaction buildSpiCardTransactionById(String cardTransactionId) {
        return new SpiCardTransaction(cardTransactionId,
                                      "999999999",
                                      DATE,
                                      OffsetDateTime.of(DATE, LocalTime.NOON, ZoneOffset.UTC),
                                      DATE,
                                      DATE.plusDays(1),
                                      new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(200)),
                                      new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(2)),
                                      new ArrayList<>(),
                                      new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(200)),
                                      new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(200)),
                                      "2",
                                      NAME,
                                      null,
                                      "+61-(02)9999999999-9999",
                                      NAME,
                                      NAME,
                                      NAME,
                                      true,
                                      "");
    }

    private SpiCardTransaction buildInformationSpiCardTransaction() {
        return buildSpiCardTransactionById("999999999");
    }
}
