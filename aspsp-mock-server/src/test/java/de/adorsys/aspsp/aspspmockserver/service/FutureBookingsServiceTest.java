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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FutureBookingsServiceTest {
    private static final String ACCOUNT_ID = "123456789";
    private static final String WRONG_ACCOUNT_ID = "0";
    private static final double BALANCE = 2000;
    private static final double AMOUNT_TO_BE_CHARGED = 500;

    @Autowired
    private FutureBookingsService futureBookingsService;
    @MockBean
    private PaymentService paymentService;
    @MockBean
    private AccountService accountService;

    @Before
    public void setUp() {
        when(paymentService.calculateAmountToBeCharged(any()))
            .thenReturn(500.0);
        when(accountService.getAccount(ACCOUNT_ID))
            .thenReturn(Optional.of(getSpiAccountDetailsWithBalance(BALANCE)));
        when(accountService.getAccount(WRONG_ACCOUNT_ID))
            .thenReturn(Optional.empty());
        when(accountService.addAccount(notNull(SpiAccountDetails.class)))
            .thenReturn(getSpiAccountDetailsWithBalance((BALANCE - AMOUNT_TO_BE_CHARGED)));
        when(accountService.addAccount(null))
            .thenReturn(null);
    }

    @Test
    public void changeBalances_Success() {
        //Given
        SpiAccountDetails expectedAccountDetails = getSpiAccountDetailsWithBalance((BALANCE - AMOUNT_TO_BE_CHARGED));

        //When
        SpiAccountDetails actualAccountDetails = futureBookingsService.changeBalances(ACCOUNT_ID).get();

        //Then
        assertThat(actualAccountDetails).isEqualTo(expectedAccountDetails);
    }

    @Test
    public void changeBalances_WrongId() {
        //Given
        Optional expectedAccountDetails = Optional.empty();

        //When
        Optional<SpiAccountDetails> actualAccountDetails = futureBookingsService.changeBalances(WRONG_ACCOUNT_ID);

        //Then
        assertThat(actualAccountDetails).isEqualTo(expectedAccountDetails);
    }

    private SpiAccountDetails getSpiAccountDetailsWithBalance(double amount) {
        return new SpiAccountDetails("qwertyuiop12345678", "DE99999999999999", null,
            "4444333322221111", "444433xxxxxx1111", null, Currency.getInstance("EUR"), "Emily",
            "GIRO", null, "ACVB222", getNewBalanceList(amount));
    }

    private ArrayList<SpiBalances> getNewBalanceList(double amount) {
        Currency euro = Currency.getInstance("EUR");

        SpiBalances balance = new SpiBalances();
        balance.setInterimAvailable(getNewSingleBalances(new SpiAmount(euro, Double.toString(amount))));
        ArrayList<SpiBalances> balances = new ArrayList<>();
        balances.add(balance);

        return balances;
    }

    private SpiAccountBalance getNewSingleBalances(SpiAmount spiAmount) {
        SpiAccountBalance sb = new SpiAccountBalance();
        sb.setDate(new Date(1523951451537L));
        sb.setSpiAmount(spiAmount);
        sb.setLastActionDateTime(new Date(1523951451537L));
        return sb;
    }
}
