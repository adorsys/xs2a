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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FutureBookingsServiceTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String IBAN = "123456789";
    private static final String WRONG_IBAN = "Wrong iban";
    private static final BigDecimal BALANCE = BigDecimal.valueOf(2000);
    private static final BigDecimal AMOUNT_TO_BE_CHARGED = BigDecimal.valueOf(500);

    @InjectMocks
    private FutureBookingsService futureBookingsService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private AccountService accountService;

    @Before
    public void setUp() {
        when(paymentService.calculateAmountToBeCharged(any()))
            .thenReturn(new BigDecimal(500));
        when(accountService.getAccountsByIban(IBAN))
            .thenReturn(getAspspAccountDetailsList(BALANCE));
        when(accountService.getAccountsByIban(WRONG_IBAN))
            .thenReturn(Collections.emptyList());
        when(accountService.updateAccount(notNull(AspspAccountDetails.class)))
            .thenReturn(Optional.of(getAspspAccountDetailsWithBalance((BALANCE.subtract(AMOUNT_TO_BE_CHARGED)))));
        when(accountService.updateAccount(null))
            .thenReturn(null);
    }

    @Test
    public void changeBalances_Success() {
        //Given
        AspspAccountDetails expectedAccountDetails = getAspspAccountDetailsWithBalance((BALANCE.subtract(AMOUNT_TO_BE_CHARGED)));

        //When
        AspspAccountDetails actualAccountDetails = futureBookingsService.changeBalances(IBAN, "EUR").get();

        //Then
        assertThat(actualAccountDetails).isEqualTo(expectedAccountDetails);
    }

    @Test
    public void changeBalances_WrongId() {
        //Given
        Optional expectedAccountDetails = Optional.empty();

        //When
        Optional<AspspAccountDetails> actualAccountDetails = futureBookingsService.changeBalances(WRONG_IBAN, "EUR");

        //Then
        assertThat(actualAccountDetails).isEqualTo(expectedAccountDetails);
    }

    private List<AspspAccountDetails> getAspspAccountDetailsList(BigDecimal amount) {
        List<AspspAccountDetails> accountList = new ArrayList<>();
        accountList.add(getAspspAccountDetailsWithBalance(amount));
        return accountList;
    }

    private AspspAccountDetails getAspspAccountDetailsWithBalance(BigDecimal amount) {
        return new AspspAccountDetails(ASPSP_ACCOUNT_ID, "qwertyuiop12345678", "DE99999999999999", null,
                                       "4444333322221111", "444433xxxxxx1111", null, Currency.getInstance("EUR"), "Emily",
                                       "GIRO", null, null, null, "ACVB222", null, null, null, getNewBalanceList(amount));
    }

    private List<AspspAccountBalance> getNewBalanceList(BigDecimal amount) {
        return Collections.singletonList(getNewSingleBalances(new AspspAmount(Currency.getInstance("EUR"), amount)));
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
