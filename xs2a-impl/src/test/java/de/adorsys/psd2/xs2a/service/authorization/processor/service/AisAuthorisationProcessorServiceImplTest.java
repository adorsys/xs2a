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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AbstractAisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.CommonDecoupledAisService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
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
class AisAuthorisationProcessorServiceImplTest {
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
    private static final ScaStatus TEST_SCA_STATUS = ScaStatus.RECEIVED;
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = buildTppMessageInformationSet();
    private static final String TEST_PSU_MESSAGE = "psu message";
    private static final ErrorType TEST_ERROR_TYPE_400 = AIS_400;

    @Mock
    private AbstractAisAuthorizationService consentAuthorizationService;
    @Mock
    private Xs2aAuthorisationService xs2aAuthorisationService;
    @Mock
    private Xs2aAisConsentService xs2aAisConsentService;
    @Mock
    private Xs2aAisConsentMapper xs2aAisConsentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private AisConsentSpi aisConsentSpi;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private AisScaAuthorisationService aisScaAuthorisationService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private CommonDecoupledAisService commonDecoupledAisService;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    private AisAuthorisationProcessorServiceImpl aisAuthorisationProcessorService;
    private final JsonReader jsonReader = new JsonReader();
    private AisConsent aisConsent;
    private Authorisation authorisation;

    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent-received-status.json", AisConsent.class);
        authorisation = jsonReader.getObjectFromFile("json/service/authorisation.json", Authorisation.class);

        AbstractAisAuthorizationService decoupledAisAuthorisationService = Mockito.mock(AbstractAisAuthorizationService.class);
        List<AbstractAisAuthorizationService> services = Arrays.asList(decoupledAisAuthorisationService, consentAuthorizationService);

