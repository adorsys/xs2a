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

package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.AccountService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {
    private static final String ACCOUNT_ID = "2123sndjk2w23";
    private static final String WRONG_ACCOUNT_ID = "wrong account id";
    private static final String CONSENT_ID = "123456789";

    @MockBean
    private AccountService accountService;
    @Autowired
    private AccountController accountController;
    private List<SpiAccountDetails> accountList = new ArrayList<>();

    @Before
    public void setUpAccountServiceMock() {
        accountList.add(getSpiAccountDetails_1());
        accountList.add(getSpiAccountDetails_2());
        when(accountService.getAccount(ACCOUNT_ID))
            .thenReturn(Optional.of(getSpiAccountDetails_1()));
        when(accountService.getAccount(WRONG_ACCOUNT_ID))
            .thenReturn(Optional.empty());
        when(accountService.getAllAccounts(anyString(), anyBoolean()))
            .thenReturn(accountList);
        when(accountService.addOrUpdateAccount(getSpiAccountDetails_1()))
            .thenReturn(getSpiAccountDetails_1());
        when(accountService.deleteAccountById(ACCOUNT_ID))
            .thenReturn(true);
        when(accountService.deleteAccountById(WRONG_ACCOUNT_ID))
            .thenReturn(false);
        when(accountService.getBalances(ACCOUNT_ID))
            .thenReturn(Optional.of(getNewBalanceList()));
        when(accountService.getBalances(WRONG_ACCOUNT_ID))
            .thenReturn(Optional.empty());
    }

    @Test
    public void readAllAccounts() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<List<SpiAccountDetails>> actualResponse = accountController.readAllAccounts(CONSENT_ID, false);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        List<SpiAccountDetails> actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(accountList);
    }

    @Test
    public void readAccountById() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<SpiAccountDetails> actualResponse = accountController.readAccountById(ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        SpiAccountDetails actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(getSpiAccountDetails_1());
    }

    @Test
    public void readAccountById_wrongId() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.NOT_FOUND;

        //When:
        ResponseEntity<SpiAccountDetails> actualResponse = accountController.readAccountById(WRONG_ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        SpiAccountDetails actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isNull();
    }

    @Test
    public void createAccount() throws Exception {
        //Given
        MockHttpServletRequest expectedRequest = new MockHttpServletRequest();
        expectedRequest.setRequestURI("/account/");
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();
        HttpStatus expectedStatusCode = HttpStatus.CREATED;
        String expectedUrl = "/account/" + expectedSpiAccountDetails.getId();

        //When
        ResponseEntity actualResponse = accountController.createAccount(expectedRequest, expectedSpiAccountDetails);

        //Then
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getHeaders().get("Location").toString()).contains(expectedUrl);
    }

    @Test
    public void deleteAccount_Success() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.NO_CONTENT;

        //When:
        ResponseEntity actualResponse = accountController.deleteAccount(ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
    }

    @Test
    public void deleteAccount_WrongId() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.NOT_FOUND;

        //When:
        ResponseEntity actualResponse = accountController.deleteAccount(WRONG_ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
    }

    @Test
    public void readBalancesById() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        List<SpiBalances> expectedBalanceList = getNewBalanceList();

        //When:
        ResponseEntity actualResponse = accountController.readBalancesById(ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(expectedBalanceList);
    }

    @Test
    public void readBalancesById_wrongID() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.NOT_FOUND;

        //When:
        ResponseEntity actualResponse = accountController.readBalancesById(WRONG_ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isNull();
    }

    private SpiAccountDetails getSpiAccountDetails_1() {
        return new SpiAccountDetails(ACCOUNT_ID, "DE12345235431234", null, "1111222233334444",
            "111122xxxxxx44", null, Currency.getInstance("EUR"), "Jack", "GIRO",
            null, "XE3DDD", null);
    }

    private SpiAccountDetails getSpiAccountDetails_2() {
        return new SpiAccountDetails("qwertyuiop12345678", "DE99999999999999", null, "4444333322221111",
            "444433xxxxxx1111", null, Currency.getInstance("EUR"), "Emily", "GIRO",
            null, "ACVB222", null);
    }

    private List<SpiBalances> getNewBalanceList() {
        Currency euro = Currency.getInstance("EUR");

        SpiBalances balance = new SpiBalances();
        balance.setAuthorised(getNewSingleBalances(new SpiAmount(euro, "1000")));
        balance.setOpeningBooked(getNewSingleBalances(new SpiAmount(euro, "200")));

        return Collections.singletonList(balance);
    }

    private SpiAccountBalance getNewSingleBalances(SpiAmount spiAmount) {
        SpiAccountBalance sb = new SpiAccountBalance();
        sb.setDate(new Date(1523951451537L));
        sb.setSpiAmount(spiAmount);
        sb.setLastActionDateTime(new Date(1523951451537L));
        return sb;
    }
}
