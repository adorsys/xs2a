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

import de.adorsys.aspsp.aspspmockserver.repository.AccountRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {
    private static final String CONSENT_ID_WITH_BALANCE_TRUE = "123456789";
    private static final String CONSENT_ID_WITH_BALANCE_FALSE = "987654321";

    @Autowired
    private AccountService accountService;
    @MockBean
    AccountRepository accountRepository;
    @MockBean
    ConsentService consentService;

    @Test
    public void test(){
        assertThat("A").isEqualTo("A"); //TODO Tests to be rewritten MAY
    }
/*
    @Before
    public void setUp() {
        when(consentService.getConsent(CONSENT_ID_WITH_BALANCE_TRUE)).thenReturn(getConsent(true));
        when(consentService.getConsent(CONSENT_ID_WITH_BALANCE_FALSE)).thenReturn(getConsent(false));
        when(accountRepository.findByIbanIn(new HashSet<>(Arrays.asList("DE12345235431234", "DE99999999999999")))).thenReturn(getAccounts());
        when(accountRepository.findOne("21fefdsdvds212sa")).thenReturn(getSpiAccountDetails_1());
        when(accountRepository.exists("21fefdsdvds212sa")).thenReturn(true);
        when(accountRepository.findOne("qwertyuiop12345678")).thenReturn(getSpiAccountDetailsWithBalance());
        when(accountRepository.save(getSpiAccountDetails_1())).thenReturn(getSpiAccountDetails_1());
    }

    @Test
    public void addAccount() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();

        //When
        SpiAccountDetails actualSpiAccountDetails = accountService.addOrUpdateAccount(expectedSpiAccountDetails);

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void getAllAccounts() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails1 = getSpiAccountDetails_1();
        SpiAccountDetails expectedSpiAccountDetails2 = getSpiAccountDetails_2();

        //When
        List<SpiAccountDetails> actualListSpiAccountDetails = accountService.getAllAccounts(CONSENT_ID_WITH_BALANCE_FALSE, false);

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
        accountService.addOrUpdateAccount(getSpiAccountDetails_1());

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

    @Test
    public void getBalances() {
        //Given
        SpiAccountDetails spiAccountDetails = getSpiAccountDetailsWithBalance();
        String spiAccountDetailsId = spiAccountDetails.getId();
        List<SpiBalances> expectedBalance = spiAccountDetails.getBalances();

        //When
        Optional<List<SpiBalances>> actualBalanceList = accountService.getBalances(spiAccountDetailsId);

        //Then
        assertThat(actualBalanceList.get()).isEqualTo(expectedBalance);
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

    private SpiAccountDetails getSpiAccountDetailsWithBalance() {
        return new SpiAccountDetails("qwertyuiop12345678", "DE99999999999991", null,
            "4444333322221111", "444433xxxxxx1111", null, Currency.getInstance("EUR"), "Emily",
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

    private List<SpiAccountReference> getReferencesList(List<SpiAccountDetails> details) {
        return details.stream()
            .map(det -> new SpiAccountReference(det.getIban(), det.getBban(), det.getPan(), det.getMaskedPan(),
                det.getMsisdn(), det.getCurrency())).collect(Collectors.toList());
    }

    private SpiAccountConsent getConsent(boolean withBalance) {
        return new SpiAccountConsent(withBalance ? CONSENT_ID_WITH_BALANCE_TRUE : CONSENT_ID_WITH_BALANCE_FALSE, new SpiAccountAccess(
            getReferencesList(getAccounts()), null, null, null, null),
            false, new Date(), 4, new Date(), null, null,
            withBalance, false);
    }*/
}
