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
import de.adorsys.aspsp.xs2a.domain.Balance;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
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
        ResponseObject<List<Balance>> balances = readBalances();
        when(accountService.getBalances(anyString(), anyString())).thenReturn(balances);
        when(accountService.getAccountReport(any(String.class), any(String.class), any(LocalDate.class), any(LocalDate.class), any(String.class), anyBoolean(), any(), anyBoolean(), anyBoolean())).thenReturn(createAccountReport(ACCOUNT_REPORT_SOURCE));
        when(accountService.getAccountDetails(anyString(), any(), anyBoolean())).thenReturn(getAccountDetails());
    }

    @Test
    public void getAccountDetails_withBalance() throws IOException {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(getAccountDetails().getBody(), HttpStatus.OK));
        //Given
        boolean withBalance = true;
        ResponseObject<AccountDetails> expectedResult = getAccountDetails();

        //When
        AccountDetails result = accountController.readAccountDetails(CONSENT_ID, ACCOUNT_ID, withBalance).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult.getBody());
    }

    @Test
    public void getAccounts_ResultTest() throws IOException {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(createAccountDetailsList(ACCOUNT_DETAILS_SOURCE).getBody(), HttpStatus.OK));
        //Given
        boolean withBalance = true;
        Map<String, List<AccountDetails>> expectedResult = createAccountDetailsList(ACCOUNT_DETAILS_SOURCE).getBody();

        //When:
        Map<String, List<AccountDetails>> result = accountController.getAccounts("id", withBalance).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getBalances_ResultTest() throws IOException {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(readBalances().getBody(), HttpStatus.OK));
        //Given:
        Balance expectedBalances = jsonConverter.toObject(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8), Balance.class).get();
        List<Balance> expectedResult = new ArrayList<>();
        expectedResult.add(expectedBalances);

        //When:
        List<Balance> result = accountController.getBalances(CONSENT_ID, ACCOUNT_ID).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactions_ResultTest() throws IOException {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(createAccountReport(ACCOUNT_REPORT_SOURCE).getBody(), HttpStatus.OK));
        //Given:
        boolean psuInvolved = true;
        AccountReport expectedResult = jsonConverter.toObject(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8), AccountReport.class).get();

        //When
        AccountReport result = accountController.getTransactions(ACCOUNT_ID, "123", null, null, TRANSACTION_ID, psuInvolved, "both", false, false).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    private ResponseObject<Map<String, List<AccountDetails>>> createAccountDetailsList(String path) throws IOException {
        AccountDetails[] array = jsonConverter.toObject(IOUtils.resourceToString(path, UTF_8), AccountDetails[].class).get();
        Map<String, List<AccountDetails>> result = new HashMap<>();
        result.put("accountList", Arrays.asList(array));
        return ResponseObject.<Map<String, List<AccountDetails>>>builder()
                   .body(result).build();
    }

    private ResponseObject<AccountDetails> getAccountDetails() throws IOException {
        Map<String, List<AccountDetails>> map = createAccountDetailsList(ACCOUNT_DETAILS_SOURCE).getBody();
        return ResponseObject.<AccountDetails>builder()
                   .body(map.get("accountList").get(0)).build();
    }

    private ResponseObject<AccountReport> createAccountReport(String path) throws IOException {
        AccountReport accountReport = jsonConverter.toObject(IOUtils.resourceToString(path, UTF_8), AccountReport.class).get();

        return ResponseObject.<AccountReport>builder()
                   .body(accountReport).build();
    }

    private ResponseObject<List<Balance>> readBalances() throws IOException {
        Balance read = jsonConverter.toObject(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8), Balance.class).get();
        List<Balance> res = new ArrayList<>();
        res.add(read);
        return ResponseObject.<List<Balance>>builder()
                   .body(res).build();
    }
}
