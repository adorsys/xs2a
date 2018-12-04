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
import de.adorsys.psd2.model.ReadBalanceResponse200;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.domain.*;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.domain.code.BankTransactionCode;
import de.adorsys.psd2.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.psd2.xs2a.service.AccountService;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
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
import java.lang.Exception;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {
    private static final UUID REQUEST_ID = UUID.fromString("ddd36e05-d67a-4830-93ad-9462f71ae1e6");
    private static final String RESOURCE_ID = "5738a1d4-89f2-459a-95b0-82717274a097";
    private static final String BOOKING_STATUS = "pending";

    private final String ACCOUNT_ID = "33333-999999999";
    private final String CONSENT_ID = "12345";
    private final String ACCOUNT_DETAILS_LIST_SOURCE = "/json/AccountDetailsList.json";
    private final String ACCOUNT_REPORT_SOURCE = "/json/AccountReportTestData.json";
    private final String BALANCES_SOURCE = "/json/ReadBalanceResponse.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

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

    @Before
    public void setUp() throws Exception {
        when(accountService.getAccountList(anyString(), anyBoolean())).thenReturn(getXs2aAccountDetailsList());
        when(accountService.getBalancesReport(anyString(), anyString())).thenReturn(getBalanceReport());
        when(accountService.getAccountDetails(anyString(), any(), anyBoolean())).thenReturn(getXs2aAccountDetails());
    }

    @Test
    public void getAccountDetails_withBalance() throws IOException {
        doReturn(new ResponseEntity<>(getAccountDetails().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        //Given
        boolean withBalance = true;
        ResponseObject<AccountDetails> expectedResult = getAccountDetails();

        //When
        AccountDetails result = (AccountDetails) accountController.readAccountDetails(ACCOUNT_ID, null,
                                                                                      CONSENT_ID, withBalance, null, null, null, null,
                                                                                      null, null, null, null, null,
                                                                                      null, null, null, null).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult.getBody());
    }

    @Test
    public void getAccounts_ResultTest() throws IOException {
        doReturn(new ResponseEntity<>(createAccountDetailsList(ACCOUNT_DETAILS_LIST_SOURCE).getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        //Given
        boolean withBalance = true;
        AccountList expectedResult = createAccountDetailsList(ACCOUNT_DETAILS_LIST_SOURCE).getBody();

        //When:
        AccountList result = (AccountList) accountController.getAccountList(null, CONSENT_ID, withBalance,
                                                                            null, null, null, null, null, null,
                                                                            null, null, null, null, null,
                                                                            null, null).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getBalances_ResultTest() throws IOException {
        doReturn(new ResponseEntity<>(createReadBalances().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        //Given:
        ReadBalanceResponse200 expectedResult = jsonConverter.toObject(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8),
                                                                       ReadBalanceResponse200.class).get();

        //When:
        ReadBalanceResponse200 result = (ReadBalanceResponse200) accountController.getBalances(ACCOUNT_ID,
                                                                                               null, CONSENT_ID, null, null, null, null,
                                                                                               null, null, null, null, null,
                                                                                               null, null, null, null).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactions_ResultTest() throws IOException {
        doReturn(new ResponseEntity<>(createAccountReport(ACCOUNT_REPORT_SOURCE).getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null));;

        doReturn(ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build())
            .when(accountService).getTransactionsReportByPeriod(anyString(), anyString(), anyString(), anyBoolean(), any(), any(), any());
        //Given:
        AccountReport expectedResult = jsonConverter.toObject(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8),
                                                              AccountReport.class).get();

        //When
        AccountReport result = (AccountReport) accountController.getTransactionList(ACCOUNT_ID, "pending",
                                                                                    null, null, null, null, "both", false,
                                                                                    false, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    private ResponseObject<Map<String, List<Xs2aAccountDetails>>> getXs2aAccountDetailsList() {
        List<Xs2aAccountDetails> accountDetails = Collections.singletonList(
            new Xs2aAccountDetails("33333-999999999", "DE371234599997", null, null, null,
                                   null, Currency.getInstance("EUR"), "Schmidt", null,
                                   CashAccountType.CACC, AccountStatus.ENABLED, "GENODEF1N02", "", Xs2aUsageType.PRIV, "", null));
        Map<String, List<Xs2aAccountDetails>> result = new HashMap<>();
        result.put("accountList", accountDetails);
        return ResponseObject.<Map<String, List<Xs2aAccountDetails>>>builder()
                   .body(result).build();

    }

    private ResponseObject<AccountList> createAccountDetailsList(String path) throws IOException {
        AccountList details = jsonConverter.toObject(IOUtils.resourceToString(path, UTF_8), AccountList.class).get();
        return ResponseObject.<AccountList>builder()
                   .body(details).build();
    }

    private ResponseObject<Xs2aAccountDetails> getXs2aAccountDetails() throws IOException {
        Map<String, List<Xs2aAccountDetails>> map = getXs2aAccountDetailsList().getBody();
        return ResponseObject.<Xs2aAccountDetails>builder()
                   .body(map.get("accountList").get(0)).build();
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

    private ResponseObject<Xs2aAccountReport> getXs2aAccountReport() {
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
        Xs2aAccountReference debtor = new Xs2aAccountReference();
        debtor.setIban("DE371234599997");
        debtor.setCurrency(Currency.getInstance("EUR"));
        transaction.setDebtorAccount(debtor);
        transaction.setRemittanceInformationStructured("Ref Number Merchant");
        transaction.setRemittanceInformationUnstructured("Ref Number Merchant");
        transaction.setPurposeCode(new Xs2aPurposeCode("BKDF"));
        transaction.setBankTransactionCodeCode(new BankTransactionCode("BankTransactionCode"));
        List<Transactions> booked = Collections.singletonList(new Transactions());
        Xs2aAccountReport accountReport = new Xs2aAccountReport(booked, Collections.emptyList(), null);
        return ResponseObject.<Xs2aAccountReport>builder().body(accountReport).build();
    }

    private ResponseObject<ReadBalanceResponse200> createReadBalances() throws IOException {
        ReadBalanceResponse200 read = jsonConverter.toObject(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8),
                                                             ReadBalanceResponse200.class).get();
        return ResponseObject.<ReadBalanceResponse200>builder()
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
}
