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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.service.AccountService;
import de.adorsys.aspsp.xs2a.util.GsonUtcDateAdapter;
import de.adorsys.aspsp.xs2a.util.GsonUtcInstantAdapter;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.Exception;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {
    private final String ACCOUNT_ID = "33333-999999999";
    private final String TRANSACTION_ID = "1234578";
    private final String ACCOUNT_DETAILS_SOURCE = "/json/AccountDetailsList.json";
    private final String ACCOUNT_REPORT_SOURCE = "/json/AccountReportTestData.json";
    private final String BALANCES_SOURCE = "/json/BalancesTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    private static final Gson GSON = new GsonBuilder()
                                         .registerTypeAdapter(Date.class, new GsonUtcDateAdapter())
                                         .registerTypeAdapter(Instant.class, new GsonUtcInstantAdapter())
                                         .create();

    @Autowired
    private AccountController accountController;

    @MockBean(name = "accountService")
    private AccountService accountServiceMocked;

    @Before
    public void setUp() throws Exception {
        when(accountServiceMocked.getAccountDetailsList(anyString(), anyBoolean(), anyBoolean())).thenReturn(createAccountDetailsList(ACCOUNT_DETAILS_SOURCE));
        ResponseObject<List<Balances>> balances = readBalances();
        when(accountServiceMocked.getBalances(any(String.class), anyBoolean())).thenReturn(balances);
        when(accountServiceMocked.getAccountReport(any(String.class), any(String.class), any(Date.class), any(Date.class), any(String.class), anyBoolean(), any(), anyBoolean(), anyBoolean())).thenReturn(createAccountReport(ACCOUNT_REPORT_SOURCE));
        when(accountServiceMocked.getAccountDetails(any(), anyBoolean(), anyBoolean())).thenReturn(getAccountDetails());
    }

    @Test
    public void getAccountDetails_withBalance() throws IOException {
        //Given
        boolean withBalance = true;
        boolean psuInvolved = true;
        ResponseObject<AccountDetails> expectedResult = getAccountDetails();

        //When
        AccountDetails result = accountController.readAccountDetails(ACCOUNT_ID, withBalance, psuInvolved).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult.getBody());
    }

    @Test
    public void getAccounts_ResultTest() throws IOException {
        //Given
        boolean withBalance = true;
        boolean psuInvolved = true;
        Map<String, List<AccountDetails>> expectedResult = createAccountDetailsList(ACCOUNT_DETAILS_SOURCE).getBody();

        //When:
        Map<String, List<AccountDetails>> result = accountController.getAccounts("id", withBalance, psuInvolved).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getBalances_ResultTest() throws IOException {
        //Given:
        boolean psuInvolved = true;
        Balances expectedBalances = GSON.fromJson(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8), Balances.class);
        List<Balances> expectedResult = new ArrayList<>();
        expectedResult.add(expectedBalances);

        //When:
        List<Balances> result = accountController.getBalances(ACCOUNT_ID, psuInvolved).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactions_ResultTest() throws IOException {
        //Given:
        boolean psuInvolved = true;
        AccountReport expectedResult = GSON.fromJson(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8), AccountReport.class);

        //When
        AccountReport result = accountController.getTransactions(ACCOUNT_ID, "123", null, null, TRANSACTION_ID, psuInvolved, "both", false, false).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getAccounts_withBalance() {
        boolean withBalance = true;
        boolean psuInvolved = false;

        checkAccountResults(withBalance, psuInvolved);
    }

    @Test
    public void getAccounts_noBalances() {
        boolean withBalance = false;
        boolean psuInvolved = false;

        checkAccountResults(withBalance, psuInvolved);
    }

    @Test
    public void getAccounts_withBalanceAndPsuInvolved() {
        boolean withBalance = true;
        boolean psuInvolved = true;

        checkAccountResults(withBalance, psuInvolved);
    }

    private void checkAccountResults(boolean withBalance, boolean psuInvolved) {
        //Given:
        AccountDetails accountDetails = new AccountDetails(
            "21fef",
            "DE1234523543",
            null,
            null,
            null,
            null,
            Currency.getInstance("EUR"),
            "name",
            "GIRO",
            null,
            "XE3DDD",
            null
        );
        Links links = new Links();
        links.setViewBalances(linkTo(AccountController.class).slash(accountDetails.getId()).slash("balances").toString());
        links.setViewTransactions(linkTo(AccountController.class).slash(accountDetails.getId()).slash("transactions").toString());
        accountDetails.setLinks(links);

        List<AccountDetails> accountDetailsList = new ArrayList<>();
        accountDetailsList.add(accountDetails);
        Map<String, List<AccountDetails>> mockMap = new HashMap<>();
        mockMap.put("accountList", accountDetailsList);
        ResponseObject mockedResponse = ResponseObject.builder()
                                            .body(mockMap).build();

        Map<String, List<AccountDetails>> expectedMap = new HashMap<>();
        expectedMap.put("accountList", accountDetailsList);
        ResponseEntity<Map<String, List<AccountDetails>>> expectedResult = new ResponseEntity<>(expectedMap, HttpStatus.OK);

        when(accountServiceMocked.getAccountDetailsList("id", withBalance, psuInvolved))
            .thenReturn(mockedResponse);

        //When:
        ResponseEntity<Map<String, List<AccountDetails>>> actualResponse = accountController.getAccounts("id", withBalance, psuInvolved);

        //Then:
        assertThat(actualResponse).isEqualTo(expectedResult);
    }

    private ResponseObject<Map<String, List<AccountDetails>>> createAccountDetailsList(String path) throws IOException {
        AccountDetails[] array = GSON.fromJson(IOUtils.resourceToString(path, UTF_8), AccountDetails[].class);
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
        AccountReport accountReport = GSON.fromJson(IOUtils.resourceToString(path, UTF_8), AccountReport.class);

        return ResponseObject.<AccountReport>builder()
                   .body(accountReport).build();
    }

    private ResponseObject<List<Balances>> readBalances() throws IOException {
        Balances read = GSON.fromJson(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8), Balances.class);
        List<Balances> res = new ArrayList<>();
        res.add(read);
        return ResponseObject.<List<Balances>>builder()
                   .body(res).build();
    }
}
