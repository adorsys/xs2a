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

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.BalanceType;
import de.adorsys.psd2.xs2a.domain.CashAccountType;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.*;
import de.adorsys.psd2.xs2a.domain.account.AccountStatus;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.service.ais.*;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.TransactionModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.TrustedBeneficiariesModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.web.controller.util.RequestUriHandler;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String DOWNLOAD_ID = "dGVzdA==";
    private static final String TEST_JSON_FILENAME = "test.json";
    private static final String ACCOUNT_ID = "33333-999999999";
    private static final String WRONG_ACCOUNT_ID = "Wrong account id";
    private static final String CONSENT_ID = "12345";
    private static final String ACCOUNT_DETAILS_LIST_JSON = "json/web/controller/AccountDetailsList.json";
    private static final String ACCOUNT_REPORT_JSON = "json/web/controller/AccountReportTestData.json";
    private static final String BALANCES_JSON = "json/web/controller/ReadBalanceResponse.json";
    private static final String BENEFICIARIES_JSON = "json/service/mapper/trusted-beneficiaries-model-mapper/trusted-beneficiaries-list.json";
    private static final String XS2A_BENEFICIARIES_JSON = "json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-trusted-beneficiaries-list.json";
    private static final String TRANSACTIONS_JSON = "json/web/controller/transactions.json";
    private static final String REQUEST_URI = "/accounts";
    private static final String REQUEST_URI_ENRICHED = "/transactions?bookingStatus=pending&pageIndex=4";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String URI = "request.uri";
    private static final MessageError MESSAGE_ERROR_AIS_404 = new MessageError(ErrorType.AIS_404, of(MessageErrorCode.RESOURCE_UNKNOWN_404));
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    public static final String PENDING_BOOKING_STATUS = "pending";
    public static final Integer PAGE_INDEX = 4;

    @InjectMocks
    private AccountController accountController;

    @Mock
    private BalanceService balanceService;
    @Mock
    private AccountListService accountListService;
    @Mock
    private AccountDetailsService accountDetailsService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private AccountModelMapper accountModelMapper;
    @Mock
    private TransactionModelMapper transactionModelMapper;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletOutputStream servletOutputStream;
    @Mock
    private ResponseErrorMapper responseErrorMapper;
    @Mock
    private TppErrorMessageWriter tppErrorMessageWriter;
    @Mock
    private TrustedBeneficiariesModelMapper trustedBeneficiariesModelMapper;
    @Mock
    private TrustedBeneficiariesService trustedBeneficiariesService;
    @Mock
    private RequestUriHandler requestUriHandler;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void getAccountDetails_withBalance() {
        // Given
        when(accountDetailsService.getAccountDetails(anyString(), any(), anyBoolean(), anyString())).thenReturn(getXs2aAccountDetailsHolder());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);

        boolean withBalance = true;
        ResponseObject<AccountDetails> expectedResult = getAccountDetails();

        doReturn(new ResponseEntity<>(getAccountDetails().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        // When
        AccountDetails result = (AccountDetails) accountController.readAccountDetails(ACCOUNT_ID, null,
                                                                                      CONSENT_ID, withBalance, null, null, null, null,
                                                                                      null, null, null, null, null,
                                                                                      null, null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult.getBody());
    }

    @Test
    void readAccountDetails_wrongId_fail() {
        // Given
        when(accountDetailsService.getAccountDetails(anyString(), any(), anyBoolean(), anyString())).thenReturn(getXs2aAccountDetailsHolder());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);

        boolean withBalance = true;
        ResponseObject<Xs2aAccountDetailsHolder> responseEntity = buildXs2aAccountDetailsWithError();
        when(accountDetailsService.getAccountDetails(WRONG_CONSENT_ID, WRONG_ACCOUNT_ID, withBalance, REQUEST_URI))
            .thenReturn(responseEntity);
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity<?> result = accountController.readAccountDetails(WRONG_ACCOUNT_ID, null, WRONG_CONSENT_ID, withBalance, null,
                                                                        null, null, null, null,
                                                                        null, null, null, null,
                                                                        null, null, null, null);
        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAccounts_ResultTest() {
        // Given
        when(accountListService.getAccountList(anyString(), anyBoolean(), anyString())).thenReturn(getXs2aAccountListHolder());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);

        boolean withBalance = true;
        AccountList expectedResult = createAccountDetailsList().getBody();

        doReturn(new ResponseEntity<>(createAccountDetailsList().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        // When
        AccountList result = (AccountList) accountController.getAccountList(null, CONSENT_ID, withBalance,
                                                                            null, null, null, null, null, null,
                                                                            null, null, null, null, null,
                                                                            null, null).getBody();

        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getAccountList_wrongId_fail() {
        // Given
        when(accountListService.getAccountList(anyString(), anyBoolean(), anyString())).thenReturn(getXs2aAccountListHolder());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);

        boolean withBalance = true;
        ResponseObject<Xs2aAccountListHolder> responseEntity = getXs2aAccountListHolderWithError();

        when(accountListService.getAccountList(WRONG_CONSENT_ID, withBalance, REQUEST_URI))
            .thenReturn(responseEntity);

        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity<?> result = accountController.getAccountList(null, WRONG_CONSENT_ID, withBalance,
                                                                    null, null, null, null, null, null,
                                                                    null, null, null, null, null,
                                                                    null, null);
        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getBalances_ResultTest() {
        // Given
        when(balanceService.getBalancesReport(anyString(), anyString(), anyString())).thenReturn(getBalanceReport());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);

        ReadAccountBalanceResponse200 expectedResult = jsonReader.getObjectFromFile(BALANCES_JSON, ReadAccountBalanceResponse200.class);

        doReturn(new ResponseEntity<>(createReadBalances().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        // When
        ReadAccountBalanceResponse200 result = (ReadAccountBalanceResponse200) accountController.getBalances(ACCOUNT_ID,
                                                                                                             null, CONSENT_ID, null, null, null, null,
                                                                                                             null, null, null, null, null,
                                                                                                             null, null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getBalances_wrongId_fail() {
        // Given
        when(balanceService.getBalancesReport(anyString(), anyString(), anyString())).thenReturn(getBalanceReport());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);

        ResponseObject<Xs2aBalancesReport> responseEntity = buildBalanceReportWithError();
        when(balanceService.getBalancesReport(WRONG_CONSENT_ID, WRONG_ACCOUNT_ID, REQUEST_URI))
            .thenReturn(responseEntity);
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity<?> result = accountController.getBalances(WRONG_ACCOUNT_ID, null, WRONG_CONSENT_ID, null,
                                                                 null, null, null, null, null,
                                                                 null, null, null,
                                                                 null, null, null, null);
        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTrustedBeneficiaries_Fail() {
        // Given
        ResponseObject<Xs2aTrustedBeneficiariesList> xs2aResponse = ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                                                                        .fail(MESSAGE_ERROR_AIS_404)
                                                                        .build();

        when(trustedBeneficiariesService.getTrustedBeneficiaries(WRONG_CONSENT_ID, WRONG_ACCOUNT_ID, REQUEST_URI)).thenReturn(xs2aResponse);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity<?> actual = accountController.listOfTrustedBeneficiaries(null, WRONG_CONSENT_ID, WRONG_ACCOUNT_ID, null, null);
        // Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTrustedBeneficiaries_ResultTest() {
        // Given
        Xs2aTrustedBeneficiariesList xs2aBeneficiaries = jsonReader.getObjectFromFile(XS2A_BENEFICIARIES_JSON, Xs2aTrustedBeneficiariesList.class);
        ResponseObject<Xs2aTrustedBeneficiariesList> xs2aResponse = ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                                                                        .body(xs2aBeneficiaries)
                                                                        .build();

        when(trustedBeneficiariesService.getTrustedBeneficiaries(CONSENT_ID, ACCOUNT_ID, REQUEST_URI)).thenReturn(xs2aResponse);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);

        TrustedBeneficiariesList expected = jsonReader.getObjectFromFile(BENEFICIARIES_JSON, TrustedBeneficiariesList.class);

        doReturn(new ResponseEntity<>(expected, HttpStatus.OK)).when(responseMapper).ok(any(), any());

        // When
        TrustedBeneficiariesList actual = (TrustedBeneficiariesList) accountController.listOfTrustedBeneficiaries(null, CONSENT_ID, ACCOUNT_ID, null, null).getBody();
        // Then
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    void getTransactions_ResultTest() {
        // Given
        AccountReport expectedResult = jsonReader.getObjectFromFile(ACCOUNT_REPORT_JSON, AccountReport.class);
        when(request.getRequestURI()).thenReturn(URI);
        when(requestUriHandler.handleTransactionUri(URI, PENDING_BOOKING_STATUS, PAGE_INDEX)).thenReturn(REQUEST_URI_ENRICHED);
        doReturn(new ResponseEntity<>(createAccountReport().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null));

        doReturn(ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build())
            .when(transactionService).getTransactionsReportByPeriod(any(Xs2aTransactionsReportByPeriodRequest.class));

        // When
        AccountReport result = (AccountReport) accountController.getTransactionList(ACCOUNT_ID, PENDING_BOOKING_STATUS,
                                                                                    null, CONSENT_ID, null, null, "both", false,
                                                                                    false, PAGE_INDEX, null, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null).getBody();

        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getTransactionList_success() {
        // Given
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.handleTransactionUri(REQUEST_URI, PENDING_BOOKING_STATUS, PAGE_INDEX)).thenReturn(REQUEST_URI_ENRICHED);

        AccountReport expectedResult = jsonReader.getObjectFromFile(ACCOUNT_REPORT_JSON, AccountReport.class);

        doReturn(new ResponseEntity<>(buildAccountReportWithError().getBody(), HttpStatus.OK))
            .when(responseErrorMapper).generateErrorResponse(MESSAGE_ERROR_AIS_404);
        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null));
        doReturn(ResponseObject.<Xs2aTransactionsReport>builder().fail(MESSAGE_ERROR_AIS_404).body(transactionsReport).build())
            .when(transactionService).getTransactionsReportByPeriod(any(Xs2aTransactionsReportByPeriodRequest.class));


        // When
        AccountReport result = (AccountReport) accountController.getTransactionList(ACCOUNT_ID, PENDING_BOOKING_STATUS,
                                                                                    null, CONSENT_ID, null, null, "both", false,
                                                                                    false, PAGE_INDEX, null, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getTransactionList_isRespContentTypeJSON_success() {
        // Given
        AccountReport expectedResult = jsonReader.getObjectFromFile(ACCOUNT_REPORT_JSON, AccountReport.class);
        when(request.getRequestURI()).thenReturn(URI);
        when(requestUriHandler.handleTransactionUri(URI, PENDING_BOOKING_STATUS, PAGE_INDEX)).thenReturn(REQUEST_URI_ENRICHED);
        doReturn(new ResponseEntity<>(createAccountReport().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null));
        transactionsReport.setResponseContentType("application/json");
        doReturn(ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build())
            .when(transactionService).getTransactionsReportByPeriod(any(Xs2aTransactionsReportByPeriodRequest.class));

        // When
        AccountReport result = (AccountReport) accountController.getTransactionList(ACCOUNT_ID, PENDING_BOOKING_STATUS,
                                                                                    null, CONSENT_ID, null, null, "both", false,
                                                                                    false, PAGE_INDEX, null, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getTransactionDetails_success() {
        // Given
        when(transactionService.getTransactionDetails(eq(CONSENT_ID), eq(ACCOUNT_ID), any(), eq(REQUEST_URI))).thenReturn(buildTransaction());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);

        doReturn(new ResponseEntity<>(createAccountReport().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        AccountReport expectedResult = jsonReader.getObjectFromFile(ACCOUNT_REPORT_JSON, AccountReport.class);


        // When
        AccountReport result = (AccountReport) accountController.getTransactionDetails(ACCOUNT_ID, null,
                                                                                       null, CONSENT_ID, null, null, null, null, null,
                                                                                       null, null, null, null, null,
                                                                                       null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getTransactionDetails_error() {
        // Given
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(requestUriHandler.trimEndingSlash(REQUEST_URI)).thenReturn(REQUEST_URI);

        when(transactionService.getTransactionDetails(eq(CONSENT_ID), eq(ACCOUNT_ID), any(), eq(REQUEST_URI))).thenReturn(buildTransactionWithError());
        doReturn(new ResponseEntity<>(buildAccountReportWithError().getBody(), HttpStatus.OK))
            .when(responseErrorMapper).generateErrorResponse(MESSAGE_ERROR_AIS_404);

        AccountReport expectedResult = jsonReader.getObjectFromFile(ACCOUNT_REPORT_JSON, AccountReport.class);

        // When
        AccountReport result = (AccountReport) accountController.getTransactionDetails(ACCOUNT_ID, null,
                                                                                       null, CONSENT_ID, null, null, null, null, null,
                                                                                       null, null, null, null, null,
                                                                                       null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void downloadTransactions_success() throws IOException {
        // Given
        when(transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID)).thenReturn(buildTransactionDownloadResponseOk());
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        // When
        accountController.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        // Then
        verify(response, times(1)).addHeader(anyString(), anyString());
        assertEquals(0, response.getBufferSize());
    }

    @Test
    void downloadTransactions_spiError() throws IOException {
        // Given

        when(transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID)).thenReturn(buildTransactionDownloadResponseError());

        // When
        accountController.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        // Then
        verify(response, times(1)).flushBuffer();
        assertEquals(0, response.getBufferSize());
    }

    @Test
    void downloadTransactions_spiErrorCustomMessage() throws IOException {
        // Given
        MessageError messageError = new MessageError(ErrorType.AIS_400, TppMessageInformation.buildWithCustomError(FORMAT_ERROR, "message from SPI"));
        ArgumentCaptor<MessageError> messageErrorArgumentCaptor = ArgumentCaptor.forClass(MessageError.class);

        when(transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID)).thenReturn(buildTransactionDownloadResponseError(messageError));

        // When
        accountController.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        // Then
        verify(tppErrorMessageWriter).writeError(any(HttpServletResponse.class), messageErrorArgumentCaptor.capture());
        assertEquals(messageError, messageErrorArgumentCaptor.getValue());
        verify(response, times(1)).flushBuffer();
        assertEquals(0, response.getBufferSize());
    }

    private ResponseObject<Xs2aAccountListHolder> getXs2aAccountListHolder() {
        List<Xs2aAccountDetails> accountDetails = Collections.singletonList(
            new Xs2aAccountDetails(ASPSP_ACCOUNT_ID, "33333-999999999", "DE371234599997", null, null, null,
                                   null, CURRENCY, "Schmidt", "Display name", null,
                                   CashAccountType.CACC, AccountStatus.ENABLED, "GENODEF1N02", "", Xs2aUsageType.PRIV, "", null, null, null));
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(accountDetails, null);
        return ResponseObject.<Xs2aAccountListHolder>builder()
                   .body(xs2aAccountListHolder).build();
    }

    private ResponseObject<AccountList> createAccountDetailsList() {
        AccountList details = jsonReader.getObjectFromFile(ACCOUNT_DETAILS_LIST_JSON, AccountList.class);

        return ResponseObject.<AccountList>builder()
                   .body(details).build();
    }

    private ResponseObject<Xs2aAccountDetailsHolder> getXs2aAccountDetailsHolder() {
        List<Xs2aAccountDetails> accountDetailsList = getXs2aAccountListHolder().getBody().getAccountDetails();
        Xs2aAccountDetailsHolder xs2aAccountDetailsHolder = new Xs2aAccountDetailsHolder(accountDetailsList.get(0), null);
        return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                   .body(xs2aAccountDetailsHolder).build();
    }

    private ResponseObject<Xs2aAccountDetailsHolder> buildXs2aAccountDetailsWithError() {
        return ResponseObject.<Xs2aAccountDetailsHolder>builder().fail(AccountControllerTest.MESSAGE_ERROR_AIS_404)
                   .build();
    }

    private ResponseObject<Xs2aAccountListHolder> getXs2aAccountListHolderWithError() {
        return ResponseObject.<Xs2aAccountListHolder>builder().fail(AccountControllerTest.MESSAGE_ERROR_AIS_404)
                   .build();
    }

    private ResponseObject<AccountDetails> getAccountDetails() {
        AccountDetails details = createAccountDetailsList().getBody().getAccounts().get(0);
        return ResponseObject.<AccountDetails>builder()
                   .body(details).build();
    }

    private ResponseObject<AccountReport> createAccountReport() {
        AccountReport accountReport = jsonReader.getObjectFromFile(ACCOUNT_REPORT_JSON, AccountReport.class);

        return ResponseObject.<AccountReport>builder()
                   .body(accountReport).build();
    }

    private ResponseObject<AccountReport> buildAccountReportWithError() {
        AccountReport accountReport = jsonReader.getObjectFromFile(ACCOUNT_REPORT_JSON, AccountReport.class);

        return ResponseObject.<AccountReport>builder()
                   .fail(AccountControllerTest.MESSAGE_ERROR_AIS_404)
                   .body(accountReport).build();
    }

    private ResponseObject<ReadAccountBalanceResponse200> createReadBalances() {
        ReadAccountBalanceResponse200 read = jsonReader.getObjectFromFile(BALANCES_JSON, ReadAccountBalanceResponse200.class);
        return ResponseObject.<ReadAccountBalanceResponse200>builder()
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
                   .fail(AccountControllerTest.MESSAGE_ERROR_AIS_404)
                   .body(balancesReport).build();
    }

    private ResponseObject<Transactions> buildTransaction() {
        Transactions transactions = jsonReader.getObjectFromFile(TRANSACTIONS_JSON, Transactions.class);
        return ResponseObject.<Transactions>builder()
                   .body(transactions).build();
    }

    private ResponseObject<Transactions> buildTransactionWithError() {
        return ResponseObject.<Transactions>builder()
                   .fail(AccountControllerTest.MESSAGE_ERROR_AIS_404)
                   .build();
    }

    private ResponseObject<Xs2aTransactionsDownloadResponse> buildTransactionDownloadResponseOk() {
        InputStream testStream = new ByteArrayInputStream("test".getBytes());
        Xs2aTransactionsDownloadResponse response = new Xs2aTransactionsDownloadResponse();
        response.setTransactionStream(testStream);
        response.setDataFileName(TEST_JSON_FILENAME);
        response.setDataSizeBytes(10000);
        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .body(response)
                   .build();
    }

    private ResponseObject<Xs2aTransactionsDownloadResponse> buildTransactionDownloadResponseError() {
        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .fail(new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR)))
                   .build();
    }


    private ResponseObject<Xs2aTransactionsDownloadResponse> buildTransactionDownloadResponseError(MessageError messageError) {
        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .fail(messageError)
                   .build();
    }
}
