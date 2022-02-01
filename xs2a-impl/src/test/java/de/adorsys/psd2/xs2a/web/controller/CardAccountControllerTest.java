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

package de.adorsys.psd2.xs2a.web.controller;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.CardAccountDetails;
import de.adorsys.psd2.model.CardAccountList;
import de.adorsys.psd2.model.CardAccountReport;
import de.adorsys.psd2.model.ReadCardAccountBalanceResponse200;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.BalanceType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.service.ais.CardAccountBalanceService;
import de.adorsys.psd2.xs2a.service.ais.CardAccountService;
import de.adorsys.psd2.xs2a.service.ais.CardTransactionService;
import de.adorsys.psd2.xs2a.service.mapper.CardAccountModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardAccountControllerTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String ACCOUNT_ID = "33333-999999999";
    private static final String WRONG_ACCOUNT_ID = "Wrong account id";
    private static final String CONSENT_ID = "12345";
    private static final String CARD_ACCOUNT_DETAILS_LIST_SOURCE = "/json/CardAccountDetailsList.json";
    private static final String TRANSACTION_LIST_REPORT_SOURCE = "/json/CardTransactionList.json";
    private static final String CARD_BALANCES_SOURCE = "/json/ReadCardBalanceResponse.json";
    private static final String REQUEST_URI = "/accounts";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final MessageError MESSAGE_ERROR_AIS_404 = new MessageError(ErrorType.AIS_404, of(MessageErrorCode.RESOURCE_UNKNOWN_404));
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    @InjectMocks
    private CardAccountController cardAccountController;

    private final Xs2aObjectMapper xs2aObjectMapper = (Xs2aObjectMapper) new Xs2aObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private CardAccountBalanceService cardAccountBalanceService;
    @Mock
    private CardAccountService cardAccountService;
    @Mock
    private CardTransactionService cardTransactionService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private CardAccountModelMapper cardAccountModelMapper;
    @Mock
    private HttpServletRequest request;
    @Mock
    private ResponseErrorMapper responseErrorMapper;

    @Test
    void readCardAccountDetails_withBalance_ok() throws IOException {
        // Given
        when(cardAccountService.getCardAccountDetails(anyString(), any(), anyString())).thenReturn(getXs2aCardAccountDetailsHolder());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        ResponseObject<CardAccountDetails> expectedResult = getCardAccountDetails();

        doReturn(new ResponseEntity<>(getCardAccountDetails().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        // When
        CardAccountDetails result = (CardAccountDetails) cardAccountController.readCardAccountDetails(ACCOUNT_ID, null,
                                                                                               CONSENT_ID, null, null, null, null,
                                                                                               null, null, null, null, null,
                                                                                               null, null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult.getBody());
    }

    @Test
    void readCardAccountDetails_wrongId_fail() {
        // Given
        when(cardAccountService.getCardAccountDetails(anyString(), any(), anyString())).thenReturn(getXs2aCardAccountDetailsHolder());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        ResponseObject<Xs2aCardAccountDetailsHolder> responseEntity = buildXs2aCardAccountDetailsWithError();
        when(cardAccountService.getCardAccountDetails(WRONG_CONSENT_ID, WRONG_ACCOUNT_ID, REQUEST_URI))
            .thenReturn(responseEntity);
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity<?> result = cardAccountController.readCardAccountDetails(WRONG_ACCOUNT_ID, null, WRONG_CONSENT_ID, null,
                                                                         null, null, null, null,
                                                                         null, null, null, null,
                                                                         null, null, null, null);
        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAccountList_ok() throws IOException {
        // Given
        when(cardAccountService.getCardAccountList(anyString(), anyString())).thenReturn(getXs2aCardAccountListHolder());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        CardAccountList expectedResult = createCardAccountDetailsList().getBody();

        doReturn(new ResponseEntity<>(createCardAccountDetailsList().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        // When
        CardAccountList result = (CardAccountList) cardAccountController.getCardAccountList(null, CONSENT_ID,
                                                                                        null, null, null, null, null, null,
                                                                                        null, null, null, null, null,
                                                                                        null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getAccountList_wrongId_fail() {
        // Given
        when(cardAccountService.getCardAccountList(anyString(), anyString())).thenReturn(getXs2aCardAccountListHolder());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        ResponseObject<Xs2aCardAccountListHolder> responseEntity = getXs2aCardAccountListHolderWithError();

        when(cardAccountService.getCardAccountList(WRONG_CONSENT_ID, REQUEST_URI))
            .thenReturn(responseEntity);

        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity<?> result = cardAccountController.getCardAccountList(null, WRONG_CONSENT_ID,
                                                                        null, null, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null, null);
        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getBalances_ok() throws IOException {
        // Given
        when(cardAccountBalanceService.getBalancesReport(anyString(), anyString(), anyString())).thenReturn(getBalanceReport());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        ReadCardAccountBalanceResponse200 expectedResult = xs2aObjectMapper.readValue(IOUtils.resourceToString(CARD_BALANCES_SOURCE, UTF_8),
                                                                                      ReadCardAccountBalanceResponse200.class);

        doReturn(new ResponseEntity<>(createReadCardBalances().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        // When
        ReadCardAccountBalanceResponse200 result = (ReadCardAccountBalanceResponse200) cardAccountController.getCardAccountBalances(ACCOUNT_ID,
                                                                                                                                    null, CONSENT_ID, null, null, null, null,
                                                                                                                                    null, null, null, null, null,
                                                                                                                                    null, null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getBalances_wrongId_fail() {
        // Given
        when(cardAccountBalanceService.getBalancesReport(anyString(), anyString(), anyString())).thenReturn(getBalanceReport());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        ResponseObject<Xs2aBalancesReport> responseEntity = buildBalanceReportWithError();

        when(cardAccountBalanceService.getBalancesReport(WRONG_CONSENT_ID, WRONG_ACCOUNT_ID, REQUEST_URI))
            .thenReturn(responseEntity);
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity<?> result = cardAccountController.getCardAccountBalances(WRONG_ACCOUNT_ID, null, WRONG_CONSENT_ID, null,
                                                                                null, null, null, null, null,
                                                                                null, null, null,
                                                                                null, null, null, null);
        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    void getTransactionList_ok() throws IOException {
        // Given
        CardAccountReport expectedResult = xs2aObjectMapper.readValue(IOUtils.resourceToString(TRANSACTION_LIST_REPORT_SOURCE, UTF_8),
                                                                      CardAccountReport.class);

        doReturn(new ResponseEntity<>(getCardTransactionReport().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        Xs2aCardTransactionsReport transactionsReport = new Xs2aCardTransactionsReport();
        transactionsReport.setCardAccountReport(new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null));

        doReturn(ResponseObject.<Xs2aCardTransactionsReport>builder().body(transactionsReport).build())
            .when(cardTransactionService).getCardTransactionsReportByPeriod(any(Xs2aCardTransactionsReportByPeriodRequest.class));

        // When
        CardAccountReport result = (CardAccountReport) cardAccountController.getCardAccountTransactionList(ACCOUNT_ID, "pending",
                                                                                                           null, CONSENT_ID, null, null, "both", false,
                                                                                                           null, null, null, null, null,
                                                                                                           null, null, null, null, null,
                                                                                                           null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    private ResponseObject<Xs2aCardAccountListHolder> getXs2aCardAccountListHolder() {
        Xs2aAmount creditLimit = new Xs2aAmount();
        creditLimit.setCurrency(Currency.getInstance("EUR"));
        creditLimit.setAmount("10000");

        List<Xs2aCardAccountDetails> accountDetails = Collections.singletonList(
            new Xs2aCardAccountDetails(ASPSP_ACCOUNT_ID, "33333-999999999", "DE371234599997", null, null, null,
                                       null, null, AccountStatus.ENABLED, Xs2aUsageType.PRIV, "details",
                                       null, creditLimit, null, null));
        Xs2aCardAccountListHolder xs2aAccountListHolder = new Xs2aCardAccountListHolder(accountDetails, null);
        return ResponseObject.<Xs2aCardAccountListHolder>builder()
                   .body(xs2aAccountListHolder).build();
    }

    private ResponseObject<CardAccountList> createCardAccountDetailsList() throws IOException {
        CardAccountList details = xs2aObjectMapper.readValue(IOUtils.resourceToString(CardAccountControllerTest.CARD_ACCOUNT_DETAILS_LIST_SOURCE, UTF_8), CardAccountList.class);
        return ResponseObject.<CardAccountList>builder()
                   .body(details).build();
    }

    private ResponseObject<Xs2aCardAccountDetailsHolder> getXs2aCardAccountDetailsHolder() {
        List<Xs2aCardAccountDetails> cardAccountDetailsList = getXs2aCardAccountListHolder().getBody().getCardAccountDetails();
        Xs2aCardAccountDetailsHolder xs2aCardAccountDetailsHolder = new Xs2aCardAccountDetailsHolder(cardAccountDetailsList.get(0), null);
        return ResponseObject.<Xs2aCardAccountDetailsHolder>builder()
                   .body(xs2aCardAccountDetailsHolder).build();
    }

    private ResponseObject<Xs2aCardAccountDetailsHolder> buildXs2aCardAccountDetailsWithError() {
        return ResponseObject.<Xs2aCardAccountDetailsHolder>builder().fail(CardAccountControllerTest.MESSAGE_ERROR_AIS_404)
                   .build();
    }

    private ResponseObject<Xs2aCardAccountListHolder> getXs2aCardAccountListHolderWithError() {
        return ResponseObject.<Xs2aCardAccountListHolder>builder().fail(CardAccountControllerTest.MESSAGE_ERROR_AIS_404)
                   .build();
    }

    private ResponseObject<CardAccountDetails> getCardAccountDetails() throws IOException {
        CardAccountDetails details = createCardAccountDetailsList().getBody().getCardAccounts().get(0);
        return ResponseObject.<CardAccountDetails>builder()
                   .body(details).build();
    }

    private ResponseObject<CardAccountReport> getCardTransactionReport() throws IOException {
        CardAccountReport accountReport = xs2aObjectMapper.readValue(IOUtils.resourceToString(CardAccountControllerTest.TRANSACTION_LIST_REPORT_SOURCE, UTF_8),
                                                                     CardAccountReport.class);

        return ResponseObject.<CardAccountReport>builder()
                   .body(accountReport).build();
    }

    private ResponseObject<ReadCardAccountBalanceResponse200> createReadCardBalances() throws IOException {
        ReadCardAccountBalanceResponse200 read = xs2aObjectMapper.readValue(IOUtils.resourceToString(CARD_BALANCES_SOURCE, UTF_8),
                                                                            ReadCardAccountBalanceResponse200.class);
        return ResponseObject.<ReadCardAccountBalanceResponse200>builder()
                   .body(read).build();
    }

    private ResponseObject<List<Xs2aBalance>> getXs2aBalances() {
        Xs2aBalance balance = new Xs2aBalance();
        Xs2aAmount amount = new Xs2aAmount();
        amount.setAmount("300.45");
        amount.setCurrency(CURRENCY);
        balance.setBalanceAmount(amount);
        balance.setBalanceType(BalanceType.INTERIM_AVAILABLE);
        balance.setLastChangeDateTime(LocalDateTime.of(2018, 3, 31, 15, 16,
                                                       16, 374));
        balance.setReferenceDate(LocalDate.of(2018, 3, 29));
        balance.setLastCommittedTransaction("abc");
        List<Xs2aBalance> balances = Collections.singletonList(balance);
        return ResponseObject.<List<Xs2aBalance>>builder().body(balances).build();
    }

    private ResponseObject<Xs2aBalancesReport> getBalanceReport() {
        Xs2aBalancesReport balancesReport = new Xs2aBalancesReport();
        balancesReport.setBalances(getXs2aBalances().getBody());
        return ResponseObject.<Xs2aBalancesReport>builder().body(balancesReport).build();
    }

    private ResponseObject<Xs2aBalancesReport> buildBalanceReportWithError() {
        Xs2aBalancesReport balancesReport = new Xs2aBalancesReport();
        balancesReport.setBalances(getXs2aBalances().getBody());

        return ResponseObject.<Xs2aBalancesReport>builder()
                   .fail(CardAccountControllerTest.MESSAGE_ERROR_AIS_404)
                   .body(balancesReport)
                   .build();
    }
}
