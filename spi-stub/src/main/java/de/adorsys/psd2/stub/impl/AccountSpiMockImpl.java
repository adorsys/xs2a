/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.Remittance;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import de.adorsys.xs2a.reader.JsonReader;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

@Slf4j
@Service
public class AccountSpiMockImpl implements AccountSpi {
    private static final String ASPSP_ACCOUNT_ID = "11111-11118";
    private static final String RESOURCE_ID = "10023-999999999";
    private static final String TRUSTED_BENEFICIARIES_ID = "874aa308-78af-11ea-bc55-0242ac130003";
    private static final String IBAN = "DE52500105173911841934";
    private static final String NAME = "Müller";

    private JsonReader jsonReader = new JsonReader();

    @Override
    public SpiResponse<List<SpiAccountDetails>> requestAccountList(@NotNull SpiContextData contextData, boolean withBalance, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#requestAccountList: contextData {}, withBalance {}, accountConsent-id {}, aspspConsentData {}", contextData, withBalance, accountConsent.getId(), aspspConsentDataProvider.loadAspspConsentData());
        SpiAccountDetails details = new SpiAccountDetails(ASPSP_ACCOUNT_ID, RESOURCE_ID, IBAN,
                                                          "52500105173911841934", "AEYPM5403H", "PM5403H****", null, Currency.getInstance("EUR"), NAME,
                                                          NAME, "SCT", null, null, "DEUTDE8EXXX", null,
                                                          null, null, Collections.singletonList(buildSpiAccountBalance()), null, null);

        return SpiResponse.<List<SpiAccountDetails>>builder()
                   .payload(Collections.singletonList(details))
                   .build();
    }

    @Override
    public SpiResponse<List<SpiTrustedBeneficiaries>> requestTrustedBeneficiariesList(@NotNull SpiContextData contextData, SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#requestTrustedBeneficiariesList: contextData {}, accountReference {}, accountConsent-id {}, aspspConsentData {}", contextData, accountReference, accountConsent.getId(), aspspConsentDataProvider.loadAspspConsentData());
        SpiTrustedBeneficiaries beneficiaries = new SpiTrustedBeneficiaries(TRUSTED_BENEFICIARIES_ID,
                                                                            null,
                                                                            buildSpiAccountReference(),
                                                                            NAME,
                                                                            NAME,
                                                                            null,
                                                                            null,
                                                                            null);

        return SpiResponse.<List<SpiTrustedBeneficiaries>>builder()
                   .payload(Collections.singletonList(beneficiaries))
                   .build();
    }

    @Override
    public SpiResponse<SpiAccountDetails> requestAccountDetailForAccount(@NotNull SpiContextData contextData, boolean withBalance, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#requestAccountDetailForAccount: contextData {}, withBalance {}, accountReference {}, accountConsent-id {}, aspspConsentData {}", contextData, withBalance, accountReference, accountConsent.getId(), aspspConsentDataProvider.loadAspspConsentData());
        SpiAccountDetails accountDetails = new SpiAccountDetails(ASPSP_ACCOUNT_ID, RESOURCE_ID, IBAN,
                                                                 null, null, null, null, Currency.getInstance("EUR"), NAME,
                                                                 NAME, "SCT", null, null, "DEUTDE8EXXX", null,
                                                                 null, null, Collections.singletonList(buildSpiAccountBalance()), null, null);

        return SpiResponse.<SpiAccountDetails>builder()
                   .payload(accountDetails)
                   .build();
    }

    @Override
    public SpiResponse<SpiTransactionReport> requestTransactionsForAccount(@NotNull SpiContextData contextData, @NotNull SpiTransactionReportParameters spiTransactionReportParameters, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#requestTransactionsForAccount: contextData {}, acceptMediaType {}, withBalance {}, dateFrom {}, dateTo {}, bookingStatus {}, accountReference {}, accountConsent-id {}, aspspConsentData {}", contextData, spiTransactionReportParameters.getAcceptMediaType(), spiTransactionReportParameters.isWithBalance(), spiTransactionReportParameters.getDateFrom(), spiTransactionReportParameters.getDateTo(), spiTransactionReportParameters.getBookingStatus(), accountReference, accountConsent.getId(), aspspConsentDataProvider.loadAspspConsentData());


        List<SpiTransaction> transactions = BookingStatus.INFORMATION == spiTransactionReportParameters.getBookingStatus() ?
                                                buildSpiInformationTransactionList() :
                                                buildSpiTransactionList();

        return SpiResponse.<SpiTransactionReport>builder()
                   .payload(new SpiTransactionReport("dGVzdA==", transactions, Collections.singletonList(buildSpiAccountBalance()), "application/json", null))
                   .build();
    }

