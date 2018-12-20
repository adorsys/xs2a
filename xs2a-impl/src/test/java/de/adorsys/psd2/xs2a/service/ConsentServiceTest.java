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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.CreateConsentRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsentServiceTest {
    private static final String WRONG_CONSENT_ID = "wrong_consent_id";
    private static final String TPP_ID = "Test TppId";
    private static final String CORRECT_ACCOUNT_ID = "123";
    private static final String CORRECT_PSU_ID = "123456789";
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
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
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String PAYMENT_ID = "594ef79c-d785-41ec-9b14-2ea3a7ae2c7b";

    @InjectMocks
    private ConsentService consentService;

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private AisConsentDataService aspspConsentDataService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    SpiToXs2aAccountAccessMapper spiToXs2aAccountAccessMapper;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private TppService tppService;
    @Mock
    private AisConsentSpi aisConsentSpi;
    @Mock
    private CreateConsentRequestValidator createConsentRequestValidator;
    @Mock
    private Xs2aToSpiPsuDataMapper psuDataMapper;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private AisAuthorizationService aisAuthorizationService;
    @Mock
    private PisAuthorisationService pisAuthorisationService;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private PisPsuDataService pisPsuDataService;
    @Mock
    private TppRedirectUriMapper tppRedirectUriMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private AccountReferenceInConsentUpdater accountReferenceUpdater;


    @Before
    public void setUp() {
        //ConsentMapping
        when(aisConsentMapper.mapToAccountConsent(getSpiConsent(CONSENT_ID, getSpiAccountAccessOptional(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false).orElse(null), false)))
            .thenReturn(getConsent(CONSENT_ID, getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false));
        when(spiToXs2aAccountAccessMapper.mapToAccountAccess(any()))
            .thenReturn(Optional.of(getXs2aAccountAccess(Collections.singletonList(getXs2aReference(CORRECT_IBAN, CURRENCY)), null, null, false, false)));

        //AisReportMock
        doNothing().when(aisConsentService).consentActionLog(anyString(), anyString(), any(ActionStatus.class));
        //ByPSU-ID
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);

        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(getReferenceList(), getReferenceList(), getReferenceList(), false, true)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        //ByAccess
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList(), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, buildTppInfo()))
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
        when(tppService.getTppInfo()).thenReturn(buildTppInfo());

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

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false)))
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ShouldRecordEvent() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false)))
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .success());

        // When
        consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());

        // Then
        verify(xs2aEventService, times(1)).recordTppRequest(argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED);
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

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false)))
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
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
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());

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
        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false)))
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
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

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false)))
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
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

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false)))
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
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
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        //Then:
        assertThat(responseObj.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void getAccountConsentsStatusById_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID);
        //Then:
        assertThat(response.getBody()).isEqualTo(new ConsentStatusResponse(ConsentStatus.VALID));
    }

    @Test
    public void getAccountConsentsStatusById_Success_ShouldRecordEvent() {
        //Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        //When:
        consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_AIS_CONSENT_STATUS_REQUEST_RECEIVED);
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
    public void getAccountConsentsById_Success_ShouldRecordEvent() {
        //Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.getAccountConsentById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_AIS_CONSENT_REQUEST_RECEIVED);
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
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .success());

        ResponseObject response = consentService.deleteAccountConsentsById(CONSENT_ID);
        //Than:
        assertThat(response.hasError()).isEqualTo(false);
    }

    @Test
    public void deleteAccountConsentsById_Success_ShouldRecordEvent() {
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .success());

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.DELETE_AIS_CONSENT_REQUEST_RECEIVED);
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

        when(createConsentRequestValidator.validateRequest(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false)))
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
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
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        MessageError messageError = responseObj.getError();

        //Then
        assertThat(messageError).isNotNull();
        assertThat(messageError.getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.PARAMETER_NOT_SUPPORTED);
    }

    @Test
    public void createConsentAuthorizationWithResponse_Success_ShouldRecordEvent() {
        when(aisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(new CreateConsentAuthorizationResponse()));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.createConsentAuthorizationWithResponse(PSU_ID_DATA, CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void getValidateConsent_DateValidAfter() {
        //When
        ResponseObject<AccountConsent> xs2aAccountAccessResponseObject = consentService.getValidatedConsent(CONSENT_ID_DATE_VALID_YESTERDAY);
        //Then
        assertThat(xs2aAccountAccessResponseObject.getBody()).isNull();
        assertThat(xs2aAccountAccessResponseObject.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void updateConsentPsuData_Success_ShouldRecordEvent() {
        when(aisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(new CreateConsentAuthorizationResponse()));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq();

        // When
        consentService.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED);
    }

    @Test
    public void createPisConsentAuthorization_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.createConsentAuthorisation(PAYMENT_ID, PaymentType.SINGLE, PSU_ID_DATA))
            .thenReturn(Optional.of(new Xsa2CreatePisConsentAuthorisationResponse(null, null, null)));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.createPisConsentAuthorization(PAYMENT_ID, PaymentType.SINGLE, PSU_ID_DATA);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void updatePisConsentPsuData_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.updateConsentPsuData(any()))
            .thenReturn(new Xs2aUpdatePisConsentPsuDataResponse(ScaStatus.STARTED));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        Xs2aUpdatePisConsentPsuDataRequest request = buildXs2aUpdatePisConsentPsuDataRequest();

        // When
        consentService.updatePisConsentPsuData(request);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_REQUEST_RECEIVED);
    }

    @Test
    public void createPisConsentCancellationAuthorization_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.createConsentCancellationAuthorisation(anyString(), any(), any()))
            .thenReturn(Optional.of(new Xs2aCreatePisConsentCancellationAuthorisationResponse(null, null, null)));
        when(pisPsuDataService.getPsuDataByPaymentId(anyString())).thenReturn(PSU_ID_DATA);

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.createPisConsentCancellationAuthorization(PAYMENT_ID, PaymentType.SINGLE);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void updatePisConsentCancellationPsuData_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.updateConsentCancellationPsuData(any()))
            .thenReturn(new Xs2aUpdatePisConsentPsuDataResponse(ScaStatus.STARTED));

        // Given:
        Xs2aUpdatePisConsentPsuDataRequest request = buildXs2aUpdatePisConsentPsuDataRequest();
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.updatePisConsentCancellationPsuData(request);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_REQUEST_RECEIVED);
    }

    @Test
    public void getPaymentInitiationCancellationAuthorisationInformation_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.getCancellationAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aPaymentCancellationAuthorisationSubResource(Collections.emptyList())));

        // Given:
        Xs2aUpdatePisConsentPsuDataRequest request = buildXs2aUpdatePisConsentPsuDataRequest();
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.getPaymentInitiationCancellationAuthorisationInformation(PAYMENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);
    }


    @Test
    public void getPaymentInitiationAuthorisation() {
        when(pisScaAuthorisationService.getAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aAuthorisationSubResources(Collections.singletonList(PAYMENT_ID))));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        ResponseObject<Xs2aAuthorisationSubResources> paymentInitiationAuthorisation = consentService.getPaymentInitiationAuthorisations(PAYMENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);

        assertThat(paymentInitiationAuthorisation.getBody()).isNotNull();
        List<String> authorisationIds = paymentInitiationAuthorisation.getBody().getAuthorisationIds();
        assertFalse(authorisationIds.isEmpty());
        assertThat(authorisationIds.get(0)).isEqualTo(PAYMENT_ID);
    }

    @Test
    public void getConsentInitiationAuthorisation() {
        when(aisAuthorizationService.getAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aAuthorisationSubResources(Collections.singletonList(CONSENT_ID))));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        ResponseObject<Xs2aAuthorisationSubResources> paymentInitiationAuthorisation = consentService.getConsentInitiationAuthorisations(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        assertThat(paymentInitiationAuthorisation.getBody()).isNotNull();
        List<String> authorisationIds = paymentInitiationAuthorisation.getBody().getAuthorisationIds();
        assertFalse(authorisationIds.isEmpty());
        assertThat(authorisationIds.get(0)).isEqualTo(CONSENT_ID);
    }

    @Test
    public void getConsentAuthorisationScaStatus_success() {
        when(aisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));

        // When
        ResponseObject<ScaStatus> actual = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertFalse(actual.hasError());
        assertEquals(ScaStatus.RECEIVED, actual.getBody());
    }

    @Test
    public void getConsentAuthorisationScaStatus_success_shouldRecordEvent() {
        when(aisAuthorizationService.getAuthorisationScaStatus(any(), any()))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_CONSENT_SCA_STATUS_REQUEST_RECEIVED);
    }

    @Test
    public void getConsentAuthorisationScaStatus_failure() {
        when(aisAuthorizationService.getAuthorisationScaStatus(WRONG_CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<ScaStatus> actual = consentService.getConsentAuthorisationScaStatus(WRONG_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertNull(actual.getBody());
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

    private Optional<SpiAccountAccess> getSpiAccountAccessOptional(List<SpiAccountReference> accounts, List<SpiAccountReference> balances, List<SpiAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return Optional.of(getSpiAccountAccess(accounts,balances, transactions, allAccounts, allPsd2));
    }

    private SpiAccountAccess getSpiAccountAccess(List<SpiAccountReference> accounts, List<SpiAccountReference> balances, List<SpiAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new SpiAccountAccess(accounts, balances, transactions, allAccounts ? SpiAccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? SpiAccountAccessType.ALL_ACCOUNTS : null);
    }

    private Xs2aAccountAccess getXs2aAccountAccess(List<Xs2aAccountReference> accounts, List<Xs2aAccountReference> balances, List<Xs2aAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new Xs2aAccountAccess(accounts, balances, transactions, allAccounts ? Xs2aAccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? Xs2aAccountAccessType.ALL_ACCOUNTS : null);
    }

    private AccountConsent getConsent(String id, Xs2aAccountAccess access, boolean withBalance) {
        return new AccountConsent(id, access, false, DATE, 4, null, ConsentStatus.VALID, withBalance, false, null, buildTppInfo());
    }

    private SpiAccountConsent getSpiConsent(String consentId, SpiAccountAccess access, boolean withBalance) {
        return new SpiAccountConsent(consentId, access, false, DATE, 4, null, ConsentStatus.VALID, withBalance, false, null, buildTppInfo());
    }

    private AccountConsent getAccountConsent(String consentId, Xs2aAccountAccess access, boolean withBalance) {
        return new AccountConsent(consentId, access, false, DATE, 4, null, ConsentStatus.VALID, withBalance, false, null, buildTppInfo());
    }

    private AccountConsent getAccountConsentDateValidYesterday(String consentId, Xs2aAccountAccess access, boolean withBalance) {
        return new AccountConsent(consentId, access, false, YESTERDAY, 4, null, ConsentStatus.VALID, withBalance, false, null, buildTppInfo());
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

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq() {
        UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
        request.setAuthorizationId(AUTHORISATION_ID);
        request.setConsentId(CONSENT_ID);
        return request;
    }

    private Xs2aUpdatePisConsentPsuDataRequest buildXs2aUpdatePisConsentPsuDataRequest() {
        Xs2aUpdatePisConsentPsuDataRequest request = new Xs2aUpdatePisConsentPsuDataRequest();
        request.setAuthorizationId(AUTHORISATION_ID);
        request.setPaymentId(PAYMENT_ID);
        return request;
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_ID);
        tppInfo.setTppRedirectUri(buildTppRedirectUri());
        return tppInfo;
    }

    private TppRedirectUri buildTppRedirectUri() {
        return new TppRedirectUri("redirectUri", "nokRedirectUri");
    }
}