        aisAuthorisationProcessorService = new AisAuthorisationProcessorServiceImpl(xs2aAuthorisationService, spiContextDataProvider, spiAspspConsentDataProviderFactory,
                                                                                    spiErrorMapper, xs2aToSpiPsuDataMapper, services,
                                                                                    xs2aAisConsentService, aisConsentSpi, xs2aAisConsentMapper,
                                                                                    commonDecoupledAisService, aisScaAuthorisationService);
    }

    @Test
    void updateAuthorisation_shouldUpdateWithCorrectService() {
        // Given
        CommonAuthorisationParameters updateAuthorisationRequest = new ConsentAuthorisationsParameters();

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();

        when(consentAuthorizationService.getScaApproachServiceType()).thenReturn(ScaApproach.EMBEDDED);

        // When
        aisAuthorisationProcessorService.updateAuthorisation(processorRequest, processorResponse);

        // Then
        verify(consentAuthorizationService).updateConsentPsuData(updateAuthorisationRequest, processorResponse);
    }

    @Test
    void updateAuthorisation_withNoAuthorisationServiceForApproach_shouldThrowException() {
        // Given
        CommonAuthorisationParameters updateAuthorisationRequest = new ConsentAuthorisationsParameters();

        AuthorisationProcessorRequest processorRequest = new AuthorisationProcessorRequest(ServiceType.AIS, ScaApproach.REDIRECT, ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();

        // When
        assertThrows(IllegalArgumentException.class, () -> aisAuthorisationProcessorService.updateAuthorisation(processorRequest, processorResponse));

        // Then
        verify(consentAuthorizationService, never()).updateConsentPsuData(any(CommonAuthorisationParameters.class), any(AuthorisationProcessorResponse.class));
    }

    @Test
    void doScaReceived_withPsuIdentificationRequest() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setUpdatePsuIdentification(true);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

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
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withOneScaMethod() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        List<AuthenticationObject> availableScaMethods = Collections.singletonList(authenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        when(aisConsentSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(buildSpiAuthorizationCodeResult())
                            .build());

        AuthenticationObject chosenScaMethod = new AuthenticationObject();

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

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

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        authenticationObject.setDecoupled(true);
        List<AuthenticationObject> availableScaMethods = Collections.singletonList(authenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        UpdateConsentPsuDataResponse decoupledResponse = buildDecoupledUpdateConsentPsuDataResponse();
        when(commonDecoupledAisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA))
            .thenReturn(decoupledResponse);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(DECOUPLED_SCA_METHOD, processorResponse.getChosenScaMethod());
        assertEquals(DECOUPLED_PSU_MESSAGE, processorResponse.getPsuMessage());

        verify(xs2aAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(commonDecoupledAisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA);
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withMultipleScaMethods() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject firstAuthenticationObject = new AuthenticationObject();
        firstAuthenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        AuthenticationObject secondAuthenticationObject = new AuthenticationObject();

        List<AuthenticationObject> availableScaMethods = Arrays.asList(firstAuthenticationObject, secondAuthenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.PSUAUTHENTICATED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(availableScaMethods, processorResponse.getAvailableScaMethods());

        verify(xs2aAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_oneFactorAuthorisation() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        when(aisScaAuthorisationService.isOneFactorAuthorisation(aisConsent)).thenReturn(true);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.FINALISED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aAisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.VALID);
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_decoupledApproach() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setDecoupled(true);
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(Collections.singletonList(authenticationObject));
        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        when(commonDecoupledAisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA))
            .thenReturn(buildDecoupledUpdateConsentPsuDataResponse());

        authorisation.setChosenScaApproach(ScaApproach.DECOUPLED);
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(commonDecoupledAisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA);
        verify(xs2aAuthorisationService, times(1)).saveAuthenticationMethods(AUTHORISATION_ID, Collections.singletonList(authenticationObject));
        verify(xs2aAuthorisationService, times(1)).updateScaApproach(AUTHORISATION_ID, ScaApproach.DECOUPLED);
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withZeroScaMethods_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(Collections.emptyList());
        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aAisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.REJECTED);
        verify(xs2aAuthorisationService).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FAILED);
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withInvalidConsentId_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withAuthorisePsuError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiPsuAuthorisationResponse> errorResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                     .error(new TppMessage(spiErrorCode))
                                                                     .build();
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(spiErrorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(aisConsentSpi, never()).requestAvailableScaMethods(any(), any(), any());
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withFailedPsuAuthorisation_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.FAILURE))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_401, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(aisConsentSpi, never()).requestAvailableScaMethods(any(), any(), any());
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withRequestScaMethodsError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiAvailableScaMethodsResponse> errorResponse = SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                        .error(new TppMessage(spiErrorCode))
                                                                        .build();

        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(spiErrorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuIdentificationRequest() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setUpdatePsuIdentification(true);


        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

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
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withOneScaMethod() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        List<AuthenticationObject> availableScaMethods = Collections.singletonList(authenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        when(aisConsentSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(buildSpiAuthorizationCodeResult())
                            .build());

        AuthenticationObject chosenScaMethod = new AuthenticationObject();

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

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

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        authenticationObject.setDecoupled(true);
        List<AuthenticationObject> availableScaMethods = Collections.singletonList(authenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        UpdateConsentPsuDataResponse decoupledResponse = buildDecoupledUpdateConsentPsuDataResponse();
        when(commonDecoupledAisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA))
            .thenReturn(decoupledResponse);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(DECOUPLED_SCA_METHOD, processorResponse.getChosenScaMethod());
        assertEquals(DECOUPLED_PSU_MESSAGE, processorResponse.getPsuMessage());

        verify(xs2aAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(commonDecoupledAisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA);
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withMultipleScaMethods() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject firstAuthenticationObject = new AuthenticationObject();
        firstAuthenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        AuthenticationObject secondAuthenticationObject = new AuthenticationObject();

        List<AuthenticationObject> availableScaMethods = Arrays.asList(firstAuthenticationObject, secondAuthenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.PSUAUTHENTICATED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(availableScaMethods, processorResponse.getAvailableScaMethods());

        verify(xs2aAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_oneFactorAuthorisation() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        when(aisScaAuthorisationService.isOneFactorAuthorisation(aisConsent)).thenReturn(true);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.FINALISED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aAisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.VALID);
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_decoupledApproach() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setDecoupled(true);
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(Collections.singletonList(authenticationObject));
        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        when(commonDecoupledAisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA))
            .thenReturn(buildDecoupledUpdateConsentPsuDataResponse());

        authorisation.setChosenScaApproach(ScaApproach.DECOUPLED);
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(commonDecoupledAisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA);
        verify(xs2aAuthorisationService, times(1)).saveAuthenticationMethods(AUTHORISATION_ID, Collections.singletonList(authenticationObject));
        verify(xs2aAuthorisationService, times(1)).updateScaApproach(AUTHORISATION_ID, ScaApproach.DECOUPLED);
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withZeroScaMethods_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(Collections.emptyList());
        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aAisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.REJECTED);
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withInvalidConsentId_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withAuthorisePsuError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiPsuAuthorisationResponse> errorResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                     .error(new TppMessage(spiErrorCode))
                                                                     .build();
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(spiErrorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(aisConsentSpi, never()).requestAvailableScaMethods(any(), any(), any());
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withFailedPsuAuthorisation_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.FAILURE))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_401, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(aisConsentSpi, never()).requestAvailableScaMethods(any(), any(), any());
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withAttemptFailedPsuAuthorisation_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.ATTEMPT_FAILURE))
                                                                   .error(new TppMessage(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                                                   .build();
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);

        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)).thenReturn(ErrorHolder.builder(ErrorType.AIS_401)
                                                                                           .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                                                                           .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_401, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.PSUIDENTIFIED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(aisConsentSpi, never()).requestAvailableScaMethods(any(), any(), any());
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withRequestScaMethodsError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiAvailableScaMethodsResponse> errorResponse = SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                        .error(new TppMessage(spiErrorCode))
                                                                        .build();

        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(spiErrorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuAuthenticated() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(aisConsentSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(buildSpiAuthorizationCodeResult())
                            .build());

        AuthenticationObject chosenScaMethod = new AuthenticationObject();

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

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

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        when(xs2aAuthorisationService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(true);

        UpdateConsentPsuDataResponse decoupledResponse = buildDecoupledUpdateConsentPsuDataResponse();
        when(commonDecoupledAisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA))
            .thenReturn(decoupledResponse);

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(DECOUPLED_SCA_METHOD, processorResponse.getChosenScaMethod());
        assertEquals(DECOUPLED_PSU_MESSAGE, processorResponse.getPsuMessage());

        verify(xs2aAuthorisationService).updateScaApproach(AUTHORISATION_ID, ScaApproach.DECOUPLED);
        verify(commonDecoupledAisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, PSU_ID_DATA);
    }

    @Test
    void doScaPsuAuthenticated_withInvalidConsentId_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuAuthenticated_withRequestAuthCodeError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        AuthenticationObject spiChosenScaMethod = new AuthenticationObject();
        spiAuthorizationCodeResult.setSelectedScaMethod(spiChosenScaMethod);
        ChallengeData challengeData = new ChallengeData();
        spiAuthorizationCodeResult.setChallengeData(challengeData);

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiAuthorizationCodeResult> errorResponse = SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                    .error(new TppMessage(spiErrorCode))
                                                                    .build();

        when(aisConsentSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(spiErrorCode)), errorHolder.getTppMessageInformationList());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
    }

    @Test
    void doScaMethodSelected() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        SpiScaConfirmation spiScaConfirmation = new SpiScaConfirmation();
        when(xs2aAisConsentMapper.mapToSpiScaConfirmation(updateAuthorisationRequest, PSU_ID_DATA))
            .thenReturn(spiScaConfirmation);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiVerifyScaAuthorisationResponse spiVerifyScaAuthorisationResponse = new SpiVerifyScaAuthorisationResponse(ConsentStatus.VALID);
        when(aisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, spiScaConfirmation, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                            .payload(spiVerifyScaAuthorisationResponse)
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.SCAMETHODSELECTED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaMethodSelected(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.FINALISED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aAisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.VALID);
        var request = getRequestFromConsent(aisConsent);
        verify(xs2aAisConsentService).findAndTerminateOldConsents(ENCRYPTED_CONSENT_ID, request);
    }

    @Test
    void doScaMethodSelected_withPartiallyAuthorisedConsent_shouldUpdateMultilevelSca() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        AisConsent aisConsent = new AisConsent();
        aisConsent.setConsentTppInformation(new ConsentTppInformation());

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        SpiScaConfirmation spiScaConfirmation = new SpiScaConfirmation();
        when(xs2aAisConsentMapper.mapToSpiScaConfirmation(updateAuthorisationRequest, PSU_ID_DATA))
            .thenReturn(spiScaConfirmation);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiVerifyScaAuthorisationResponse spiVerifyScaAuthorisationResponse = new SpiVerifyScaAuthorisationResponse(ConsentStatus.PARTIALLY_AUTHORISED);
        when(aisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, spiScaConfirmation, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                            .payload(spiVerifyScaAuthorisationResponse)
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.SCAMETHODSELECTED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaMethodSelected(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.FINALISED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aAisConsentService).updateMultilevelScaRequired(ENCRYPTED_CONSENT_ID, true);
        verify(xs2aAisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.PARTIALLY_AUTHORISED);
        var request = getRequestFromConsent(aisConsent);
        verify(xs2aAisConsentService).findAndTerminateOldConsents(ENCRYPTED_CONSENT_ID, request);
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

    @Test
    void doScaMethodSelected_withInvalidConsentId_shouldReturnError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.SCAMETHODSELECTED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaMethodSelected(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void doScaMethodSelected_withRequestScaMethodsError_shouldReturnSpiError() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);

        SpiScaConfirmation spiScaConfirmation = new SpiScaConfirmation();
        when(xs2aAisConsentMapper.mapToSpiScaConfirmation(updateAuthorisationRequest, PSU_ID_DATA))
            .thenReturn(spiScaConfirmation);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiVerifyScaAuthorisationResponse> errorResponse = SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                                                                           .error(new TppMessage(spiErrorCode))
                                                                           .build();

        when(aisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, spiScaConfirmation, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.SCAMETHODSELECTED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaMethodSelected(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        ErrorHolder errorHolder = processorResponse.getErrorHolder();
        assertEquals(AIS_403, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(spiErrorCode)), errorHolder.getTppMessageInformationList());

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
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = SpiResponse.<SpiStartAuthorisationResponse>builder()
                                                                     .payload(new SpiStartAuthorisationResponse(TEST_SCA_APPROACH, TEST_SCA_STATUS, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES))
                                                                     .build();
        when(aisConsentSpi.startAuthorisation(SPI_CONTEXT_DATA, TEST_SCA_APPROACH, TEST_SCA_STATUS, TEST_AUTHORISATION_ID, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);

        // When
        AuthorisationProcessorResponse actual = aisAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);
        CreateConsentAuthorisationProcessorResponse expected = buildCreateConsentAuthorisationProcessorResponse();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void doScaStarted_spiError() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = SpiResponse.<SpiStartAuthorisationResponse>builder()
                                                                     .error(new TppMessage(MessageErrorCode.FORMAT_ERROR_ABSENT_HEADER))
                                                                     .build();
        when(aisConsentSpi.startAuthorisation(SPI_CONTEXT_DATA, TEST_SCA_APPROACH, TEST_SCA_STATUS, TEST_AUTHORISATION_ID, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);
        ErrorHolder errorHolder = ErrorHolder.builder(TEST_ERROR_TYPE_400).build();
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(errorHolder);

        // When
        AuthorisationProcessorResponse actual = aisAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);
        CreateConsentAuthorisationProcessorResponse expected = buildCreateConsentAuthorisationProcessorResponseWithError(errorHolder);

        // Then
        assertThat(actual.hasError()).isTrue();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void doScaStarted_cmsError() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());
        ErrorHolder errorHolder = ErrorHolder.builder(TEST_ERROR_TYPE_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                      .build();

        // When
        AuthorisationProcessorResponse actual = aisAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);

        // Then
        assertThat(actual.hasError()).isTrue();
        ErrorHolder errorHolderActual = actual.getErrorHolder();
        assertThat(errorHolderActual.getErrorType()).isEqualTo(AIS_400);
        assertThat(errorHolderActual.getTppMessageInformationList()).isEqualTo(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)));
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FAILED);
        assertThat(actual.getConsentId()).isEqualTo(ENCRYPTED_CONSENT_ID);

        verify(aisConsentSpi, never()).startAuthorisation(any(), any(), any(), any(), any(), any());
    }

    @Test
    void doScaFinalised_shouldReturnFinalisedResponse() {
        // Given
        ConsentAuthorisationsParameters updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.FINALISED, updateAuthorisationRequest, null);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaFinalised(processorRequest);

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
        assertThrows(UnsupportedOperationException.class, () -> aisAuthorisationProcessorService.doScaFailed(processorRequest));
    }

    @Test
    void doScaExempted_shouldThrowException() {
        // Given
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.EXEMPTED, null, null);

        // When
        assertThrows(UnsupportedOperationException.class, () -> aisAuthorisationProcessorService.doScaExempted(processorRequest));
    }

    private ConsentAuthorisationsParameters buildUpdateConsentPsuDataReq() {
        ConsentAuthorisationsParameters updateAuthorisationRequest = new ConsentAuthorisationsParameters();
        updateAuthorisationRequest.setConsentId(ENCRYPTED_CONSENT_ID);
        updateAuthorisationRequest.setAuthorizationId(AUTHORISATION_ID);
        updateAuthorisationRequest.setPsuData(PSU_ID_DATA);

        return updateAuthorisationRequest;
    }

    private AuthorisationProcessorRequest buildAuthorisationProcessorRequest(ScaStatus scaStatus, CommonAuthorisationParameters updateAuthorisationRequest, Authorisation authorisation) {
        return new AuthorisationProcessorRequest(ServiceType.AIS, ScaApproach.EMBEDDED, scaStatus, updateAuthorisationRequest, authorisation);
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
        AuthenticationObject method = new AuthenticationObject();
        spiAuthorizationCodeResult.setSelectedScaMethod(method);
        spiAuthorizationCodeResult.setChallengeData(new ChallengeData());
        return spiAuthorizationCodeResult;
    }

    private static Set<TppMessageInformation> buildTppMessageInformationSet() {
        return Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
    }

    private CreateConsentAuthorisationProcessorResponse buildCreateConsentAuthorisationProcessorResponse() {
        return new CreateConsentAuthorisationProcessorResponse(TEST_SCA_STATUS, TEST_SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, ENCRYPTED_CONSENT_ID, TEST_PSU_DATA);
    }

    private CreateConsentAuthorisationProcessorResponse buildCreateConsentAuthorisationProcessorResponseWithError(ErrorHolder errorHolder) {
        return new CreateConsentAuthorisationProcessorResponse(errorHolder, TEST_SCA_APPROACH, ENCRYPTED_CONSENT_ID, TEST_PSU_DATA);
    }
}
