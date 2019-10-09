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

package de.adorsys.psd2.xs2a.web.controller;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.AccountDetails;
import de.adorsys.psd2.model.AccountList;
import de.adorsys.psd2.model.AccountReport;
import de.adorsys.psd2.model.ReadAccountBalanceResponse200;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.PurposeCode;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.*;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.domain.code.BankTransactionCode;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ais.AccountDetailsService;
import de.adorsys.psd2.xs2a.service.ais.AccountListService;
import de.adorsys.psd2.xs2a.service.ais.BalanceService;
import de.adorsys.psd2.xs2a.service.ais.TransactionService;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageBuilder;
import de.adorsys.psd2.xs2a.web.filter.TppErrorMessage;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CERTIFICATE_EXPIRED;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.exception.MessageCategory.ERROR;
import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String DOWNLOAD_ID = "dGVzdA==";
    private static final String TEST_JSON_FILENAME = "test.json";
    private static final String ACCOUNT_ID = "33333-999999999";
    private static final String WRONG_ACCOUNT_ID = "Wrong account id";
    private static final String CONSENT_ID = "12345";
    private static final String ACCOUNT_DETAILS_LIST_SOURCE = "/json/AccountDetailsList.json";
    private static final String ACCOUNT_REPORT_SOURCE = "/json/AccountReportTestData.json";
    private static final String BALANCES_SOURCE = "/json/ReadBalanceResponse.json";
    private static final String REQUEST_URI = "/accounts";
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final MessageError MESSAGE_ERROR_AIS_404 = new MessageError(ErrorType.AIS_404, of(MessageErrorCode.RESOURCE_UNKNOWN_404));
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    @InjectMocks
    private AccountController accountController;

    private Xs2aObjectMapper xs2aObjectMapper = (Xs2aObjectMapper) new Xs2aObjectMapper().registerModule(new JavaTimeModule());

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
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletOutputStream servletOutputStream;
    @Mock
    private PrintWriter printWriter;
    @Mock
    private ResponseErrorMapper responseErrorMapper;
    @Mock
    private TppErrorMessageBuilder tppErrorMessageBuilder;

    @Before
    public void setUp() {
        when(accountListService.getAccountList(anyString(), anyBoolean(), anyString())).thenReturn(getXs2aAccountListHolder());
        when(balanceService.getBalancesReport(anyString(), anyString(), anyString())).thenReturn(getBalanceReport());
        when(accountDetailsService.getAccountDetails(anyString(), any(), anyBoolean(), anyString())).thenReturn(getXs2aAccountDetailsHolder());
        when(transactionService.getTransactionDetails(eq(CONSENT_ID), eq(ACCOUNT_ID), any(), eq(REQUEST_URI))).thenReturn(buildTransaction());
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
    }

    @Test
    public void getAccountDetails_withBalance() throws IOException {
        // Given
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
    public void readAccountDetails_wrongId_fail() {
        // Given
        boolean withBalance = true;
        ResponseObject<Xs2aAccountDetailsHolder> responseEntity = buildXs2aAccountDetailsWithError();
        when(accountDetailsService.getAccountDetails(WRONG_CONSENT_ID, WRONG_ACCOUNT_ID, withBalance, REQUEST_URI))
            .thenReturn(responseEntity);
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity result = accountController.readAccountDetails(WRONG_ACCOUNT_ID, null, WRONG_CONSENT_ID, withBalance, null,
                                                                     null, null, null, null,
                                                                     null, null, null, null,
                                                                     null, null, null, null);
        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getAccounts_ResultTest() throws IOException {
        // Given
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
    public void getAccountList_wrongId_fail() {
        // Given
        boolean withBalance = true;
        ResponseObject<Xs2aAccountListHolder> responseEntity = getXs2aAccountListHolderWithError();

        when(accountListService.getAccountList(WRONG_CONSENT_ID, withBalance, REQUEST_URI))
            .thenReturn(responseEntity);

        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity result = accountController.getAccountList(null, WRONG_CONSENT_ID, withBalance,
                                                                 null, null, null, null, null, null,
                                                                 null, null, null, null, null,
                                                                 null, null);
        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getBalances_ResultTest() throws IOException {
        // Given
        ReadAccountBalanceResponse200 expectedResult = xs2aObjectMapper.readValue(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8),
                                                                              ReadAccountBalanceResponse200.class);

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
    public void getBalances_wrongId_fail() {
        // Given
        ResponseObject<Xs2aBalancesReport> responseEntity = buildBalanceReportWithError();
        when(balanceService.getBalancesReport(WRONG_CONSENT_ID, WRONG_ACCOUNT_ID, REQUEST_URI))
            .thenReturn(responseEntity);
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity result = accountController.getBalances(WRONG_ACCOUNT_ID, null, WRONG_CONSENT_ID, null,
                                                              null, null, null, null, null,
                                                              null, null, null,
                                                              null, null, null, null);
        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getTransactions_ResultTest() throws IOException {
        // Given
        AccountReport expectedResult = xs2aObjectMapper.readValue(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class);

        doReturn(new ResponseEntity<>(createAccountReport().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null));

        doReturn(ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build())
            .when(transactionService).getTransactionsReportByPeriod(any(Xs2aTransactionsReportByPeriodRequest.class));

        // When
        AccountReport result = (AccountReport) accountController.getTransactionList(ACCOUNT_ID, "pending",
                                                                                    null, CONSENT_ID, null, null, "both", false,
                                                                                    false, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null).getBody();

        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactionList_success() throws IOException {
        // Given
        AccountReport expectedResult = xs2aObjectMapper.readValue(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class);

        doReturn(new ResponseEntity<>(buildAccountReportWithError().getBody(), HttpStatus.OK))
            .when(responseErrorMapper).generateErrorResponse(MESSAGE_ERROR_AIS_404);
        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null));
        doReturn(ResponseObject.<Xs2aTransactionsReport>builder().fail(MESSAGE_ERROR_AIS_404).body(transactionsReport).build())
            .when(transactionService).getTransactionsReportByPeriod(any(Xs2aTransactionsReportByPeriodRequest.class));


        // When
        AccountReport result = (AccountReport) accountController.getTransactionList(ACCOUNT_ID, "pending",
                                                                                    null, CONSENT_ID, null, null, "both", false,
                                                                                    false, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactionList_isRespContentTypeJSON_success() throws IOException {
        // Given
        AccountReport expectedResult = xs2aObjectMapper.readValue(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class);

        doReturn(new ResponseEntity<>(createAccountReport().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null));
        transactionsReport.setResponseContentType("application/json");
        doReturn(ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build())
            .when(transactionService).getTransactionsReportByPeriod(any(Xs2aTransactionsReportByPeriodRequest.class));

        // When
        AccountReport result = (AccountReport) accountController.getTransactionList(ACCOUNT_ID, "pending",
                                                                                    null, CONSENT_ID, null, null, "both", false,
                                                                                    false, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactionDetails_success() throws IOException {
        doReturn(new ResponseEntity<>(createAccountReport().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        // Given
        AccountReport expectedResult = xs2aObjectMapper.readValue(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class);

        // When
        AccountReport result = (AccountReport) accountController.getTransactionDetails(ACCOUNT_ID, null,
                                                                                       null, CONSENT_ID, null, null, null, null, null,
                                                                                       null, null, null, null, null,
                                                                                       null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactionDetails1_success() throws IOException {
        // Given
        when(transactionService.getTransactionDetails(eq(CONSENT_ID), eq(ACCOUNT_ID), any(), eq(REQUEST_URI))).thenReturn(buildTransactionWithError());
        doReturn(new ResponseEntity<>(buildAccountReportWithError().getBody(), HttpStatus.OK))
            .when(responseErrorMapper).generateErrorResponse(MESSAGE_ERROR_AIS_404);

        AccountReport expectedResult = xs2aObjectMapper.readValue(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class);

        // When
        AccountReport result = (AccountReport) accountController.getTransactionDetails(ACCOUNT_ID, null,
                                                                                       null, CONSENT_ID, null, null, null, null, null,
                                                                                       null, null, null, null, null,
                                                                                       null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void downloadTransactions_success() throws IOException {
        // Given
        when(transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID)).thenReturn(buildTransactionDownloadResponseOk());
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        // When
        accountController.downloadTransactions(UUID.randomUUID(), CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        // Then
        verify(response, times(1)).addHeader(anyString(), anyString());
        assertEquals(0, response.getBufferSize());
    }

    @Test
    public void downloadTransactions_spiError() throws IOException {
        // Given
        TppErrorMessage tppErrorMessage = new TppErrorMessage(ERROR, CERTIFICATE_EXPIRED, "Certificate is expired");

        when(transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID)).thenReturn(buildTransactionDownloadResponseError());
        when(response.getWriter()).thenReturn(printWriter);
        when(tppErrorMessageBuilder.buildTppErrorMessage(ERROR, FORMAT_ERROR)).thenReturn(tppErrorMessage);

        // When
        accountController.downloadTransactions(UUID.randomUUID(), CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        // Then
        verify(response, times(1)).setStatus(anyInt());
        assertEquals(0, response.getBufferSize());
    }

    private ResponseObject<Xs2aAccountListHolder> getXs2aAccountListHolder() {
        List<Xs2aAccountDetails> accountDetails = Collections.singletonList(
            new Xs2aAccountDetails(ASPSP_ACCOUNT_ID, "33333-999999999", "DE371234599997", null, null, null,
                                   null, CURRENCY, "Schmidt", null,
                                   CashAccountType.CACC, AccountStatus.ENABLED, "GENODEF1N02", "", Xs2aUsageType.PRIV, "", null, null, null));
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(accountDetails, null);
        return ResponseObject.<Xs2aAccountListHolder>builder()
                   .body(xs2aAccountListHolder).build();
    }

    private ResponseObject<AccountList> createAccountDetailsList() throws IOException {
        AccountList details = xs2aObjectMapper.readValue(IOUtils.resourceToString(AccountControllerTest.ACCOUNT_DETAILS_LIST_SOURCE, UTF_8), AccountList.class);
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

    private ResponseObject<AccountDetails> getAccountDetails() throws IOException {
        AccountDetails details = createAccountDetailsList().getBody().getAccounts().get(0);
        return ResponseObject.<AccountDetails>builder()
                   .body(details).build();
    }

    private ResponseObject<AccountReport> createAccountReport() throws IOException {
        AccountReport accountReport = xs2aObjectMapper.readValue(IOUtils.resourceToString(AccountControllerTest.ACCOUNT_REPORT_SOURCE, UTF_8),
                                                             AccountReport.class);

        return ResponseObject.<AccountReport>builder()
                   .body(accountReport).build();
    }

    private ResponseObject<AccountReport> buildAccountReportWithError() throws IOException {
        AccountReport accountReport = xs2aObjectMapper.readValue(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                             AccountReport.class);

        return ResponseObject.<AccountReport>builder()
                   .fail(AccountControllerTest.MESSAGE_ERROR_AIS_404)
                   .body(accountReport).build();
    }

    private ResponseObject<ReadAccountBalanceResponse200> createReadBalances() throws IOException {
        ReadAccountBalanceResponse200 read = xs2aObjectMapper.readValue(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8),
                                                                    ReadAccountBalanceResponse200.class);
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
        Transactions transaction = new Transactions();
        transaction.setTransactionId("1234578");
        transaction.setEndToEndId("EndToEndId");
        transaction.setMandateId("MandateId");
        transaction.setCreditorId("CreditorId");
        transaction.setBookingDate(LocalDate.of(2018, 3, 9));
        Xs2aAmount amount = new Xs2aAmount();
        amount.setAmount("3000.45");
        amount.setCurrency(CURRENCY);
        transaction.setAmount(amount);
        AccountReference debtor = new AccountReference();
        debtor.setIban("DE371234599997");
        debtor.setCurrency(CURRENCY);
        transaction.setDebtorAccount(debtor);
        transaction.setRemittanceInformationStructured("Ref Number Merchant");
        transaction.setRemittanceInformationUnstructured("Ref Number Merchant");
        transaction.setPurposeCode(PurposeCode.fromValue("BKDF"));
        transaction.setBankTransactionCodeCode(new BankTransactionCode("BankTransactionCode"));

        return ResponseObject.<Transactions>builder()
                   .body(transaction).build();
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

}
