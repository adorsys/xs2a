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

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {
    @Autowired
    private AccountService accountService;

    @Test
    public void addAccount() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();

        //When
        SpiAccountDetails actualSpiAccountDetails = accountService.addAccount(expectedSpiAccountDetails);

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void getAllAccounts() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails1 = getSpiAccountDetails_1();
        SpiAccountDetails expectedSpiAccountDetails2 = getSpiAccountDetails_2();
        accountService.addAccount(expectedSpiAccountDetails1);
        accountService.addAccount(expectedSpiAccountDetails2);

        //When
        List<SpiAccountDetails> actualListSpiAccountDetails = accountService.getAllAccounts();

        //Then
        assertThat(actualListSpiAccountDetails).isNotNull();
        assertThat(actualListSpiAccountDetails.get(0)).isEqualTo(expectedSpiAccountDetails1);
        assertThat(actualListSpiAccountDetails.get(1)).isEqualTo(expectedSpiAccountDetails2);
    }

    @Test
    public void getAccount_Success() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();
        String spiAccountDetailsId = expectedSpiAccountDetails.getId();
        accountService.addAccount(expectedSpiAccountDetails);

        //When
        Optional<SpiAccountDetails> actualSpiAccountDetails = accountService.getAccount(spiAccountDetailsId);

        //Then
        assertThat(actualSpiAccountDetails).isNotNull();
        assertThat(actualSpiAccountDetails.get()).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void getAccount_WrongId() {
        //Given
        String wrongId = "Really wrong id";
        accountService.addAccount(getSpiAccountDetails_1());

        //When
        Optional<SpiAccountDetails> actualSpiAccountDetails = accountService.getAccount(wrongId);

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(Optional.empty());
    }

    @Test
    public void deleteAccountById_Success() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();
        String spiAccountDetailsId = expectedSpiAccountDetails.getId();
        accountService.addAccount(expectedSpiAccountDetails);

        //When
        boolean actualResult = accountService.deleteAccountById(spiAccountDetailsId);

        //Then
        assertThat(actualResult).isTrue();
    }

    @Test
    public void deleteAccountById_WrongId() {
        //Given
        String wrongId = "Really wrong id";

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

    private SpiAccountDetails getSpiAccountDetails_1() {
        return new SpiAccountDetails("21fefdsdvds212sa", "DE12345235431234", null, "1111222233334444",
        "111122xxxxxx44", null, Currency.getInstance("EUR"), "Jack", "GIRO",
        null, "XE3DDD", null);
    }

    private SpiAccountDetails getSpiAccountDetails_2() {
        return new SpiAccountDetails("qwertyuiop12345678", "DE99999999999999", null,
        "4444333322221111", "444433xxxxxx1111", null, Currency.getInstance("EUR"), "Emily",
        "GIRO", null, "ACVB222", null);
    }
}
