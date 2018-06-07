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
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {
    private static final String PSU_ID = "334455777";
    private static final String WRONG_PSU_ID = "Wrong psu id";
    private static final String ACCOUNT_ID = "3278921mxl-n2131-13nw-2n123";
    private static final String WRONG_ACCOUNT_ID = "Really wrong id";
    private static final String IBAN = "DE1789232872";
    private static final String WRONG_IBAN = "Wrongest iban ever";
    private static final Currency EUR = Currency.getInstance("EUR");

    @Autowired
    private AccountService accountService;
    @MockBean
    PsuRepository psuRepository;

    @Before
    public void setUp() {
        List<Psu> psuList = new ArrayList<>();
        psuList.add(getPsuWithRightAccounts());
        when(psuRepository.findOne(PSU_ID))
            .thenReturn(getPsuWithRightAccounts());
        when(psuRepository.findOne(WRONG_PSU_ID))
            .thenReturn(null);
        when(psuRepository.findPsuByAccountDetailsList_Iban(IBAN))
            .thenReturn(psuList);
        when(psuRepository.findPsuByAccountDetailsList_Iban(WRONG_IBAN))
            .thenReturn(Collections.emptyList());

        when(psuRepository.findPsuByAccountDetailsList_Id(ACCOUNT_ID))
            .thenReturn(Optional.of(getPsuWithRightAccounts()));
        when(psuRepository.findPsuByAccountDetailsList_Id(WRONG_ACCOUNT_ID))
            .thenReturn(Optional.empty());
        when(psuRepository.findPsuByAccountDetailsList_Id(null))
            .thenReturn(Optional.empty());

        when(psuRepository.save(any(Psu.class)))
            .thenReturn(getPsuWithRightAccounts());
    }

    @Test
    public void addAccount() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();

        //When
        SpiAccountDetails actualSpiAccountDetails = accountService.addAccount(PSU_ID, expectedSpiAccountDetails).get();

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void updateAccount() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();

        //When
        SpiAccountDetails actualSpiAccountDetails = accountService.updateAccount(expectedSpiAccountDetails).get();

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void getAccountByIban_Success() {
        //Given
        List<SpiAccountDetails> expectedSpiAccountDetails = getAccounts();
        //When
        List<SpiAccountDetails> actualSpiAccountDetails = accountService.getAccountsByIban(IBAN);

        //Then
        assertThat(actualSpiAccountDetails).isNotNull();
        assertThat(actualSpiAccountDetails).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void getAccountByIban_WrongIban() {
        //When
        List<SpiAccountDetails> actualSpiAccountDetails = accountService.getAccountsByIban(WRONG_IBAN);

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(Collections.emptyList());
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
    public void getBalances() {
        //Given
        List<SpiBalances> expectedBalance = getNewBalanceList();

        //When
        List<SpiBalances> actualBalanceList = accountService.getAccountBalancesById(ACCOUNT_ID);

        //Then
        assertThat(actualBalanceList).isEqualTo(expectedBalance);
    }

    @Test
    public void getAccountsByPsuId() {
        //When:
        List<SpiAccountDetails> actualList = accountService.getAccountsByPsuId(PSU_ID);
        //Then:
        assertThat(actualList).isNotNull();
        assertThat(actualList).isNotEmpty();
        assertThat(actualList).isEqualTo(getAccounts());
    }

    @Test
    public void getAccountsByPsuId_Failure() {
        //When:
        List<SpiAccountDetails> actualList = accountService.getAccountsByPsuId(WRONG_PSU_ID);
        //Then:
        assertThat(actualList).isEmpty();
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

    private List<SpiBalances> getNewBalanceList() {
        Currency euro = Currency.getInstance("EUR");

        SpiBalances balance = new SpiBalances();
        balance.setAuthorised(getNewSingleBalances(new SpiAmount(euro, BigDecimal.valueOf(1000))));
        balance.setOpeningBooked(getNewSingleBalances(new SpiAmount(euro, BigDecimal.valueOf(200))));

        return Collections.singletonList(balance);
    }

    private SpiAccountBalance getNewSingleBalances(SpiAmount spiAmount) {
        SpiAccountBalance sb = new SpiAccountBalance();
        sb.setDate(new Date(1523951451537L));
        sb.setSpiAmount(spiAmount);
        sb.setLastActionDateTime(new Date(1523951451537L));
        return sb;
    }

    private Psu getPsuWithRightAccounts() {
        return new Psu("12345678910", getAccounts());
    }

    private List<SpiAccountDetails> getAccounts() {
        List<SpiAccountDetails> list = new ArrayList<>();
        list.add(getSpiAccountDetails_1());
        list.add(getSpiAccountDetails_2());
        return list;
    }
}