    @Override
    public SpiResponse<SpiTransaction> requestTransactionForAccountByTransactionId(@NotNull SpiContextData contextData, @NotNull String transactionId, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#requestTransactionForAccountByTransactionId: contextData {}, aspspConsent-id {}, accountReference {}, accountConsent-id {}, aspspConsentData {}", contextData, transactionId, accountReference, accountConsent.getId(), aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiTransaction>builder()
                   .payload(buildSpiTransactionById("0001"))
                   .build();
    }

    @Override
    public SpiResponse<List<SpiAccountBalance>> requestBalancesForAccount(@NotNull SpiContextData contextData, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#requestBalancesForAccount: contextData {}, accountReference {}, accountConsent-id {}, aspspConsentData {}", contextData, accountReference, accountConsent.getId(), aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<List<SpiAccountBalance>>builder()
                   .payload(Collections.singletonList(buildSpiAccountBalance()))
                   .build();
    }

    @Override
    public SpiResponse<SpiTransactionsDownloadResponse> requestTransactionsByDownloadLink(@NotNull SpiContextData contextData, @NotNull SpiAccountConsent accountConsent, @NotNull String downloadId, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#requestTransactionsByDownloadLink: contextData {}, accountConsent-ID {}, download-ID {}, aspspConsentData {}", contextData, accountConsent.getId(), downloadId, aspspConsentDataProvider.loadAspspConsentData());

        String testTransactionData = jsonReader.getStringFromFile("account-access-test-file.json");

        InputStream stream = new ByteArrayInputStream(testTransactionData.getBytes());
        SpiTransactionsDownloadResponse response = new SpiTransactionsDownloadResponse(stream,
                                                                                       "transactions_" + new Date().getTime() + ".json",
                                                                                       testTransactionData.getBytes().length);
        return SpiResponse.<SpiTransactionsDownloadResponse>builder()
                   .payload(response)
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

    private List<SpiTransaction> buildSpiInformationTransactionList() {
        List<SpiTransaction> transactions = new ArrayList<>();
        transactions.add(buildInformationSpiTransaction());
        return transactions;
    }

    private List<SpiTransaction> buildSpiTransactionList() {
        List<SpiTransaction> transactions = new ArrayList<>();
        transactions.add(buildSpiTransactionById("0001"));
        transactions.add(buildSpiTransactionById("0002"));
        transactions.add(buildSpiTransactionById("0003"));
        return transactions;
    }

    private SpiTransaction buildSpiTransactionById(String transactionId) {
        Remittance remittanceInformationStructured = buildRemittance();
        return new SpiTransaction(transactionId, "", "", "", "", "aspsp", LocalDate.of(2019, Month.JANUARY, 4),
                                  LocalDate.of(2019, Month.JANUARY, 4), new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(200)), Collections.emptyList(),
                                  NAME, buildSpiAccountReference(), NAME, NAME, NAME, buildSpiAccountReference(),
                                  NAME, NAME, "", Collections.singletonList("remittance information unstructured"),
                                  remittanceInformationStructured, Collections.singletonList(remittanceInformationStructured), "", "",
                                  "some additional information", null, null, buildSpiAccountBalance());
    }

    private SpiTransaction buildInformationSpiTransaction() {
        SpiStandingOrderDetails standingOrderDetails = new SpiStandingOrderDetails(LocalDate.of(2021, Month.JANUARY, 4),
                                                                                   LocalDate.of(2021, Month.MARCH, 12),
                                                                                   PisExecutionRule.PRECEDING, null,
                                                                                   FrequencyCode.MONTHLY, null, null, PisDayOfExecution._24, null);

        SpiAdditionalInformationStructured additionalInformationStructured = new SpiAdditionalInformationStructured(standingOrderDetails);
        Remittance remittanceInformationStructured = buildRemittance();
        return new SpiTransaction(null, null, null, null, null,
                                  null, null, null, null, null,
                                  "John Miles", buildSpiAccountReference(), null, null,
                                  null, null, null,
                                  "", null, null,
                                  remittanceInformationStructured, Collections.singletonList(remittanceInformationStructured),
                                  null, null, null,
                                  null, additionalInformationStructured, buildSpiAccountBalance());
    }

    private Remittance buildRemittance() {
        Remittance remittance = new Remittance();
        remittance.setReference("PMNT-ICDT-STDO");
        return remittance;
    }

    private SpiAccountReference buildSpiAccountReference() {
        return new SpiAccountReference(ASPSP_ACCOUNT_ID, RESOURCE_ID, IBAN,
                                       "52500105173911841934", "AEYPM5403H", "PM5403H****", null, Currency.getInstance("EUR"), null);
    }
}
