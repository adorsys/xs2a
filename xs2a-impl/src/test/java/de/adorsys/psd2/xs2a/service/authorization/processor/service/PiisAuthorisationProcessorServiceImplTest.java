/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.AbstractPiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.CommonDecoupledPiisService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiMessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiTppMessage;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiTppMessageInformation;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaApproach;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaStatus;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PiisAuthorisationProcessorServiceImplTest {
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String ENCRYPTED_CONSENT_ID = "encrypted-consent-id";
    private static final String PSU_ID = "some-psu";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final String DECOUPLED_PSU_MESSAGE = "some message";
    private static final String PSU_PASSWORD = "some password";
    private static final String AUTHENTICATION_METHOD_ID = "authentication method id";
    private static final AuthenticationObject DECOUPLED_SCA_METHOD = new AuthenticationObject();
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final String TEST_AUTHORISATION_ID = "assddsff";
    private static final PsuIdData TEST_PSU_DATA = new PsuIdData("test-user", null, null, null, null);
    private static final ScaApproach TEST_SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final SpiScaApproach TEST_SPI_SCA_APPROACH = SpiScaApproach.EMBEDDED;
    private static final ScaStatus TEST_SCA_STATUS = ScaStatus.RECEIVED;
    private static final SpiScaStatus TEST_SPI_SCA_STATUS = SpiScaStatus.RECEIVED;
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = buildTppMessageInformationSet();
    private static final Set<SpiTppMessageInformation> TEST_SPI_TPP_MESSAGES = buildSpiTppMessageInformationSet();
    private static final String TEST_PSU_MESSAGE = "psu message";
    private static final ErrorType TEST_ERROR_TYPE_400 = PIIS_400;

    @Mock
    private AbstractPiisAuthorizationService consentAuthorisationService;
    @Mock
    private Xs2aAuthorisationService xs2aAuthorisationService;
    @Mock
    private Xs2aPiisConsentService xs2aPiisConsentService;
    @Mock
    private Xs2aToSpiPiisConsentMapper xs2aPiisConsentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private PiisConsentSpi piisConsentSpi;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private PiisScaAuthorisationService piisScaAuthorisationService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private CommonDecoupledPiisService commonDecoupledPiisService;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private SpiToXs2aScaStatusMapper spiToXs2aScaStatusMapper;
    @Mock
    private SpiToXs2aScaApproachMapper spiToXs2aScaApproachMapper;
    @Mock
    private SpiToXs2aTppMessageInformationMapper spiToXs2aTppMessageInformationMapper;
    @Mock
    private SpiToXs2aConsentStatusMapper spiToXs2aConsentStatusMapper;
    @Mock
    private SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    @Mock
    private SpiToXs2aChallengeDataMapper spiToXs2aChallengeDataMapper;
    @Mock
    private PiisScaAuthorisationServiceResolver piisScaAuthorisationServiceResolver;

    private PiisAuthorisationProcessorServiceImpl piisAuthorisationProcessorService;
    private final JsonReader jsonReader = new JsonReader();
    private PiisConsent piisConsent;
    private Authorisation authorisation;

    @BeforeEach
    void setUp() {
        piisConsent = jsonReader.getObjectFromFile("json/service/piis-consent.json", PiisConsent.class);
        authorisation = jsonReader.getObjectFromFile("json/service/authorisation.json", Authorisation.class);

        AbstractPiisAuthorizationService decoupledAisAuthorisationService = Mockito.mock(AbstractPiisAuthorizationService.class);
        List<AbstractPiisAuthorizationService> services = Arrays.asList(decoupledAisAuthorisationService, consentAuthorisationService);

        piisAuthorisationProcessorService = new PiisAuthorisationProcessorServiceImpl(xs2aAuthorisationService, spiContextDataProvider, spiAspspConsentDataProviderFactory,
                                                                                      spiErrorMapper, xs2aToSpiPsuDataMapper, services,
                                                                                      xs2aPiisConsentService, piisConsentSpi, xs2aPiisConsentMapper,
                                                                                      commonDecoupledPiisService, piisScaAuthorisationService,
                                                                                      spiToXs2aScaStatusMapper, spiToXs2aScaApproachMapper,
                                                                                      spiToXs2aTppMessageInformationMapper, spiToXs2aConsentStatusMapper,
                                                                                      spiToXs2aAuthenticationObjectMapper, spiToXs2aChallengeDataMapper,
                                                                                      piisScaAuthorisationServiceResolver);

    }

    @Test
    void updateAuthorisation_shouldUpdateWithCorrectService() {
        // Given
        CommonAuthorisationParameters updateAuthorisationRequest = new ConsentAuthorisationsParameters();

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();

        when(consentAuthorisationService.getScaApproachServiceType()).thenReturn(ScaApproach.EMBEDDED);

        // When
        piisAuthorisationProcessorService.updateAuthorisation(processorRequest, processorResponse);

        // Then
        verify(consentAuthorisationService).updateConsentPsuData(updateAuthorisationRequest, processorResponse);
    }

    @Test
    void updateAuthorisation_withNoAuthorisationServiceForApproach_shouldThrowException() {
        // Given
        CommonAuthorisationParameters updateAuthorisationRequest = new ConsentAuthorisationsParameters();

        AuthorisationProcessorRequest processorRequest = new AuthorisationProcessorRequest(ServiceType.PIIS, ScaApproach.REDIRECT, ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();

        // When
        assertThrows(IllegalArgumentException.class, () -> piisAuthorisationProcessorService.updateAuthorisation(processorRequest, processorResponse));

        // Then
        verify(consentAuthorisationService, never()).updateConsentPsuData(any(CommonAuthorisationParameters.class), any(AuthorisationProcessorResponse.class));
    }

    @Test
    void doScaReceived_withPsuIdentificationRequest() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setUpdatePsuIdentification(true);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.PSUIDENTIFIED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
    }

    @Test
    void doScaReceived_withPsuIdentificationRequest_withoutPsuData_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setUpdatePsuIdentification(true);
        updateAuthorisationRequest.setPsuData(null);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(PIIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withOneScaMethod() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        List<AuthenticationObject> availableScaMethods = buildTestAuthenticationObjectSingleValueList(false);

        List<SpiAuthenticationObject> availableSpiScaMethods = buildTestSpiAuthenticationObjectSingleValueList(false);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableSpiScaMethods);
        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        when(piisConsentSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(buildSpiAuthorizationCodeResult())
                            .build());
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(buildTestSpiAuthenticationObjectSingleValueList(false))).thenReturn(buildTestAuthenticationObjectSingleValueList(false));
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObject(buildTestSpiAuthenticationObject(false))).thenReturn(buildTestAuthenticationObject(false));

        AuthenticationObject chosenScaMethod = new AuthenticationObject();

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(chosenScaMethod, processorResponse.getChosenScaMethod());

        verify(xs2aAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withOneDecoupledScaMethod() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        List<AuthenticationObject> availableScaMethods = buildTestAuthenticationObjectSingleValueList(true);

        List<SpiAuthenticationObject> availableSpiScaMethods = buildTestSpiAuthenticationObjectSingleValueList(true);

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableSpiScaMethods);
        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        UpdateConsentPsuDataResponse decoupledResponse = buildDecoupledUpdateConsentPsuDataResponse();
        when(commonDecoupledPiisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiPiisConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA))
            .thenReturn(decoupledResponse);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(buildTestSpiAuthenticationObjectSingleValueList(true))).thenReturn(buildTestAuthenticationObjectSingleValueList(true));

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(DECOUPLED_SCA_METHOD, processorResponse.getChosenScaMethod());
        assertEquals(DECOUPLED_PSU_MESSAGE, processorResponse.getPsuMessage());

        verify(xs2aAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(commonDecoupledPiisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiPiisConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA);
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withMultipleScaMethods() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject firstAuthenticationObject = new AuthenticationObject();
        firstAuthenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        AuthenticationObject secondAuthenticationObject = new AuthenticationObject();

        SpiAuthenticationObject firstSpiAuthenticationObject = new SpiAuthenticationObject();
        firstSpiAuthenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        SpiAuthenticationObject secondSpiAuthenticationObject = new SpiAuthenticationObject();

        List<AuthenticationObject> availableScaMethods = Arrays.asList(firstAuthenticationObject, secondAuthenticationObject);
        List<SpiAuthenticationObject> availableSpiScaMethods = Arrays.asList(firstSpiAuthenticationObject, secondSpiAuthenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableSpiScaMethods);
        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(availableSpiScaMethods)).thenReturn(availableScaMethods);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.PSUAUTHENTICATED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(availableScaMethods, processorResponse.getAvailableScaMethods());

        verify(xs2aAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_oneFactorAuthorisation() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        when(piisScaAuthorisationService.isOneFactorAuthorisation(piisConsent)).thenReturn(true);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.FINALISED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aPiisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.VALID);
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_decoupledApproach() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setDecoupled(true);
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        SpiAuthenticationObject spiAuthenticationObject = new SpiAuthenticationObject();
        spiAuthenticationObject.setDecoupled(true);
        spiAuthenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(Collections.singletonList(spiAuthenticationObject));
        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        when(commonDecoupledPiisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiPiisConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA))
            .thenReturn(buildDecoupledUpdateConsentPsuDataResponse());
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(buildTestSpiAuthenticationObjectSingleValueList(true))).thenReturn(buildTestAuthenticationObjectSingleValueList(true));

        authorisation.setChosenScaApproach(ScaApproach.DECOUPLED);
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(commonDecoupledPiisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiPiisConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA);
        verify(xs2aAuthorisationService, times(1)).saveAuthenticationMethods(AUTHORISATION_ID, Collections.singletonList(authenticationObject));
        verify(xs2aAuthorisationService, times(1)).updateScaApproach(AUTHORISATION_ID, ScaApproach.DECOUPLED);
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withZeroScaMethods_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(Collections.emptyList());
        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aPiisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.REJECTED);
        verify(xs2aAuthorisationService).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FAILED);
        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withInvalidConsentId_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(PIIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withAuthorisePsuError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        MessageErrorCode errorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiMessageErrorCode spiErrorCode = SpiMessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiPsuAuthorisationResponse> errorResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                     .error(new SpiTppMessage(spiErrorCode))
                                                                     .build();
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(ErrorType.AIS_403)
                            .tppMessages(TppMessageInformation.of(errorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(errorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(piisConsentSpi, never()).requestAvailableScaMethods(any(), any(), any());
        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withFailedPsuAuthorisation_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.FAILURE))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(PIIS_401, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(piisConsentSpi, never()).requestAvailableScaMethods(any(), any(), any());
        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withRequestScaMethodsError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        MessageErrorCode errorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiMessageErrorCode spiErrorCode = SpiMessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiAvailableScaMethodsResponse> errorResponse = SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                        .error(new SpiTppMessage(spiErrorCode))
                                                                        .build();

        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(errorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(errorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuIdentificationRequest() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setUpdatePsuIdentification(true);


        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.PSUIDENTIFIED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
    }

    @Test
    void doScaPsuIdentified_withPsuIdentificationRequest_withoutPsuData_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setUpdatePsuIdentification(true);
        updateAuthorisationRequest.setPsuData(null);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(PIIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withOneScaMethod() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        List<AuthenticationObject> availableScaMethods = buildTestAuthenticationObjectSingleValueList(false);

        List<SpiAuthenticationObject> availableSpiScaMethods = buildTestSpiAuthenticationObjectSingleValueList(false);

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableSpiScaMethods);
        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        when(piisConsentSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(buildSpiAuthorizationCodeResult())
                            .build());
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(buildTestSpiAuthenticationObjectSingleValueList(false))).thenReturn(buildTestAuthenticationObjectSingleValueList(false));
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObject(buildTestSpiAuthenticationObject(false))).thenReturn(buildTestAuthenticationObject(false));

        AuthenticationObject chosenScaMethod = new AuthenticationObject();

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(chosenScaMethod, processorResponse.getChosenScaMethod());

        verify(xs2aAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withOneDecoupledScaMethod() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        List<AuthenticationObject> availableScaMethods = buildTestAuthenticationObjectSingleValueList(true);

        List<SpiAuthenticationObject> availableSpiScaMethods = buildTestSpiAuthenticationObjectSingleValueList(true);

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableSpiScaMethods);
        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        UpdateConsentPsuDataResponse decoupledResponse = buildDecoupledUpdateConsentPsuDataResponse();
        when(commonDecoupledPiisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiPiisConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA))
            .thenReturn(decoupledResponse);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(availableSpiScaMethods)).thenReturn(availableScaMethods);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(DECOUPLED_SCA_METHOD, processorResponse.getChosenScaMethod());
        assertEquals(DECOUPLED_PSU_MESSAGE, processorResponse.getPsuMessage());

        verify(xs2aAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(commonDecoupledPiisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiPiisConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA);
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withMultipleScaMethods() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject firstAuthenticationObject = new AuthenticationObject();
        firstAuthenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        AuthenticationObject secondAuthenticationObject = new AuthenticationObject();
        List<AuthenticationObject> availableScaMethods = Arrays.asList(firstAuthenticationObject, secondAuthenticationObject);

        SpiAuthenticationObject firstSpiAuthenticationObject = new SpiAuthenticationObject();
        firstSpiAuthenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        SpiAuthenticationObject secondSpiAuthenticationObject = new SpiAuthenticationObject();
        List<SpiAuthenticationObject> availableSpiScaMethods = Arrays.asList(firstSpiAuthenticationObject, secondSpiAuthenticationObject);

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableSpiScaMethods);
        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(availableSpiScaMethods)).thenReturn(availableScaMethods);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.PSUAUTHENTICATED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(availableScaMethods, processorResponse.getAvailableScaMethods());

        verify(xs2aAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_oneFactorAuthorisation() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        when(piisScaAuthorisationService.isOneFactorAuthorisation(piisConsent)).thenReturn(true);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.FINALISED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aPiisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.VALID);
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_decoupledApproach() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        List<AuthenticationObject> availableScaMethods = buildTestAuthenticationObjectSingleValueList(true);

        List<SpiAuthenticationObject> availableSpiScaMethods = buildTestSpiAuthenticationObjectSingleValueList(true);

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableSpiScaMethods);
        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        when(commonDecoupledPiisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiPiisConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA))
            .thenReturn(buildDecoupledUpdateConsentPsuDataResponse());
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(availableSpiScaMethods)).thenReturn(availableScaMethods);

        authorisation.setChosenScaApproach(ScaApproach.DECOUPLED);
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(commonDecoupledPiisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiPiisConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA);
        verify(xs2aAuthorisationService, times(1)).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(xs2aAuthorisationService, times(1)).updateScaApproach(AUTHORISATION_ID, ScaApproach.DECOUPLED);
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withZeroScaMethods_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(Collections.emptyList());
        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aPiisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.REJECTED);
        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withInvalidConsentId_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(PIIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withAuthorisePsuError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        MessageErrorCode errorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiMessageErrorCode spiErrorCode = SpiMessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiPsuAuthorisationResponse> errorResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                     .error(new SpiTppMessage(spiErrorCode))
                                                                     .build();
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(errorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(errorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(piisConsentSpi, never()).requestAvailableScaMethods(any(), any(), any());
        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withFailedPsuAuthorisation_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.FAILURE))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(PIIS_401, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(piisConsentSpi, never()).requestAvailableScaMethods(any(), any(), any());
        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withAttemptFailedPsuAuthorisation_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.ATTEMPT_FAILURE))
                                                                   .error(new SpiTppMessage(SpiMessageErrorCode.PSU_CREDENTIALS_INVALID))
                                                                   .build();
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);

        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS)).thenReturn(ErrorHolder.builder(ErrorType.AIS_401)
                                                                                           .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                                                                           .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_401, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.PSUIDENTIFIED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(piisConsentSpi, never()).requestAvailableScaMethods(any(), any(), any());
        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withRequestScaMethodsError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(piisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        MessageErrorCode errorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiMessageErrorCode spiErrorCode = SpiMessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiAvailableScaMethodsResponse> errorResponse = SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                        .error(new SpiTppMessage(spiErrorCode))
                                                                        .build();

        when(piisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(errorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(errorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuAuthenticated() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(piisConsentSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(buildSpiAuthorizationCodeResult())
                            .build());

        AuthenticationObject chosenScaMethod = new AuthenticationObject();

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObject(new SpiAuthenticationObject())).thenReturn(new AuthenticationObject());

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(chosenScaMethod, processorResponse.getChosenScaMethod());
    }

    @Test
    void doScaPsuAuthenticated_decoupledApproach() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        when(xs2aAuthorisationService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(true);

        UpdateConsentPsuDataResponse decoupledResponse = buildDecoupledUpdateConsentPsuDataResponse();
        when(commonDecoupledPiisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiPiisConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA))
            .thenReturn(decoupledResponse);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(DECOUPLED_SCA_METHOD, processorResponse.getChosenScaMethod());
        assertEquals(DECOUPLED_PSU_MESSAGE, processorResponse.getPsuMessage());

        verify(xs2aAuthorisationService).updateScaApproach(AUTHORISATION_ID, ScaApproach.DECOUPLED);
        verify(commonDecoupledPiisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiPiisConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA);
    }

    @Test
    void doScaPsuAuthenticated_withInvalidConsentId_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(PIIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(piisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuAuthenticated_withRequestAuthCodeError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        SpiAuthenticationObject spiChosenScaMethod = new SpiAuthenticationObject();
        spiAuthorizationCodeResult.setSelectedScaMethod(spiChosenScaMethod);

        SpiChallengeData challengeData = new SpiChallengeData();
        spiAuthorizationCodeResult.setChallengeData(challengeData);

        MessageErrorCode errorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiMessageErrorCode spiErrorCode = SpiMessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiAuthorizationCodeResult> errorResponse = SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                    .error(new SpiTppMessage(spiErrorCode))
                                                                    .build();

        when(piisConsentSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(errorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(errorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
    }

    @Test
    void doScaMethodSelected() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiVerifyScaAuthorisationResponse spiVerifyScaAuthorisationResponse = new SpiVerifyScaAuthorisationResponse(SpiConsentStatus.VALID);
        when(piisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, null, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                            .payload(spiVerifyScaAuthorisationResponse)
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.SCAMETHODSELECTED, updateAuthorisationRequest, authorisation);
        when(spiToXs2aConsentStatusMapper.mapToXs2aConsentStatus(SpiConsentStatus.VALID)).thenReturn(ConsentStatus.VALID);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaMethodSelected(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.FINALISED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
    }

    @Test
    void doScaMethodSelected_withPartiallyAuthorisedConsent_shouldUpdateMultilevelSca() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        PiisConsent piisConsent = new PiisConsent();
        piisConsent.setPsuIdDataList(new ArrayList<>());
        piisConsent.setConsentTppInformation(new ConsentTppInformation());
        piisConsent.setPsuIdDataList(new ArrayList<>());

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiVerifyScaAuthorisationResponse spiVerifyScaAuthorisationResponse = new SpiVerifyScaAuthorisationResponse(SpiConsentStatus.PARTIALLY_AUTHORISED);
        when(piisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, null, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                            .payload(spiVerifyScaAuthorisationResponse)
                            .build());
        when(spiToXs2aConsentStatusMapper.mapToXs2aConsentStatus(SpiConsentStatus.PARTIALLY_AUTHORISED)).thenReturn(ConsentStatus.PARTIALLY_AUTHORISED);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.SCAMETHODSELECTED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaMethodSelected(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.FINALISED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aPiisConsentService).updateMultilevelScaRequired(ENCRYPTED_CONSENT_ID, true);
        verify(xs2aPiisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.PARTIALLY_AUTHORISED);
    }

    @Test
    void doScaMethodSelected_withInvalidConsentId_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.SCAMETHODSELECTED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaMethodSelected(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(PIIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void doScaMethodSelected_withRequestScaMethodsError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));

        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);


        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        MessageErrorCode errorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiMessageErrorCode spiErrorCode = SpiMessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiVerifyScaAuthorisationResponse> errorResponse = SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                                                                           .error(new SpiTppMessage(spiErrorCode))
                                                                           .build();

        when(piisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, null, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(errorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.SCAMETHODSELECTED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaMethodSelected(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(errorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
    }

    @Test
    void doScaStarted_success() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);
        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = SpiResponse.<SpiStartAuthorisationResponse>builder()
                                                                     .payload(new SpiStartAuthorisationResponse(TEST_SPI_SCA_APPROACH, TEST_SPI_SCA_STATUS, TEST_PSU_MESSAGE, TEST_SPI_TPP_MESSAGES))
                                                                     .build();
        when(piisConsentSpi.startAuthorisation(SPI_CONTEXT_DATA, TEST_SPI_SCA_APPROACH, TEST_SPI_SCA_STATUS, TEST_AUTHORISATION_ID, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiToXs2aScaStatusMapper.mapToXs2aScaStatus(SpiScaStatus.RECEIVED)).thenReturn(ScaStatus.RECEIVED);
        when(spiToXs2aScaApproachMapper.mapToXs2aScaApproach(SpiScaApproach.EMBEDDED)).thenReturn(ScaApproach.EMBEDDED);
        when(spiToXs2aTppMessageInformationMapper.toTppMessageInformationSet(TEST_SPI_TPP_MESSAGES)).thenReturn(TEST_TPP_MESSAGES);
        when(piisScaAuthorisationServiceResolver.getService()).thenReturn(consentAuthorisationService);
        when(consentAuthorisationService.createConsentAuthorization(any())).thenReturn(Optional.of(getCreateConsentAuthorizationResponse()));

        // When
        AuthorisationProcessorResponse actual = piisAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);
        CreateConsentAuthorisationProcessorResponse expected = buildCreateConsentAuthorisationProcessorResponse();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void doScaStarted_authorisationEmpty() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);
        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = SpiResponse.<SpiStartAuthorisationResponse>builder()
                                                                     .payload(new SpiStartAuthorisationResponse(TEST_SPI_SCA_APPROACH, TEST_SPI_SCA_STATUS, TEST_PSU_MESSAGE, TEST_SPI_TPP_MESSAGES))
                                                                     .build();
        when(piisConsentSpi.startAuthorisation(SPI_CONTEXT_DATA, TEST_SPI_SCA_APPROACH, TEST_SPI_SCA_STATUS, TEST_AUTHORISATION_ID, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiToXs2aScaStatusMapper.mapToXs2aScaStatus(SpiScaStatus.RECEIVED)).thenReturn(ScaStatus.RECEIVED);
        when(spiToXs2aScaApproachMapper.mapToXs2aScaApproach(SpiScaApproach.EMBEDDED)).thenReturn(ScaApproach.EMBEDDED);
        when(piisScaAuthorisationServiceResolver.getService()).thenReturn(consentAuthorisationService);
        when(consentAuthorisationService.createConsentAuthorization(any())).thenReturn(Optional.empty());

        // When
        AuthorisationProcessorResponse actual = piisAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);

        ErrorHolder errorHolder = ErrorHolder.builder(PIIS_403)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403))
                                      .build();
        CreateConsentAuthorisationProcessorResponse expected = buildCreateConsentAuthorisationProcessorResponseWithError(errorHolder);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void doScaStarted_spiError() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);
        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        when(xs2aPiisConsentMapper.mapToSpiPiisConsent(piisConsent)).thenReturn(spiPiisConsent);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = SpiResponse.<SpiStartAuthorisationResponse>builder()
                                                                     .error(new SpiTppMessage(SpiMessageErrorCode.FORMAT_ERROR_ABSENT_HEADER))
                                                                     .build();
        when(piisConsentSpi.startAuthorisation(SPI_CONTEXT_DATA, TEST_SPI_SCA_APPROACH, TEST_SPI_SCA_STATUS, TEST_AUTHORISATION_ID, spiPiisConsent, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);
        ErrorHolder errorHolder = ErrorHolder.builder(TEST_ERROR_TYPE_400).build();
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(errorHolder);

        // When
        AuthorisationProcessorResponse actual = piisAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);
        CreateConsentAuthorisationProcessorResponse expected = buildCreateConsentAuthorisationProcessorResponseWithError(errorHolder);

        // Then
        assertThat(actual.hasError()).isTrue();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void doScaStarted_cmsError() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        when(xs2aPiisConsentService.getPiisConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());
        ErrorHolder errorHolder = ErrorHolder.builder(TEST_ERROR_TYPE_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                      .build();

        // When
        AuthorisationProcessorResponse actual = piisAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);

        // Then
        assertThat(actual.hasError()).isTrue();
        ErrorHolder errorHolderActual = actual.getErrorHolder();
        assertThat(errorHolderActual.getErrorType()).isEqualTo(PIIS_400);
        assertThat(errorHolderActual.getTppMessageInformationList()).isEqualTo(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)));
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FAILED);
        assertThat(actual.getConsentId()).isEqualTo(ENCRYPTED_CONSENT_ID);

        verify(piisConsentSpi, never()).startAuthorisation(any(), any(), any(), any(), any(), any());
    }

    @Test
    void doScaFinalised_shouldReturnFinalisedResponse() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.FINALISED, updateAuthorisationRequest, null);

        // When
        AuthorisationProcessorResponse processorResponse = piisAuthorisationProcessorService.doScaFinalised(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.FINALISED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
    }

    @Test
    void doScaFailed_shouldThrowException() {
        // Given
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.FAILED, null, null);

        // When
        assertThrows(UnsupportedOperationException.class, () -> piisAuthorisationProcessorService.doScaFailed(processorRequest));
    }

    @Test
    void doScaExempted_shouldThrowException() {
        // Given
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.EXEMPTED, null, null);

        // When
        assertThrows(UnsupportedOperationException.class, () -> piisAuthorisationProcessorService.doScaExempted(processorRequest));
    }

    private TerminateOldConsentsRequest getRequestFromConsent(AisConsent aisConsent) {
        return new TerminateOldConsentsRequest(aisConsent.isOneAccessType(),
                                               aisConsent.isWrongConsentData(),
                                               aisConsent.getPsuIdDataList(),
                                               Optional.ofNullable(aisConsent.getTppInfo())
                                                   .map(TppInfo::getAuthorisationNumber)
                                                   .orElse(null),
                                               aisConsent.getInstanceId());
    }

    private ConsentAuthorisationsParameters buildUpdateConsentPsuDataReq() {
        ConsentAuthorisationsParameters updateAuthorisationRequest = new ConsentAuthorisationsParameters();
        updateAuthorisationRequest.setConsentId(ENCRYPTED_CONSENT_ID);
        updateAuthorisationRequest.setAuthorizationId(AUTHORISATION_ID);
        updateAuthorisationRequest.setPsuData(PSU_ID_DATA);

        return updateAuthorisationRequest;
    }

    private AuthorisationProcessorRequest buildAuthorisationProcessorRequest(ScaStatus scaStatus, CommonAuthorisationParameters updateAuthorisationRequest, Authorisation authorisation) {
        return new AuthorisationProcessorRequest(ServiceType.PIIS, ScaApproach.EMBEDDED, scaStatus, updateAuthorisationRequest, authorisation);
    }

    private AuthorisationProcessorRequest buildAuthorisationProcessorRequest() {
        ConsentAuthorisationsParameters request = new ConsentAuthorisationsParameters();
        request.setConsentId(ENCRYPTED_CONSENT_ID);
        request.setAuthorizationId(TEST_AUTHORISATION_ID);
        request.setPsuData(TEST_PSU_DATA);
        Authorisation authorisation = new Authorisation();
        return new AisAuthorisationProcessorRequest(TEST_SCA_APPROACH,
                                                    TEST_SCA_STATUS,
                                                    request,
                                                    authorisation);
    }

    private UpdateConsentPsuDataResponse buildDecoupledUpdateConsentPsuDataResponse() {
        UpdateConsentPsuDataResponse decoupledResponse = new UpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED, ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, PSU_ID_DATA);
        decoupledResponse.setPsuMessage(DECOUPLED_PSU_MESSAGE);
        decoupledResponse.setChosenScaMethod(DECOUPLED_SCA_METHOD);
        return decoupledResponse;
    }

    private SpiAuthorizationCodeResult buildSpiAuthorizationCodeResult() {
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        SpiAuthenticationObject method = new SpiAuthenticationObject();
        spiAuthorizationCodeResult.setSelectedScaMethod(method);
        spiAuthorizationCodeResult.setChallengeData(new SpiChallengeData());
        return spiAuthorizationCodeResult;
    }

    private static Set<TppMessageInformation> buildTppMessageInformationSet() {
        return Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
    }

    private static Set<SpiTppMessageInformation> buildSpiTppMessageInformationSet() {
        return Collections.singleton(SpiTppMessageInformation.of(SpiMessageErrorCode.FORMAT_ERROR));
    }

    private CreateConsentAuthorisationProcessorResponse buildCreateConsentAuthorisationProcessorResponse() {
        return new CreateConsentAuthorisationProcessorResponse(TEST_SCA_STATUS, TEST_SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, ENCRYPTED_CONSENT_ID, TEST_AUTHORISATION_ID, null, TEST_PSU_DATA);
    }

    private CreateConsentAuthorisationProcessorResponse buildCreateConsentAuthorisationProcessorResponseWithError(ErrorHolder errorHolder) {
        return new CreateConsentAuthorisationProcessorResponse(errorHolder, TEST_SCA_APPROACH, ENCRYPTED_CONSENT_ID, TEST_PSU_DATA);
    }

    private List<AuthenticationObject> buildTestAuthenticationObjectSingleValueList(boolean isDecoupled) {
        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        authenticationObject.setDecoupled(isDecoupled);
        return Collections.singletonList(authenticationObject);
    }

    private List<SpiAuthenticationObject> buildTestSpiAuthenticationObjectSingleValueList(boolean isDecoupled) {
        SpiAuthenticationObject spiAuthenticationObject = new SpiAuthenticationObject();
        spiAuthenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        spiAuthenticationObject.setDecoupled(isDecoupled);
        return Collections.singletonList(spiAuthenticationObject);
    }

    private AuthenticationObject buildTestAuthenticationObject(boolean isDecoupled) {
        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setDecoupled(isDecoupled);
        return authenticationObject;
    }

    private SpiAuthenticationObject buildTestSpiAuthenticationObject(boolean isDecoupled) {
        SpiAuthenticationObject authenticationObject = new SpiAuthenticationObject();
        authenticationObject.setDecoupled(isDecoupled);
        return authenticationObject;
    }

    private CreateConsentAuthorizationResponse getCreateConsentAuthorizationResponse() {
        CreateConsentAuthorizationResponse createConsentAuthResponse = new CreateConsentAuthorizationResponse();
        createConsentAuthResponse.setScaApproach(TEST_SCA_APPROACH);
        createConsentAuthResponse.setScaStatus(TEST_SCA_STATUS);
        return createConsentAuthResponse;
    }
}
