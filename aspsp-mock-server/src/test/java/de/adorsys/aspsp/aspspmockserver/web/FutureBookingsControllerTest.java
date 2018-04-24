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

import de.adorsys.aspsp.aspspmockserver.service.FutureBookingsService;
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
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FutureBookingsControllerTest {
    private static final String ACCOUNT_ID = "123456789";
    private static final String WRONG_ACCOUNT_ID = "0";
    private static final double BALANCE = 2000;
    private static final double AMOUNT_TO_BE_CHARGED = 500;


    @Autowired
    private FutureBookingsController futureBookingsController;

    @MockBean(name = "futureBookingsService")
    private FutureBookingsService futureBookingsService;

    @Before
    public void setUp() {
        when(futureBookingsService.changeBalances(ACCOUNT_ID))
            .thenReturn(getSpiAccountDetails((BALANCE - AMOUNT_TO_BE_CHARGED)));
        when(futureBookingsService.changeBalances(WRONG_ACCOUNT_ID))
            .thenReturn(Optional.empty());
    }

    @Test
    public void changeBalances_Success() throws Exception {
        //Given
        HttpStatus expectedStatusCode = HttpStatus.OK;
        double expectedAmount = BALANCE - AMOUNT_TO_BE_CHARGED;

        //When:
        ResponseEntity<SpiAccountDetails> actualResult = futureBookingsController.changeBalances(ACCOUNT_ID);

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResult.getBody()).isEqualTo(getSpiAccountDetails(expectedAmount).get());
    }

    @Test
    public void changeBalances_WrongId() throws Exception {
        //Given
        HttpStatus expectedStatusCode = HttpStatus.NOT_FOUND;

        //When:
        ResponseEntity<SpiAccountDetails> actualResult = futureBookingsController.changeBalances(WRONG_ACCOUNT_ID);

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedStatusCode);
    }

    private Optional<SpiAccountDetails> getSpiAccountDetails(double amount) {
        return Optional.of(new SpiAccountDetails("qwertyuiop12345678", "DE99999999999999", null, "4444333322221111",
            "444433xxxxxx1111", null, Currency.getInstance("EUR"), "Emily", "GIRO",
            null, "ACVB222", getNewBalanceList(amount)));
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
