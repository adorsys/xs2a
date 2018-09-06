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

package de.adorsys.aspsp.xs2a.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.Xs2aBalance;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.aspsp.xs2a.service.AccountService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {
    private final String ACCOUNT_ID = "33333-999999999";
    private final String CONSENT_ID = "12345";
    private final String TRANSACTION_ID = "1234578";
    private final String ACCOUNT_DETAILS_SOURCE = "/json/AccountDetailsList.json";
    private final String ACCOUNT_REPORT_SOURCE = "/json/AccountReportTestData.json";
    private final String BALANCES_SOURCE = "/json/BalancesTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @InjectMocks
    private AccountController accountController;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonConverter jsonConverter = new JsonConverter(objectMapper);

    @Mock
    private AccountService accountService;
    @Mock
    private ResponseMapper responseMapper;

    @Before
    public void setUp() throws Exception {
        when(accountService.getAccountDetailsList(anyString(), anyBoolean())).thenReturn(createAccountDetailsList(ACCOUNT_DETAILS_SOURCE));
        ResponseObject<List<Xs2aBalance>> balances = readBalances();
        when(accountService.getBalances(anyString(), anyString())).thenReturn(balances);
        when(accountService.getAccountReport(any(String.class), any(String.class), any(LocalDate.class), any(LocalDate.class), any(String.class), anyBoolean(), any(), anyBoolean(), anyBoolean())).thenReturn(createAccountReport(ACCOUNT_REPORT_SOURCE));
        when(accountService.getAccountDetails(anyString(), any(), anyBoolean())).thenReturn(getAccountDetails());
    }

    @Test
    public void getAccountDetails_withBalance() throws IOException {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(getAccountDetails().getBody(), HttpStatus.OK));
        //Given
        boolean withBalance = true;
        ResponseObject<Xs2aAccountDetails> expectedResult = getAccountDetails();

        //When
        Xs2aAccountDetails result = accountController.readAccountDetails(CONSENT_ID, ACCOUNT_ID, withBalance).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult.getBody());
    }

    @Test
    public void getAccounts_ResultTest() throws IOException {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(createAccountDetailsList(ACCOUNT_DETAILS_SOURCE).getBody(), HttpStatus.OK));
        //Given
        boolean withBalance = true;
        Map<String, List<Xs2aAccountDetails>> expectedResult = createAccountDetailsList(ACCOUNT_DETAILS_SOURCE).getBody();

        //When:
        Map<String, List<Xs2aAccountDetails>> result = accountController.getAccounts("id", withBalance).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getBalances_ResultTest() throws IOException {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(readBalances().getBody(), HttpStatus.OK));
        //Given:
        Xs2aBalance expectedBalances = jsonConverter.toObject(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8), Xs2aBalance.class).get();
        List<Xs2aBalance> expectedResult = new ArrayList<>();
        expectedResult.add(expectedBalances);

        //When:
        List<Xs2aBalance> result = accountController.getBalances(CONSENT_ID, ACCOUNT_ID).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactions_ResultTest() throws IOException {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(createAccountReport(ACCOUNT_REPORT_SOURCE).getBody(), HttpStatus.OK));
        //Given:
        boolean psuInvolved = true;
        Xs2aAccountReport expectedResult = jsonConverter.toObject(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8), Xs2aAccountReport.class).get();

        //When
        Xs2aAccountReport result = accountController.getTransactions(ACCOUNT_ID, "123", null, null, TRANSACTION_ID, psuInvolved, "both", false, false).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    private ResponseObject<Map<String, List<Xs2aAccountDetails>>> createAccountDetailsList(String path) throws IOException {
        Xs2aAccountDetails[] array = jsonConverter.toObject(IOUtils.resourceToString(path, UTF_8), Xs2aAccountDetails[].class).get();
        Map<String, List<Xs2aAccountDetails>> result = new HashMap<>();
        result.put("accountList", Arrays.asList(array));
        return ResponseObject.<Map<String, List<Xs2aAccountDetails>>>builder()
                   .body(result).build();
    }

    private ResponseObject<Xs2aAccountDetails> getAccountDetails() throws IOException {
        Map<String, List<Xs2aAccountDetails>> map = createAccountDetailsList(ACCOUNT_DETAILS_SOURCE).getBody();
        return ResponseObject.<Xs2aAccountDetails>builder()
                   .body(map.get("accountList").get(0)).build();
    }

    private ResponseObject<Xs2aAccountReport> createAccountReport(String path) throws IOException {
        Xs2aAccountReport accountReport = jsonConverter.toObject(IOUtils.resourceToString(path, UTF_8), Xs2aAccountReport.class).get();

        return ResponseObject.<Xs2aAccountReport>builder()
                   .body(accountReport).build();
    }

    private ResponseObject<List<Xs2aBalance>> readBalances() throws IOException {
        Xs2aBalance read = jsonConverter.toObject(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8), Xs2aBalance.class).get();
        List<Xs2aBalance> res = new ArrayList<>();
        res.add(read);
        return ResponseObject.<List<Xs2aBalance>>builder()
                   .body(res).build();
    }
}
