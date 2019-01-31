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
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountBalance;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.account.AspspBalanceType;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspAuthenticationObject;
import de.adorsys.psd2.aspsp.mock.api.psu.Psu;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String PSU_ID = "334455777";
    private static final String WRONG_PSU_ID = "Wrong psu id";
    private static final String ACCOUNT_ID = "3278921mxl-n2131-13nw-2n123";
    private static final String WRONG_ACCOUNT_ID = "Really wrong id";
    private static final String IBAN = "DE1789232872";
    private static final String WRONG_IBAN = "Wrongest iban ever";
    private static final Currency EUR = Currency.getInstance("EUR");

    @InjectMocks
    private AccountService accountService;
    @Mock
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
            .thenReturn(Optional.of(getPsuWithRightAccounts()));
        when(psuRepository.findPsuByAccountDetailsList_Iban(WRONG_IBAN))
            .thenReturn(Optional.empty());

        when(psuRepository.findPsuByAccountDetailsList_ResourceId(ACCOUNT_ID))
            .thenReturn(Optional.of(getPsuWithRightAccounts()));
        when(psuRepository.findPsuByAccountDetailsList_ResourceId(WRONG_ACCOUNT_ID))
            .thenReturn(Optional.empty());
        when(psuRepository.findPsuByAccountDetailsList_ResourceId(null))
            .thenReturn(Optional.empty());

        when(psuRepository.save(any(Psu.class)))
            .thenReturn(getPsuWithRightAccounts());
    }

    @Test
    public void addAccount() {
        //Given
        AspspAccountDetails expectedAspspAccountDetails = getAspspAccountDetails_1();

        //When
        AspspAccountDetails actualAspspAccountDetails = accountService.addAccount(PSU_ID, expectedAspspAccountDetails).get();

        //Then
        assertThat(actualAspspAccountDetails).isEqualTo(expectedAspspAccountDetails);
    }

    @Test
    public void updateAccount() {
        //Given
        AspspAccountDetails expectedAspspAccountDetails = getAspspAccountDetails_1();

        //When
        AspspAccountDetails actualAspspAccountDetails = accountService.updateAccount(expectedAspspAccountDetails).get();

        //Then
        assertThat(actualAspspAccountDetails).isEqualTo(expectedAspspAccountDetails);
    }

    @Test
    public void getAccountByIban_Success() {
        //Given
        List<AspspAccountDetails> expectedAspspAccountDetails = getAccounts();
        //When
        List<AspspAccountDetails> actualAspspAccountDetails = accountService.getAccountsByIban(IBAN);

        //Then
        assertThat(actualAspspAccountDetails).isNotNull();
        assertThat(actualAspspAccountDetails).isEqualTo(expectedAspspAccountDetails);
    }

    @Test
    public void getAccountByIban_WrongIban() {
        //When
        List<AspspAccountDetails> actualAspspAccountDetails = accountService.getAccountsByIban(WRONG_IBAN);

        //Then
        assertThat(actualAspspAccountDetails).isEqualTo(Collections.emptyList());
    }

    @Test
    public void getAccount_Success() {
        //Given
        AspspAccountDetails expectedAspspAccountDetails = getAspspAccountDetails_1();
        //When
        Optional<AspspAccountDetails> actualAspspAccountDetails = accountService.getAccountById(ACCOUNT_ID);

        //Then
        assertThat(actualAspspAccountDetails).isNotNull();
        assertThat(actualAspspAccountDetails.get()).isEqualTo(expectedAspspAccountDetails);
    }

    @Test
    public void getAccount_WrongId() {
        //Given
        accountService.addAccount("12234556", getAspspAccountDetails_1());

        //When
        Optional<AspspAccountDetails> actualAspspAccountDetails = accountService.getAccountById(WRONG_ACCOUNT_ID);

        //Then
        assertThat(actualAspspAccountDetails).isEqualTo(Optional.empty());
    }

    @Test
    public void getBalances() {
        //Given
        List<AspspAccountBalance> expectedBalance = getNewBalanceList();

        //When
        List<AspspAccountBalance> actualBalanceList = accountService.getAccountBalancesById(ACCOUNT_ID);

        //Then
        assertThat(actualBalanceList).isEqualTo(expectedBalance);
    }

    @Test
    public void getAccountsByPsuId() {
        //When:
        List<AspspAccountDetails> actualList = accountService.getAccountsByPsuId(PSU_ID);
        //Then:
        assertThat(actualList).isNotNull();
        assertThat(actualList).isNotEmpty();
        assertThat(actualList).isEqualTo(getAccounts());
    }

    @Test
    public void getAccountsByPsuId_Failure() {
        //When:
        List<AspspAccountDetails> actualList = accountService.getAccountsByPsuId(WRONG_PSU_ID);
        //Then:
        assertThat(actualList).isEmpty();
    }

    private AspspAccountDetails getAspspAccountDetails_1() {
        return new AspspAccountDetails(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, null, "1111222233334444",
                                       "111122xxxxxx44", null, Currency.getInstance("EUR"), "Jack", "GIRO",
                                       null, null, null, "XE3DDD", null, null, null, getNewBalanceList());
    }

    private AspspAccountDetails getAspspAccountDetails_2() {
        return new AspspAccountDetails(ASPSP_ACCOUNT_ID, "qwertyuiop12345678", IBAN, null,
                                       "4444333322221111", "444433xxxxxx1111", null, null, "Emily",
                                       "GIRO", null, null, null, "ACVB222", null, null, null, null);
    }

    private List<AspspAccountBalance> getNewBalanceList() {
        return Collections.singletonList(getNewSingleBalances(new AspspAmount(EUR, BigDecimal.valueOf(1000))));
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

    private Psu getPsuWithRightAccounts() {
        return new Psu("12345678910", "test@gmail.com", "aspsp", "zzz", getAccounts(), null, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")));
    }

    private List<AspspAccountDetails> getAccounts() {
        List<AspspAccountDetails> list = new ArrayList<>();
        list.add(getAspspAccountDetails_1());
        list.add(getAspspAccountDetails_2());
        return list;
    }
}
