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
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.service.consent.ais.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentServiceTest {
    private final String TPP_ID = "This is a test TppId";
    private final String CORRECT_PSU_ID = "123456789";
    private final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private final String WRONG_PSU_ID = "WRONG PSU ID";
    private final String CORRECT_IBAN = "DE123456789";
    private final String CORRECT_IBAN_1 = "DE987654321";
    private final String WRONG_IBAN = "WRONG IBAN";
    private final Currency CURRENCY = Currency.getInstance("EUR");
    private final Currency CURRENCY_2 = Currency.getInstance("USD");
    private final Date DATE = new Date(321554477);

    @Autowired
    private ConsentService consentService;
    @Autowired
    private ConsentMapper mapper;

    @MockBean(name = "consentSpi")
    ConsentSpiImpl consentSpi;
    @MockBean(name = "aisConsent")
    AisConsentService aisConsent;
    @MockBean(name = "accountSpi")
    AccountSpi accountSpi;

    @Before
    public void setUp() {
        when(consentSpi.getAccountConsentStatusById(CONSENT_ID))
            .thenReturn(SpiConsentStatus.RECEIVED);
        when(aisConsent.createConsent(mapper.mapToAisConsentRequest(getCreateConsentRequest(
            getAccess(getReferenceList(CORRECT_IBAN, CURRENCY), Collections.emptyList(), Collections.emptyList(), false, false)
        ), CORRECT_PSU_ID, TPP_ID)))
            .thenReturn(CONSENT_ID);
        when(aisConsent.createConsent(mapper.mapToAisConsentRequest(
            getCreateConsentRequest(
                getAccess(getReferenceList(WRONG_IBAN, CURRENCY), Collections.emptyList(), Collections.emptyList(), false, false)), CORRECT_PSU_ID, TPP_ID)))
            .thenReturn(null);
        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Collections.singletonList(CORRECT_IBAN)))).thenReturn(getSpiDetailsList("1", CORRECT_IBAN));
        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Arrays.asList(CORRECT_IBAN, CORRECT_IBAN_1)))).thenReturn(Arrays.asList(getSpiDetails("1", CORRECT_IBAN, CURRENCY), getSpiDetails("2", CORRECT_IBAN_1, CURRENCY)));
        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Collections.singletonList(WRONG_IBAN)))).thenReturn(null);
        when(accountSpi.readAccountsByPsuId(CORRECT_PSU_ID)).thenReturn(getSpiDetailsList("1", CORRECT_IBAN));
        when(accountSpi.readAccountsByPsuId(WRONG_PSU_ID)).thenReturn(Collections.emptyList());

        when(consentSpi.getAccountConsentById(CORRECT_PSU_ID)).thenReturn(getSpiConsent(CORRECT_PSU_ID, getSpiAccountAccess(getSpiReferensesList(CORRECT_IBAN), null, null, false, false), false));
        when(consentSpi.getAccountConsentById(WRONG_PSU_ID)).thenReturn(null);

        consentSpi.deleteAccountConsentById(anyString());
    }

    @Test
    public void createAccountConsentsWithResponse_Success() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(getReferenceList(CORRECT_IBAN, CURRENCY), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        //When:
        ResponseObject<CreateConsentResp> responseObj = consentService.createAccountConsentsWithResponse(
            req, false, false, CORRECT_PSU_ID);
        CreateConsentResp response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isNotEmpty();
    }

    @Test
    public void createAccountConsentsWithResponse_Failure() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(getReferenceList(WRONG_IBAN, CURRENCY), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, false, false, CORRECT_PSU_ID);
        //Then:
        // TODO clarify
    }

    @Test
    public void getAccountConsentsStatusById_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID);
        //Then:
        assertThat(response.getBody()).isEqualTo(ConsentStatus.RECEIVED);
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
        ResponseObject response = consentService.getAccountConsentById(CORRECT_PSU_ID);
        AccountConsent consent = (AccountConsent) response.getBody();
        //Than:
        assertThat(consent.getAccess().getAccounts().get(0).getIban()).isEqualTo(CORRECT_IBAN);
    }

    @Test
    public void getAccountConsentsById_Failure() {
        //When:
        ResponseObject response = consentService.getAccountConsentById(WRONG_PSU_ID);
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
        return new SpiAccountConsent(id, access, false, DATE, 4, null, SpiConsentStatus.VALID, withBalance, false);
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

    private CreateConsentReq getCreateConsentRequest(AccountAccess access) {
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(access);
        req.setValidUntil(DATE);
        req.setFrequencyPerDay(4);
        req.setCombinedServiceIndicator(false);
        req.setRecurringIndicator(false);
        return req;
    }

    private AccountAccess getAccess(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new AccountAccess(accounts, balances, transactions, allAccounts ? AccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? AccountAccessType.ALL_ACCOUNTS : null);
    }

    private List<AccountReference> getReferenceList(String iban, Currency currency) {
        List<AccountReference> list = new ArrayList<>();
        list.add(getReference(iban, currency));

        return list;
    }

    private AccountReference getReference(String iban, Currency currency) {
        AccountReference ref = new AccountReference();
        ref.setIban(iban);
        ref.setCurrency(currency);
        return ref;
    }
}
