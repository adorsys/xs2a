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

import de.adorsys.aspsp.xs2a.consent.api.ActionStatus;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.AisConsentMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentServiceTest {
    private static final String WRONG_CONSENT_ID = "wrong_consent_id";
    private final String TPP_ID = "This is a test TppId";
    private final String CORRECT_ACCOUNT_ID = "123";
    private final String CORRECT_PSU_ID = "123456789";
    private final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private final String WRONG_PSU_ID = "WRONG PSU ID";
    private final String CORRECT_IBAN = "DE123456789";
    private final String CORRECT_IBAN_1 = "DE987654321";
    private final String WRONG_IBAN = "WRONG IBAN";
    private final Currency CURRENCY = Currency.getInstance("EUR");
    private final Currency CURRENCY_2 = Currency.getInstance("USD");
    private final LocalDate DATE = LocalDate.parse("2019-03-03");
    private final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData("zzzzzzzzzzzzzz".getBytes());

    @InjectMocks
    private ConsentService consentService;

    @Mock
    AccountSpi accountSpi;
    @Mock
    ConsentSpi consentSpi;
    @Mock
    AccountMapper accountMapper;
    @Mock
    AisConsentMapper aisConsentMapper;
    @Mock
    AspspProfileService aspspProfileService;

    @Before
    public void setUp() {
        //AccountMapping
        when(accountMapper.mapToAccountReferencesFromDetails(getSpiDetailsList()))
            .thenReturn(getReferenceList());
        //ConsentMapping
        when(aisConsentMapper.mapToAccountConsent(getSpiConsent(CONSENT_ID, getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false)))
            .thenReturn(getConsent(CONSENT_ID, getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false));
        when(aisConsentMapper.mapToConsentStatus(SpiConsentStatus.RECEIVED)).thenReturn(Optional.of(ConsentStatus.RECEIVED));
        when(aisConsentMapper.mapToConsentStatus(null)).thenReturn(Optional.empty());

        //AisReportMock
        doNothing().when(consentSpi).consentActionLog(anyString(), anyString(), any(ActionStatus.class));
        when(accountSpi.readAccountsByPsuId(CORRECT_PSU_ID, ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(getSpiDetailsList(), ASPSP_CONSENT_DATA));
        when(accountSpi.readAccountsByPsuId(WRONG_PSU_ID, ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(Collections.emptyList(), ASPSP_CONSENT_DATA));
        //ByPSU-ID
        when(consentSpi.createConsent(aisConsentMapper.mapToSpiCreateAisConsentRequest(getCreateConsentRequest(getAccess(Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), Collections.emptyList(), true, false)), CORRECT_PSU_ID, TPP_ID, ASPSP_CONSENT_DATA)))
            .thenReturn(CONSENT_ID);
        when(consentSpi.createConsent(aisConsentMapper.mapToSpiCreateAisConsentRequest(getCreateConsentRequest(getAccess(Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), true, false)), CORRECT_PSU_ID, TPP_ID, ASPSP_CONSENT_DATA)))
            .thenReturn(CONSENT_ID);

        when(consentSpi.createConsent(aisConsentMapper.mapToSpiCreateAisConsentRequest(getCreateConsentRequest(getAccess(getReferenceList(), getReferenceList(), getReferenceList(), false, true)), CORRECT_PSU_ID, TPP_ID, ASPSP_CONSENT_DATA)))
            .thenReturn(CONSENT_ID);
        //ByAccess
        when(consentSpi.createConsent(aisConsentMapper.mapToSpiCreateAisConsentRequest(getCreateConsentRequest(getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList(), false, false)), null, TPP_ID, ASPSP_CONSENT_DATA)))
            .thenReturn(CONSENT_ID);
        when(consentSpi.createConsent(aisConsentMapper.mapToSpiCreateAisConsentRequest(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), null, TPP_ID, ASPSP_CONSENT_DATA)))
            .thenReturn(CONSENT_ID);
        when(consentSpi.createConsent(aisConsentMapper.mapToSpiCreateAisConsentRequest(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), null, TPP_ID, ASPSP_CONSENT_DATA)))
            .thenReturn(CONSENT_ID);
        when(consentSpi.createConsent(aisConsentMapper.mapToSpiCreateAisConsentRequest(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), null, TPP_ID, ASPSP_CONSENT_DATA)))
            .thenReturn(CONSENT_ID);
        when(consentSpi.createConsent(aisConsentMapper.mapToSpiCreateAisConsentRequest(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), null, TPP_ID, ASPSP_CONSENT_DATA)))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), CORRECT_PSU_ID, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)), CORRECT_PSU_ID, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)), CORRECT_PSU_ID, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), null, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), null, TPP_ID))
            .thenReturn(CONSENT_ID);


        //GetAccDetails
        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Arrays.asList(CORRECT_IBAN, CORRECT_IBAN_1)), ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(getSpiDetailsList(), ASPSP_CONSENT_DATA));
        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Arrays.asList(WRONG_IBAN)), ASPSP_CONSENT_DATA)).thenReturn(new SpiResponse<>(Collections.emptyList(), ASPSP_CONSENT_DATA));

        //GetConsentById
        when(consentSpi.getAccountConsentById(CONSENT_ID)).thenReturn(getSpiConsent(CONSENT_ID, getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false));
        when(consentSpi.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(null);

        //GetStatusById
        when(consentSpi.getAccountConsentStatusById(CONSENT_ID))
            .thenReturn(SpiConsentStatus.RECEIVED);
        when(consentSpi.getAccountConsentStatusById(WRONG_CONSENT_ID))
            .thenReturn(null);
        doNothing().when(consentSpi).revokeConsent(anyString());

        when(aspspProfileService.getConsentLifetime())
            .thenReturn(0);

        when(aspspProfileService.getAllPsd2Support())
            .thenReturn(true);

        when(aspspProfileService.isBankOfferedConsentSupported())
            .thenReturn(true);

    }

    @Test
    public void createAccountConsentsWithResponse_Success_AllAccounts() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        //When:
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, CORRECT_PSU_ID);
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_AllPSD2() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)
        );

        //When:
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, CORRECT_PSU_ID);
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Failure_AllPSD2() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)
        );

        //When:
        when(aspspProfileService.getAllPsd2Support())
            .thenReturn(false);

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, CORRECT_PSU_ID);

        MessageError messageError = responseObj.getError();

        //Then:
        assertThat(messageError).isNotNull();
        assertThat(messageError.getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.PARAMETER_NOT_SUPPORTED);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ByAccess_Account() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        //When:
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, null);
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ByAccess_Balances() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)
        );

        //When:
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, null);
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ByAccess_Balances_Transactions() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)
        );

        //When:
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, null);
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Failure() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(WRONG_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, CORRECT_PSU_ID);
        //Then:
        assertThat(responseObj.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void getAccountConsentsStatusById_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID);
        //Then:
        assertThat(response.getBody()).isEqualTo(new ConsentStatusResponse(ConsentStatus.RECEIVED));
    }

    @Test
    public void getAccountConsentsStatusById_Failure() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(WRONG_CONSENT_ID);
        //Then:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void getAccountConsentsById_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentById(CONSENT_ID);
        AccountConsent consent = (AccountConsent) response.getBody();
        //Than:
        assertThat(consent.getAccess().getAccounts().get(0).getIban()).isEqualTo(CORRECT_IBAN);
    }

    @Test
    public void getAccountConsentsById_Failure() {
        //When:
        ResponseObject response = consentService.getAccountConsentById(WRONG_CONSENT_ID);
        //Than:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void deleteAccountConsentsById_Success() {
        //When:
        ResponseObject response = consentService.deleteAccountConsentsById(CONSENT_ID);
        //Than:
        assertThat(response.hasError()).isEqualTo(false);
    }

    @Test
    public void deleteAccountConsentsById_Failure() {
        //When:
        ResponseObject response = consentService.deleteAccountConsentsById(WRONG_CONSENT_ID);
        //Than:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void createAccountConsentWithResponse_Success_BankOfferedConsent() {
        //Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, CORRECT_PSU_ID);
        CreateConsentResponse response = responseObj.getBody();

        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentWithResponse_Failure_BankOfferedConsent() {
        //Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        //When
        when(aspspProfileService.isBankOfferedConsentSupported())
            .thenReturn(false);

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, CORRECT_PSU_ID);
        MessageError messageError = responseObj.getError();

        //Then
        assertThat(messageError).isNotNull();
        assertThat(messageError.getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.PARAMETER_NOT_SUPPORTED);
    }

    /**
     * Basic test AccountDetails used in all cases
     */
    private SpiAccountDetails getSpiDetails(String accId, String iban, Currency currency) {
        return new SpiAccountDetails(accId, iban, null, null, null, null, currency, null, null, null, null, Collections.emptyList());
    }

    private List<SpiAccountDetails> getSpiDetailsList() {
        return Arrays.asList(getSpiDetails(CORRECT_ACCOUNT_ID, CORRECT_IBAN, CURRENCY), getSpiDetails(CORRECT_ACCOUNT_ID, CORRECT_IBAN_1, CURRENCY_2));
    }

    private SpiAccountReference getSpiReference(String iban, Currency currency) {
        return new SpiAccountReference(iban, null, null, null, null, currency);
    }

    private List<SpiAccountReference> getSpiReferensesList(String iban) {
        return Collections.singletonList(getSpiReference(iban, CURRENCY));
    }

    private SpiAccountAccess getSpiAccountAccess(List<SpiAccountReference> accounts, List<SpiAccountReference> balances, List<SpiAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new SpiAccountAccess(accounts, balances, transactions, allAccounts ? SpiAccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? SpiAccountAccessType.ALL_ACCOUNTS : null);
    }

    private AccountConsent getConsent(String id, AccountAccess access, boolean withBalance) {
        return new AccountConsent(id, access, false, DATE, 4, null, ConsentStatus.VALID, withBalance, false);
    }

    private SpiAccountConsent getSpiConsent(String id, SpiAccountAccess access, boolean withBalance) {
        return new SpiAccountConsent(id, access, false, DATE, 4, null, SpiConsentStatus.VALID, withBalance, false);
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

    private List<AccountReference> getReferenceList() {
        List<AccountReference> list = new ArrayList<>();
        list.add(getReference(CORRECT_IBAN, CURRENCY));
        list.add(getReference(CORRECT_IBAN_1, CURRENCY_2));

        return list;
    }

    private AccountReference getReference(String iban, Currency currency) {
        AccountReference ref = new AccountReference();
        ref.setIban(iban);
        ref.setCurrency(currency);
        return ref;
    }
}
