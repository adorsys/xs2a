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

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.service.validator.CreateConsentRequestValidator;
import de.adorsys.aspsp.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentServiceTest {
    private static final String WRONG_CONSENT_ID = "wrong_consent_id";
    private static final String TPP_ID = "Test TppId";
    private static final String CORRECT_ACCOUNT_ID = "123";
    private static final String CORRECT_PSU_ID = "123456789";
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String WRONG_PSU_ID = "WRONG PSU ID";
    private static final String CORRECT_IBAN = "DE123456789";
    private static final String CORRECT_IBAN_1 = "DE987654321";
    private static final String WRONG_IBAN = "WRONG IBAN";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Currency CURRENCY_2 = Currency.getInstance("USD");
    private static final LocalDate DATE = LocalDate.parse("2019-03-03");
    private static final boolean EXPLICIT_PREFERRED = true;
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final String CONSENT_ID_DATE_VALID_YESTERDAY = "c966f143-f6a2-41db-9036-8abaeeef3af8";
    private static final LocalDate YESTERDAY = LocalDate.now().minus(Period.ofDays(1));
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(CORRECT_PSU_ID, null, null, null);

    @InjectMocks
    private ConsentService consentService;

    @Mock
    AisConsentService aisConsentService;
    @Mock
    AisConsentDataService aspspConsentDataService;
    @Mock
    Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    AspspProfileServiceWrapper aspspProfileService;
    @Mock
    TppService tppService;
    @Mock
    AisConsentSpi aisConsentSpi;
    @Mock
    CreateConsentRequestValidator createConsentRequestValidator;
    @Mock
    Xs2aToSpiPsuDataMapper psuDataMapper;

    @Before
    public void setUp() {
        //ConsentMapping
        when(aisConsentMapper.mapToAccountConsent(getSpiConsent(CONSENT_ID, getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false)))
            .thenReturn(getConsent(CONSENT_ID, getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false));

        //AisReportMock
        doNothing().when(aisConsentService).consentActionLog(anyString(), anyString(), any(ActionStatus.class));
        //ByPSU-ID
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);

        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(getReferenceList(), getReferenceList(), getReferenceList(), false, true)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        //ByAccess
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList(), false, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, TPP_ID))
            .thenReturn(CONSENT_ID);

        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(getAccountConsent(CONSENT_ID, getXs2aAccountAccess(Collections.singletonList(getXs2aReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false));
        when(aisConsentService.getAccountConsentById(CONSENT_ID_DATE_VALID_YESTERDAY)).thenReturn(getAccountConsentDateValidYesterday(CONSENT_ID_DATE_VALID_YESTERDAY, getXs2aAccountAccess(Collections.singletonList(getXs2aReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false));
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(null);

        //GetStatusById
        when(aisConsentService.getAccountConsentStatusById(CONSENT_ID))
            .thenReturn(ConsentStatus.RECEIVED);
        when(aisConsentService.getAccountConsentStatusById(WRONG_CONSENT_ID))
            .thenReturn(null);
        doNothing().when(aisConsentService).revokeConsent(anyString());

        when(aspspProfileService.getConsentLifetime())
            .thenReturn(0);

        when(aspspProfileService.getAllPsd2Support())
            .thenReturn(true);

        when(aspspProfileService.isBankOfferedConsentSupported())
            .thenReturn(true);
        when(tppService.getTppId()).thenReturn(TPP_ID);

        when(aspspConsentDataService.getAspspConsentDataByConsentId(anyString()))
            .thenReturn(ASPSP_CONSENT_DATA);

        doNothing().when(aspspConsentDataService).updateAspspConsentData(any(AspspConsentData.class));

        when(psuDataMapper.mapToSpiPsuData(PSU_ID_DATA))
            .thenReturn(SPI_PSU_DATA);

    }

    @Test
    public void createAccountConsentsWithResponse_Success_AllAccounts() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        //When:
        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiPsuData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder().success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
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
        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiPsuData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder().success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
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
        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(false, createMessageError(MessageErrorCode.PARAMETER_NOT_SUPPORTED)));

        when(aspspProfileService.getAllPsd2Support())
            .thenReturn(false);

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        MessageError messageError = responseObj.getError();

        //Then:
        assertThat(messageError).isNotNull();
        assertThat(messageError.getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);

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
        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiPsuData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder().success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
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
        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiPsuData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder().success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
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
        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiPsuData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder().success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
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
        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(true, null));

        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        //Then:
        assertThat(responseObj.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
    }

    @Test
    public void getAccountConsentsStatusById_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID);
        //Then:
        assertThat(response.getBody()).isEqualTo(new ConsentStatusResponse(ConsentStatus.VALID));
    }

    @Test
    public void getAccountConsentsStatusById_Failure() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(WRONG_CONSENT_ID);
        //Then:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
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
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
    }

    @Test
    public void deleteAccountConsentsById_Success() {
        //When:
        when(aisConsentSpi.revokeAisConsent(any(SpiPsuData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder().success());

        ResponseObject response = consentService.deleteAccountConsentsById(CONSENT_ID);
        //Than:
        assertThat(response.hasError()).isEqualTo(false);
    }

    @Test
    public void deleteAccountConsentsById_Failure() {
        //When:
        ResponseObject response = consentService.deleteAccountConsentsById(WRONG_CONSENT_ID);
        //Than:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
    }

    @Test
    public void createAccountConsentWithResponse_Success_BankOfferedConsent() {
        //Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiPsuData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder().success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
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
        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(false, createMessageError(MessageErrorCode.PARAMETER_NOT_SUPPORTED)));

        when(aspspProfileService.isBankOfferedConsentSupported())
            .thenReturn(false);

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        MessageError messageError = responseObj.getError();

        //Then
        assertThat(messageError).isNotNull();
        assertThat(messageError.getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.PARAMETER_NOT_SUPPORTED);
    }

    @Test
    public void getValidateConsent_DateValidAfter() {
        //When
        ResponseObject<AccountConsent> xs2aAccountAccessResponseObject = consentService.getValidatedConsent(CONSENT_ID_DATE_VALID_YESTERDAY);
        //Then
        assertThat(xs2aAccountAccessResponseObject.getBody()).isNull();
        assertThat(xs2aAccountAccessResponseObject.getError().getTransactionStatus()).isEqualTo(Xs2aTransactionStatus.RJCT);
    }

    /**
     * Basic test AccountDetails used in all cases
     */
    private SpiAccountDetails getSpiDetails(String accId, String iban, Currency currency) {
        return new SpiAccountDetails(accId, iban, null, null, null, null, currency, null, null, null, null, null, null, null, null, Collections.emptyList());
    }

    private List<SpiAccountDetails> getSpiDetailsList() {
        return Arrays.asList(getSpiDetails(CORRECT_ACCOUNT_ID, CORRECT_IBAN, CURRENCY), getSpiDetails(CORRECT_ACCOUNT_ID, CORRECT_IBAN_1, CURRENCY_2));
    }

    private SpiAccountReference getSpiReference(String iban, Currency currency) {
        return new SpiAccountReference(null, iban, null, null, null, null, currency);
    }

    private Xs2aAccountReference getXs2aReference(String iban, Currency currency) {
        return new Xs2aAccountReference(null, iban, null, null, null, null, currency);
    }

    private List<SpiAccountReference> getSpiReferensesList(String iban) {
        return Collections.singletonList(getSpiReference(iban, CURRENCY));
    }

    private SpiAccountAccess getSpiAccountAccess(List<SpiAccountReference> accounts, List<SpiAccountReference> balances, List<SpiAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new SpiAccountAccess(accounts, balances, transactions, allAccounts ? SpiAccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? SpiAccountAccessType.ALL_ACCOUNTS : null);
    }

    private Xs2aAccountAccess getXs2aAccountAccess(List<Xs2aAccountReference> accounts, List<Xs2aAccountReference> balances, List<Xs2aAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new Xs2aAccountAccess(accounts, balances, transactions, allAccounts ? Xs2aAccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? Xs2aAccountAccessType.ALL_ACCOUNTS : null);
    }

    private AccountConsent getConsent(String id, Xs2aAccountAccess access, boolean withBalance) {
        return new AccountConsent(id, access, false, DATE, 4, null, ConsentStatus.VALID, withBalance, false, null, TPP_ID);
    }

    private SpiAccountConsent getSpiConsent(String consentId, SpiAccountAccess access, boolean withBalance) {
        return new SpiAccountConsent(consentId, access, false, DATE, 4, null, ConsentStatus.VALID, withBalance, false, null, TPP_ID);
    }

    private AccountConsent getAccountConsent(String consentId, Xs2aAccountAccess access, boolean withBalance) {
        return new AccountConsent(consentId, access, false, DATE, 4, null, ConsentStatus.VALID, withBalance, false, null, TPP_ID);
    }

    private AccountConsent getAccountConsentDateValidYesterday(String consentId, Xs2aAccountAccess access, boolean withBalance) {
        return new AccountConsent(consentId, access, false, YESTERDAY, 4, null, ConsentStatus.VALID, withBalance, false, null, TPP_ID);
    }

    private CreateConsentReq getCreateConsentRequest(Xs2aAccountAccess access) {
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(access);
        req.setValidUntil(DATE);
        req.setFrequencyPerDay(4);
        req.setCombinedServiceIndicator(false);
        req.setRecurringIndicator(false);
        return req;
    }

    private Xs2aAccountAccess getAccess(List<Xs2aAccountReference> accounts, List<Xs2aAccountReference> balances, List<Xs2aAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new Xs2aAccountAccess(accounts, balances, transactions, allAccounts ? Xs2aAccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? Xs2aAccountAccessType.ALL_ACCOUNTS : null);
    }

    private List<Xs2aAccountReference> getReferenceList() {
        List<Xs2aAccountReference> list = new ArrayList<>();
        list.add(getReference(CORRECT_IBAN, CURRENCY));
        list.add(getReference(CORRECT_IBAN_1, CURRENCY_2));

        return list;
    }

    private Xs2aAccountReference getReference(String iban, Currency currency) {
        Xs2aAccountReference ref = new Xs2aAccountReference();
        ref.setIban(iban);
        ref.setCurrency(currency);
        return ref;
    }

    private ValidationResult createValidationResult(boolean isValid, MessageError messageError) {
        return new ValidationResult(isValid, messageError);
    }

    private MessageError createMessageError(MessageErrorCode errorCode) {
        return new MessageError(new TppMessageInformation(MessageCategory.ERROR, errorCode));
    }
}
