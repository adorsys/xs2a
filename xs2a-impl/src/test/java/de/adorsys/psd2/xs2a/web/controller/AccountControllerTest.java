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

package de.adorsys.psd2.xs2a.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.psd2.model.AccountDetails;
import de.adorsys.psd2.model.AccountList;
import de.adorsys.psd2.model.AccountReport;
import de.adorsys.psd2.model.ReadAccountBalanceResponse200;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.*;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.domain.code.BankTransactionCode;
import de.adorsys.psd2.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.AccountService;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private final String ACCOUNT_ID = "33333-999999999";
    private final String WRONG_ACCOUNT_ID = "Wrong account id";
    private final String CONSENT_ID = "12345";
    private final String ACCOUNT_DETAILS_LIST_SOURCE = "/json/AccountDetailsList.json";
    private final String ACCOUNT_REPORT_SOURCE = "/json/AccountReportTestData.json";
    private final String BALANCES_SOURCE = "/json/ReadBalanceResponse.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final MessageError MESSAGE_ERROR_AIS_404 = new MessageError(ErrorType.AIS_404, of(MessageErrorCode.RESOURCE_UNKNOWN_404));

    @InjectMocks
    private AccountController accountController;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonConverter jsonConverter = new JsonConverter(objectMapper);

    @Mock
    private AccountService accountService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private AccountModelMapper accountModelMapper;
    @Mock
    private HttpServletRequest request;
    @Mock
    private ResponseErrorMapper responseErrorMapper;

    @Before
    public void setUp() {
        when(accountService.getAccountList(anyString(), anyBoolean())).thenReturn(getXs2aAccountListHolder());
        when(accountService.getBalancesReport(anyString(), anyString())).thenReturn(getBalanceReport());
        when(accountService.getAccountDetails(anyString(), any(), anyBoolean())).thenReturn(getXs2aAccountDetailsHolder());
        when(accountService.getTransactionDetails(anyString(), anyString(), anyString())).thenReturn(buildTransaction());
    }

    @Test
    public void getAccountDetails_withBalance() throws IOException {
        //Given
        boolean withBalance = true;
        ResponseObject<AccountDetails> expectedResult = getAccountDetails();

        doReturn(new ResponseEntity<>(getAccountDetails().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        //When
        AccountDetails result = (AccountDetails) accountController.readAccountDetails(ACCOUNT_ID, null,
                                                                                      CONSENT_ID, withBalance, null, null, null, null,
                                                                                      null, null, null, null, null,
                                                                                      null, null, null, null).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult.getBody());
    }

    @Test
    public void readAccountDetails_wrongId_fail() throws IOException {
        // Given
        boolean withBalance = true;
        ResponseObject<Xs2aAccountDetailsHolder> responseEntity = buildXs2aAccountDetailsWithError(MESSAGE_ERROR_AIS_404);
        when(accountService.getAccountDetails(WRONG_CONSENT_ID, WRONG_ACCOUNT_ID, withBalance))
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
        //Given
        boolean withBalance = true;
        AccountList expectedResult = createAccountDetailsList(ACCOUNT_DETAILS_LIST_SOURCE).getBody();

        doReturn(new ResponseEntity<>(createAccountDetailsList(ACCOUNT_DETAILS_LIST_SOURCE).getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        //When:
        AccountList result = (AccountList) accountController.getAccountList(null, CONSENT_ID, withBalance,
                                                                            null, null, null, null, null, null,
                                                                            null, null, null, null, null,
                                                                            null, null).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getAccountList_wrongId_fail() throws IOException {
        // Given
        boolean withBalance = true;
        ResponseObject<Xs2aAccountListHolder> responseEntity = getXs2aAccountListHolderWithError(MESSAGE_ERROR_AIS_404);

        when(accountService.getAccountList(WRONG_CONSENT_ID, withBalance))
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
        //Given:
        ReadAccountBalanceResponse200 expectedResult = jsonConverter.toObject(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8),
                                                                              ReadAccountBalanceResponse200.class).get();

        doReturn(new ResponseEntity<>(createReadBalances().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        //When:
        ReadAccountBalanceResponse200 result = (ReadAccountBalanceResponse200) accountController.getBalances(ACCOUNT_ID,
                                                                                                             null, CONSENT_ID, null, null, null, null,
                                                                                                             null, null, null, null, null,
                                                                                                             null, null, null, null).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getBalances_wrongId_fail() throws IOException {
        // Given
        ResponseObject<Xs2aBalancesReport> responseEntity = buildBalanceReportWithError(MESSAGE_ERROR_AIS_404);
        when(accountService.getBalancesReport(WRONG_CONSENT_ID, WRONG_ACCOUNT_ID))
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
        //Given:
        AccountReport expectedResult = jsonConverter.toObject(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class).get();

        doReturn(new ResponseEntity<>(createAccountReport(ACCOUNT_REPORT_SOURCE).getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null));
        ;

        doReturn(ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build())
            .when(accountService).getTransactionsReportByPeriod(anyString(), anyString(), anyString(), anyBoolean(), any(), any(), any());

        //When
        AccountReport result = (AccountReport) accountController.getTransactionList(ACCOUNT_ID, "pending",
                                                                                    null, null, null, null, "both", false,
                                                                                    false, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactionList_success() throws IOException {
        // Given
        AccountReport expectedResult = jsonConverter.toObject(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class).get();

        doReturn(new ResponseEntity<>(buildAccountReportWithError(ACCOUNT_REPORT_SOURCE, MESSAGE_ERROR_AIS_404).getBody(), HttpStatus.OK))
            .when(responseErrorMapper).generateErrorResponse(MESSAGE_ERROR_AIS_404);
        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null));
        doReturn(ResponseObject.<Xs2aTransactionsReport>builder().fail(MESSAGE_ERROR_AIS_404).body(transactionsReport).build())
            .when(accountService).getTransactionsReportByPeriod(anyString(), anyString(), anyString(), anyBoolean(), any(), any(), any());


        // When
        AccountReport result = (AccountReport) accountController.getTransactionList(ACCOUNT_ID, "pending",
                                                                                    null, null, null, null, "both", false,
                                                                                    false, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null).getBody();

        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactionList_isRespContentTypeJSON_success() throws IOException {
        // Given
        AccountReport expectedResult = jsonConverter.toObject(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class).get();

        doReturn(new ResponseEntity<>(createAccountReport(ACCOUNT_REPORT_SOURCE).getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null));
        transactionsReport.setResponseContentType("application/json");
        doReturn(ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build())
            .when(accountService).getTransactionsReportByPeriod(anyString(), anyString(), anyString(), anyBoolean(), any(), any(), any());


        // When
        AccountReport result = (AccountReport) accountController.getTransactionList(ACCOUNT_ID, "pending",
                                                                                    null, null, null, null, "both", false,
                                                                                    false, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null).getBody();

        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactionDetails_success() throws IOException {
        doReturn(new ResponseEntity<>(createAccountReport(ACCOUNT_REPORT_SOURCE).getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        // Given
        AccountReport expectedResult = jsonConverter.toObject(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class).get();

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
        when(accountService.getTransactionDetails(anyString(), anyString(), anyString())).thenReturn(buildTransactionWithError(MESSAGE_ERROR_AIS_404));
        doReturn(new ResponseEntity<>(buildAccountReportWithError(ACCOUNT_REPORT_SOURCE, MESSAGE_ERROR_AIS_404).getBody(), HttpStatus.OK))
            .when(responseErrorMapper).generateErrorResponse(MESSAGE_ERROR_AIS_404);

        AccountReport expectedResult = jsonConverter.toObject(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class).get();

        // When
        AccountReport result = (AccountReport) accountController.getTransactionDetails(ACCOUNT_ID, null,
                                                                                       null, CONSENT_ID, null, null, null, null, null,
                                                                                       null, null, null, null, null,
                                                                                       null, null, null).getBody();
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    private ResponseObject<Xs2aAccountListHolder> getXs2aAccountListHolder() {
        List<Xs2aAccountDetails> accountDetails = Collections.singletonList(
            new Xs2aAccountDetails(ASPSP_ACCOUNT_ID, "33333-999999999", "DE371234599997", null, null, null,
                                   null, Currency.getInstance("EUR"), "Schmidt", null,
                                   CashAccountType.CACC, AccountStatus.ENABLED, "GENODEF1N02", "", Xs2aUsageType.PRIV, "", null));
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(accountDetails, null);
        return ResponseObject.<Xs2aAccountListHolder>builder()
                   .body(xs2aAccountListHolder).build();
    }

    private ResponseObject<AccountList> createAccountDetailsList(String path) throws IOException {
        AccountList details = jsonConverter.toObject(IOUtils.resourceToString(path, UTF_8), AccountList.class).get();
        return ResponseObject.<AccountList>builder()
                   .body(details).build();
    }

    private ResponseObject<Xs2aAccountDetailsHolder> getXs2aAccountDetailsHolder() {
        List<Xs2aAccountDetails> accountDetailsList = getXs2aAccountListHolder().getBody().getAccountDetails();
        Xs2aAccountDetailsHolder xs2aAccountDetailsHolder = new Xs2aAccountDetailsHolder(accountDetailsList.get(0), null);
        return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                   .body(xs2aAccountDetailsHolder).build();
    }

    private ResponseObject<Map<String, List<Xs2aAccountDetails>>> buildAccountDetailsListWithError(MessageError messageError) throws IOException {
        List<Xs2aAccountDetails> accountDetails = Collections.singletonList(
            new Xs2aAccountDetails(ASPSP_ACCOUNT_ID, "33333-999999999", "DE371234599997", null, null, null,
                                   null, Currency.getInstance("EUR"), "Schmidt", null,
                                   CashAccountType.CACC, AccountStatus.ENABLED, "GENODEF1N02", "", Xs2aUsageType.PRIV, "", null));
        Map<String, List<Xs2aAccountDetails>> result = new HashMap<>();
        result.put("accountList", accountDetails);

        return ResponseObject.<Map<String, List<Xs2aAccountDetails>>>builder()
                   .fail(messageError)
                   .body(result).build();
    }

    private ResponseObject<Xs2aAccountDetailsHolder> buildXs2aAccountDetailsWithError(MessageError messageError) {
        return ResponseObject.<Xs2aAccountDetailsHolder>builder().fail(messageError)
                   .build();
    }

    private ResponseObject<Xs2aAccountListHolder> getXs2aAccountListHolderWithError(MessageError messageError) {
        return ResponseObject.<Xs2aAccountListHolder>builder().fail(messageError)
                   .build();
    }

    private ResponseObject<AccountDetails> getAccountDetails() throws IOException {
        AccountDetails details = createAccountDetailsList(ACCOUNT_DETAILS_LIST_SOURCE).getBody().getAccounts().get(0);
        return ResponseObject.<AccountDetails>builder()
                   .body(details).build();
    }

    private ResponseObject<AccountReport> createAccountReport(String path) throws IOException {
        AccountReport accountReport = jsonConverter.toObject(IOUtils.resourceToString(path, UTF_8),
                                                             AccountReport.class).get();

        return ResponseObject.<AccountReport>builder()
                   .body(accountReport).build();
    }

    private ResponseObject<AccountReport> buildAccountReportWithError(String path, MessageError messageError) throws IOException {
        AccountReport accountReport = jsonConverter.toObject(IOUtils.resourceToString(path, UTF_8),
                                                             AccountReport.class).get();

        return ResponseObject.<AccountReport>builder()
                   .fail(messageError)
                   .body(accountReport).build();
    }

    private ResponseObject<ReadAccountBalanceResponse200> createReadBalances() throws IOException {
        ReadAccountBalanceResponse200 read = jsonConverter.toObject(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8),
                                                                    ReadAccountBalanceResponse200.class).get();
        return ResponseObject.<ReadAccountBalanceResponse200>builder()
                   .body(read).build();
    }

    private ResponseObject<List<Xs2aBalance>> getXs2aBalances() {
        Xs2aBalance balance = new Xs2aBalance();
        Xs2aAmount amount = new Xs2aAmount();
        amount.setAmount("300.45");
        amount.setCurrency(Currency.getInstance("EUR"));
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

    private ResponseObject<Xs2aBalancesReport> buildBalanceReportWithError(MessageError messageError) throws IOException {
        Xs2aBalancesReport balancesReport = new Xs2aBalancesReport();
        balancesReport.setBalances(getXs2aBalances().getBody());
        return ResponseObject.<Xs2aBalancesReport>builder()
                   .fail(messageError)
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
        amount.setCurrency(Currency.getInstance("EUR"));
        transaction.setAmount(amount);
        AccountReference debtor = new AccountReference();
        debtor.setIban("DE371234599997");
        debtor.setCurrency(Currency.getInstance("EUR"));
        transaction.setDebtorAccount(debtor);
        transaction.setRemittanceInformationStructured("Ref Number Merchant");
        transaction.setRemittanceInformationUnstructured("Ref Number Merchant");
        transaction.setPurposeCode(new Xs2aPurposeCode("BKDF"));
        transaction.setBankTransactionCodeCode(new BankTransactionCode("BankTransactionCode"));

        return ResponseObject.<Transactions>builder()
                   .body(transaction).build();
    }

    private ResponseObject<Transactions> buildTransactionWithError(MessageError messageError) {
        return ResponseObject.<Transactions>builder()
                   .fail(messageError)
                   .build();
    }

}
