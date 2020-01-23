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


package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.ScaMethod;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.ScaMethodMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.xs2a.reader.JsonReader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisAuthorisationServiceInternalTest {
    private static final long CONSENT_ID = 1;
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String PSU_ID = "psu-id-1";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final PsuData PSU_DATA = new PsuData(PSU_ID, null, null, null, null);
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final String AUTHENTICATION_METHOD_ID = "Method id";
    private static final String WRONG_AUTHENTICATION_METHOD_ID = "Wrong method id";
    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    private AisConsent aisConsent;
    private AisConsentAuthorization aisConsentAuthorisation;
    private List<AisConsentAuthorization> aisConsentAuthorisationList = new ArrayList<>();
    private static final JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private AisAuthorisationServiceInternal aisAuthorisationServiceInternal;
    @Mock
    private AisConsentJpaRepository aisConsentJpaRepository;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AisConsentAuthorisationRepository aisConsentAuthorisationRepository;
    @Mock
    private PsuData psuDataMocked;
    @Mock
    private CmsPsuService cmsPsuService;

    @Mock
    private ScaMethodMapper scaMethodMapper;

    @BeforeEach
    void setUp() {
        aisConsentAuthorisation = buildAisConsentAuthorisation(AUTHORISATION_ID, ScaStatus.RECEIVED);
        aisConsentAuthorisationList.add(aisConsentAuthorisation);
        aisConsent = buildConsent(EXTERNAL_CONSENT_ID);
    }

    @Test
    void getAuthorisationScaStatus_success() {
        List<AisConsentAuthorization> authorisations = Collections.singletonList(buildAisConsentAuthorisation(AUTHORISATION_ID, SCA_STATUS));
        AisConsent consent = buildConsentWithAuthorisations(EXTERNAL_CONSENT_ID, authorisations);
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));

        // When
        CmsResponse<ScaStatus> actual = aisAuthorisationServiceInternal.getAuthorisationScaStatus(EXTERNAL_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(SCA_STATUS, actual.getPayload());
    }

    @Test
    void getAuthorisationScaStatus_failure_wrongConsentId() {
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<ScaStatus> actual = aisAuthorisationServiceInternal.getAuthorisationScaStatus(EXTERNAL_CONSENT_ID_NOT_EXIST, AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    @Test
    void getAuthorisationScaStatus_failure_wrongAuthorisationId() {
        List<AisConsentAuthorization> authorisations = Collections.singletonList(buildAisConsentAuthorisation(WRONG_AUTHORISATION_ID, SCA_STATUS));
        AisConsent consent = buildConsentWithAuthorisations(EXTERNAL_CONSENT_ID, authorisations);
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));

        // When
        CmsResponse<ScaStatus> actual = aisAuthorisationServiceInternal.getAuthorisationScaStatus(EXTERNAL_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    @Test
    void getAuthorisationScaApproach_success() {
        //Given
        AisConsentAuthorization aisConsentAuthorization = new AisConsentAuthorization();
        aisConsentAuthorization.setScaApproach(SCA_APPROACH);
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(aisConsentAuthorization));

        // When
        CmsResponse<AuthorisationScaApproachResponse> actual = aisAuthorisationServiceInternal.getAuthorisationScaApproach(AUTHORISATION_ID);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(SCA_APPROACH, actual.getPayload().getScaApproach());
    }

    @Test
    void getAuthorisationScaApproach_failure_wrongAuthorisationId() {
        //Given
        when(aisConsentAuthorisationRepository.findByExternalId(WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<AuthorisationScaApproachResponse> actual = aisAuthorisationServiceInternal.getAuthorisationScaApproach(WRONG_AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        ;
    }

    @Test
    void createAuthorizationWithClosingPreviousAuthorisations_success() {
        //Given
        ArgumentCaptor<AisConsentAuthorization> argument = ArgumentCaptor.forClass(AisConsentAuthorization.class);
        //noinspection unchecked
        ArgumentCaptor<List<AisConsentAuthorization>> failedAuthorisationsArgument = ArgumentCaptor.forClass(List.class);

        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());
        when(aisConsentAuthorisationRepository.save(any(AisConsentAuthorization.class))).thenReturn(aisConsentAuthorisation);
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.of(PSU_DATA));
        when(cmsPsuService.enrichPsuData(any(), any())).thenReturn(Collections.singletonList(PSU_DATA));

        AisConsentAuthorizationRequest aisConsentAuthorisationRequest = new AisConsentAuthorizationRequest();
        aisConsentAuthorisationRequest.setPsuData(PSU_ID_DATA);
        aisConsentAuthorisationRequest.setScaStatus(aisConsentAuthorisation.getScaStatus());
        aisConsentAuthorisationRequest.setTppRedirectURIs(TPP_REDIRECT_URIs);

        // When
        CmsResponse<CreateAisConsentAuthorizationResponse> actual = aisAuthorisationServiceInternal.createAuthorizationWithResponse(EXTERNAL_CONSENT_ID, aisConsentAuthorisationRequest);

        // Then
        assertTrue(actual.isSuccessful());

        verify(aisConsentAuthorisationRepository).save(argument.capture());
        AisConsentAuthorization aisConsentAuthorization = argument.getValue();
        assertSame(ScaStatus.PSUIDENTIFIED, aisConsentAuthorization.getScaStatus());

        verify(aisConsentAuthorisationRepository).saveAll(failedAuthorisationsArgument.capture());
        List<AisConsentAuthorization> failedAuthorisations = failedAuthorisationsArgument.getValue();
        Set<ScaStatus> scaStatuses = failedAuthorisations.stream()
                                         .map(AisConsentAuthorization::getScaStatus)
                                         .collect(Collectors.toSet());
        assertEquals(scaStatuses.size(), 1);
        assertTrue(scaStatuses.contains(ScaStatus.FAILED));
        assertEquals(TPP_REDIRECT_URIs.getUri(), aisConsentAuthorization.getTppOkRedirectUri());
        assertEquals(TPP_REDIRECT_URIs.getNokUri(), aisConsentAuthorization.getTppNokRedirectUri());
        assertEquals(aisConsent.getInternalRequestId(), actual.getPayload().getInternalRequestId());
    }

    @Test
    void createAuthorizationWithClosingPreviousAuthorisationsTppRedirectLinksFromAuthorisationTemplate_success() {
        //Given
        AuthorisationTemplateEntity authorisationTemplateEntity = buildAuthorisationTemplateEntity();
        AisConsent aisConsent = buildConsent(EXTERNAL_CONSENT_ID, authorisationTemplateEntity);
        ArgumentCaptor<AisConsentAuthorization> argument = ArgumentCaptor.forClass(AisConsentAuthorization.class);
        //noinspection unchecked
        ArgumentCaptor<List<AisConsentAuthorization>> failedAuthorisationsArgument = ArgumentCaptor.forClass(List.class);

        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());
        when(aisConsentAuthorisationRepository.save(any(AisConsentAuthorization.class))).thenReturn(aisConsentAuthorisation);
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.of(PSU_DATA));
        when(cmsPsuService.enrichPsuData(any(), any())).thenReturn(Collections.singletonList(PSU_DATA));

        AisConsentAuthorizationRequest aisConsentAuthorisationRequest = new AisConsentAuthorizationRequest();
        aisConsentAuthorisationRequest.setPsuData(PSU_ID_DATA);
        aisConsentAuthorisationRequest.setScaStatus(aisConsentAuthorisation.getScaStatus());
        aisConsentAuthorisationRequest.setTppRedirectURIs(new TppRedirectUri("", ""));

        // When
        CmsResponse<CreateAisConsentAuthorizationResponse> actual = aisAuthorisationServiceInternal.createAuthorizationWithResponse(EXTERNAL_CONSENT_ID, aisConsentAuthorisationRequest);

        // Then
        assertTrue(actual.isSuccessful());
        verify(aisConsentAuthorisationRepository).save(argument.capture());
        AisConsentAuthorization aisConsentAuthorization = argument.getValue();
        assertSame(ScaStatus.PSUIDENTIFIED, aisConsentAuthorization.getScaStatus());

        verify(aisConsentAuthorisationRepository).saveAll(failedAuthorisationsArgument.capture());
        List<AisConsentAuthorization> failedAuthorisations = failedAuthorisationsArgument.getValue();
        Set<ScaStatus> scaStatuses = failedAuthorisations.stream()
                                         .map(AisConsentAuthorization::getScaStatus)
                                         .collect(Collectors.toSet());
        assertEquals(1, scaStatuses.size());
        assertTrue(scaStatuses.contains(ScaStatus.FAILED));
        assertEquals(authorisationTemplateEntity.getRedirectUri(), aisConsentAuthorization.getTppOkRedirectUri());
        assertEquals(authorisationTemplateEntity.getNokRedirectUri(), aisConsentAuthorization.getTppNokRedirectUri());
    }

    @Test
    void isAuthenticationMethodDecoupled_success_decoupled() {
        // Given
        List<ScaMethod> methods = Collections.singletonList(buildScaMethod(true));
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorisationWithMethods(methods)));

        // When
        CmsResponse<Boolean> actualResult = aisAuthorisationServiceInternal.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResult.isSuccessful());

        assertTrue(actualResult.getPayload());
    }

    @Test
    void isAuthenticationMethodDecoupled_success_notDecoupled() {
        // Given
        List<ScaMethod> methods = Collections.singletonList(buildScaMethod(false));
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorisationWithMethods(methods)));

        // When
        CmsResponse<Boolean> actualResult = aisAuthorisationServiceInternal.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResult.isSuccessful());

        assertFalse(actualResult.getPayload());
    }

    @Test
    void isAuthenticationMethodDecoupled_failure_wrongMethodId() {
        // Given
        List<ScaMethod> methods = Collections.singletonList(buildScaMethod(true));
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorisationWithMethods(methods)));

        // When
        CmsResponse<Boolean> actualResult = aisAuthorisationServiceInternal.isAuthenticationMethodDecoupled(AUTHORISATION_ID, WRONG_AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResult.isSuccessful());

        assertFalse(actualResult.getPayload());
    }

    @Test
    void isAuthenticationMethodDecoupled_failure_wrongAuthorisationId() {
        // Given
        when(aisConsentAuthorisationRepository.findByExternalId(WRONG_AUTHORISATION_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actualResult = aisAuthorisationServiceInternal.isAuthenticationMethodDecoupled(WRONG_AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResult.isSuccessful());

        assertFalse(actualResult.getPayload());
    }

    @Test
    void isAuthenticationMethodDecoupled_failure_noMethodsPresent() {
        // Given
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorisationWithMethods(Collections.emptyList())));

        // When
        CmsResponse<Boolean> actualResult = aisAuthorisationServiceInternal.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResult.isSuccessful());

        assertFalse(actualResult.getPayload());
    }

    @Test
    void saveAuthenticationMethods_success() {
        // Given
        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(buildCmsScaMethod(true));
        List<ScaMethod> scaMethods = Collections.singletonList(buildScaMethod(true));
        when(scaMethodMapper.mapToScaMethods(cmsScaMethods)).thenReturn(scaMethods);

        ArgumentCaptor<AisConsentAuthorization> authorisationArgumentCaptor = ArgumentCaptor.forClass(AisConsentAuthorization.class);
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorisation(AUTHORISATION_ID, SCA_STATUS)));

        // When
        CmsResponse<Boolean> actualResult = aisAuthorisationServiceInternal.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertTrue(actualResult.isSuccessful());

        assertTrue(actualResult.getPayload());
        verify(aisConsentAuthorisationRepository, times(1)).save(authorisationArgumentCaptor.capture());
        assertEquals(authorisationArgumentCaptor.getValue().getAvailableScaMethods(), scaMethods);
    }

    @Test
    void saveAuthenticationMethods_failure_wrongAuthorisationId() {
        // Given
        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(buildCmsScaMethod(true));

        when(aisConsentAuthorisationRepository.findByExternalId(WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actualResult = aisAuthorisationServiceInternal.saveAuthenticationMethods(WRONG_AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertTrue(actualResult.isSuccessful());

        assertFalse(actualResult.getPayload());
        verify(aisConsentAuthorisationRepository, never()).save(any(AisConsentAuthorization.class));
    }

    private AisConsentAuthorization buildAisConsentAuthorisation(String externalId, ScaStatus scaStatus) {
        AisConsentAuthorization aisConsentAuthorization = new AisConsentAuthorization();
        aisConsentAuthorization.setConsent(aisConsent);
        aisConsentAuthorization.setExternalId(externalId);
        aisConsentAuthorization.setPsuData(PSU_DATA);
        aisConsentAuthorization.setScaStatus(scaStatus);
        return aisConsentAuthorization;
    }

    @NotNull
    private AspspSettings getAspspSettings() {
        return jsonReader.getObjectFromFile("json/AspspSetting.json", AspspSettings.class);
    }

    private AisConsent buildConsent(String externalId) {
        return buildConsent(externalId, Collections.singletonList(psuDataMocked));
    }

    private AisConsent buildConsent(String externalId, AuthorisationTemplateEntity authorisationTemplateEntity) {
        AisConsent aisConsent = buildConsent(externalId, Collections.singletonList(psuDataMocked));
        aisConsent.setAuthorisationTemplate(authorisationTemplateEntity);
        return aisConsent;
    }

    private AisConsent buildConsent(String externalId, List<PsuData> psuDataList) {
        return buildConsent(externalId, psuDataList, LocalDate.now());
    }

    private AisConsent buildConsent(String externalId, List<PsuData> psuDataList, LocalDate validUntil) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        aisConsent.setExternalId(externalId);
        aisConsent.setValidUntil(validUntil);
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        aisConsent.setAuthorizations(aisConsentAuthorisationList);
        aisConsent.setPsuDataList(psuDataList);
        aisConsent.setAuthorisationTemplate(new AuthorisationTemplateEntity());
        aisConsent.setInternalRequestId(INTERNAL_REQUEST_ID);
        return aisConsent;
    }

    private AisConsent buildConsentWithAuthorisations(String externalId, List<AisConsentAuthorization> authorisations) {
        AisConsent aisConsent = buildConsent(externalId);
        aisConsent.setAuthorizations(authorisations);
        return aisConsent;
    }

    private AisConsentAuthorization buildAisConsentAuthorisationWithMethods(List<ScaMethod> scaMethods) {
        AisConsentAuthorization authorisation = buildAisConsentAuthorisation(AUTHORISATION_ID, SCA_STATUS);
        authorisation.setAvailableScaMethods(scaMethods);
        return authorisation;
    }

    private ScaMethod buildScaMethod(boolean decoupled) {
        ScaMethod scaMethod = new ScaMethod();
        scaMethod.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        scaMethod.setDecoupled(decoupled);
        return scaMethod;
    }

    private CmsScaMethod buildCmsScaMethod(boolean decoupled) {
        return new CmsScaMethod(AUTHENTICATION_METHOD_ID, decoupled);
    }

    private AuthorisationTemplateEntity buildAuthorisationTemplateEntity() {
        AuthorisationTemplateEntity authorisationTemplateEntity = new AuthorisationTemplateEntity();
        authorisationTemplateEntity.setRedirectUri("template_redirect_uri");
        authorisationTemplateEntity.setNokRedirectUri("template_nok_redirect_uri");
        authorisationTemplateEntity.setCancelRedirectUri("template_cancel_redirect_uri");
        authorisationTemplateEntity.setCancelNokRedirectUri("template_cancel_nok_redirect_uri");
        return authorisationTemplateEntity;
    }
}

