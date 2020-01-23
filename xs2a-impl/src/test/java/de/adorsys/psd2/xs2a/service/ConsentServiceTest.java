/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreateAisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.ais.RedirectAisAuthorizationService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountReferenceMapperImpl;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.AisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String WRONG_CONSENT_ID = "wrong_consent_id";
    private static final String AUTHORISATION = "Bearer 1111111";
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
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = SpiPsuData.builder().psuId(CORRECT_PSU_ID).build();
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.MAX;
    private static final MessageError VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(MessageErrorCode.CONSENT_INVALID));
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, new TppInfo(), UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
    private static final MessageError CONSENT_UNKNOWN_403_ERROR =
        new MessageError(ErrorType.AIS_403, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403));

    @InjectMocks
    private ConsentService consentService;

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Spy
    private SpiToXs2aAccountAccessMapper spiToXs2aAccountAccessMapper = new SpiToXs2aAccountAccessMapper(new SpiToXs2aAccountReferenceMapperImpl());
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private TppService tppService;
    @Mock
    private AisConsentSpi aisConsentSpi;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private RedirectAisAuthorizationService redirectAisAuthorizationService;
    @Mock
    private AisEndpointAccessCheckerService endpointAccessCheckerService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private ConsentValidationService consentValidationService;
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
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private ConsentAuthorisationService consentAuthorisationService;
    @Mock
    private AccountReferenceInConsentUpdater accountReferenceUpdater;

    private AccountConsent accountConsent;

    private TppInfo tppInfo;
    private Xs2aCreateAisConsentResponse xs2aCreateAisConsentResponse;

    @BeforeEach
    void setUp() {
        //ByPSU-ID
        tppInfo = buildTppInfo();
        accountConsent = getAccountConsent();

        //ByAccess
        xs2aCreateAisConsentResponse = new Xs2aCreateAisConsentResponse(CONSENT_ID, getAccountConsent(), null);
    }

    @Test
    void createAccountConsentsWithResponse_Success_AllAccounts() {
        // Given
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_Success_withMultilevelScaAndOneFactorAuthorisation() {
        // Given
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(aisScaAuthorisationService.isOneFactorAuthorisation(accountConsent)).thenReturn(true);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_Success_withMultilevelSca() {
        // Given
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(aisScaAuthorisationService.isOneFactorAuthorisation(accountConsent)).thenReturn(false);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_Success_ShouldRecordEvent() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_Success_AllPSD2() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)
        );
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_Failure_AllPSD2() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)
        );

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_Success_ByAccess_Account() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList(), false, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_Success_ByAccess_Balances() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)
        );
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_Success_ByAccess_Balances_Transactions() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)
        );
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        // When
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentWithResponse_Success_BankOfferedConsent() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_shouldSaveAspspConsentDataAfterSpiCall() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE))
                            .build());

        // When
        ResponseObject<CreateConsentResponse> response = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        assertResponseIsCorrect(response.getBody());

        InOrder inOrder = inOrder(aspspConsentDataProviderFactory, initialSpiAspspConsentDataProvider, aisConsentSpi);
        inOrder.verify(aisConsentSpi).initiateAisConsent(any(), eq(SPI_ACCOUNT_CONSENT), eq(initialSpiAspspConsentDataProvider));
        inOrder.verify(initialSpiAspspConsentDataProvider).saveWith(CONSENT_ID);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void createAccountConsentsWithResponse_onImplicitApproach_shouldCreateAuthorisation() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_shouldRecordIntoLoggingContext() {
        // Given
        Xs2aAccountAccess access = getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false);
        CreateConsentReq req = getCreateConsentRequest(access);
        ArgumentCaptor<ConsentStatus> consentStatusCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_implicit_shouldRecordIntoLoggingContext() {
        // Given
        Xs2aAccountAccess access = getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false);
        CreateConsentReq req = getCreateConsentRequest(access);
        ArgumentCaptor<ConsentStatus> consentStatusCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_Failure() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(WRONG_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        // When
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));

        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        // Then
        assertThat(responseObj.getError().getErrorType()).isEqualTo(ErrorType.AIS_400);
    }

    @Test
    void createAccountConsentWithResponse_Failure_BankOfferedConsent() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        // When
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentWithResponse_Failure_NotSupportedAvailableAccounts() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        // When
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void createAccountConsentsWithResponse_withInvalidConsent_shouldReturnValidationError() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(WRONG_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<CreateConsentResponse> actualResponse = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        verify(consentValidationService).validateConsentOnCreate(req, PSU_ID_DATA);
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    void createAccountConsentsWithResponse_onSpiError_shouldReturnError() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, tppInfo))
            .thenReturn(Optional.of(xs2aCreateAisConsentResponse));

        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo)).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);

        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
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
    void getAccountConsentsStatusById_Success() {
        // Given
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide()).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnGettingStatusById(accountConsent)).thenReturn(ValidationResult.valid());
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
    void getAccountConsentsStatusById_status_finalised_Success() {
        // When
        AccountConsent finalisedAccountConsent = mock(AccountConsent.class);
        when(aisConsentService.getAccountConsentById(CONSENT_ID_FINALISED)).thenReturn(Optional.of(finalisedAccountConsent));
        when(consentValidationService.validateConsentOnGettingStatusById(finalisedAccountConsent)).thenReturn(ValidationResult.valid());
        when(finalisedAccountConsent.getConsentStatus()).thenReturn(ConsentStatus.REJECTED);

        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID_FINALISED);

        // Then
        assertThat(response.getBody()).isEqualTo(new ConsentStatusResponse(ConsentStatus.REJECTED));
    }

    @Test
    void getAccountConsentsStatusById_spi_response_has_error() {
        // Given
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide()).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnGettingStatusById(accountConsent)).thenReturn(ValidationResult.valid());
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
    void getAccountConsentsStatusById_Success_ShouldRecordEvent() {
        // Given
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide()).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnGettingStatusById(accountConsent)).thenReturn(ValidationResult.valid());
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
    void getAccountConsentsStatusById_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.getAccountConsentsStatusById(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void getAccountConsentsStatusById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(consentValidationService.validateConsentOnGettingStatusById(accountConsent))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<ConsentStatusResponse> actualResponse = consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        verify(consentValidationService).validateConsentOnGettingStatusById(accountConsent);
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    void getAccountConsentsStatusById_shouldRecordIntoLoggingContext() {
        // Given
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .build();
        ArgumentCaptor<ConsentStatus> consentStatusCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide()).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnGettingStatusById(accountConsent)).thenReturn(ValidationResult.valid());
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);

        // When
        consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        verify(loggingContextService).storeConsentStatus(consentStatusCaptor.capture());
        assertThat(consentStatusCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    void getAccountConsentsById_Success() {
        // Given
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .build();
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide()).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnGettingById(accountConsent)).thenReturn(ValidationResult.valid());
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
    void getAccountConsentsById_shouldRecordIntoLoggingContext() {
        // Given
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .build();
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide()).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnGettingById(accountConsent)).thenReturn(ValidationResult.valid());
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
    void getAccountConsentsById_finalised_shouldRecordIntoLoggingContext() {
        // Given
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        AccountConsent finalisedAccountConsentCaptor = mock(AccountConsent.class);

        when(aisConsentService.getAccountConsentById(CONSENT_ID_FINALISED)).thenReturn(Optional.of(finalisedAccountConsentCaptor));
        when(consentValidationService.validateConsentOnGettingById(finalisedAccountConsentCaptor)).thenReturn(ValidationResult.valid());
        when(finalisedAccountConsentCaptor.getConsentStatus()).thenReturn(ConsentStatus.REJECTED);

        // When
        consentService.getAccountConsentById(CONSENT_ID_FINALISED);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.REJECTED);
    }

    @Test
    void getAccountConsentsById_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.getAccountConsentById(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void getAccountConsentsById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(consentValidationService.validateConsentOnGettingById(accountConsent))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<AccountConsent> actualResponse = consentService.getAccountConsentById(CONSENT_ID);

        // Then
        verify(consentValidationService).validateConsentOnGettingById(accountConsent);
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    void getAccountConsentsById_Success_ShouldRecordEvent() {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .build();

        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provide()).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnGettingById(accountConsent)).thenReturn(ValidationResult.valid());
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
    void deleteAccountConsentsById_Success() {
        // Given
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provideWithPsuIdData(any())).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnDelete(accountConsent)).thenReturn(ValidationResult.valid());
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
    void deleteAccountConsentsById_Success_ShouldRecordEvent() {
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provideWithPsuIdData(any())).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnDelete(accountConsent)).thenReturn(ValidationResult.valid());
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
    void deleteAccountConsentsById_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.deleteAccountConsentsById(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void deleteAccountConsentsById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(consentValidationService.validateConsentOnDelete(accountConsent))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Void> actualResponse = consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        verify(consentValidationService).validateConsentOnDelete(accountConsent);
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    void deleteAccountConsentsById_shouldRecordStatusInLoggingContext() {
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provideWithPsuIdData(any())).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnDelete(accountConsent)).thenReturn(ValidationResult.valid());
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
    void deleteAccountConsentsById_revokeAisConsentResponse_hasError() {
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent()));

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiContextData spiContextData = new SpiContextData(SPI_PSU_DATA, tppInfo, UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
        when(spiContextDataProvider.provideWithPsuIdData(any())).thenReturn(spiContextData);

        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentOnDelete(accountConsent)).thenReturn(ValidationResult.valid());
        SpiResponse<SpiResponse.VoidResponse> spiResponse = SpiResponse.<SpiResponse.VoidResponse>builder()
                                                                .build();
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)).thenReturn(ErrorHolder.builder(ErrorType.AIS_400).build());

        // Given
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        ResponseObject<Void> responseObject = consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        assertTrue(responseObject.hasError());
    }

    @Test
    void getConsentAuthorisationScaStatus() {
        // Given
        when(consentAuthorisationService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(ResponseObject.<ScaStatus>builder().build());

        // When
        consentService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);

        verify(consentAuthorisationService).getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);
    }

    @Test
    void updateConsentPsuData() {
        UpdateConsentPsuDataReq updatePsuData = new UpdateConsentPsuDataReq();
        consentService.updateConsentPsuData(updatePsuData);
        verify(consentAuthorisationService, times(1)).updateConsentPsuData(updatePsuData);
    }

    @Test
    void getConsentInitiationAuthorisations() {
        consentService.getConsentInitiationAuthorisations(CONSENT_ID);
        verify(consentAuthorisationService, times(1)).getConsentInitiationAuthorisations(CONSENT_ID);
    }

    /**
     * Basic test AccountDetails used in all cases
     */

    private AccountReference getXs2aReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, null, CORRECT_IBAN, null, null, null, null, CURRENCY);
    }

    private SpiAccountAccess getSpiAccountAccess() {
        return new SpiAccountAccess(Collections.singletonList(new SpiAccountReference(null, CORRECT_IBAN, null, null, null, null, CURRENCY)), null, null, null, null, null, null);
    }

    private Xs2aAccountAccess getXs2aAccountAccess(List<AccountReference> accounts) {
        return new Xs2aAccountAccess(accounts, null, null, null, null, null, null);
    }

    private AccountConsent getAccountConsent() {
        Xs2aAccountAccess access = getXs2aAccountAccess(Collections.singletonList(getXs2aReference()));

        return new AccountConsent(CONSENT_ID, access, access, false, DATE, null, 4, null, ConsentStatus.VALID, false, false, Collections.singletonList(PSU_ID_DATA), tppInfo, AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.MAX, Collections.singletonMap("/accounts", 0), OffsetDateTime.MAX);
    }

    private AccountConsent getAccountConsentFinalised(Xs2aAccountAccess access) {
        return new AccountConsent(CONSENT_ID, access, access, false, DATE, null, 4, null, ConsentStatus.REJECTED, false, false, null, tppInfo, AisConsentRequestType.GLOBAL, false, Collections.emptyList(), STATUS_CHANGE_TIMESTAMP, Collections.emptyMap(), OffsetDateTime.MAX);
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
        return new Xs2aAccountAccess(accounts, balances, transactions, allAccounts ? AccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? AccountAccessType.ALL_ACCOUNTS : null, null, null);
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
