/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentAuthorisationMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAuthenticationObjectToCmsScaMethodMapper;
import de.adorsys.psd2.xs2a.service.profile.FrequencyPerDateCalculationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aAisConsentServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String NEW_ID = "fhu53g-6811-19ff-4b5a-8188";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final String AUTHENTICATION_METHOD_ID = "19ff-4b5a-8188";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final CreateConsentReq CREATE_CONSENT_REQ = buildCreateConsentReq();
    private static final CreateAisConsentRequest CREATE_AIS_CONSENT_REQUEST = new CreateAisConsentRequest();
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final AisAccountConsent AIS_ACCOUNT_CONSENT = new AisAccountConsent();
    private static final AccountConsent ACCOUNT_CONSENT = createConsent(CONSENT_ID);
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.VALID;
    private static final AisConsentAuthorizationRequest AIS_CONSENT_AUTHORIZATION_REQUEST = buildAisConsentAuthorizationRequest();
    private static final AisConsentAuthorizationResponse AIS_CONSENT_AUTHORIZATION_RESPONSE = new AisConsentAuthorizationResponse();
    private static final AccountConsentAuthorization ACCOUNT_CONSENT_AUTHORIZATION = new AccountConsentAuthorization();
    private static final List<String> STRING_LIST = Collections.singletonList(AUTHORISATION_ID);
    private static final List<Xs2aAuthenticationObject> AUTHENTICATION_OBJECT_LIST = Collections.singletonList(new Xs2aAuthenticationObject());
    private static final List<CmsScaMethod> CMS_SCA_METHOD_LIST = Collections.singletonList(new CmsScaMethod(AUTHORISATION_ID, true));

    @InjectMocks
    private Xs2aAisConsentService xs2aAisConsentService;
    @Mock
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
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


    @Test
    public void createConsent_success() {
        //given
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCreateAisConsentRequest(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1))
            .thenReturn(CREATE_AIS_CONSENT_REQUEST);
        when(aisConsentServiceEncrypted.createConsent(CREATE_AIS_CONSENT_REQUEST))
            .thenReturn(Optional.of(CONSENT_ID));

        //when
        String actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        //then
        assertThat(actualResponse).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createConsent_failed() {
        //given
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCreateAisConsentRequest(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1))
            .thenReturn(CREATE_AIS_CONSENT_REQUEST);
        when(aisConsentServiceEncrypted.createConsent(CREATE_AIS_CONSENT_REQUEST))
            .thenReturn(Optional.empty());

        //when
        String actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        //then
        assertThat(actualResponse).isNull();
    }


    @Test
    public void getAccountConsentById_success() {
        //given
        when(aisConsentServiceEncrypted.getAisAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(AIS_ACCOUNT_CONSENT));
        when(aisConsentMapper.mapToAccountConsent(AIS_ACCOUNT_CONSENT))
            .thenReturn(ACCOUNT_CONSENT);

        //when
        AccountConsent actualResponse = xs2aAisConsentService.getAccountConsentById(CONSENT_ID);

        //then
        assertThat(actualResponse).isEqualTo(ACCOUNT_CONSENT);
    }

    @Test
    public void getAccountConsentById_failed() {
        //given
        when(aisConsentServiceEncrypted.getAisAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        //when
        AccountConsent actualResponse = xs2aAisConsentService.getAccountConsentById(CONSENT_ID);

        //then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void getInitialAccountConsentById_success() {
        //given
        when(aisConsentServiceEncrypted.getInitialAisAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(AIS_ACCOUNT_CONSENT));
        when(aisConsentMapper.mapToAccountConsent(AIS_ACCOUNT_CONSENT))
            .thenReturn(ACCOUNT_CONSENT);

        //when
        AccountConsent actualResponse = xs2aAisConsentService.getInitialAccountConsentById(CONSENT_ID);

        //then
        assertThat(actualResponse).isEqualTo(ACCOUNT_CONSENT);
    }

    @Test
    public void getInitialAccountConsentById_failed() {
        //given
        when(aisConsentServiceEncrypted.getInitialAisAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        //when
        AccountConsent actualResponse = xs2aAisConsentService.getInitialAccountConsentById(CONSENT_ID);

        //then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void getAccountConsentStatusById_success() {
        //given
        when(aisConsentServiceEncrypted.getConsentStatusById(CONSENT_ID))
            .thenReturn(Optional.of(CONSENT_STATUS));

        //when
        ConsentStatus actualResponse = xs2aAisConsentService.getAccountConsentStatusById(CONSENT_ID);

        //then
        assertThat(actualResponse).isEqualTo(CONSENT_STATUS);
    }

    @Test
    public void getAccountConsentStatusById_failed() {
        //given
        when(aisConsentServiceEncrypted.getConsentStatusById(CONSENT_ID))
            .thenReturn(Optional.empty());

        //when
        ConsentStatus actualResponse = xs2aAisConsentService.getAccountConsentStatusById(CONSENT_ID);

        //then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success() {
        //given
        when(aisConsentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(true);

        //when
        boolean actualResponse = xs2aAisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID);

        //then
        assertThat(actualResponse).isTrue();
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_false() {
        //given
        when(aisConsentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(false);

        //when
        boolean actualResponse = xs2aAisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID);

        //then
        assertThat(actualResponse).isFalse();
    }

    @Test
    public void createAisConsentAuthorization_success() {
        //given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(aisConsentAuthorisationMapper.mapToAisConsentAuthorization(SCA_STATUS, PSU_DATA, SCA_APPROACH))
            .thenReturn(AIS_CONSENT_AUTHORIZATION_REQUEST);
        when(aisConsentServiceEncrypted.createAuthorization(CONSENT_ID, AIS_CONSENT_AUTHORIZATION_REQUEST))
            .thenReturn(Optional.of(NEW_ID));

        //when
        Optional<String> actualResponse = xs2aAisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA);

        //then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(NEW_ID);
    }

    @Test
    public void createAisConsentAuthorization_false() {
        //given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(aisConsentAuthorisationMapper.mapToAisConsentAuthorization(SCA_STATUS, PSU_DATA, SCA_APPROACH))
            .thenReturn(AIS_CONSENT_AUTHORIZATION_REQUEST);
        when(aisConsentServiceEncrypted.createAuthorization(CONSENT_ID, AIS_CONSENT_AUTHORIZATION_REQUEST))
            .thenReturn(Optional.empty());

        //when
        Optional<String> actualResponse = xs2aAisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA);

        //then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAccountConsentAuthorizationById_success() {
        //given
        when(aisConsentServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(AIS_CONSENT_AUTHORIZATION_RESPONSE));
        when(aisConsentAuthorisationMapper.mapToAccountConsentAuthorization(AIS_CONSENT_AUTHORIZATION_RESPONSE))
            .thenReturn(ACCOUNT_CONSENT_AUTHORIZATION);

        //when
        AccountConsentAuthorization actualResponse = xs2aAisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        //then
        assertThat(actualResponse).isEqualTo(ACCOUNT_CONSENT_AUTHORIZATION);
    }

    @Test
    public void getAccountConsentAuthorizationById_failed() {
        //given
        when(aisConsentServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.empty());

        //when
        AccountConsentAuthorization actualResponse = xs2aAisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        //then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void getAuthorisationSubResources_success() {
        //given
        when(aisConsentServiceEncrypted.getAuthorisationsByConsentId(CONSENT_ID))
            .thenReturn(Optional.of(STRING_LIST));

        //when
        Optional<List<String>> actualResponse = xs2aAisConsentService.getAuthorisationSubResources(CONSENT_ID);

        //then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(STRING_LIST);
    }

    @Test
    public void getAuthorisationSubResources_failed() {
        //given
        when(aisConsentServiceEncrypted.getAuthorisationsByConsentId(CONSENT_ID))
            .thenReturn(Optional.empty());

        //when
        Optional<List<String>> actualResponse = xs2aAisConsentService.getAuthorisationSubResources(CONSENT_ID);

        //then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        //given
        when(aisConsentServiceEncrypted.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        //when
        Optional<ScaStatus> actualResponse = xs2aAisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        //then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SCA_STATUS);
    }

    @Test
    public void getAuthorisationScaStatus_failed() {
        //given
        when(aisConsentServiceEncrypted.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        //when
        Optional<ScaStatus> actualResponse = xs2aAisConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        //then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void isAuthenticationMethodDecoupled_success() {
        //given
        when(aisConsentServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(true);

        //when
        boolean actualResponse = xs2aAisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        //then
        assertThat(actualResponse).isTrue();
    }

    @Test
    public void isAuthenticationMethodDecoupled_failed() {
        //given
        when(aisConsentServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(false);

        //when
        boolean actualResponse = xs2aAisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        //then
        assertThat(actualResponse).isFalse();
    }

    @Test
    public void saveAuthenticationMethods_success() {
        //given
        when(xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(AUTHENTICATION_OBJECT_LIST))
            .thenReturn(CMS_SCA_METHOD_LIST);
        when(aisConsentServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, CMS_SCA_METHOD_LIST))
            .thenReturn(true);

        //when
        boolean actualResponse = xs2aAisConsentService.saveAuthenticationMethods(AUTHORISATION_ID, AUTHENTICATION_OBJECT_LIST);

        //then
        assertThat(actualResponse).isTrue();
    }

    @Test
    public void saveAuthenticationMethods_failed() {
        //given
        when(xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(AUTHENTICATION_OBJECT_LIST))
            .thenReturn(CMS_SCA_METHOD_LIST);
        when(aisConsentServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, CMS_SCA_METHOD_LIST))
            .thenReturn(false);

        //when
        boolean actualResponse = xs2aAisConsentService.saveAuthenticationMethods(AUTHORISATION_ID, AUTHENTICATION_OBJECT_LIST);

        //then
        assertThat(actualResponse).isFalse();
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

    private static AisConsentAuthorizationRequest buildAisConsentAuthorizationRequest() {
        AisConsentAuthorizationRequest consentAuthorization = new AisConsentAuthorizationRequest();
        consentAuthorization.setPsuData(PSU_DATA);
        consentAuthorization.setScaStatus(SCA_STATUS);
        consentAuthorization.setScaApproach(SCA_APPROACH);
        return consentAuthorization;
    }

    private static AccountConsent createConsent(String id) {
        return new AccountConsent(id, createEmptyAccountAccess(), false, LocalDate.now(), 4, null, ConsentStatus.VALID, false, false, null, buildTppInfo(), AisConsentRequestType.GLOBAL);
    }

    private static Xs2aAccountAccess createEmptyAccountAccess() {
        return new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES, Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES);
    }
}
