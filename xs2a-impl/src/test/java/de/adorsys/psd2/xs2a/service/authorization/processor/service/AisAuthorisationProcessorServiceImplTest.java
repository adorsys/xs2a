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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.CommonDecoupledAisService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisAuthorisationProcessorServiceImplTest {
    private static final String AUTHORISATION_ID = "authorisation-id";
    private static final String ENCRYPTED_CONSENT_ID = "encrypted-consent-id";
    private static final String INTERNAL_CONSENT_ID = "consent-id";
    private static final String PSU_ID = "some-psu";
    private static final String DECOUPLED_PSU_MESSAGE = "some message";
    private static final String PSU_PASSWORD = "some password";
    private static final String AUTHENTICATION_METHOD_ID = "authentication method id";
    private static final AuthenticationObject DECOUPLED_SCA_METHOD = new AuthenticationObject();

    @Mock
    private AisAuthorizationService embeddedAisAuthorisationService;
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

    private AisAuthorisationProcessorServiceImpl aisAuthorisationProcessorService;

    @BeforeEach
    void setUp() {
        AisAuthorizationService decoupledAisAuthorisationService = Mockito.mock(AisAuthorizationService.class);
        List<AisAuthorizationService> services = Arrays.asList(decoupledAisAuthorisationService, embeddedAisAuthorisationService);

        aisAuthorisationProcessorService = new AisAuthorisationProcessorServiceImpl(services, xs2aAisConsentService, aisConsentSpi, xs2aAisConsentMapper, spiContextDataProvider, spiAspspConsentDataProviderFactory, spiErrorMapper, commonDecoupledAisService, aisScaAuthorisationService, xs2aToSpiPsuDataMapper);
    }

    @Test
    void updateAuthorisation_shouldUpdateWithCorrectService() {
        // Given
        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateConsentPsuDataReq();
        Object authorisation = new Object();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();

        when(embeddedAisAuthorisationService.getScaApproachServiceType()).thenReturn(ScaApproach.EMBEDDED);

        // When
        aisAuthorisationProcessorService.updateAuthorisation(processorRequest, processorResponse);

        // Then
        verify(embeddedAisAuthorisationService).updateConsentPsuData(updateAuthorisationRequest, processorResponse);
    }

    @Test
    void updateAuthorisation_withNoAuthorisationServiceForApproach_shouldThrowException() {
        // Given
        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateConsentPsuDataReq();
        Object authorisation = new Object();
        AuthorisationProcessorRequest processorRequest = new AuthorisationProcessorRequest(ServiceType.AIS, PaymentAuthorisationType.CREATED, ScaApproach.OAUTH, ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();

        // When
        assertThrows(IllegalArgumentException.class, () -> aisAuthorisationProcessorService.updateAuthorisation(processorRequest, processorResponse));

        // Then
        verify(embeddedAisAuthorisationService, never()).updateConsentPsuData(any(UpdateAuthorisationRequest.class), any(AuthorisationProcessorResponse.class));
    }

    @Test
    void doScaReceived_withPsuIdentificationRequest() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setUpdatePsuIdentification(true);
        updateAuthorisationRequest.setPsuData(new PsuIdData(PSU_ID, null, null, null, null));

        Object authorisation = new Object();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setUpdatePsuIdentification(true);

        Object authorisation = new Object();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        List<AuthenticationObject> availableScaMethods = Collections.singletonList(authenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        SpiAuthorizationCodeResult authorizationCodeResult = new SpiAuthorizationCodeResult();
        AuthenticationObject spiChosenScaMethod = new AuthenticationObject();
        authorizationCodeResult.setSelectedScaMethod(spiChosenScaMethod);
        ChallengeData challengeData = new ChallengeData();
        authorizationCodeResult.setChallengeData(challengeData);

        when(aisConsentSpi.requestAuthorisationCode(spiContextData, AUTHENTICATION_METHOD_ID, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(authorizationCodeResult)
                            .build());

        AuthenticationObject chosenScaMethod = new AuthenticationObject();

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(chosenScaMethod, processorResponse.getChosenScaMethod());
        assertEquals(challengeData, processorResponse.getChallengeData());

        verify(xs2aAisConsentService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withOneDecoupledScaMethod() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        authenticationObject.setDecoupled(true);
        List<AuthenticationObject> availableScaMethods = Collections.singletonList(authenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        UpdateConsentPsuDataResponse decoupledResponse = buildDecoupledUpdateConsentPsuDataResponse();
        when(commonDecoupledAisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, psuIdData))
            .thenReturn(decoupledResponse);

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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

        verify(xs2aAisConsentService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(commonDecoupledAisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, psuIdData);
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withMultipleScaMethods() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject firstAuthenticationObject = new AuthenticationObject();
        firstAuthenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        AuthenticationObject secondAuthenticationObject = new AuthenticationObject();

        List<AuthenticationObject> availableScaMethods = Arrays.asList(firstAuthenticationObject, secondAuthenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.PSUAUTHENTICATED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(availableScaMethods, processorResponse.getAvailableScaMethods());

        verify(xs2aAisConsentService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_oneFactorAuthorisation() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        when(aisScaAuthorisationService.isOneFactorAuthorisation(accountConsent)).thenReturn(true);

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        when(commonDecoupledAisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, psuIdData))
            .thenReturn(buildDecoupledUpdateConsentPsuDataResponse());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        authorisation.setChosenScaApproach(ScaApproach.DECOUPLED);
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(commonDecoupledAisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, psuIdData);
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withZeroScaMethods_shouldReturnError() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(Collections.emptyList());
        when(aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_METHOD_UNKNOWN))
                                      .build();
        UpdateConsentPsuDataResponse failedUpdateResponse = new UpdateConsentPsuDataResponse(errorHolder, ENCRYPTED_CONSENT_ID, AUTHORISATION_ID);
        UpdateConsentPsuDataReq failedUpdateConsentPsuDataReq = new UpdateConsentPsuDataReq();
        when(xs2aAisConsentMapper.mapToSpiUpdateConsentPsuDataReq(updateAuthorisationRequest, failedUpdateResponse))
            .thenReturn(failedUpdateConsentPsuDataReq);

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.RECEIVED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaReceived(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aAisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.REJECTED);
        verify(xs2aAisConsentService).updateConsentAuthorization(failedUpdateConsentPsuDataReq);
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaReceived_withPsuAuthorisationRequest_withInvalidConsentId_shouldReturnError() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());
        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();

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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiPsuAuthorisationResponse> errorResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                     .error(new TppMessage(spiErrorCode))
                                                                     .build();
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        String psuPassword = PSU_PASSWORD;
        updateAuthorisationRequest.setPassword(psuPassword);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, psuPassword, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.FAILURE))
                            .build());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        String psuPassword = PSU_PASSWORD;
        updateAuthorisationRequest.setPassword(psuPassword);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, psuPassword, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiAvailableScaMethodsResponse> errorResponse = SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                        .error(new TppMessage(spiErrorCode))
                                                                        .build();

        when(aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setUpdatePsuIdentification(true);
        updateAuthorisationRequest.setPsuData(new PsuIdData(PSU_ID, null, null, null, null));

        Object authorisation = new Object();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        updateAuthorisationRequest.setUpdatePsuIdentification(true);

        Object authorisation = new Object();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        List<AuthenticationObject> availableScaMethods = Collections.singletonList(authenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        AuthenticationObject spiChosenScaMethod = new AuthenticationObject();
        spiAuthorizationCodeResult.setSelectedScaMethod(spiChosenScaMethod);
        ChallengeData challengeData = new ChallengeData();
        spiAuthorizationCodeResult.setChallengeData(challengeData);

        when(aisConsentSpi.requestAuthorisationCode(spiContextData, AUTHENTICATION_METHOD_ID, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(spiAuthorizationCodeResult)
                            .build());

        AuthenticationObject chosenScaMethod = new AuthenticationObject();

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(chosenScaMethod, processorResponse.getChosenScaMethod());
        assertEquals(challengeData, processorResponse.getChallengeData());

        verify(xs2aAisConsentService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withOneDecoupledScaMethod() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        authenticationObject.setDecoupled(true);
        List<AuthenticationObject> availableScaMethods = Collections.singletonList(authenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        UpdateConsentPsuDataResponse decoupledResponse = buildDecoupledUpdateConsentPsuDataResponse();
        when(commonDecoupledAisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, psuIdData))
            .thenReturn(decoupledResponse);

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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

        verify(xs2aAisConsentService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(commonDecoupledAisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, psuIdData);
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withMultipleScaMethods() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        AuthenticationObject firstAuthenticationObject = new AuthenticationObject();
        firstAuthenticationObject.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        AuthenticationObject secondAuthenticationObject = new AuthenticationObject();

        List<AuthenticationObject> availableScaMethods = Arrays.asList(firstAuthenticationObject, secondAuthenticationObject);
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(availableScaMethods);
        when(aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.PSUAUTHENTICATED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(availableScaMethods, processorResponse.getAvailableScaMethods());

        verify(xs2aAisConsentService).saveAuthenticationMethods(AUTHORISATION_ID, availableScaMethods);
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_oneFactorAuthorisation() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        when(aisScaAuthorisationService.isOneFactorAuthorisation(accountConsent)).thenReturn(true);

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        when(commonDecoupledAisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, psuIdData))
            .thenReturn(buildDecoupledUpdateConsentPsuDataResponse());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        authorisation.setChosenScaApproach(ScaApproach.DECOUPLED);
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(commonDecoupledAisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, psuIdData);
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withZeroScaMethods_shouldReturnError() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(Collections.emptyList());
        when(aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_METHOD_UNKNOWN))
                                      .build();
        UpdateConsentPsuDataResponse failedUpdateResponse = new UpdateConsentPsuDataResponse(errorHolder, ENCRYPTED_CONSENT_ID, AUTHORISATION_ID);
        UpdateConsentPsuDataReq failedUpdateConsentPsuDataReq = new UpdateConsentPsuDataReq();
        when(xs2aAisConsentMapper.mapToSpiUpdateConsentPsuDataReq(updateAuthorisationRequest, failedUpdateResponse))
            .thenReturn(failedUpdateConsentPsuDataReq);

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuIdentified(processorRequest);

        // Then
        assertTrue(processorResponse.hasError());

        assertEquals(ScaStatus.FAILED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aAisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.REJECTED);
        verify(xs2aAisConsentService).updateConsentAuthorization(failedUpdateConsentPsuDataReq);
        verify(aisConsentSpi, never()).requestAuthorisationCode(any(), any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_withPsuAuthorisationRequest_withInvalidConsentId_shouldReturnError() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());
        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();

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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setPassword(PSU_PASSWORD);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiPsuAuthorisationResponse> errorResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                     .error(new TppMessage(spiErrorCode))
                                                                     .build();
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, PSU_PASSWORD, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        String psuPassword = PSU_PASSWORD;
        updateAuthorisationRequest.setPassword(psuPassword);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, psuPassword, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.FAILURE))
                            .build());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
    void doScaPsuIdentified_withPsuAuthorisationRequest_withRequestScaMethodsError_shouldReturnSpiError() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        String psuPassword = PSU_PASSWORD;
        updateAuthorisationRequest.setPassword(psuPassword);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData)).thenReturn(spiPsuData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiPsuAuthorisationResponse spiPsuAuthorisationResponse = new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS);
        when(aisConsentSpi.authorisePsu(spiContextData, spiPsuData, psuPassword, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(spiPsuAuthorisationResponse)
                            .build());

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiAvailableScaMethodsResponse> errorResponse = SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                        .error(new TppMessage(spiErrorCode))
                                                                        .build();

        when(aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        AuthenticationObject spiChosenScaMethod = new AuthenticationObject();
        spiAuthorizationCodeResult.setSelectedScaMethod(spiChosenScaMethod);
        ChallengeData challengeData = new ChallengeData();
        spiAuthorizationCodeResult.setChallengeData(challengeData);

        when(aisConsentSpi.requestAuthorisationCode(spiContextData, AUTHENTICATION_METHOD_ID, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(spiAuthorizationCodeResult)
                            .build());

        AuthenticationObject chosenScaMethod = new AuthenticationObject();

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.PSUIDENTIFIED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.SCAMETHODSELECTED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());
        assertEquals(chosenScaMethod, processorResponse.getChosenScaMethod());
        assertEquals(challengeData, processorResponse.getChallengeData());
    }

    @Test
    void doScaPsuAuthenticated_decoupledApproach() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);

        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        when(xs2aAisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(true);

        UpdateConsentPsuDataResponse decoupledResponse = buildDecoupledUpdateConsentPsuDataResponse();
        when(commonDecoupledAisService.proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, psuIdData))
            .thenReturn(decoupledResponse);

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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

        verify(xs2aAisConsentService).updateScaApproach(AUTHORISATION_ID, ScaApproach.DECOUPLED);
        verify(commonDecoupledAisService).proceedDecoupledApproach(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID, spiAccountConsent, AUTHENTICATION_METHOD_ID, psuIdData);
    }

    @Test
    void doScaPsuAuthenticated_withInvalidConsentId_shouldReturnError() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

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

        when(aisConsentSpi.requestAuthorisationCode(spiContextData, AUTHENTICATION_METHOD_ID, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        SpiScaConfirmation spiScaConfirmation = new SpiScaConfirmation();
        when(xs2aAisConsentMapper.mapToSpiScaConfirmation(updateAuthorisationRequest, psuIdData))
            .thenReturn(spiScaConfirmation);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiVerifyScaAuthorisationResponse spiVerifyScaAuthorisationResponse = new SpiVerifyScaAuthorisationResponse(ConsentStatus.VALID);
        when(aisConsentSpi.verifyScaAuthorisation(spiContextData, spiScaConfirmation, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                            .payload(spiVerifyScaAuthorisationResponse)
                            .build());


        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.SCAMETHODSELECTED, updateAuthorisationRequest, authorisation);

        // When
        AuthorisationProcessorResponse processorResponse = aisAuthorisationProcessorService.doScaMethodSelected(processorRequest);

        // Then
        assertFalse(processorResponse.hasError());

        assertEquals(ScaStatus.FINALISED, processorResponse.getScaStatus());
        assertEquals(ENCRYPTED_CONSENT_ID, processorResponse.getConsentId());
        assertEquals(AUTHORISATION_ID, processorResponse.getAuthorisationId());

        verify(xs2aAisConsentService).updateConsentStatus(ENCRYPTED_CONSENT_ID, ConsentStatus.VALID);
        verify(xs2aAisConsentService).findAndTerminateOldConsentsByNewConsentId(ENCRYPTED_CONSENT_ID);
    }

    @Test
    void doScaMethodSelected_withPartiallyAuthorisedConsent_shouldUpdateMultilevelSca() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        SpiScaConfirmation spiScaConfirmation = new SpiScaConfirmation();
        when(xs2aAisConsentMapper.mapToSpiScaConfirmation(updateAuthorisationRequest, psuIdData))
            .thenReturn(spiScaConfirmation);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        SpiVerifyScaAuthorisationResponse spiVerifyScaAuthorisationResponse = new SpiVerifyScaAuthorisationResponse(ConsentStatus.PARTIALLY_AUTHORISED);
        when(aisConsentSpi.verifyScaAuthorisation(spiContextData, spiScaConfirmation, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                            .payload(spiVerifyScaAuthorisationResponse)
                            .build());


        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        verify(xs2aAisConsentService).findAndTerminateOldConsentsByNewConsentId(ENCRYPTED_CONSENT_ID);
    }

    @Test
    void doScaMethodSelected_withInvalidConsentId_shouldReturnError() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);
        updateAuthorisationRequest.setPsuData(psuIdData);
        updateAuthorisationRequest.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);

        AccountConsent accountConsent = buildAccountConsent();
        when(xs2aAisConsentService.getAccountConsentById(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(accountConsent));

        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();
        when(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).thenReturn(spiAccountConsent);

        SpiPsuData spiPsuData = SpiPsuData.builder().psuId(PSU_ID).build();
        SpiContextData spiContextData = new SpiContextData(spiPsuData, null, null, null, null);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(spiContextData);

        SpiScaConfirmation spiScaConfirmation = new SpiScaConfirmation();
        when(xs2aAisConsentMapper.mapToSpiScaConfirmation(updateAuthorisationRequest, psuIdData))
            .thenReturn(spiScaConfirmation);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = mock(SpiAspspConsentDataProvider.class);
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPTED_CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);

        MessageErrorCode spiErrorCode = MessageErrorCode.SERVICE_BLOCKED;
        SpiResponse<SpiVerifyScaAuthorisationResponse> errorResponse = SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                                                                           .error(new TppMessage(spiErrorCode))
                                                                           .build();

        when(aisConsentSpi.verifyScaAuthorisation(spiContextData, spiScaConfirmation, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(errorResponse);

        when(spiErrorMapper.mapToErrorHolder(errorResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_403)
                            .tppMessages(TppMessageInformation.of(spiErrorCode))
                            .build());

        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
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

    private AccountConsent buildAccountConsent() {
        return new AccountConsent(INTERNAL_CONSENT_ID, null, null, false, null, null, 1, null, null, false, false, null, null, null, false, null, null, null, null);
    }

    @Test
    void doScaStarted_shouldThrowException() {
        // Given
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest(ScaStatus.STARTED, null, null);

        // When
        assertThrows(UnsupportedOperationException.class, () -> aisAuthorisationProcessorService.doScaStarted(processorRequest));
    }

    @Test
    void doScaFinalised_shouldReturnFinalisedResponse() {
        // Given
        UpdateConsentPsuDataReq updateAuthorisationRequest = buildUpdateConsentPsuDataReq();
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

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq() {
        UpdateConsentPsuDataReq updateAuthorisationRequest = new UpdateConsentPsuDataReq();
        updateAuthorisationRequest.setConsentId(ENCRYPTED_CONSENT_ID);
        updateAuthorisationRequest.setAuthorizationId(AUTHORISATION_ID);
        return updateAuthorisationRequest;
    }

    private AuthorisationProcessorRequest buildAuthorisationProcessorRequest(ScaStatus scaStatus, UpdateAuthorisationRequest updateAuthorisationRequest, Object authorisation) {
        return new AuthorisationProcessorRequest(ServiceType.AIS, PaymentAuthorisationType.CREATED, ScaApproach.EMBEDDED, scaStatus, updateAuthorisationRequest, authorisation);
    }

    private UpdateConsentPsuDataResponse buildDecoupledUpdateConsentPsuDataResponse() {
        UpdateConsentPsuDataResponse decoupledResponse = new UpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED, ENCRYPTED_CONSENT_ID, AUTHORISATION_ID);
        decoupledResponse.setPsuMessage(DECOUPLED_PSU_MESSAGE);
        decoupledResponse.setChosenScaMethod(DECOUPLED_SCA_METHOD);
        return decoupledResponse;
    }
}
