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


package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.*;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.authorisation.AisAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreateAisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentAuthorisationMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAuthenticationObjectToCmsScaMethodMapper;
import de.adorsys.psd2.xs2a.service.profile.FrequencyPerDateCalculationService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aAisConsentServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String WRONG_CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "00000000-0000-4b5a-8188-c45d85bfa20a";
    private static final String AUTHENTICATION_METHOD_ID = "19ff-4b5a-8188";
    private static final String TPP_ID = "Test TppId";
    private static final String REQUEST_URI = "request/uri";
    private static final String REDIRECT_URI = "request/redirect_uri";
    private static final String NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final CreateConsentReq CREATE_CONSENT_REQ = buildCreateConsentReq();
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final CmsConsent CMS_CONSENT = new CmsConsent();
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.VALID;
    private static final CreateAuthorisationRequest AIS_CONSENT_AUTHORIZATION_REQUEST = buildAisConsentAuthorizationRequest();
    private static final List<String> STRING_LIST = Collections.singletonList(AUTHORISATION_ID);
    private static final List<AuthenticationObject> AUTHENTICATION_OBJECT_LIST = Collections.singletonList(new AuthenticationObject());
    private static final List<CmsScaMethod> CMS_SCA_METHOD_LIST = Collections.singletonList(new CmsScaMethod(AUTHORISATION_ID, true));
    private static final String INTERNAL_REQUEST_ID = "1234-5678-9012-3456";

    @InjectMocks
    private Xs2aAisConsentService xs2aAisConsentService;

    @Mock
    private ConsentServiceEncrypted consentServiceEncrypted;
    @Mock
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private Xs2aAisConsentAuthorisationMapper aisConsentAuthorisationMapper;
    @Mock
    private FrequencyPerDateCalculationService frequencyPerDateCalculationService;
    @Mock
    private Xs2aAuthenticationObjectToCmsScaMethodMapper xs2AAuthenticationObjectToCmsScaMethodMapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private LoggingContextService loggingContextService;

    private JsonReader jsonReader = new JsonReader();
    private AisConsent aisConsent;

    @BeforeEach
    void init() {
        aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
    }

    @Test
    void createConsent_success() throws WrongChecksumException {
        // Given
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCmsConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1))
            .thenReturn(CMS_CONSENT);
        when(consentServiceEncrypted.createConsent(any()))
            .thenReturn(CmsResponse.<CmsCreateConsentResponse>builder()
                            .payload(new CmsCreateConsentResponse(CONSENT_ID, getCmsConsentWithNotifications()))
                            .build());
        when(aisConsentMapper.mapToAisConsent(any()))
            .thenReturn(aisConsent);

        Xs2aCreateAisConsentResponse expected = new Xs2aCreateAisConsentResponse(CONSENT_ID, aisConsent, null);

        // When
        Optional<Xs2aCreateAisConsentResponse> actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        // Then
        assertTrue(actualResponse.isPresent());
        assertEquals(expected, actualResponse.get());
    }

    @Test
    void createConsent_failed() throws WrongChecksumException {
        // Given
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCmsConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1))
            .thenReturn(CMS_CONSENT);
        when(consentServiceEncrypted.createConsent(any(CmsConsent.class)))
            .thenReturn(CmsResponse.<CmsCreateConsentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Optional<Xs2aCreateAisConsentResponse> actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        // Then
        assertFalse(actualResponse.isPresent());
    }

    @Test
    void getAccountConsentById_success() {
        // Given
        when(consentServiceEncrypted.getConsentById(CONSENT_ID))
            .thenReturn(CmsResponse.<CmsConsent>builder().payload(CMS_CONSENT).build());
        when(aisConsentMapper.mapToAisConsent(CMS_CONSENT))
            .thenReturn(aisConsent);

        // When
        Optional<AisConsent> actualResponse = xs2aAisConsentService.getAccountConsentById(CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(aisConsent);
    }

    @Test
    void getAccountConsentById_failed() {
        // Given
        when(consentServiceEncrypted.getConsentById(CONSENT_ID))
            .thenReturn(CmsResponse.<CmsConsent>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Optional<AisConsent> actualResponse = xs2aAisConsentService.getAccountConsentById(CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_success() {
        // Given
        when(consentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        // When
        boolean actualResponse = xs2aAisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID);

        // Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_false() {
        // Given
        when(consentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        // When
        boolean actualResponse = xs2aAisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID);

        // Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void createAisConsentAuthorization_success() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(aisConsentAuthorisationMapper.mapToAuthorisationRequest(SCA_STATUS, PSU_DATA, SCA_APPROACH, REDIRECT_URI, NOK_REDIRECT_URI))
            .thenReturn(AIS_CONSENT_AUTHORIZATION_REQUEST);
        when(authorisationServiceEncrypted.createAuthorisation(new AisAuthorisationParentHolder(CONSENT_ID), AIS_CONSENT_AUTHORIZATION_REQUEST))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().payload(buildCreateAisConsentAuthorizationResponse()).build());
        when(requestProviderService.getTppRedirectURI())
            .thenReturn(REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI())
            .thenReturn(NOK_REDIRECT_URI);

        // When
        Optional<CreateAuthorisationResponse> actualResponse = xs2aAisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(buildCreateAisConsentAuthorizationResponse());
    }

    @Test
    void createAisConsentAuthorization_false() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(authorisationServiceEncrypted.createAuthorisation(any(AisAuthorisationParentHolder.class), any()))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Optional<CreateAuthorisationResponse> actualResponse = xs2aAisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAccountConsentAuthorizationById_failed() {
        // Given
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Optional<Authorisation> actualResponse = xs2aAisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationSubResources_success() {
        // Given
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<List<String>>builder().payload(STRING_LIST).build());

        // When
        Optional<List<String>> actualResponse = xs2aAisConsentService.getAuthorisationSubResources(CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(STRING_LIST);
    }

    @Test
    void getAuthorisationSubResources_failed() {
        // Given
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<List<String>>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Optional<List<String>> actualResponse = xs2aAisConsentService.getAuthorisationSubResources(CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationScaStatus_success() {
        // Given
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(SCA_STATUS).build());

        // When
        Optional<ScaStatus> actualResponse = xs2aAisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SCA_STATUS);
    }

    @Test
    void getAuthorisationScaStatus_failed() {
        // Given
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_AUTHORISATION_ID, new AisAuthorisationParentHolder(WRONG_CONSENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Optional<ScaStatus> actualResponse = xs2aAisConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void isAuthenticationMethodDecoupled_success() {
        // Given
        when(authorisationServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        // When
        boolean actualResponse = xs2aAisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void isAuthenticationMethodDecoupled_failed() {
        // Given
        when(authorisationServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        // When
        boolean actualResponse = xs2aAisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void saveAuthenticationMethods_success() {
        // Given
        when(xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(AUTHENTICATION_OBJECT_LIST))
            .thenReturn(CMS_SCA_METHOD_LIST);
        when(authorisationServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, CMS_SCA_METHOD_LIST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        // When
        boolean actualResponse = xs2aAisConsentService.saveAuthenticationMethods(AUTHORISATION_ID, AUTHENTICATION_OBJECT_LIST);

        // Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void saveAuthenticationMethods_failed() {
        // Given
        when(xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(AUTHENTICATION_OBJECT_LIST))
            .thenReturn(CMS_SCA_METHOD_LIST);
        when(authorisationServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, CMS_SCA_METHOD_LIST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        // When
        boolean actualResponse = xs2aAisConsentService.saveAuthenticationMethods(AUTHORISATION_ID, AUTHENTICATION_OBJECT_LIST);

        // Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void updateConsentStatus_shouldStoreConsentStatusInLoggingContext() throws WrongChecksumException {
        // Given
        when(consentServiceEncrypted.updateConsentStatusById(CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        // When
        xs2aAisConsentService.updateConsentStatus(CONSENT_ID, CONSENT_STATUS);

        // Then
        verify(consentServiceEncrypted).updateConsentStatusById(CONSENT_ID, CONSENT_STATUS);
        verify(loggingContextService).storeConsentStatus(CONSENT_STATUS);
    }

    @Test
    void updateConsentStatus_failure_shouldNotStoreConsentStatusInLoggingContext() throws WrongChecksumException {
        // Given
        when(consentServiceEncrypted.updateConsentStatusById(CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        // When
        xs2aAisConsentService.updateConsentStatus(CONSENT_ID, CONSENT_STATUS);

        // Then
        verify(consentServiceEncrypted).updateConsentStatusById(CONSENT_ID, CONSENT_STATUS);
        verify(loggingContextService, never()).storeConsentStatus(any());
    }

    @Test
    void consentActionLog() throws WrongChecksumException {
        // Given
        ActionStatus actionStatus = ActionStatus.SUCCESS;
        ArgumentCaptor<AisConsentActionRequest> argumentCaptor = ArgumentCaptor.forClass(AisConsentActionRequest.class);

        // When
        xs2aAisConsentService.consentActionLog(TPP_ID, CONSENT_ID, actionStatus, REQUEST_URI, true, null, null);

        // Then
        verify(aisConsentServiceEncrypted).checkConsentAndSaveActionLog(argumentCaptor.capture());

        AisConsentActionRequest aisConsentActionRequest = argumentCaptor.getValue();
        assertThat(aisConsentActionRequest.getTppId()).isEqualTo(TPP_ID);
        assertThat(aisConsentActionRequest.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(aisConsentActionRequest.getActionStatus()).isEqualTo(actionStatus);
        assertThat(aisConsentActionRequest.getRequestUri()).isEqualTo(REQUEST_URI);
        assertThat(aisConsentActionRequest.isUpdateUsage()).isTrue();
    }

    @Test
    void createConsentCheckInternalRequestId() throws WrongChecksumException {
        // Given
        ArgumentCaptor<CmsConsent> argumentCaptor = ArgumentCaptor.forClass(CmsConsent.class);
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        CreateAisConsentRequest createAisConsentRequesWithInternalRequestId = new CreateAisConsentRequest();
        createAisConsentRequesWithInternalRequestId.setInternalRequestId(INTERNAL_REQUEST_ID);
        when(consentServiceEncrypted.createConsent(any()))
            .thenReturn(CmsResponse.<CmsCreateConsentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        // Then
        verify(consentServiceEncrypted).createConsent(argumentCaptor.capture());
    }

    @Test
    void updateConsentAuthorization() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = new UpdateConsentPsuDataReq();
        updateConsentPsuDataReq.setAuthorizationId(AUTHORISATION_ID);
        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();

        when(aisConsentAuthorisationMapper.mapToAuthorisationRequest(updateConsentPsuDataReq))
            .thenReturn(request);

        // When
        xs2aAisConsentService.updateConsentAuthorization(updateConsentPsuDataReq);

        // Then
        verify(authorisationServiceEncrypted, times(1)).updateAuthorisation(AUTHORISATION_ID, request);
    }

    @Test
    void updateConsentAuthorization_nullValue() {
        // When
        xs2aAisConsentService.updateConsentAuthorization(null);

        // Then
        verify(authorisationServiceEncrypted, never()).updateAuthorisation(any(), any());
    }

    @Test
    void updateConsentAuthorisationStatus() {
        // When
        xs2aAisConsentService.updateConsentAuthorisationStatus(AUTHORISATION_ID, ScaStatus.RECEIVED);

        // Then
        verify(authorisationServiceEncrypted, times(1)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.RECEIVED);
    }

    @Test
    void updateScaApproach() {
        // When
        xs2aAisConsentService.updateScaApproach(AUTHORISATION_ID, ScaApproach.REDIRECT);

        // Then
        verify(authorisationServiceEncrypted, times(1)).updateScaApproach(AUTHORISATION_ID, ScaApproach.REDIRECT);
    }

    @Test
    void updateMultilevelScaRequired() throws WrongChecksumException {
        // When
        xs2aAisConsentService.updateMultilevelScaRequired(CONSENT_ID, true);

        // Then
        verify(consentServiceEncrypted, times(1)).updateMultilevelScaRequired(CONSENT_ID, true);
    }

    @Test
    void getAuthorisationScaApproach() {
        // Given
        AuthorisationScaApproachResponse payload = new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED);
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(payload)
                            .build());

        // When
        assertEquals(Optional.of(payload), xs2aAisConsentService.getAuthorisationScaApproach(AUTHORISATION_ID));

        // Then
        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(AUTHORISATION_ID);
    }

    @Test
    void getAuthorisationScaApproach_error() {
        // Given
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .error(CmsError.TECHNICAL_ERROR)
                            .build());

        // When
        assertEquals(Optional.empty(), xs2aAisConsentService.getAuthorisationScaApproach(AUTHORISATION_ID));

        // Then
        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(AUTHORISATION_ID);
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static CreateConsentReq buildCreateConsentReq() {
        CreateConsentReq createConsentReq = new CreateConsentReq();
        createConsentReq.setFrequencyPerDay(1);
        return createConsentReq;
    }

    private static CreateAuthorisationRequest buildAisConsentAuthorizationRequest() {
        CreateAuthorisationRequest consentAuthorization = new CreateAuthorisationRequest();
        consentAuthorization.setPsuData(PSU_DATA);
        consentAuthorization.setScaApproach(SCA_APPROACH);
        return consentAuthorization;
    }

    private static CreateAuthorisationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", null);
    }

    private CmsConsent getCmsConsentWithNotifications() {
        CmsConsent cmsConsent = new CmsConsent();
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppNotificationSupportedModes(Collections.singletonList(NotificationSupportedMode.SCA));
        cmsConsent.setTppInformation(new ConsentTppInformation());
        return cmsConsent;
    }
}
