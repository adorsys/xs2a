/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */


package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Xs2aResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreateAisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.ais.RedirectAisAuthorizationService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountReferenceMapperImpl;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.psd2.xs2a.web.mapper.ScaMethodsMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String WRONG_CONSENT_ID = "wrong_consent_id";
    private static final String CONSENT_ID_FINALISED = "finalised_consent_id";
    private static final String TPP_ID = "Test TppId";
    private static final String CORRECT_PSU_ID = "marion.mueller";
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String CORRECT_IBAN_1 = "DE52500105173911841934";
    private static final String CORRECT_IBAN_2 = "DE69760700240340283600";
    private static final String WRONG_IBAN = "DE11111111111111841934";
    private static final String TEST_PSU_MESSAGE = "This test message is created in ASPSP and directed to PSU";
    private static final Currency CURRENCY_1 = Currency.getInstance("EUR");
    private static final Currency CURRENCY_2 = Currency.getInstance("USD");
    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final boolean EXPLICIT_PREFERRED = true;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_CLEAR = new PsuIdData(null, null, null, null, null, null);
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final MessageError CONSENT_INVALID_401_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(MessageErrorCode.CONSENT_INVALID));
    private static final MessageError CONSENT_UNKNOWN_403_ERROR =
        new MessageError(ErrorType.AIS_403, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403));
    private static final MessageError PARAMETER_NOT_SUPPORTED_400_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.PARAMETER_NOT_SUPPORTED));
    private static final MessageError SERVICE_INVALID_400_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.SERVICE_INVALID_400));
    private static final ScaStatus SCA_STATUS = ScaStatus.STARTED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));

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
    private LoggingContextService loggingContextService;
    @Mock
    private ConsentAuthorisationService consentAuthorisationService;
    @Mock
    private AccountReferenceInConsentUpdater accountReferenceUpdater;
    @Mock
    private AdditionalInformationSupportedService additionalInformationSupportedService;
    @Mock
    private Xs2aAuthorisationService xs2aAuthorisationService;
    @Mock
    private SpiToXs2aLinksMapper spiToXs2aLinksMapper;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private PsuDataCleaner psuDataCleaner;
    @Mock
    private ScaMethodsMapper scaMethodsMapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;

    private AisConsent aisConsent;

    private TppInfo tppInfo;
    private Xs2aCreateAisConsentResponse xs2aCreateAisConsentResponse;
    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        //ByPSU-ID
        tppInfo = buildTppInfo();
        aisConsent = getAisConsent();

        //ByAccess
        xs2aCreateAisConsentResponse = new Xs2aCreateAisConsentResponse(CONSENT_ID, getAisConsent(), null);
    }

    @Test
    void createAccountConsentsWithResponse_Success_AllAccounts() {
        // Given
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            true,
            false
        );

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());

        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));
        // When
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        CreateConsentResponse response = responseObj.getBody();

        // Then
        verify(authorisationMethodDecider, atLeastOnce()).isImplicitMethod(anyBoolean(), argumentCaptor.capture());
        assertFalse(argumentCaptor.getValue());
        assertResponseIsCorrect(response);
    }

    @Test
    void createAccountConsentsWithResponse_Success_AllAccounts_psuIgnored() {
        // Given
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            true,
            false
        );

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false), PSU_ID_DATA_CLEAR, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());

        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA_CLEAR, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA_CLEAR))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());

        when(aspspProfileService.isPsuInInitialRequestIgnored()).thenReturn(true);
        when(psuDataCleaner.clearPsuData(PSU_ID_DATA)).thenReturn(PSU_ID_DATA_CLEAR);
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));

        // When
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        CreateConsentResponse response = responseObj.getBody();

        // Then
        verify(authorisationMethodDecider, atLeastOnce()).isImplicitMethod(anyBoolean(), argumentCaptor.capture());
        verify(psuDataCleaner).clearPsuData(PSU_ID_DATA);
        assertFalse(argumentCaptor.getValue());
        assertResponseIsCorrect(response);
    }

    @Test
    void createAccountConsentsWithResponse_Success_withMultilevelScaAndOneFactorAuthorisation() {
        // Given
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            true,
            false
        );

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(aisScaAuthorisationService.isOneFactorAuthorisation(aisConsent))
            .thenReturn(true);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), true, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));
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
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            true,
            false
        );

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(aisScaAuthorisationService.isOneFactorAuthorisation(aisConsent))
            .thenReturn(false);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), true, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));
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
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false
        );
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));
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
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), false, true
        );

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), false, true), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));
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
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), false, true
        );

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(false, PARAMETER_NOT_SUPPORTED_400_ERROR));

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
            getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList()), false, false
        );
        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList()), false, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));
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
            getAccess(Collections.singletonList(getReference(CORRECT_IBAN_2, CURRENCY_1)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList()), false, false
        );
        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN_2, CURRENCY_1)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList()), false, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(spiContextDataProvider.provide(eq(PSU_ID_DATA), any(TppInfo.class)))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));
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
            getAccess(Collections.singletonList(getReference(CORRECT_IBAN_2, CURRENCY_1)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN_2, CURRENCY_1))), false, false
        );
        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN_2, CURRENCY_1)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN_2, CURRENCY_1))), false, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));

        // When
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
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
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), false, false
        );

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), false, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());

        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(spiContextDataProvider.provide(eq(PSU_ID_DATA), any(TppInfo.class))).thenReturn(SPI_CONTEXT_DATA);

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));

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
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false
        );

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));

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
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false
        );
        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(ValidationResult.valid());
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());

        when(authorisationMethodDecider.isImplicitMethod(true, false))
            .thenReturn(true);
        CreateConsentAuthorisationProcessorResponse response = new CreateConsentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, CONSENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse authorisationResponse = new CreateConsentAuthorizationResponse();
        authorisationResponse.setAuthorisationId(AUTHORISATION_ID);

        when(redirectAisAuthorizationService.createConsentAuthorization(any()))
            .thenReturn(Optional.of(authorisationResponse));
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));

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
        AccountAccess access = getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        CreateConsentReq req = getCreateConsentRequest(access, true, false);
        ArgumentCaptor<ConsentStatus> consentStatusCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));

        // When
        consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        verify(loggingContextService).storeConsentStatus(consentStatusCaptor.capture());
        assertThat(consentStatusCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    void createAccountConsentsWithResponse_implicit_shouldRecordIntoLoggingContext() {
        // Given
        AccountAccess access = getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        CreateConsentReq req = getCreateConsentRequest(access, true, false);
        ArgumentCaptor<ConsentStatus> consentStatusCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(), false, TEST_PSU_MESSAGE, null, null))
                            .build());
        when(authorisationMethodDecider.isImplicitMethod(true, false))
            .thenReturn(true);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        when(aisScaAuthorisationServiceResolver.getService())
            .thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse authorisationResponse = new CreateConsentAuthorizationResponse();
        authorisationResponse.setScaStatus(ScaStatus.RECEIVED);

        when(redirectAisAuthorizationService.createConsentAuthorization(any()))
            .thenReturn(Optional.of(authorisationResponse));
        when(scaMethodsMapper.mapToAuthenticationObjectList(any())).thenReturn(Collections.singletonList(getAuthenticationObject()));
        CreateConsentAuthorisationProcessorResponse response = new CreateConsentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, CONSENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);

        // When
        consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);

        // Then
        verify(loggingContextService).storeConsentStatus(consentStatusCaptor.capture());
        verify(loggingContextService).storeScaStatus(scaStatusCaptor.capture());
        assertThat(consentStatusCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
        assertThat(scaStatusCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
    }

    @Test
    void createAccountConsentsWithResponse_Failure() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(WRONG_IBAN, CURRENCY_1)), Collections.emptyList(), Collections.emptyList()), false, false
        );

        // When
        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentService.createConsent(req, PSU_ID_DATA, null))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .build());

        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        // Then
        assertThat(responseObj.getError().getErrorType()).isEqualTo(ErrorType.AIS_400);
    }

    @Test
    void createAccountConsentWithResponse_Failure_BankOfferedConsent() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), false, false
        );

        // When
        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(false, PARAMETER_NOT_SUPPORTED_400_ERROR));

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
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false
        );

        // When
        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(createValidationResult(false, SERVICE_INVALID_400_ERROR));

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED);
        MessageError messageError = responseObj.getError();

        //Then
        assertThat(messageError).isNotNull();
        assertThat(messageError.getErrorType()).isEqualTo(ErrorType.AIS_400);

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.SERVICE_INVALID_400);
    }

    @Test
    void createAccountConsentsWithResponse_withInvalidConsent_shouldReturnValidationError() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(WRONG_IBAN, CURRENCY_1)), Collections.emptyList(), Collections.emptyList()), false, false
        );

        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(consentValidationService.validateConsentOnCreate(req, PSU_ID_DATA))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));

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
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false
        );
        when(additionalInformationSupportedService.checkIfAdditionalInformationSupported(req)).thenReturn(req);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), true, false), PSU_ID_DATA, tppInfo))
            .thenReturn(Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                            .payload(xs2aCreateAisConsentResponse)
                            .build());
        when(tppService.getTppInfo())
            .thenReturn(tppInfo);
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide(PSU_ID_DATA, tppInfo))
            .thenReturn(SPI_CONTEXT_DATA);
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
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnGettingStatusById(aisConsent))
            .thenReturn(ValidationResult.valid());
        SpiResponse<SpiConsentStatusResponse> spiResponse = SpiResponse.<SpiConsentStatusResponse>builder()
                                                                .payload(new SpiConsentStatusResponse(ConsentStatus.VALID, TEST_PSU_MESSAGE))
                                                                .build();

        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);

        // When
        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID);
        // Then
        assertThat(response.getBody()).isEqualTo(new ConsentStatusResponse(ConsentStatus.VALID, TEST_PSU_MESSAGE));
    }

    @Test
    void getAccountConsentsStatusById_status_finalised_Success() {
        // When
        AisConsent finalisedAccountConsent = mock(AisConsent.class);
        when(aisConsentService.getAccountConsentById(CONSENT_ID_FINALISED))
            .thenReturn(Optional.of(finalisedAccountConsent));
        when(consentValidationService.validateConsentOnGettingStatusById(finalisedAccountConsent))
            .thenReturn(ValidationResult.valid());
        when(finalisedAccountConsent.getConsentStatus())
            .thenReturn(ConsentStatus.REJECTED);

        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID_FINALISED);

        // Then
        assertThat(response.getBody()).isEqualTo(new ConsentStatusResponse(ConsentStatus.REJECTED, null));
    }

    @Test
    void getAccountConsentsStatusById_spi_response_has_error() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnGettingStatusById(aisConsent))
            .thenReturn(ValidationResult.valid());
        SpiResponse<SpiConsentStatusResponse> spiResponse = SpiResponse.<SpiConsentStatusResponse>builder()
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
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnGettingStatusById(aisConsent)).thenReturn(ValidationResult.valid());
        SpiResponse<SpiConsentStatusResponse> spiResponse = SpiResponse.<SpiConsentStatusResponse>builder()
                                                                .payload(new SpiConsentStatusResponse(ConsentStatus.VALID, TEST_PSU_MESSAGE))
                                                                .build();

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);
        // When
        consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_AIS_CONSENT_STATUS_REQUEST_RECEIVED);
    }

    @Test
    void getAccountConsentsStatusById_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.getAccountConsentsStatusById(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void getAccountConsentsStatusById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(consentValidationService.validateConsentOnGettingStatusById(aisConsent))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));

        // When
        ResponseObject<ConsentStatusResponse> actualResponse = consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        verify(consentValidationService).validateConsentOnGettingStatusById(aisConsent);
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    void getAccountConsentsStatusById_shouldRecordIntoLoggingContext() {
        // Given
        SpiResponse<SpiConsentStatusResponse> spiResponse = SpiResponse.<SpiConsentStatusResponse>builder()
                                                                .payload(new SpiConsentStatusResponse(ConsentStatus.VALID, TEST_PSU_MESSAGE))
                                                                .build();
        ArgumentCaptor<ConsentStatus> consentStatusCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnGettingStatusById(aisConsent))
            .thenReturn(ValidationResult.valid());
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
        SpiResponse<SpiConsentStatusResponse> spiResponse = SpiResponse.<SpiConsentStatusResponse>builder()
                                                                .payload(new SpiConsentStatusResponse(ConsentStatus.VALID, TEST_PSU_MESSAGE))
                                                                .build();
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnGettingById(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);

        // When
        ResponseObject response = consentService.getAccountConsentById(CONSENT_ID);
        AisConsent consent = (AisConsent) response.getBody();

        // Then
        assertThat(consent.getAccess().getAccounts().get(0).getIban()).isEqualTo(CORRECT_IBAN_2);
    }

    @Test
    void getAccountConsentsById_shouldRecordIntoLoggingContext() {
        // Given
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        SpiResponse<SpiConsentStatusResponse> spiResponse = SpiResponse.<SpiConsentStatusResponse>builder()
                                                                .payload(new SpiConsentStatusResponse(ConsentStatus.VALID, TEST_PSU_MESSAGE))
                                                                .build();
        //GetConsentById
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnGettingById(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);

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
        AisConsent finalisedAccountConsentCaptor = mock(AisConsent.class);

        when(aisConsentService.getAccountConsentById(CONSENT_ID_FINALISED))
            .thenReturn(Optional.of(finalisedAccountConsentCaptor));
        when(consentValidationService.validateConsentOnGettingById(finalisedAccountConsentCaptor))
            .thenReturn(ValidationResult.valid());
        when(finalisedAccountConsentCaptor.getConsentStatus())
            .thenReturn(ConsentStatus.REJECTED);

        // When
        consentService.getAccountConsentById(CONSENT_ID_FINALISED);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.REJECTED);
    }

    @Test
    void getAccountConsentsById_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject response = consentService.getAccountConsentById(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void getAccountConsentsById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(consentValidationService.validateConsentOnGettingById(aisConsent))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));

        // When
        ResponseObject<AisConsent> actualResponse = consentService.getAccountConsentById(CONSENT_ID);

        // Then
        verify(consentValidationService).validateConsentOnGettingById(aisConsent);
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    void getAccountConsentsById_Success_ShouldRecordEvent() {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        SpiResponse<SpiConsentStatusResponse> spiResponse = SpiResponse.<SpiConsentStatusResponse>builder()
                                                                .payload(new SpiConsentStatusResponse(ConsentStatus.VALID, TEST_PSU_MESSAGE))
                                                                .build();

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnGettingById(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);

        // When
        consentService.getAccountConsentById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_AIS_CONSENT_REQUEST_RECEIVED);
    }

    @Test
    void deleteAccountConsentsById_Success() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provideWithPsuIdData(any()))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnDelete(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .build());

        // When
        ResponseObject response = consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        assertThat(response.hasError()).isFalse();
    }

    @Test
    void deleteAccountConsentsById_Success_ShouldRecordEvent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provideWithPsuIdData(any()))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnDelete(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .build());

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
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
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(consentValidationService.validateConsentOnDelete(aisConsent))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));

        // When
        ResponseObject<Void> actualResponse = consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        verify(consentValidationService).validateConsentOnDelete(aisConsent);
        assertValidationErrorIsPresent(actualResponse);
    }

    @Test
    void deleteAccountConsentsById_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provideWithPsuIdData(any()))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnDelete(aisConsent))
            .thenReturn(ValidationResult.valid());
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
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(getAisConsent()));
        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiContextDataProvider.provideWithPsuIdData(any()))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentOnDelete(aisConsent))
            .thenReturn(ValidationResult.valid());
        SpiResponse<SpiResponse.VoidResponse> spiResponse = SpiResponse.<SpiResponse.VoidResponse>builder()
                                                                .build();
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(ErrorType.AIS_400).build());

        // When
        ResponseObject<Void> responseObject = consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        assertTrue(responseObject.hasError());
    }

    @Test
    void getConsentAuthorisationScaStatus() {
        // Given
        ResponseObject<Xs2aScaStatusResponse> expected = ResponseObject.<Xs2aScaStatusResponse>builder()
                                                             .body(new Xs2aScaStatusResponse(ScaStatus.FINALISED, false, "psu message", null, null))
                                                             .build();

        ConsentScaStatus consentScaStatus = new ConsentScaStatus(null, aisConsent, ScaStatus.RECEIVED);
        ResponseObject<ConsentScaStatus> responseObject = ResponseObject.<ConsentScaStatus>builder()
                                                              .body(consentScaStatus)
                                                              .build();

        when(consentAuthorisationService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(responseObject);
        when(requestProviderService.getPsuIdData()).thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentSpi.getScaStatus(ScaStatus.RECEIVED, SPI_CONTEXT_DATA, WRONG_AUTHORISATION_ID, aisConsentMapper.mapToSpiAccountConsent(aisConsent), spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiScaStatusResponse>builder()
                            .payload(new SpiScaStatusResponse(ScaStatus.FINALISED, false, "psu message", null, null))
                            .build());

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertFalse(actual.hasError());
        assertEquals(expected.getBody(), actual.getBody());

        verify(consentAuthorisationService).getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);
        verify(xs2aAuthorisationService).updateAuthorisationStatus(WRONG_AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void getConsentAuthorisationScaStatus_withBeneficiariesFlag() {
        // Given
        ResponseObject<Xs2aScaStatusResponse> expected = ResponseObject.<Xs2aScaStatusResponse>builder()
                                                             .body(new Xs2aScaStatusResponse(ScaStatus.FINALISED, true, "psu message", null, null))
                                                             .build();

        ConsentScaStatus consentScaStatus = new ConsentScaStatus(null, aisConsent, ScaStatus.FINALISED);
        ResponseObject<ConsentScaStatus> responseObject = ResponseObject.<ConsentScaStatus>builder()
                                                              .body(consentScaStatus)
                                                              .build();

        when(consentAuthorisationService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(responseObject);
        when(requestProviderService.getPsuIdData()).thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        when(aisConsentSpi.getScaStatus(ScaStatus.FINALISED, SPI_CONTEXT_DATA, WRONG_AUTHORISATION_ID, aisConsentMapper.mapToSpiAccountConsent(aisConsent), spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiScaStatusResponse>builder()
                            .payload(new SpiScaStatusResponse(ScaStatus.FINALISED, true, "psu message", null, null))
                            .build());

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertFalse(actual.hasError());
        assertEquals(expected.getBody(), actual.getBody());

        verify(consentAuthorisationService).getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);
        verifyNoInteractions(xs2aAuthorisationService);
    }

    @Test
    void getConsentAuthorisationScaStatus_withBeneficiariesFlag_spiError() {
        // Given
        ResponseObject<Xs2aScaStatusResponse> expected = ResponseObject.<Xs2aScaStatusResponse>builder()
                                                             .fail(ErrorHolder.builder(ErrorType.AIS_400).build())
                                                             .build();

        ConsentScaStatus consentScaStatus = new ConsentScaStatus(null, aisConsent, ScaStatus.FINALISED);
        ResponseObject<ConsentScaStatus> responseObject = ResponseObject.<ConsentScaStatus>builder()
                                                              .body(consentScaStatus)
                                                              .build();

        when(consentAuthorisationService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(responseObject);
        when(requestProviderService.getPsuIdData()).thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        TppMessage tppMessage = new TppMessage(MessageErrorCode.FORMAT_ERROR);
        SpiResponse<SpiScaStatusResponse> spiResponse = SpiResponse.<SpiScaStatusResponse>builder()
                                                            .error(tppMessage)
                                                            .build();
        when(aisConsentSpi.getScaStatus(ScaStatus.FINALISED, SPI_CONTEXT_DATA, WRONG_AUTHORISATION_ID, aisConsentMapper.mapToSpiAccountConsent(aisConsent), spiAspspConsentDataProvider)).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)).thenReturn(ErrorHolder.builder(ErrorType.AIS_400).build());

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(expected.getError(), actual.getError());

        verify(consentAuthorisationService).getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);
    }

    @Test
    void getConsentAuthorisationScaStatus_spiError() {
        // Given
        ResponseObject<Xs2aScaStatusResponse> expected = ResponseObject.<Xs2aScaStatusResponse>builder()
                                                             .fail(ErrorHolder.builder(ErrorType.AIS_400).build())
                                                             .build();
        ResponseObject<ConsentScaStatus> responseObject = ResponseObject.<ConsentScaStatus>builder()
                                                              .fail(ErrorHolder.builder(ErrorType.AIS_400).build())
                                                              .build();

        when(consentAuthorisationService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(responseObject);

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(expected.getError(), actual.getError());

        verify(consentAuthorisationService).getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);
    }

    @Test
    void updateConsentPsuData() {
        // Given
        ConsentAuthorisationsParameters updatePsuData = new ConsentAuthorisationsParameters();

        // When
        consentService.updateConsentPsuData(updatePsuData);

        // Then
        verify(consentAuthorisationService, times(1)).updateConsentPsuData(updatePsuData);
    }

    @Test
    void getConsentInitiationAuthorisations() {
        // When
        consentService.getConsentInitiationAuthorisations(CONSENT_ID);

        // Then
        verify(consentAuthorisationService, times(1)).getConsentInitiationAuthorisations(CONSENT_ID);
    }

    private AccountReference getXs2aReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, null, CORRECT_IBAN_2, null, null, null, null, CURRENCY_1, null);
    }

    private SpiAccountAccess getSpiAccountAccess() {
        return new SpiAccountAccess(Collections.singletonList(SpiAccountReference.builder()
                                                                  .iban(CORRECT_IBAN_2)
                                                                  .currency(CURRENCY_1)
                                                                  .build()), null, null, null, null, null, null);
    }

    private AccountAccess getXs2aAccountAccess(List<AccountReference> accounts) {
        return new AccountAccess(accounts, null, null, null);
    }

    private AisConsent getAisConsent() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);

        AccountAccess accountAccess = getXs2aAccountAccess(Collections.singletonList(getXs2aReference()));
        aisConsent.setTppAccountAccesses(accountAccess);
        aisConsent.setAspspAccountAccesses(accountAccess);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());
        aisConsent.setUsages(Collections.emptyMap());

        return aisConsent;
    }

    private CreateConsentReq getCreateConsentRequest(AccountAccess access, boolean allAccounts, boolean allPsd2) {
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(access);
        req.setValidUntil(DATE);
        req.setFrequencyPerDay(1);
        req.setCombinedServiceIndicator(false);
        req.setRecurringIndicator(false);
        req.setAvailableAccounts(allAccounts ? AccountAccessType.ALL_ACCOUNTS : null);
        req.setAllPsd2(allPsd2 ? AccountAccessType.ALL_ACCOUNTS : null);
        return req;
    }

    private AccountAccess getAccess(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions) {
        return new AccountAccess(accounts, balances, transactions, null);
    }

    private List<AccountReference> getReferenceList() {
        List<AccountReference> list = new ArrayList<>();
        list.add(getReference(CORRECT_IBAN_2, CURRENCY_1));
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

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_ID);
        return tppInfo;
    }

    private void assertValidationErrorIsPresent(ResponseObject response) {
        assertThat(response).isNotNull();
        assertThat(response.hasError()).isTrue();
        assertThat(response.getError()).isEqualTo(CONSENT_INVALID_401_ERROR);
    }

    private void assertResponseIsCorrect(CreateConsentResponse response) {
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(response.getPsuMessage()).isEqualTo(TEST_PSU_MESSAGE);
    }

    private de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject getAuthenticationObject() {
        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationType("MockedAuthenticationType");
        authenticationObject.setAuthenticationMethodId("MockedAuthenticationMethodId");
        authenticationObject.setAuthenticationVersion("MockedAuthenticationVersion");
        authenticationObject.setName("MockedName");
        authenticationObject.setDecoupled(false);
        authenticationObject.setExplanation("MockedExplanation");

        return authenticationObject;
    }
}
