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
import de.adorsys.aspsp.aspspmockserver.web.rest.FutureBookingsController;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalanceType;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
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
public class FutureBookingsControllerTest {
    private static final String IBAN = "123456789";
    private static final String WRONG_IBAN = "Wrong iban";
    private static final BigDecimal BALANCE = BigDecimal.valueOf(2000);
    private static final BigDecimal AMOUNT_TO_BE_CHARGED = BigDecimal.valueOf(500);


    @InjectMocks
    private FutureBookingsController futureBookingsController;

    @Mock
    private FutureBookingsService futureBookingsService;

    @Before
    public void setUp() {
        when(futureBookingsService.changeBalances(IBAN, "EUR"))
            .thenReturn(getSpiAccountDetails((BALANCE.subtract(AMOUNT_TO_BE_CHARGED))));
        when(futureBookingsService.changeBalances(WRONG_IBAN, "EUR"))
            .thenReturn(Optional.empty());
    }

    @Test
    public void changeBalances_Success() {
        //Given
        HttpStatus expectedStatusCode = HttpStatus.OK;
        BigDecimal expectedAmount = BALANCE.subtract(AMOUNT_TO_BE_CHARGED);

        //When:
        ResponseEntity<SpiAccountDetails> actualResult = futureBookingsController.changeBalances(IBAN, "EUR");

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResult.getBody()).isEqualTo(getSpiAccountDetails(expectedAmount).get());
    }

    @Test
    public void changeBalances_WrongId() {
        //Given
        HttpStatus expectedStatusCode = HttpStatus.NO_CONTENT;

        //When:
        ResponseEntity<SpiAccountDetails> actualResult = futureBookingsController.changeBalances(WRONG_IBAN, "EUR");

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedStatusCode);
    }

    private Optional<SpiAccountDetails> getSpiAccountDetails(BigDecimal amount) {
        return Optional.of(new SpiAccountDetails("qwertyuiop12345678", "DE99999999999999", null, "4444333322221111",
            "444433xxxxxx1111", null, Currency.getInstance("EUR"), "Emily", "GIRO",
            null, "ACVB222", getNewBalanceList(amount)));
    }

    private List<SpiAccountBalance> getNewBalanceList(BigDecimal amount) {
        return Collections.singletonList(getNewSingleBalances(new SpiAmount(Currency.getInstance("EUR"), amount)));
    }

    private SpiAccountBalance getNewSingleBalances(SpiAmount spiAmount) {
        SpiAccountBalance sb = new SpiAccountBalance();
        sb.setReferenceDate(LocalDate.parse("2019-03-03"));
        sb.setSpiBalanceAmount(spiAmount);
        sb.setLastChangeDateTime(LocalDateTime.parse("2019-03-03T13:34:28.387"));
        sb.setSpiBalanceType(SpiBalanceType.INTERIM_AVAILABLE);
        sb.setLastCommittedTransaction("abc");
        return sb;
    }
}
