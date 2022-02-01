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
import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
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

    private final JsonReader jsonReader = new JsonReader();

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

        SpiTransactionLinks spiTransactionLinks = buildSpiTransactionLinks();
        return SpiResponse.<SpiTransactionReport>builder()
                   .payload(new SpiTransactionReport("dGVzdA==", transactions, Collections.singletonList(buildSpiAccountBalance()), "application/json", null, spiTransactionLinks, 1))
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
        return new SpiTransaction(transactionId, "", "", "", "", "aspsp", LocalDate.of(2019, Month.JANUARY, 4),
                                  LocalDate.of(2019, Month.JANUARY, 4), new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(200)), Collections.emptyList(),
                                  buildSpiTransactionInfo(), "",
                                  "some additional information", null, null, buildSpiAccountBalance(),
                                  false, 10, Collections.singletonList(buildSpiEntryDetails()));
    }

    private SpiEntryDetails buildSpiEntryDetails() {
        return new SpiEntryDetails("endToEndId", "mandateId", "checkId", "creditorId",
                                   new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(345)),
                                   Collections.emptyList(), buildSpiTransactionInfo());
    }

    private SpiTransactionInfo buildSpiTransactionInfo() {
        return new SpiTransactionInfo("creditorName", buildSpiAccountReference(),
                                      "creditorAgent", "ultimateCreditor",
                                      "debtorName", buildSpiAccountReference(),
                                      "debtorAgent", "ultimateDebtor",
                                      "remittanceInformationUnstructured",
                                      Collections.singletonList("remittanceInformationUnstructuredArray"),
                                      "remittanceInformationStructured",
                                      Collections.singletonList("remittanceInformationStructuredArray"),
                                      "CDCB");
    }

    private SpiTransaction buildInformationSpiTransaction() {
        SpiStandingOrderDetails standingOrderDetails = new SpiStandingOrderDetails(LocalDate.of(2021, Month.JANUARY, 4),
                                                                                   LocalDate.of(2021, Month.MARCH, 12),
                                                                                   PisExecutionRule.PRECEDING, null,
                                                                                   FrequencyCode.MONTHLY, null, null, PisDayOfExecution.DAY_24, null);

        SpiAdditionalInformationStructured additionalInformationStructured = new SpiAdditionalInformationStructured(standingOrderDetails);
        return new SpiTransaction(null, null, null, null, null,
                                  null, null, null, null, null,
                                  buildSpiTransactionInfo(), null, null,
                                  null, additionalInformationStructured, buildSpiAccountBalance(),
                                  false, 15, Collections.singletonList(buildSpiEntryDetails()));
    }

    private SpiAccountReference buildSpiAccountReference() {
        return new SpiAccountReference(ASPSP_ACCOUNT_ID, RESOURCE_ID, IBAN,
                                       "52500105173911841934", "AEYPM5403H", "PM5403H****", null, Currency.getInstance("EUR"), null);
    }

    private SpiTransactionLinks buildSpiTransactionLinks() {
        return new SpiTransactionLinks(
            "http://localhost:8089/v1/accounts/account-id/transactions?pageIndex=0&itemsPerPage=20",
            "http://localhost:8089/v1/accounts/account-id/transactions?pageIndex=3&itemsPerPage=20",
            "http://localhost:8089/v1/accounts/account-id/transactions?pageIndex=1&itemsPerPage=20",
            "http://localhost:8089/v1/accounts/account-id/transactions?pageIndex=7&itemsPerPage=20"
        );
    }
}
