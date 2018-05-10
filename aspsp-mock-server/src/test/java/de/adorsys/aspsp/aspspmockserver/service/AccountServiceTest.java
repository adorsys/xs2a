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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {
    private static final String ACCOUNT_ID = "3278921mxl-n2131-13nw-2n123";
    private static final String WRONG_ACCOUNT_ID = "Really wrong id";
    private static final String IBAN = "DE1789232872";
    private static final String WRONG_IBAN = "Wrongest iban ever";

    @Autowired
    private AccountService accountService;
    @MockBean
    PsuRepository psuRepository;
    @MockBean
    ConsentService consentService;

    @Before
    public void setUp() {
        when(psuRepository.findPsuByAccountDetailsList_Iban(IBAN))
            .thenReturn(getPsuWithRightAccounts());
        when(psuRepository.findPsuByAccountDetailsList_Id(ACCOUNT_ID))
            .thenReturn(getPsuWithRightAccounts());
        when(psuRepository.findPsuByAccountDetailsList_Id(WRONG_ACCOUNT_ID))
            .thenReturn(Optional.empty());
        when(psuRepository.findPsuByAccountDetailsList_Id(null))
            .thenReturn(Optional.empty());
        when(psuRepository.save(getPsuWithRightAccounts().get()))
            .thenReturn(getPsuWithRightAccounts().get());
        when(psuRepository.findPsuByAccountDetailsList_Iban(IBAN))
            .thenReturn(getPsuWithRightAccounts());
        when(psuRepository.findPsuByAccountDetailsList_Iban(WRONG_IBAN))
            .thenReturn(Optional.empty());
        when(psuRepository.findOne(anyString()))
            .thenReturn(getPsuWithRightAccounts().get());
    }

    @Test
    public void addAccount() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();

        //When
        SpiAccountDetails actualSpiAccountDetails = accountService.addAccount("12234556", expectedSpiAccountDetails).get();

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void updateAccount() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetailsToUpdate();

        //When
        SpiAccountDetails actualSpiAccountDetails = accountService.updateAccount(expectedSpiAccountDetails).get();

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void getAllAccounts() {
        //TODO this is a task https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/71
    }

    @Test
    public void getAccountByIban_Success() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();
        //When
        Optional<SpiAccountDetails> actualSpiAccountDetails = accountService.getAccountByIban(IBAN, Currency.getInstance("EUR"));

        //Then
        assertThat(actualSpiAccountDetails).isNotNull();
        assertThat(actualSpiAccountDetails.get()).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void getAccountByIban_WrongIban() {
        //When
        Optional<SpiAccountDetails> actualSpiAccountDetails = accountService.getAccountByIban(WRONG_IBAN, Currency.getInstance("EUR"));

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(Optional.empty());
    }

    @Test
    public void getAccount_Success() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();
        //When
        Optional<SpiAccountDetails> actualSpiAccountDetails = accountService.getAccountById(ACCOUNT_ID);

        //Then
        assertThat(actualSpiAccountDetails).isNotNull();
        assertThat(actualSpiAccountDetails.get()).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void getAccount_WrongId() {
        //Given
        accountService.addAccount("12234556", getSpiAccountDetails_1());

        //When
        Optional<SpiAccountDetails> actualSpiAccountDetails = accountService.getAccountById(WRONG_ACCOUNT_ID);

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(Optional.empty());
    }

    @Test
    public void deleteAccountById_Success() {
        //Given
        String spiAccountDetailsId = ACCOUNT_ID;

        //When
        boolean actualResult = accountService.deleteAccountById(spiAccountDetailsId);

        //Then
        assertThat(actualResult).isTrue();
    }

    @Test
    public void deleteAccountById_WrongId() {
        //Given
        String wrongId = WRONG_ACCOUNT_ID;

        //When
        boolean actualResult = accountService.deleteAccountById(wrongId);

        //Then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void deleteAccountById_Null() {
        //Given
        String wrongId = null;

        //When
        boolean actualResult = accountService.deleteAccountById(wrongId);

        //Then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void getBalances() {
        //Given
        List<SpiBalances> expectedBalance = getNewBalanceList();

        //When
        List<SpiBalances> actualBalanceList = accountService.getBalances(ACCOUNT_ID);

        //Then
        assertThat(actualBalanceList).isEqualTo(expectedBalance);
    }

    private SpiAccountDetails getSpiAccountDetails_1() {
        return new SpiAccountDetails(ACCOUNT_ID, IBAN, null, "1111222233334444",
            "111122xxxxxx44", null, Currency.getInstance("EUR"), "Jack", "GIRO",
            null, "XE3DDD", getNewBalanceList());
    }

    private SpiAccountDetails getSpiAccountDetails_2() {
        return new SpiAccountDetails("qwertyuiop12345678", IBAN, null,
            "4444333322221111", "444433xxxxxx1111", null, null, "Emily",
            "GIRO", null, "ACVB222", null);
    }

    private SpiAccountDetails getSpiAccountDetailsToUpdate() {
        return new SpiAccountDetails("qwertyuiop12345678", IBAN, null,
            "9999999999999999", "444433xxxxxx1111", null, null, "Irene Forsyte",
            "GIRO", null, "ACVB222", getNewBalanceList());
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

    private List<SpiAccountDetails> getAccounts() {
        List<SpiAccountDetails> list = new ArrayList<>();
        list.add(getSpiAccountDetails_1());
        list.add(getSpiAccountDetails_2());
        return list;
    }

    private Optional<Psu> getPsuWithRightAccounts() {
        return Optional.of(new Psu("12345678910", getAccounts()));
    }
}
