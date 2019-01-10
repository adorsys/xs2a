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
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountBalance;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.account.AspspBalanceType;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {
    private static final String ACCOUNT_ID = "3278921mxl-n2131-13nw-2n123";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String WRONG_ACCOUNT_ID = "Really wrong id";
    private static final String IBAN = "DE1789232872";
    private static final String WRONG_IBAN = "Wrongest iban ever";
    private static final String PSU_ID = "111111111111";
    private static final String WRONG_PSU_ID = "Wrong PSU id";
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    @Mock
    private AccountService accountService;
    @InjectMocks
    private AccountController accountController;
    private List<AspspAccountDetails> accountList = new ArrayList<>();

    @Before
    public void setUpAccountServiceMock() {
        accountList.add(getAspspAccountDetails_1());
        accountList.add(getAspspAccountDetails_2());

        when(accountService.getAccountById(ACCOUNT_ID))
            .thenReturn(Optional.of(getAspspAccountDetails_1()));
        when(accountService.getAccountById(WRONG_ACCOUNT_ID))
            .thenReturn(Optional.empty());
        when(accountService.getAllAccounts())
            .thenReturn(accountList);
        when(accountService.addAccount(PSU_ID, getAspspAccountDetails_1()))
            .thenReturn(Optional.of(getAspspAccountDetails_1()));
        when(accountService.getAccountBalancesById(ACCOUNT_ID))
            .thenReturn(getNewBalanceList());
        when(accountService.getAccountBalancesById(WRONG_ACCOUNT_ID))
            .thenReturn(Collections.emptyList());
        when(accountService.getAccountsByIban(IBAN))
            .thenReturn(accountList);
        when(accountService.getAccountsByIban(WRONG_IBAN))
            .thenReturn(Collections.emptyList());
        when(accountService.getAccountsByPsuId(PSU_ID))
            .thenReturn(accountList);
        when(accountService.getAccountsByPsuId(WRONG_PSU_ID))
            .thenReturn(Collections.emptyList());
    }

    @Test
    public void readAllAccounts() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<List<AspspAccountDetails>> actualResponse = accountController.readAllAccounts();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(accountList);
    }

    @Test
    public void readAccountById() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<AspspAccountDetails> actualResponse = accountController.readAccountById(ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        AspspAccountDetails actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(getAspspAccountDetails_1());
    }

    @Test
    public void readAccountById_wrongId() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.NO_CONTENT;

        //When:
        ResponseEntity<AspspAccountDetails> actualResponse = accountController.readAccountById(WRONG_ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        AspspAccountDetails actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isNull();
    }

    @Test
    public void readAccountByIban() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<List<AspspAccountDetails>> actualResponse = accountController.readAccountsByIban(IBAN);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        List<AspspAccountDetails> actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(accountList);
    }

    @Test
    public void readAccountByIban_wrongId() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.NO_CONTENT;

        //When:
        ResponseEntity<List<AspspAccountDetails>> actualResponse = accountController.readAccountsByIban(WRONG_IBAN);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        List<AspspAccountDetails> actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isNull();
    }

    @Test
    public void createAccount() {
        //Given
        AspspAccountDetails expectedAspspAccountDetails = getAspspAccountDetails_1();
        HttpStatus expectedStatusCode = HttpStatus.CREATED;

        //When
        ResponseEntity actualResponse = accountController.createAccount(PSU_ID, expectedAspspAccountDetails);

        //Then
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
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
    public void readBalancesById() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        List<AspspAccountBalance> expectedBalanceList = getNewBalanceList();

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
        HttpStatus expectedStatusCode = HttpStatus.NO_CONTENT;

        //When:
        ResponseEntity actualResponse = accountController.readBalancesById(WRONG_ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isNull();
    }

    @Test
    public void readAccountsByPsuId() {
        //Given:
        HttpStatus expectedStatus = HttpStatus.OK;
        //When:
        ResponseEntity<List<AspspAccountDetails>> response = accountController.readAccountsByPsuId(PSU_ID);
        //Then:
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isEqualTo(accountList);
    }

    @Test
    public void readAccountsByPsuId_Failure() {
        //Given:
        HttpStatus expectedStatus = HttpStatus.NO_CONTENT;
        //When:
        ResponseEntity<List<AspspAccountDetails>> response = accountController.readAccountsByPsuId(WRONG_PSU_ID);
        //Then:
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNullOrEmpty();
    }

    private AspspAccountDetails getAspspAccountDetails_1() {
        return new AspspAccountDetails(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, null, "1111222233334444",
                                       "111122xxxxxx44", null, CURRENCY, "Jack", "GIRO",
                                       null, null, "XE3DDD", null, null, null, getNewBalanceList());
    }

    private AspspAccountDetails getAspspAccountDetails_2() {
        return new AspspAccountDetails(ASPSP_ACCOUNT_ID, "qwertyuiop12345678", IBAN, null, "4444333322221111",
                                       "444433xxxxxx1111", null, null, "Emily", "GIRO",
                                       null, null, "ACVB222", null, null, null, getNewBalanceList());
    }

    private List<AspspAccountBalance> getNewBalanceList() {
        return Collections.singletonList(getNewSingleBalances(new AspspAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(1000))));
    }

    private AspspAccountBalance getNewSingleBalances(AspspAmount aspspAmount) {
        AspspAccountBalance sb = new AspspAccountBalance();
        sb.setReferenceDate(LocalDate.parse("2019-03-03"));
        sb.setSpiBalanceAmount(aspspAmount);
        sb.setLastChangeDateTime(LocalDateTime.parse("2019-03-03T13:34:28.387"));
        sb.setSpiBalanceType(AspspBalanceType.INTERIM_AVAILABLE);
        sb.setLastCommittedTransaction("abc");
        return sb;
    }
}
