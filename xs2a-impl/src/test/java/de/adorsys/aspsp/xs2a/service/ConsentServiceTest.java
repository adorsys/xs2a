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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.impl.ConsentSpiImpl;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
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
public class ConsentServiceTest {
    private final String CORRECT_PSU_ID = "123456789";
    private final String WRONG_PSU_ID = "WRONG PSU ID";
    private final String CORRECT_IBAN = "DE123456789";
    private final String CORRECT_IBAN_1 = "DE987654321";
    private final String WRONG_IBAN = "WRONG IBAN";
    private final Currency CURRENCY = Currency.getInstance("EUR");
    private final Currency CURRENCY_2 = Currency.getInstance("USD");
    private final Date DATE = new Date(321554477);

    @Autowired
    private ConsentService consentService;

    @MockBean(name = "consentSpi")
    ConsentSpiImpl consentSpi;
    @MockBean(name = "accountSpi")
    AccountSpi accountSpi;

    @Before
    public void setUp() {
        //WB Acc Create Case
        when(consentSpi.createAccountConsents(
            getSpiConsent(null, getSpiAccountAccess(
                getSpiReferensesList(CORRECT_IBAN), Collections.emptyList(), Collections.emptyList(), false, false), true)))
            .thenReturn(CORRECT_PSU_ID);
        //WB Acc noCurrency set Create Case
        when(consentSpi.createAccountConsents(
            getSpiConsent(null, getSpiAccountAccess(
                Arrays.asList(getSpiReference(CORRECT_IBAN, CURRENCY), getSpiReference(CORRECT_IBAN, CURRENCY_2)), Collections.emptyList(), Collections.emptyList(), false, false), true)))
            .thenReturn(CORRECT_PSU_ID);
        //WB Acc noCurrency set Create Case
        when(consentSpi.createAccountConsents(
            getSpiConsent(null, getSpiAccountAccess(
                Arrays.asList(getSpiReference(CORRECT_IBAN, CURRENCY_2), getSpiReference(CORRECT_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), false, false), true)))
            .thenReturn(CORRECT_PSU_ID);
        //WoB Acc Create Case
        when(consentSpi.createAccountConsents(
            getSpiConsent(null, getSpiAccountAccess(
                getSpiReferensesList(CORRECT_IBAN), Collections.emptyList(), Collections.emptyList(), false, false), false)))
            .thenReturn(CORRECT_PSU_ID);
        //WB PSU allAvailable Create Case
        when(consentSpi.createAccountConsents(
            getSpiConsent(null, getSpiAccountAccess(
                Arrays.asList(getSpiReference(CORRECT_IBAN, CURRENCY), getSpiReference(CORRECT_IBAN, CURRENCY_2)), Collections.emptyList(), Collections.emptyList(), true, false), true)))
            .thenReturn(CORRECT_PSU_ID);
        //WB PSU allPsd2 Create Case
        when(consentSpi.createAccountConsents(
            getSpiConsent(null, getSpiAccountAccess(
                Arrays.asList(getSpiReference(CORRECT_IBAN, CURRENCY), getSpiReference(CORRECT_IBAN, CURRENCY_2)), Arrays.asList(getSpiReference(CORRECT_IBAN, CURRENCY), getSpiReference(CORRECT_IBAN, CURRENCY_2)), Arrays.asList(getSpiReference(CORRECT_IBAN, CURRENCY), getSpiReference(CORRECT_IBAN, CURRENCY_2)), false, true), true)))
            .thenReturn(CORRECT_PSU_ID);
        //WB Trans Create Case
        when(consentSpi.createAccountConsents(
            getSpiConsent(null, getSpiAccountAccess(
                getSpiReferensesList(CORRECT_IBAN), Collections.emptyList(), getSpiReferensesList(CORRECT_IBAN), false, false), true)))
            .thenReturn(CORRECT_PSU_ID);
        //WB Bal Create Case
        when(consentSpi.createAccountConsents(
            getSpiConsent(null, getSpiAccountAccess(
                getSpiReferensesList(CORRECT_IBAN), getSpiReferensesList(CORRECT_IBAN), Collections.emptyList(), false, false), true)))
            .thenReturn(CORRECT_PSU_ID);
        //WB Bal+Tr Create Case
        when(consentSpi.createAccountConsents(
            getSpiConsent(null, getSpiAccountAccess(
                Arrays.asList(getSpiReference(CORRECT_IBAN, CURRENCY), getSpiReference(CORRECT_IBAN_1, CURRENCY)), getSpiReferensesList(CORRECT_IBAN), getSpiReferensesList(CORRECT_IBAN_1), false, false), true)))
            .thenReturn(CORRECT_PSU_ID);
        when(consentSpi.createAccountConsents(
            getSpiConsent(null, getSpiAccountAccess(
                Arrays.asList(getSpiReference(CORRECT_IBAN_1, CURRENCY), getSpiReference(CORRECT_IBAN, CURRENCY)), getSpiReferensesList(CORRECT_IBAN), getSpiReferensesList(CORRECT_IBAN_1), false, false), true)))
            .thenReturn(CORRECT_PSU_ID);

        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Collections.singletonList(CORRECT_IBAN)))).thenReturn(getSpiDetailsList("1", CORRECT_IBAN));
        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Arrays.asList(CORRECT_IBAN, CORRECT_IBAN_1)))).thenReturn(Arrays.asList(getSpiDetails("1", CORRECT_IBAN, CURRENCY), getSpiDetails("2", CORRECT_IBAN_1, CURRENCY)));
        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Collections.singletonList(WRONG_IBAN)))).thenReturn(null);
        when(accountSpi.readAccountsByPsuId(CORRECT_PSU_ID)).thenReturn(getSpiDetailsList("1", CORRECT_IBAN));
        when(accountSpi.readAccountsByPsuId(WRONG_PSU_ID)).thenReturn(Collections.emptyList());

        when(consentSpi.getAccountConsentById(CORRECT_PSU_ID)).thenReturn(getSpiConsent(CORRECT_PSU_ID, getSpiAccountAccess(getSpiReferensesList(CORRECT_IBAN), null, null, false, false), false));
        when(consentSpi.getAccountConsentById(WRONG_PSU_ID)).thenReturn(null);

        consentSpi.expireConsent(any());
        consentSpi.deleteAccountConsentsById(anyString());
    }

    @Test
    public void createAccountConsentsWithResponse_ByAccInAccAccess_WB_Success() {
        //Given:
        boolean withBalance = true;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(getReferencesArr(CORRECT_IBAN), new AccountReference[]{}, new AccountReference[]{}, false, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, null);
        CreateConsentResp response = (CreateConsentResp) responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_ByAccInAccAccess_NoCurrencySet_WB_Success() {
        //Given:
        boolean withBalance = true;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(new AccountReference[]{getReference(CORRECT_IBAN, null)}, new AccountReference[]{}, new AccountReference[]{}, false, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, null);
        CreateConsentResp response = (CreateConsentResp) responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_ByAccInAccAccess_WoB_Success() {
        //Given:
        boolean withBalance = false;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(getReferencesArr(CORRECT_IBAN), new AccountReference[]{}, new AccountReference[]{}, false, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, null);
        CreateConsentResp response = (CreateConsentResp) responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_ByAccInAccAccess_WB_Failure() {
        //Given:
        boolean withBalance = true;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(getReferencesArr(WRONG_IBAN), new AccountReference[]{}, new AccountReference[]{}, false, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, null);
        //Then:
        assertThat(responseObj.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void createAccountConsentsWithResponse_ByAccInAccAccess_WoB_Failure() {
        //Given:
        boolean withBalance = false;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(getReferencesArr(WRONG_IBAN), new AccountReference[]{}, new AccountReference[]{}, false, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, null);
        //Then:
        assertThat(responseObj.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void createAccountConsentsWithResponse_ByPsuId_AllAvailable_WB_Success() {
        //Given:
        boolean withBalance = true;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(new AccountReference[]{}, new AccountReference[]{}, new AccountReference[]{}, true, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, CORRECT_PSU_ID);
        CreateConsentResp response = (CreateConsentResp) responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_ByPsuId_AllPsd2_WB_Success() {
        //Given:
        boolean withBalance = true;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(new AccountReference[]{}, new AccountReference[]{}, new AccountReference[]{}, false, true)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, CORRECT_PSU_ID);
        CreateConsentResp response = (CreateConsentResp) responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_ByPsuId_AllAvailable_WB_Failure() {
        //Given:
        boolean withBalance = true;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(new AccountReference[]{}, new AccountReference[]{}, new AccountReference[]{}, true, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, WRONG_PSU_ID);

        //Then:
        assertThat(responseObj.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void createAccountConsentsWithResponse_ByTransactionsInAccAccess_WB_Success() {
        //Given:
        boolean withBalance = true;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(new AccountReference[]{}, new AccountReference[]{}, getReferencesArr(CORRECT_IBAN), false, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, null);
        CreateConsentResp response = (CreateConsentResp) responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_ByBalancesInAccAccess_WB_Success() {
        //Given:
        boolean withBalance = true;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(new AccountReference[]{}, getReferencesArr(CORRECT_IBAN), new AccountReference[]{}, false, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, null);
        CreateConsentResp response = (CreateConsentResp) responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_ByBalancesAndTransactionsInAccAccess_WB_Success() {
        //Given:
        boolean withBalance = true;
        CreateConsentReq req = getCreateCosnentRequest(
            getAccess(new AccountReference[]{}, getReferencesArr(CORRECT_IBAN), getReferencesArr(CORRECT_IBAN_1), false, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, withBalance, false, null);
        CreateConsentResp response = (CreateConsentResp) responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CORRECT_PSU_ID);
    }


    @Test
    public void getAccountConsentsStatusById_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(CORRECT_PSU_ID);
        //Then:
        assertThat(response.getBody()).isEqualTo(TransactionStatus.ACCP);
    }

    @Test
    public void getAccountConsentsStatusById_Failure() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(WRONG_PSU_ID);
        //Then:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void getAccountConsentsById_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentsById(CORRECT_PSU_ID);
        AccountConsent consent = (AccountConsent) response.getBody();
        //Than:
        assertThat(consent.getAccess().getAccounts()[0].getIban()).isEqualTo(CORRECT_IBAN);
    }

    @Test
    public void getAccountConsentsById_Failure() {
        //When:
        ResponseObject response = consentService.getAccountConsentsById(WRONG_PSU_ID);
        //Than:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void deleteAccountConsentsById_Success() {
        //When:
        ResponseObject response = consentService.deleteAccountConsentsById(CORRECT_PSU_ID);
        //Than:
        assertThat(response.hasError()).isEqualTo(false);
    }

    @Test
    public void deleteAccountConsentsById_Failure() {
        //When:
        ResponseObject response = consentService.deleteAccountConsentsById(WRONG_PSU_ID);
        //Than:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    private SpiAccountConsent getSpiConsent(String id, SpiAccountAccess access, boolean withBalance) {
        return new SpiAccountConsent(id, access, false, DATE, 4, null, SpiTransactionStatus.ACCP, SpiConsentStatus.VALID, withBalance, false);
    }

    private SpiAccountAccess getSpiAccountAccess(List<SpiAccountReference> accounts, List<SpiAccountReference> balances, List<SpiAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new SpiAccountAccess(accounts, balances, transactions, allAccounts ? SpiAccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? SpiAccountAccessType.ALL_ACCOUNTS : null);
    }

    private List<SpiAccountReference> getSpiReferensesList(String iban) {
        return Collections.singletonList(getSpiReference(iban, CURRENCY));
    }

    private List<SpiAccountDetails> getSpiDetailsList(String accId, String iban) {
        return Arrays.asList(getSpiDetails(accId, iban, CURRENCY), getSpiDetails("111", iban, CURRENCY_2));
    }

    private SpiAccountReference getSpiReference(String iban, Currency currency) {
        return new SpiAccountReference(iban, null, null, null, null, currency);
    }

    /**
     * Basic test AccountDetails used in all cases
     */
    private SpiAccountDetails getSpiDetails(String accId, String iban, Currency currency) {
        return new SpiAccountDetails(accId, iban, null, null, null, null, currency, null, null, null, null, Collections.emptyList());
    }

    private CreateConsentReq getCreateCosnentRequest(AccountAccess access) {
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(access);
        req.setValidUntil(DATE);
        req.setFrequencyPerDay(4);
        req.setCombinedServiceIndicator(false);
        req.setRecurringIndicator(false);
        return req;
    }

    private AccountAccess getAccess(AccountReference[] accounts, AccountReference[] balances, AccountReference[] transactions, boolean allAccounts, boolean allPsd2) {
        return new AccountAccess(accounts, balances, transactions, allAccounts ? AccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? AccountAccessType.ALL_ACCOUNTS : null);
    }

    private AccountReference[] getReferencesArr(String iban) {
        return new AccountReference[]{getReference(iban, CURRENCY)};
    }

    private AccountReference getReference(String iban, Currency currency) {
        AccountReference ref = new AccountReference();
        ref.setIban(iban);
        ref.setCurrency(currency);
        return ref;
    }
}
