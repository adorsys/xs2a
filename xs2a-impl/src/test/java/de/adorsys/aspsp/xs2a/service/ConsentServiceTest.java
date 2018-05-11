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

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.ais.consent.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentServiceTest {
    private final String CORRECT_PSU_ID = "123456789";
    private final String WRONG_PSU_ID = "987654321";
    private final String CORRECT_IBAN = "DE123456789";
    private final String WRONG_IBAN = "DE987654321";
    private final String ACCOUNT_ID = "666";
    private final Currency CURRENCY = Currency.getInstance("EUR");
    private final Date DATE = new Date(321554477);

    @Autowired
    private ConsentService consentService;

    @MockBean(name = "consentSpi")
    ConsentSpiImpl consentSpi;
    @MockBean(name = "accountSpi")
    AccountSpi accountSpi;

    @Before
    public void setUp() {
        when(consentSpi.createAccountConsents(
            getConsent(new AccountReference[]{gtRef()}, null, null, true, false, false)))
            .thenReturn(CORRECT_PSU_ID);
        /*when(consentSpi.createAccountConsents(any()))
            .thenReturn(CORRECT_PSU_ID);*/
        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Collections.singletonList(CORRECT_IBAN)))).thenReturn(getSpiDetailsList());
        when(consentSpi.getAccountConsentById(CORRECT_PSU_ID)).thenReturn(null);
        when(consentSpi.getAccountConsentById(WRONG_PSU_ID)).thenReturn(null);
        consentSpi.deleteAccountConsentsById(anyString());
        when(accountSpi.readAccountsByPsuId(CORRECT_PSU_ID)).thenReturn(getSpiDetailsList());
        when(accountSpi.readAccountsByPsuId(WRONG_PSU_ID)).thenReturn(Collections.emptyList());
        consentSpi.expireConsent(any());
    }
