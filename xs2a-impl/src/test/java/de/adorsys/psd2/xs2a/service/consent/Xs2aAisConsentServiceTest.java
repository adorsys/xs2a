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


package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.Xs2aResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreateAisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.CmsCreateConsentResponseService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aAisConsentServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String TPP_ID = "Test TppId";
    private static final String REQUEST_URI = "request/uri";
    private static final CreateConsentReq CREATE_CONSENT_REQ = buildCreateConsentReq();
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final CmsConsent CMS_CONSENT = new CmsConsent();
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.VALID;

    @InjectMocks
    private Xs2aAisConsentService xs2aAisConsentService;

    @Mock
    private ConsentServiceEncrypted consentServiceEncrypted;
    @Mock
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    @Mock
    private Xs2aAuthorisationService authorisationService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private Xs2aConsentAuthorisationMapper aisConsentAuthorisationMapper;
    @Mock
    private FrequencyPerDateCalculationService frequencyPerDateCalculationService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private CmsCreateConsentResponseService cmsCreateConsentResponseService;

    private final JsonReader jsonReader = new JsonReader();
    private AisConsent aisConsent;

    @BeforeEach
    void init() {
        aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
    }

    @Test
    void createConsent_success() {
        // Given
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCmsConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1))
            .thenReturn(CMS_CONSENT);
        CmsCreateConsentResponse cmsCreateConsentResponse = new CmsCreateConsentResponse(CONSENT_ID, getCmsConsentWithNotifications());
        when(aisConsentMapper.mapToAisConsent(any()))
            .thenReturn(aisConsent);
        when(cmsCreateConsentResponseService.getCmsCreateConsentResponse(CMS_CONSENT)).thenReturn(Xs2aResponse.<CmsCreateConsentResponse>builder()
                                                                                                      .payload(cmsCreateConsentResponse)
                                                                                                      .build());

        Xs2aCreateAisConsentResponse expected = new Xs2aCreateAisConsentResponse(CONSENT_ID, aisConsent, null);

        // When
        Xs2aResponse<Xs2aCreateAisConsentResponse> actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        // Then
        assertThat(actualResponse.isSuccessful()).isTrue();
        assertThat(actualResponse.getPayload()).isEqualTo(expected);
    }

    @Test
    void createConsent_WrongChecksumException() {
        // Given
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCmsConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1))
            .thenReturn(CMS_CONSENT);
        when(cmsCreateConsentResponseService.getCmsCreateConsentResponse(CMS_CONSENT)).thenReturn(Xs2aResponse.<CmsCreateConsentResponse>builder()
                                                                                                      .build());


        // When
        Xs2aResponse<Xs2aCreateAisConsentResponse> actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
    }

    @Test
    void createConsent_failed() {
        // Given
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCmsConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1))
            .thenReturn(CMS_CONSENT);
        when(cmsCreateConsentResponseService.getCmsCreateConsentResponse(CMS_CONSENT)).thenReturn(Xs2aResponse.<CmsCreateConsentResponse>builder()
                                                                                                      .build());

        // When
        Xs2aResponse<Xs2aCreateAisConsentResponse> actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
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
        assertThat(actualResponse).isPresent().contains(aisConsent);
    }

    @Test
    void getAccountConsentById_failed() {
        // Given
        when(consentServiceEncrypted.getConsentById(CONSENT_ID))
            .thenReturn(CmsResponse.<CmsConsent>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Optional<AisConsent> actualResponse = xs2aAisConsentService.getAccountConsentById(CONSENT_ID);

        // Then
        assertThat(actualResponse).isEmpty();
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_success() {
        // Given
        when(consentServiceEncrypted.findAndTerminateOldConsents(CONSENT_ID, getRequest(aisConsent)))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        // When
        boolean actualResponse = xs2aAisConsentService.findAndTerminateOldConsents(CONSENT_ID, getRequest(aisConsent));

        // Then
        assertThat(actualResponse).isTrue();
    }

    private TerminateOldConsentsRequest getRequest(AisConsent aisConsent) {
        return new TerminateOldConsentsRequest(aisConsent.isOneAccessType(),
                                               aisConsent.isWrongConsentData(),
                                               aisConsent.getPsuIdDataList(),
                                               aisConsent.getTppInfo().getAuthorisationNumber(),
                                               aisConsent.getInstanceId());
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_false() {
        // Given
        when(consentServiceEncrypted.findAndTerminateOldConsents(CONSENT_ID, getRequest(aisConsent)))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        // When
        boolean actualResponse = xs2aAisConsentService.findAndTerminateOldConsents(CONSENT_ID, getRequest(aisConsent));

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
    void updateConsentStatus_WrongChecksumException() throws WrongChecksumException {
        // Given
        when(consentServiceEncrypted.updateConsentStatusById(CONSENT_ID, CONSENT_STATUS))
            .thenThrow(new WrongChecksumException());

        // When
        xs2aAisConsentService.updateConsentStatus(CONSENT_ID, CONSENT_STATUS);

        // Then
        verify(consentServiceEncrypted, times(1)).updateConsentStatusById(CONSENT_ID, CONSENT_STATUS);
        verify(loggingContextService, never()).storeConsentStatus(any(ConsentStatus.class));
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
    void consentActionLog_WrongChecksumException() throws WrongChecksumException {
        // Given
        ActionStatus actionStatus = ActionStatus.SUCCESS;
        ArgumentCaptor<AisConsentActionRequest> argumentCaptor = ArgumentCaptor.forClass(AisConsentActionRequest.class);
        when(aisConsentServiceEncrypted.checkConsentAndSaveActionLog(any(AisConsentActionRequest.class)))
            .thenThrow(WrongChecksumException.class);

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
    void createConsentCheckInternalRequestId() {
        // Given
        ArgumentCaptor<CmsConsent> argumentCaptor = ArgumentCaptor.forClass(CmsConsent.class);
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCmsConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1))
            .thenReturn(CMS_CONSENT);
        when(cmsCreateConsentResponseService.getCmsCreateConsentResponse(CMS_CONSENT)).thenReturn(Xs2aResponse.<CmsCreateConsentResponse>builder()
                                                                                                      .build());

        // When
        xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        // Then
        verify(cmsCreateConsentResponseService).getCmsCreateConsentResponse(argumentCaptor.capture());
    }

    @Test
    void updateConsentAuthorization() {
        // Given
        ConsentAuthorisationsParameters updateConsentPsuDataReq = new ConsentAuthorisationsParameters();
        updateConsentPsuDataReq.setAuthorizationId(AUTHORISATION_ID);
        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();

        when(aisConsentAuthorisationMapper.mapToAuthorisationRequest(updateConsentPsuDataReq))
            .thenReturn(request);

        // When
        xs2aAisConsentService.updateConsentAuthorisation(updateConsentPsuDataReq);

        // Then
        verify(authorisationService, times(1)).updateAuthorisation(request, AUTHORISATION_ID);
    }

    @Test
    void updateConsentAuthorization_nullValue() {
        // When
        xs2aAisConsentService.updateConsentAuthorisation(null);

        // Then
        verify(authorisationService, never()).updateAuthorisation(any(), any());
    }

    @Test
    void updateMultilevelScaRequired() throws WrongChecksumException {
        // When
        xs2aAisConsentService.updateMultilevelScaRequired(CONSENT_ID, true);

        // Then
        verify(consentServiceEncrypted, times(1)).updateMultilevelScaRequired(CONSENT_ID, true);
    }

    @Test
    void updateMultilevelScaRequired_WrongChecksumException() throws WrongChecksumException {
        // When
        doThrow(new WrongChecksumException()).when(consentServiceEncrypted).updateMultilevelScaRequired(CONSENT_ID, true);

        // Then
        xs2aAisConsentService.updateMultilevelScaRequired(CONSENT_ID, true);
        verify(consentServiceEncrypted, times(1)).updateMultilevelScaRequired(CONSENT_ID, true);
    }

    @Test
    void getAuthorisationSubResources() {
        xs2aAisConsentService.getAuthorisationSubResources(CONSENT_ID);
        verify(authorisationService, times(1)).getAuthorisationSubResources(CONSENT_ID, AuthorisationType.CONSENT);
    }

    @Test
    void updateAspspAccountAccess() throws WrongChecksumException {
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/aspect/account-access.json", AccountAccess.class);

        CmsConsent cmsConsent = new CmsConsent();
        when(aisConsentServiceEncrypted.updateAspspAccountAccess(CONSENT_ID, accountAccess))
            .thenReturn(CmsResponse.<CmsConsent>builder().payload(cmsConsent).build());
        when(aisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        CmsResponse<AisConsent> actual = xs2aAisConsentService.updateAspspAccountAccess(CONSENT_ID, accountAccess);

        assertThat(actual.hasError()).isFalse();
        assertThat(actual.getPayload()).isEqualTo(aisConsent);
    }

    @Test
    void updateAspspAccountAccess_checksumError() throws WrongChecksumException {
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/aspect/account-access.json", AccountAccess.class);

        when(aisConsentServiceEncrypted.updateAspspAccountAccess(CONSENT_ID, accountAccess))
            .thenThrow(new WrongChecksumException());

        CmsResponse<AisConsent> actual = xs2aAisConsentService.updateAspspAccountAccess(CONSENT_ID, accountAccess);

        assertThat(actual.hasError()).isTrue();
        assertThat(actual.getError()).isEqualTo(CmsError.CHECKSUM_ERROR);
    }

    @Test
    void updateAspspAccountAccess_updateError() throws WrongChecksumException {
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/aspect/account-access.json", AccountAccess.class);

        when(aisConsentServiceEncrypted.updateAspspAccountAccess(CONSENT_ID, accountAccess))
            .thenReturn(CmsResponse.<CmsConsent>builder().error(CmsError.TECHNICAL_ERROR).build());

        CmsResponse<AisConsent> actual = xs2aAisConsentService.updateAspspAccountAccess(CONSENT_ID, accountAccess);

        assertThat(actual.hasError()).isTrue();
        assertThat(actual.getError()).isEqualTo(CmsError.TECHNICAL_ERROR);
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

    private CmsConsent getCmsConsentWithNotifications() {
        CmsConsent cmsConsent = new CmsConsent();
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppNotificationSupportedModes(Collections.singletonList(NotificationSupportedMode.SCA));
        cmsConsent.setTppInformation(new ConsentTppInformation());
        return cmsConsent;
    }
}
