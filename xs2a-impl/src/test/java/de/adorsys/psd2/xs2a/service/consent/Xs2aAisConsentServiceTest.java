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
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.psd2.consent.api.authorisation.AisAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreateAisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentAuthorisationMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAuthenticationObjectToCmsScaMethodMapper;
import de.adorsys.psd2.xs2a.service.profile.FrequencyPerDateCalculationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aAisConsentServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final String AUTHENTICATION_METHOD_ID = "19ff-4b5a-8188";
    private static final String TPP_ID = "Test TppId";
    private static final String REQUEST_URI = "request/uri";
    private static final String REDIRECT_URI = "request/redirect_uri";
    private static final String NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final CreateConsentReq CREATE_CONSENT_REQ = buildCreateConsentReq();
    private static final CreateAisConsentRequest CREATE_AIS_CONSENT_REQUEST = new CreateAisConsentRequest();
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final AisAccountConsent AIS_ACCOUNT_CONSENT = new AisAccountConsent();
    private static final AccountConsent ACCOUNT_CONSENT = createConsent(CONSENT_ID);
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.VALID;
    private static final CreateAuthorisationRequest AIS_CONSENT_AUTHORIZATION_REQUEST = buildAisConsentAuthorizationRequest();
    private static final List<String> STRING_LIST = Collections.singletonList(AUTHORISATION_ID);
    private static final List<AuthenticationObject> AUTHENTICATION_OBJECT_LIST = Collections.singletonList(new AuthenticationObject());
    private static final List<CmsScaMethod> CMS_SCA_METHOD_LIST = Collections.singletonList(new CmsScaMethod(AUTHORISATION_ID, true));
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private Xs2aAisConsentService xs2aAisConsentService;
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

    @Test
    void createConsent_success() throws WrongChecksumException {
        //Given
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCreateAisConsentRequest(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1, INTERNAL_REQUEST_ID))
            .thenReturn(CREATE_AIS_CONSENT_REQUEST);
        when(aisConsentServiceEncrypted.createConsent(CREATE_AIS_CONSENT_REQUEST))
            .thenReturn(CmsResponse.<CreateAisConsentResponse>builder().payload(new CreateAisConsentResponse(CONSENT_ID, AIS_ACCOUNT_CONSENT, null)).build());
        when(aisConsentMapper.mapToAccountConsent(AIS_ACCOUNT_CONSENT))
            .thenReturn(ACCOUNT_CONSENT);

        Xs2aCreateAisConsentResponse expected = new Xs2aCreateAisConsentResponse(CONSENT_ID, ACCOUNT_CONSENT, null);

        //When
        Optional<Xs2aCreateAisConsentResponse> actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        //Then
        assertTrue(actualResponse.isPresent());
        assertEquals(expected, actualResponse.get());
    }

    @Test
    void createConsent_failed() throws WrongChecksumException {
        //Given
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCreateAisConsentRequest(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1, INTERNAL_REQUEST_ID))
            .thenReturn(CREATE_AIS_CONSENT_REQUEST);
        when(aisConsentServiceEncrypted.createConsent(CREATE_AIS_CONSENT_REQUEST))
            .thenReturn(CmsResponse.<CreateAisConsentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<Xs2aCreateAisConsentResponse> actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        //Then
        assertFalse(actualResponse.isPresent());
    }

    @Test
    void getAccountConsentById_success() {
        //Given
        when(aisConsentServiceEncrypted.getAisAccountConsentById(CONSENT_ID))
            .thenReturn(CmsResponse.<AisAccountConsent>builder().payload(AIS_ACCOUNT_CONSENT).build());
        when(aisConsentMapper.mapToAccountConsent(AIS_ACCOUNT_CONSENT))
            .thenReturn(ACCOUNT_CONSENT);

        //When
        Optional<AccountConsent> actualResponse = xs2aAisConsentService.getAccountConsentById(CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(ACCOUNT_CONSENT);
    }

    @Test
    void getAccountConsentById_failed() {
        //Given
        when(aisConsentServiceEncrypted.getAisAccountConsentById(CONSENT_ID))
            .thenReturn(CmsResponse.<AisAccountConsent>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<AccountConsent> actualResponse = xs2aAisConsentService.getAccountConsentById(CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_success() {
        //Given
        when(aisConsentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        //When
        boolean actualResponse = xs2aAisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID);

        //Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_false() {
        //Given
        when(aisConsentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        //When
        boolean actualResponse = xs2aAisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID);

        //Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void createAisConsentAuthorization_success() {
        //Given
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

        //When
        Optional<CreateAuthorisationResponse> actualResponse = xs2aAisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(buildCreateAisConsentAuthorizationResponse());
    }

    @Test
    void createAisConsentAuthorization_false() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(authorisationServiceEncrypted.createAuthorisation(any(AisAuthorisationParentHolder.class), any()))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<CreateAuthorisationResponse> actualResponse = xs2aAisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAccountConsentAuthorizationById_failed() {
        //Given
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<Authorisation> actualResponse = xs2aAisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationSubResources_success() {
        //Given
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<List<String>>builder().payload(STRING_LIST).build());

        //When
        Optional<List<String>> actualResponse = xs2aAisConsentService.getAuthorisationSubResources(CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(STRING_LIST);
    }

    @Test
    void getAuthorisationSubResources_failed() {
        //Given
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<List<String>>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<List<String>> actualResponse = xs2aAisConsentService.getAuthorisationSubResources(CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationScaStatus_success() {
        //Given
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(SCA_STATUS).build());

        //When
        Optional<ScaStatus> actualResponse = xs2aAisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SCA_STATUS);
    }

    @Test
    void getAuthorisationScaStatus_failed() {
        //Given
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_AUTHORISATION_ID, new AisAuthorisationParentHolder(WRONG_CONSENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<ScaStatus> actualResponse = xs2aAisConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void isAuthenticationMethodDecoupled_success() {
        //Given
        when(authorisationServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        //When
        boolean actualResponse = xs2aAisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        //Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void isAuthenticationMethodDecoupled_failed() {
        //Given
        when(authorisationServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        //When
        boolean actualResponse = xs2aAisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        //Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void saveAuthenticationMethods_success() {
        //Given
        when(xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(AUTHENTICATION_OBJECT_LIST))
            .thenReturn(CMS_SCA_METHOD_LIST);
        when(authorisationServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, CMS_SCA_METHOD_LIST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        //When
        boolean actualResponse = xs2aAisConsentService.saveAuthenticationMethods(AUTHORISATION_ID, AUTHENTICATION_OBJECT_LIST);

        //Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void saveAuthenticationMethods_failed() {
        //Given
        when(xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(AUTHENTICATION_OBJECT_LIST))
            .thenReturn(CMS_SCA_METHOD_LIST);
        when(authorisationServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, CMS_SCA_METHOD_LIST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        //When
        boolean actualResponse = xs2aAisConsentService.saveAuthenticationMethods(AUTHORISATION_ID, AUTHENTICATION_OBJECT_LIST);

        //Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void updateConsentStatus_shouldStoreConsentStatusInLoggingContext() throws WrongChecksumException {
        // Given
        when(aisConsentServiceEncrypted.updateConsentStatusById(CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        // When
        xs2aAisConsentService.updateConsentStatus(CONSENT_ID, CONSENT_STATUS);

        // Then
        verify(aisConsentServiceEncrypted).updateConsentStatusById(CONSENT_ID, CONSENT_STATUS);
        verify(loggingContextService).storeConsentStatus(CONSENT_STATUS);
    }

    @Test
    void updateConsentStatus_failure_shouldNotStoreConsentStatusInLoggingContext() throws WrongChecksumException {
        // Given
        when(aisConsentServiceEncrypted.updateConsentStatusById(CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        // When
        xs2aAisConsentService.updateConsentStatus(CONSENT_ID, CONSENT_STATUS);

        // Then
        verify(aisConsentServiceEncrypted).updateConsentStatusById(CONSENT_ID, CONSENT_STATUS);
        verify(loggingContextService, never()).storeConsentStatus(any());
    }

    @Test
    void consentActionLog() throws WrongChecksumException {
        //Given
        ActionStatus actionStatus = ActionStatus.SUCCESS;
        ArgumentCaptor<AisConsentActionRequest> argumentCaptor = ArgumentCaptor.forClass(AisConsentActionRequest.class);
        //When
        xs2aAisConsentService.consentActionLog(TPP_ID, CONSENT_ID, actionStatus, REQUEST_URI, true, null, null);
        //Then
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
        //Given
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        ArgumentCaptor<CreateAisConsentRequest> argumentCaptor = ArgumentCaptor.forClass(CreateAisConsentRequest.class);
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        CreateAisConsentRequest createAisConsentRequesWithInternalRequestId = new CreateAisConsentRequest();
        createAisConsentRequesWithInternalRequestId.setInternalRequestId(INTERNAL_REQUEST_ID);
        when(aisConsentMapper.mapToCreateAisConsentRequest(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1, INTERNAL_REQUEST_ID))
            .thenReturn(createAisConsentRequesWithInternalRequestId);
        when(aisConsentServiceEncrypted.createConsent(any()))
            .thenReturn(CmsResponse.<CreateAisConsentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);
        verify(aisConsentServiceEncrypted).createConsent(argumentCaptor.capture());

        //Then
        CreateAisConsentRequest createAisConsentRequest = argumentCaptor.getValue();
        assertThat(createAisConsentRequest.getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);
    }

    @Test
    void updateConsentAuthorization() {
        UpdateConsentPsuDataReq updateConsentPsuDataReq = new UpdateConsentPsuDataReq();
        updateConsentPsuDataReq.setAuthorizationId(AUTHORISATION_ID);
        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();

        when(aisConsentAuthorisationMapper.mapToAuthorisationRequest(updateConsentPsuDataReq)).thenReturn(request);

        xs2aAisConsentService.updateConsentAuthorization(updateConsentPsuDataReq);

        verify(authorisationServiceEncrypted, times(1)).updateAuthorisation(AUTHORISATION_ID, request);
    }

    @Test
    void updateConsentAuthorization_nullValue() {
        xs2aAisConsentService.updateConsentAuthorization(null);
        verify(authorisationServiceEncrypted, never()).updateAuthorisation(any(), any());
    }

    @Test
    void updateConsentAuthorisationStatus() {
        xs2aAisConsentService.updateConsentAuthorisationStatus(AUTHORISATION_ID, ScaStatus.RECEIVED);
        verify(authorisationServiceEncrypted, times(1)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.RECEIVED);
    }

    @Test
    void updateScaApproach() {
        xs2aAisConsentService.updateScaApproach(AUTHORISATION_ID, ScaApproach.REDIRECT);
        verify(authorisationServiceEncrypted, times(1)).updateScaApproach(AUTHORISATION_ID, ScaApproach.REDIRECT);
    }

    @Test
    void updateMultilevelScaRequired() throws WrongChecksumException {
        xs2aAisConsentService.updateMultilevelScaRequired(CONSENT_ID, true);
        verify(aisConsentServiceEncrypted, times(1)).updateMultilevelScaRequired(CONSENT_ID, true);
    }

    @Test
    void getAuthorisationScaApproach() {
        AuthorisationScaApproachResponse payload = new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED);
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID)).thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                                                                                                         .payload(payload)
                                                                                                         .build());

        assertEquals(Optional.of(payload), xs2aAisConsentService.getAuthorisationScaApproach(AUTHORISATION_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(AUTHORISATION_ID);
    }

    @Test
    void getAuthorisationScaApproach_error() {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID)).thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                                                                                                         .error(CmsError.TECHNICAL_ERROR)
                                                                                                         .build());

        assertEquals(Optional.empty(), xs2aAisConsentService.getAuthorisationScaApproach(AUTHORISATION_ID));

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

    private static AccountConsent createConsent(String id) {
        return new AccountConsent(id, new Xs2aAccountAccess(null, null, null, null, null, null, null), new Xs2aAccountAccess(null, null, null, null, null, null, null), false, LocalDate.now(), LocalDate.now(), 4, LocalDate.now(), ConsentStatus.VALID, false, false, null, null, AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), Collections.emptyMap(), OffsetDateTime.now());
    }

    private static CreateAuthorisationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", null);
    }
}