private AccountReference gtRef(){
    AccountReference ref = new AccountReference();
    ref.setAccountId(any());
    ref.setIban(CORRECT_IBAN);
    ref.setCurrency(CURRENCY);
    return ref;
}
    @Test
    public void createAccountConsentsWithResponse_ByAccInAccAccess_WB_Success() {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            consentReq_byAccount(getReferences(), null, null), withBalance, tppRedirectPreferred, null);
        CreateConsentResp response = (CreateConsentResp) responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Failure() {
        //Given:
        boolean withBalance = false;
        boolean tppRedirectPreferred = false;
        //When:
       /* ResponseObject response = consentService.createAccountConsentsWithResponse();
        //Then:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);*/
    }


    @Test
    public void getAccountConsentsStatusById_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(CORRECT_PSU_ID);
        //Then:
        assertThat(response.getBody()).isEqualTo(TransactionStatus.RCVD);
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
        assertThat(consent.getId()).isEqualTo(CORRECT_PSU_ID);
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
        assertThat(response.getBody()).isEqualTo(true);
    }

    @Test
    public void deleteAccountConsentsById_Failure() {
        //When:
        ResponseObject response = consentService.deleteAccountConsentsById(WRONG_PSU_ID);
        //Than:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    //TRUE req
    private CreateConsentReq consentReq_byPsuId(String psuId, AccountAccessType allAccounts, AccountAccessType allPsd2) {
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(createAccountAccess(null, null, null, allAccounts, allPsd2));
        req.setValidUntil(DATE);
        req.setFrequencyPerDay(4);
        req.setRecurringIndicator(true);
        req.setCombinedServiceIndicator(false);
        return req;
    }

    //TRUE req
    private CreateConsentReq consentReq_byAccount(AccountReference[] accounts, AccountReference[] balances, AccountReference[] transactions) {
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(createAccountAccess(accounts, balances, transactions, null, null));
        req.setValidUntil(DATE);
        req.setFrequencyPerDay(4);
        req.setRecurringIndicator(true);
        req.setCombinedServiceIndicator(false);
        return req;
    }

    //TRUE Access
    private AccountAccess createAccountAccess(AccountReference[] accounts, AccountReference[] balances, AccountReference[] transactions, AccountAccessType allAccounts, AccountAccessType allPsd2) {
        return new AccountAccess(accounts,balances,transactions,allAccounts,allPsd2);
    }

    private SpiAccountConsent getConsent(AccountReference[] accounts, AccountReference[] balances, AccountReference[] transactions,
                                         boolean withBalance, boolean allAccounts, boolean allPsd2) {
        SpiAccountAccess acc = mapToSpiAccountAccess(
            createAccountAccess(
                mapAccountsForAccess(accounts, balances, transactions),
                balances, transactions, allAccounts ? AccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? AccountAccessType.ALL_ACCOUNTS : null));

        return new SpiAccountConsent(null, acc, true, DATE, 4, null, SpiTransactionStatus.ACCP, SpiConsentStatus.VALID, withBalance, false);
    }

    private SpiAccountAccess mapToSpiAccountAccess(AccountAccess access) {
        return new SpiAccountAccess(mapToSpiAccountReferenceList(access.getAccounts()),
            mapToSpiAccountReferenceList(access.getBalances()),
            mapToSpiAccountReferenceList(access.getTransactions()),
            access.getAvailableAccounts() != null ? SpiAccountAccessType.valueOf(access.getAvailableAccounts().name()) : null,
            access.getAllPsd2() != null ? SpiAccountAccessType.valueOf(access.getAllPsd2().name()) : null);
    }

    private List<SpiAccountReference> mapToSpiAccountReferenceList(AccountReference[] accounts) {
        return Optional.ofNullable(accounts)
                   .map(Arrays::stream)
                   .map(ar -> ar
                                  .map(a -> new SpiAccountReference(a.getAccountId(), a.getIban(), a.getBban(), a.getPan(), a.getMaskedPan(), a.getMsisdn(), a.getCurrency()))
                                  .collect(Collectors.toList()))
                   .orElse(null);
    }

    //Initial details As Array
    private AccountDetails[] getDetails() {
        return new AccountDetails[]{
            new AccountDetails(ACCOUNT_ID, CORRECT_IBAN, null, null, null, null, CURRENCY,
                "David", null, null, null, new ArrayList<>(), new Links())};
    }

    //Initial details mapped from previous
    private List<SpiAccountDetails> getSpiDetailsList() {
        return Optional.of(Arrays.stream(getDetails())
                               .map(ad -> new SpiAccountDetails(ad.getId(), ad.getIban(), ad.getBban(), ad.getPan(),
                                   ad.getMaskedPan(), ad.getMsisdn(), ad.getCurrency(), ad.getName(), ad.getAccountType(),
                                   Optional.ofNullable(ad.getCashAccountType()).map(at -> SpiAccountType.valueOf(at.name())).orElse(null), ad.getBic(), balances(ad.getBalances())))
                               .collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    private AccountReference[] getReferences() {
        return new AccountReference[]{getReference()};
    }

    //Not used in current
    private List<SpiBalances> balances(List<Balances> list) {
        return list.stream().map(b -> {
            SpiBalances bal = new SpiBalances();
            bal.setAuthorised(mapBalance(b.getAuthorised()));
            bal.setExpected(mapBalance(b.getExpected()));
            bal.setOpeningBooked(mapBalance(b.getOpeningBooked()));
            bal.setClosingBooked(mapBalance(b.getClosingBooked()));
            bal.setInterimAvailable(mapBalance(b.getInterimAvailable()));
            return bal;
        }).collect(Collectors.toList());

    }

    //Not used in current
    private SpiAccountBalance mapBalance(SingleBalance balance) {
        return Optional.of(balance).map(b -> {
            SpiAccountBalance bal = new SpiAccountBalance();
            bal.setDate(Date.from(b.getDate()));
            bal.setLastActionDateTime(Date.from(b.getLastActionDateTime()));
            bal.setSpiAmount(new SpiAmount(b.getAmount().getCurrency(), b.getAmount().getContent()));
            return bal;
        }).orElse(new SpiAccountBalance());
    }

    private AccountReference getReference() {
        AccountReference ref = new AccountReference();
        ref.setAccountId(getDetails()[0].getId());
        ref.setIban(getDetails()[0].getIban());
        ref.setBban(getDetails()[0].getBban());
        ref.setPan(getDetails()[0].getPan());
        ref.setMaskedPan(getDetails()[0].getMaskedPan());
        ref.setMsisdn(getDetails()[0].getMsisdn());
        ref.setCurrency(getDetails()[0].getCurrency());
        return ref;
    }

    private AccountReference[] mapAccountsForAccess(AccountReference[] accounts, AccountReference[] balances, AccountReference[] transactions) {
        accounts = Optional.ofNullable(balances).orElse(accounts);
        return Optional.ofNullable(transactions).orElse(accounts);
    }

}
