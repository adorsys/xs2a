/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.ais.RedirectAisAuthorizationService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.LoggingContextService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.AisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.CommonConsentObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.*;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.UpdateConsentPsuDataRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAisConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsentServiceTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String WRONG_CONSENT_ID = "wrong_consent_id";
    private static final String PASSWORD = "password";
    private static final String CONSENT_ID_FINALISED = "finalised_consent_id";
    private static final String TPP_ID = "Test TppId";
    private static final String CORRECT_PSU_ID = "123456789";
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String CORRECT_IBAN = "DE123456789";
    private static final String CORRECT_IBAN_1 = "DE987654321";
    private static final String WRONG_IBAN = "WRONG IBAN";
    private static final String TEST_PSU_MESSAGE = "This test message is created in ASPSP and directed to PSU";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Currency CURRENCY_2 = Currency.getInstance("USD");
    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final boolean EXPLICIT_PREFERRED = true;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null);
    private static final PsuIdData PSU_ID_DATA_EMPTY = new PsuIdData(null, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(CORRECT_PSU_ID, null, null, null, null);
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.MAX;
    private static final MessageError VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(MessageErrorCode.CONSENT_INVALID));
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, new TppInfo(), UUID.randomUUID(), UUID.randomUUID());
    private static final MessageError CONSENT_UNKNOWN_403_ERROR =
        new MessageError(ErrorType.AIS_403, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403));
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private ConsentService consentService;

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private SpiToXs2aAccountAccessMapper spiToXs2aAccountAccessMapper;
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private TppService tppService;
    @Mock
    private AisConsentSpi aisConsentSpi;
    @Mock
    private CreateConsentRequestValidator createConsentRequestValidator;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private AccountReferenceInConsentUpdater accountReferenceUpdater;
    @Mock
    private RedirectAisAuthorizationService redirectAisAuthorizationService;
    @Mock
    private AisEndpointAccessCheckerService endpointAccessCheckerService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private GetAccountConsentsStatusByIdValidator getAccountConsentsStatusByIdValidator;
    @Mock
    private GetAccountConsentByIdValidator getAccountConsentByIdValidator;
    @Mock
    private DeleteAccountConsentsByIdValidator deleteAccountConsentsByIdValidator;
    @Mock
    private CreateConsentAuthorisationValidator createConsentAuthorisationValidator;
    @Mock
    private UpdateConsentPsuDataValidator updateConsentPsuDataValidator;
    @Mock
    private GetConsentAuthorisationsValidator getConsentAuthorisationsValidator;
    @Mock
    private GetConsentAuthorisationScaStatusValidator getConsentAuthorisationScaStatusValidator;
    @Mock
    private AisScaAuthorisationService aisScaAuthorisationService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private InitialSpiAspspConsentDataProvider initialSpiAspspConsentDataProvider;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private AisAuthorisationValidator aisAuthorisationValidator;
    @Mock
    private LoggingContextService loggingContextService;

    private AccountConsent accountConsent;

    private TppInfo tppInfo;

    @Before
    public void setUp() {
        //ConsentMapping
        when(spiToXs2aAccountAccessMapper.mapToAccountAccess(any()))
            .thenReturn(Optional.of(getXs2aAccountAccess(Collections.singletonList(getXs2aReference()))));

        //ByPSU-ID
        tppInfo = buildTppInfo();

        accountConsent = getAccountConsent();

        //ByAccess
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList(), false, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)), PSU_ID_DATA, tppInfo))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(CONSENT_ID);

        //GetConsentById
        when(aisConsentService.getInitialAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));
        when(aisConsentService.getAccountConsentById(CONSENT_ID_FINALISED)).thenReturn(Optional.of(getAccountConsentFinalised(getXs2aAccountAccess(Collections.singletonList(getXs2aReference())))));
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(null);

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(getAccountConsentsStatusByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getAccountConsentByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(deleteAccountConsentsByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(createConsentAuthorisationValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(updateConsentPsuDataValidator.validate(any(UpdateConsentPsuDataRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getConsentAuthorisationScaStatusValidator.validate(any(GetConsentAuthorisationScaStatusPO.class)))
            .thenReturn(ValidationResult.valid());

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID());
        when(spiContextDataProvider.provide()).thenReturn(spiContextData);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);
        when(spiContextDataProvider.provideWithPsuIdData(any())).thenReturn(spiContextData);

        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.fromString(INTERNAL_REQUEST_ID));

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_AllAccounts() {
        // Given
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());
        // When
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        CreateConsentResponse response = responseObj.getBody();

        // Then
        verify(authorisationMethodDecider, atLeastOnce()).isImplicitMethod(anyBoolean(), argumentCaptor.capture());
        assertFalse(argumentCaptor.getValue());
        assertResponseIsCorrect(response);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_AllAccountsSpiMultilevelTrueScaRequiredFalse() {
        // Given
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        when(aisScaAuthorisationService.isOneFactorAuthorisation(true, true)).thenReturn(true);
        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), true, TEST_PSU_MESSAGE))
                            .build());
        // When
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        CreateConsentResponse response = responseObj.getBody();

        // Then
        verify(authorisationMethodDecider, atLeastOnce()).isImplicitMethod(anyBoolean(), argumentCaptor.capture());
        assertFalse(argumentCaptor.getValue());
        assertResponseIsCorrect(response);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_AllAccountsSpiMultilevelTrueScaRequiredTrue() {
        // Given
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        when(aisScaAuthorisationService.isOneFactorAuthorisation(true, true)).thenReturn(false);
        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), true, TEST_PSU_MESSAGE))
                            .build());
        // When
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        CreateConsentResponse response = responseObj.getBody();

        // Then
        verify(authorisationMethodDecider, atLeastOnce()).isImplicitMethod(anyBoolean(), argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue());
        assertResponseIsCorrect(response);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ShouldRecordEvent() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());

        // When
        consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        verify(xs2aEventService, times(1)).recordTppRequest(argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_AllPSD2() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)
        );

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());
        // When
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        CreateConsentResponse response = responseObj.getBody();

        // Then
        assertResponseIsCorrect(response);
    }

    @Test
    public void createAccountConsentsWithResponse_Failure_AllPSD2() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)
        );

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(false, createMessageError(ErrorType.AIS_400, MessageErrorCode.PARAMETER_NOT_SUPPORTED)));

        // When
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        MessageError messageError = responseObj.getError();

        // Then
        assertThat(messageError).isNotNull();

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.PARAMETER_NOT_SUPPORTED);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ByAccess_Account() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());
        // When
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        CreateConsentResponse response = responseObj.getBody();

        // Then
        assertResponseIsCorrect(response);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ByAccess_Balances() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)
        );

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));
        when(spiContextDataProvider.provide(eq(PSU_ID_DATA), any(TppInfo.class))).thenReturn(SPI_CONTEXT_DATA);

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());
        // When
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        CreateConsentResponse response = responseObj.getBody();

        // Then
        assertResponseIsCorrect(response);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ByAccess_Balances_Transactions() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)
        );

        // When
        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        CreateConsentResponse response = responseObj.getBody();
        // Then
        assertResponseIsCorrect(response);
    }

    @Test
    public void createAccountConsentWithResponse_Success_BankOfferedConsent() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));
        when(spiContextDataProvider.provide(eq(PSU_ID_DATA), any(TppInfo.class))).thenReturn(SPI_CONTEXT_DATA);

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());

        // When
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        CreateConsentResponse response = responseObj.getBody();

        // Then
        assertResponseIsCorrect(response);
    }

    @Test
    public void createAccountConsentsWithResponse_onImplicitApproach_shouldCreateAuthorisation() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(ValidationResult.valid());

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());

        when(authorisationMethodDecider.isImplicitMethod(true, false))
            .thenReturn(true);

        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse authorisationResponse = new CreateConsentAuthorizationResponse();
        authorisationResponse.setAuthorisationId(AUTHORISATION_ID);
        when(redirectAisAuthorizationService.createConsentAuthorization(PSU_ID_DATA, CONSENT_ID))
            .thenReturn(Optional.of(authorisationResponse));

        // When
        ResponseObject<CreateConsentResponse> actualResponse = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        assertFalse(actualResponse.hasError());

        CreateConsentResponse responseBody = actualResponse.getBody();
        assertEquals(CONSENT_ID, responseBody.getConsentId());
        assertEquals(AUTHORISATION_ID, responseBody.getAuthorizationId());
    }

    @Test
    public void createAccountConsentsWithResponse_shouldRecordIntoLoggingContext() {
        // Given
        Xs2aAccountAccess access = getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false);
        CreateConsentReq req = getCreateConsentRequest(access);
        ArgumentCaptor<ConsentStatus> consentStatusCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());

        // When
        consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        verify(loggingContextService).storeConsentStatus(consentStatusCaptor.capture());
        assertThat(consentStatusCaptor.getValue()).isEqualTo(ConsentStatus.RECEIVED);
    }

    @Test
    public void createAccountConsentsWithResponse_implicit_shouldRecordIntoLoggingContext() {
        // Given
        Xs2aAccountAccess access = getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false);
        CreateConsentReq req = getCreateConsentRequest(access);
        ArgumentCaptor<ConsentStatus> consentStatusCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());
        when(authorisationMethodDecider.isImplicitMethod(true, false))
            .thenReturn(true);
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse authorisationResponse = new CreateConsentAuthorizationResponse();
        authorisationResponse.setScaStatus(ScaStatus.RECEIVED);
        when(redirectAisAuthorizationService.createConsentAuthorization(PSU_ID_DATA, CONSENT_ID))
            .thenReturn(Optional.of(authorisationResponse));

        // When
        consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        verify(loggingContextService).storeConsentStatus(consentStatusCaptor.capture());
        verify(loggingContextService).storeScaStatus(scaStatusCaptor.capture());
        assertThat(consentStatusCaptor.getValue()).isEqualTo(ConsentStatus.RECEIVED);
        assertThat(scaStatusCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
    }

    @Test
    public void createAccountConsentsWithResponse_Failure() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(WRONG_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        // When
        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(true, null));

        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        // Then
        assertThat(responseObj.getError().getErrorType()).isEqualTo(ErrorType.AIS_400);
    }

    @Test
    public void createAccountConsentWithResponse_Failure_BankOfferedConsent() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        // When
        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(false, createMessageError(ErrorType.AIS_400, MessageErrorCode.PARAMETER_NOT_SUPPORTED)));

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        MessageError messageError = responseObj.getError();

        // Then
        assertThat(messageError).isNotNull();
        assertThat(messageError.getErrorType()).isEqualTo(ErrorType.AIS_400);

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.PARAMETER_NOT_SUPPORTED);
    }

    @Test
    public void createAccountConsentWithResponse_Failure_NotSupportedAvailableAccounts() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        // When
        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(createValidationResult(false, createMessageError(ErrorType.AIS_405, MessageErrorCode.SERVICE_INVALID_400)));

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        MessageError messageError = responseObj.getError();

        //Then
        assertThat(messageError).isNotNull();
        assertThat(messageError.getErrorType()).isEqualTo(ErrorType.AIS_405);

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.SERVICE_INVALID_400);
    }

    @Test
    public void createAccountConsentsWithResponse_withInvalidConsent_shouldReturnValidationError() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(WRONG_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        when(createConsentRequestValidator.validate(any(CreateConsentRequestObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<CreateConsentResponse> actualResponse = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        verify(createConsentRequestValidator).validate(new CreateConsentRequestObject(req, PSU_ID_DATA));
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    public void createAccountConsentsWithResponse_onInitialConsentNotFound_shouldReturnError() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getInitialAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<CreateConsentResponse> actualResponse = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), actualResponse.getError());
        assertEquals(new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), actualResponse.getError());
    }

    @Test
    public void createAccountConsentsWithResponse_onSpiError_shouldReturnError() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        when(createConsentRequestValidator.validate(new CreateConsentRequestObject(req, PSU_ID_DATA)))
            .thenReturn(ValidationResult.valid());

        SpiResponse<SpiInitiateAisConsentResponse> spiErrorResponse = SpiResponse.<SpiInitiateAisConsentResponse>builder()
                                                                          .error(new TppMessage(MessageErrorCode.SERVICE_BLOCKED))
                                                                          .build();
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiErrorResponse);

        when(spiErrorMapper.mapToErrorHolder(spiErrorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(ErrorType.AIS_403)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.SERVICE_BLOCKED))
                            .build());

        // When
        ResponseObject<CreateConsentResponse> actualResponse = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(new MessageError(ErrorType.AIS_403, TppMessageInformation.of(MessageErrorCode.SERVICE_BLOCKED)), actualResponse.getError());
    }

    @Test
    public void getAccountConsentsStatusById_Success() {
        // Given
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .build();

        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);

        // When
        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID);
        // Then
        assertThat(response.getBody()).isEqualTo(new ConsentStatusResponse(ConsentStatus.VALID));
    }

    @Test
    public void getAccountConsentsStatusById_status_finalised_Success() {
        // When
        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID_FINALISED);

        // Then
        assertThat(response.getBody()).isEqualTo(new ConsentStatusResponse(ConsentStatus.REJECTED));
    }

    @Test
    public void getAccountConsentsStatusById_spi_response_has_error() {
        // Given
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                                                                   .build();

        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(ErrorType.AIS_400)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                            .build());

        // When
        ResponseObject actualResponse = consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getBody()).isNull();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(ErrorType.AIS_400);
    }

    @Test
    public void getAccountConsentsStatusById_Success_ShouldRecordEvent() {
        // Given
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .build();

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);
        // When
        consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_AIS_CONSENT_STATUS_REQUEST_RECEIVED);
    }

    @Test
    public void getAccountConsentsStatusById_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.getAccountConsentsStatusById(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    public void getAccountConsentsStatusById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getAccountConsentsStatusByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<ConsentStatusResponse> actualResponse = consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        verify(getAccountConsentsStatusByIdValidator).validate(new CommonConsentObject(accountConsent));
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    public void getAccountConsentsStatusById_shouldRecordIntoLoggingContext() {
        // Given
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .build();
        ArgumentCaptor<ConsentStatus> consentStatusCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);

        // When
        consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        verify(loggingContextService).storeConsentStatus(consentStatusCaptor.capture());
        assertThat(consentStatusCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    public void getAccountConsentsById_Success() {
        // Given
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .build();
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);
        when(aisConsentMapper.mapToAccountConsentWithNewStatus(accountConsent, spiResponse.getPayload().getConsentStatus()))
            .thenReturn(accountConsent);

        // When
        ResponseObject response = consentService.getAccountConsentById(CONSENT_ID);
        AccountConsent consent = (AccountConsent) response.getBody();

        // Then
        assertThat(consent.getAccess().getAccounts().get(0).getIban()).isEqualTo(CORRECT_IBAN);
    }

    @Test
    public void getAccountConsentsById_shouldRecordIntoLoggingContext() {
        // Given
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .build();
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);
        when(aisConsentMapper.mapToAccountConsentWithNewStatus(accountConsent, spiResponse.getPayload().getConsentStatus()))
            .thenReturn(accountConsent);

        // When
        consentService.getAccountConsentById(CONSENT_ID);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    public void getAccountConsentsById_finalised_shouldRecordIntoLoggingContext() {
        // Given
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        consentService.getAccountConsentById(CONSENT_ID_FINALISED);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.REJECTED);
    }

    @Test
    public void getAccountConsentsById_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.getAccountConsentById(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    public void getAccountConsentsById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getAccountConsentByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<AccountConsent> actualResponse = consentService.getAccountConsentById(CONSENT_ID);

        // Then
        verify(getAccountConsentByIdValidator).validate(new CommonConsentObject(accountConsent));
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    public void getAccountConsentsById_Success_ShouldRecordEvent() {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .build();
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);
        when(aisConsentMapper.mapToAccountConsentWithNewStatus(accountConsent, spiResponse.getPayload().getConsentStatus()))
            .thenReturn(accountConsent);

        // When
        consentService.getAccountConsentById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_AIS_CONSENT_REQUEST_RECEIVED);
    }

    @Test
    public void deleteAccountConsentsById_Success() {
        // Given
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .build());

        // When
        ResponseObject response = consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        assertThat(response.hasError()).isEqualTo(false);
    }

    @Test
    public void deleteAccountConsentsById_Success_ShouldRecordEvent() {
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .build());

        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.DELETE_AIS_CONSENT_REQUEST_RECEIVED);
    }

    @Test
    public void deleteAccountConsentsById_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.deleteAccountConsentsById(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    public void deleteAccountConsentsById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(deleteAccountConsentsByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Void> actualResponse = consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        verify(deleteAccountConsentsByIdValidator).validate(new CommonConsentObject(accountConsent));
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    public void deleteAccountConsentsById_shouldRecordStatusInLoggingContext() {
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .build());

        // Given
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.TERMINATED_BY_TPP);
    }

    @Test
    public void createConsentAuthorizationWithResponse_Success_ShouldRecordEvent() {
        // Given
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = new CreateConsentAuthorizationResponse();
        createConsentAuthorizationResponse.setPsuIdData(PSU_ID_DATA);
        when(redirectAisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void createConsentAuthorizationWithResponse_Success_WithoutPsuIdHeader() {
        // Given
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = new CreateConsentAuthorizationResponse();
        createConsentAuthorizationResponse.setPsuIdData(PSU_ID_DATA);
        when(redirectAisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.createAisAuthorisation(PSU_ID_DATA_EMPTY, CONSENT_ID, PASSWORD);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void createConsentAuthorisationWithResponse_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(createConsentAuthorisationValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<AuthorisationResponse> actualResponse = consentService.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(createConsentAuthorisationValidator).validate(new CommonConsentObject(accountConsent));
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    public void createAisAuthorisation_PSU_ID_empty_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = new CreateConsentAuthorizationResponse();
        createConsentAuthorizationResponse.setScaStatus(ScaStatus.RECEIVED);
        createConsentAuthorizationResponse.setPsuIdData(PSU_ID_DATA);
        when(redirectAisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));
        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsentWithNullPsuIdData()));
        // When
        consentService.createAisAuthorisation(PSU_ID_DATA_EMPTY, CONSENT_ID, PASSWORD);

        // Then
        verify(loggingContextService, never()).storeScaStatus(any());
        verify(loggingContextService).storeConsentStatus(consentStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    public void createAisAuthorisation_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = new CreateConsentAuthorizationResponse();
        createConsentAuthorizationResponse.setAuthorisationId(AUTHORISATION_ID);
        createConsentAuthorizationResponse.setScaStatus(ScaStatus.RECEIVED);
        createConsentAuthorizationResponse.setPsuIdData(PSU_ID_DATA);
        when(redirectAisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(getAccountConsentAuthorisation()));

        UpdateConsentPsuDataReq updatePsuData = buildUpdateConsentPsuDataReq(CONSENT_ID);
        updatePsuData.setPassword(PASSWORD);
        UpdateConsentPsuDataResponse updateConsentPsuDataResponse = buildUpdateConsentPsuDataResponse();
        updateConsentPsuDataResponse.setScaStatus(ScaStatus.RECEIVED);

        when(redirectAisAuthorizationService.updateConsentPsuData(updatePsuData, getAccountConsentAuthorisation()))
            .thenReturn(updateConsentPsuDataResponse);

        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        // When
        consentService.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(loggingContextService).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
        verify(loggingContextService, times(2)).storeConsentStatus(consentStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    public void createAisAuthorisation_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.createAisAuthorisation(PSU_ID_DATA, WRONG_CONSENT_ID, "");

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    public void updateConsentPsuData_Success_ShouldRecordEvent() {
        // Given
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);

        AccountConsentAuthorization authorization = new AccountConsentAuthorization();
        when(redirectAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(authorization));

        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);
        when(redirectAisAuthorizationService.updateConsentPsuData(updateConsentPsuDataReq, authorization))
            .thenReturn(buildUpdateConsentPsuDataResponse());


        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED);
    }

    @Test
    public void updateConsentPsuData_Failure_EndpointIsNotAccessible() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        doNothing()
            .when(xs2aEventService).recordAisTppRequest(CONSENT_ID, EventType.UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED, updateConsentPsuDataReq);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(false);

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponse = consentService.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(ErrorType.AIS_403);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.SERVICE_BLOCKED);
        assertThat(actualResponse.getError().getTppMessage().getCategory()).isEqualTo(MessageCategory.ERROR);
    }

    @Test
    public void updateConsentPsuData_withInvalidRequest_shouldReturnValidationError() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);

        when(updateConsentPsuDataValidator.validate(any(UpdateConsentPsuDataRequestObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponse = consentService.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(updateConsentPsuDataValidator).validate(new UpdateConsentPsuDataRequestObject(accountConsent, updateConsentPsuDataReq));

        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    public void updateConsentPsuData_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(WRONG_CONSENT_ID);
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, WRONG_CONSENT_ID))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    public void updateConsentPsuData_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);

        AccountConsentAuthorization authorization = new AccountConsentAuthorization();
        when(redirectAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(authorization));

        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);
        when(redirectAisAuthorizationService.updateConsentPsuData(updateConsentPsuDataReq, authorization))
            .thenReturn(buildUpdateConsentPsuDataResponse());

        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        // When
        consentService.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(loggingContextService).storeConsentStatus(consentStatusArgumentCaptor.capture());
        verify(loggingContextService).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
    }

    @Test
    public void updateConsentPsuData_shouldStoreConsentStatusInLoggingContextWhenUpdateConsentRequestInvalid() {
        // Given
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);

        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);
        MessageError messageError = new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID));
        when(updateConsentPsuDataValidator.validate(any(UpdateConsentPsuDataRequestObject.class)))
            .thenReturn(ValidationResult.invalid(messageError));

        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuDataResponseResponseObject = consentService.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        assertEquals(messageError, updateConsentPsuDataResponseResponseObject.getError());
        verify(loggingContextService).storeConsentStatus(consentStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    public void getConsentInitiationAuthorisation() {
        // Given
        when(aisConsentService.getAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(Collections.singletonList(CONSENT_ID)));
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
    public void getConsentInitiationAuthorisations_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getConsentAuthorisationsValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aAuthorisationSubResources> actualResponse = consentService.getConsentInitiationAuthorisations(CONSENT_ID);

        // Then
        verify(getConsentAuthorisationsValidator).validate(new CommonConsentObject(accountConsent));
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    public void getConsentInitiationAuthorisations_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.getConsentInitiationAuthorisations(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    public void getConsentInitiationAuthorisations_shouldRecordStatusInLoggingContext() {
        when(getConsentAuthorisationsValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());

        consentService.getConsentInitiationAuthorisations(CONSENT_ID);

        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        verify(loggingContextService).storeConsentStatus(consentStatusArgumentCaptor.capture());
    }

    @Test
    public void getConsentAuthorisationScaStatus_success() {
        // Given
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));

        // When
        ResponseObject<ScaStatus> actual = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertFalse(actual.hasError());
        assertEquals(ScaStatus.RECEIVED, actual.getBody());
    }

    @Test
    public void getConsentAuthorisationScaStatus_success_shouldRecordEvent() {
        // Given
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(any(), any()))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_CONSENT_SCA_STATUS_REQUEST_RECEIVED);
    }

    @Test
    public void getConsentAuthorisationScaStatus_failure() {
        // Given
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(WRONG_AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<ScaStatus> actual = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertNull(actual.getBody());
    }

    @Test
    public void getConsentAuthorisationScaStatus_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getConsentAuthorisationScaStatusValidator.validate(any(GetConsentAuthorisationScaStatusPO.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<ScaStatus> actualResponse = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(getConsentAuthorisationScaStatusValidator).validate(new GetConsentAuthorisationScaStatusPO(accountConsent, AUTHORISATION_ID));
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    public void getConsentAuthorisationScaStatus_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.getConsentAuthorisationScaStatus(WRONG_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    public void getConsentAuthorisationScaStatus_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(any(), any()))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));
        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        // When
        consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(loggingContextService).storeConsentStatus(consentStatusArgumentCaptor.capture());
        verify(loggingContextService).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
    }

    /**
     * Basic test AccountDetails used in all cases
     */

    private AccountReference getXs2aReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, null, CORRECT_IBAN, null, null, null, null, CURRENCY);
    }

    private SpiAccountAccess getSpiAccountAccess() {
        return new SpiAccountAccess(Collections.singletonList(new SpiAccountReference(null, CORRECT_IBAN, null, null, null, null, CURRENCY)), null, null, null, null, null);
    }

    private Xs2aAccountAccess getXs2aAccountAccess(List<AccountReference> accounts) {
        return new Xs2aAccountAccess(accounts, null, null, null, null, null);
    }

    private AccountConsentAuthorization getAccountConsentAuthorisation() {
        return new AccountConsentAuthorization();
    }

    private AccountConsent getAccountConsent() {
        Xs2aAccountAccess access = getXs2aAccountAccess(Collections.singletonList(getXs2aReference()));

        return new AccountConsent(CONSENT_ID, access, access, false, DATE, null, 4, null, ConsentStatus.VALID, false, false, Collections.singletonList(PSU_ID_DATA), tppInfo, AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.MAX, Collections.singletonMap("/accounts", 0));
    }

    private AccountConsent getAccountConsentWithNullPsuIdData() {
        Xs2aAccountAccess access = getXs2aAccountAccess(Collections.singletonList(getXs2aReference()));

        return new AccountConsent(CONSENT_ID, access, access, false, DATE, null, 4, null, ConsentStatus.VALID, false, false, Collections.emptyList(), tppInfo, AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.MAX, Collections.singletonMap("/accounts", 0));
    }

    private AccountConsent getAccountConsentFinalised(Xs2aAccountAccess access) {
        return new AccountConsent(CONSENT_ID, access, access, false, DATE, null, 4, null, ConsentStatus.REJECTED, false, false, null, tppInfo, AisConsentRequestType.GLOBAL, false, Collections.emptyList(), STATUS_CHANGE_TIMESTAMP, Collections.emptyMap());
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

    private Xs2aAccountAccess getAccess(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new Xs2aAccountAccess(accounts, balances, transactions, allAccounts ? AccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? AccountAccessType.ALL_ACCOUNTS : null, null);
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

    private ValidationResult createValidationResult(boolean isValid, MessageError messageError) {
        return isValid
                   ? ValidationResult.valid()
                   : ValidationResult.invalid(messageError);
    }

    private MessageError createMessageError(ErrorType errorType, MessageErrorCode errorCode) {
        return new MessageError(errorType, of(errorCode));
    }

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq(String consentId) {
        return buildUpdateConsentPsuDataReq(consentId, AUTHORISATION_ID);
    }

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq(String consentId, String authorisationId) {
        UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
        request.setConsentId(consentId);
        request.setAuthorizationId(authorisationId);
        request.setPsuData(PSU_ID_DATA);
        return request;
    }

    private UpdateConsentPsuDataResponse buildUpdateConsentPsuDataResponse() {
        return new UpdateConsentPsuDataResponse(ScaStatus.RECEIVED, CONSENT_ID, AUTHORISATION_ID);
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_ID);
        return tppInfo;
    }

    private void assertValidationErrorIsPresent(ResponseObject response) {
        assertThat(response).isNotNull();
        assertThat(response.hasError()).isTrue();
        assertThat(response.getError()).isEqualTo(VALIDATION_ERROR);
    }

    private void assertResponseIsCorrect(CreateConsentResponse response) {
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(response.getPsuMessage()).isEqualTo(TEST_PSU_MESSAGE);
    }
}
