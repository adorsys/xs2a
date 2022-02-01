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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ConsentPsuDataMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecoupledAisAuthorizationServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String INTERNAL_REQUEST_ID = "internal request id";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final Authorisation ACCOUNT_CONSENT_AUTHORIZATION = buildAccountConsentAuthorization();
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final CreateConsentAuthorizationResponse CREATE_CONSENT_AUTHORIZATION_RESPONSE = buildCreateConsentAuthorizationResponse();

    @InjectMocks
    private DecoupledAisAuthorizationService decoupledAisAuthorizationService;

    @Mock
    private Xs2aAuthorisationService authorisationService;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aConsentService consentService;
    @Mock
    private ConsentPsuDataMapper consentPsuDataMapper;
    @Mock
    private AisScaStageAuthorisationFactory scaStageAuthorisationFactory;
    @Mock
    private Xs2aConsentAuthorisationMapper xs2aConsentAuthorisationMapper;

    @Test
    void createConsentAuthorization_success() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(buildConsent()));
        when(xs2aConsentAuthorisationMapper.mapToAuthorisationRequest(AUTHORISATION_ID, SCA_STATUS, PSU_DATA, SCA_APPROACH)).thenReturn(getTestCreateAuthRequest());
        when(consentService.createConsentAuthorisation(CONSENT_ID, getTestCreateAuthRequest()))
            .thenReturn(Optional.of(buildCreateAuthorisationResponse()));
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .psuData(PSU_DATA)
                                                                            .consentId(CONSENT_ID)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .build();
        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);

        // Then
        assertThat(actualResponse).isPresent().contains(CREATE_CONSENT_AUTHORIZATION_RESPONSE);
    }

    @Test
    void createConsentAuthorization_wrongConsentId_fail() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .psuData(PSU_DATA)
                                                                            .consentId(WRONG_CONSENT_ID)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .build();
        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void updateConsentPsuData() {
        ConsentAuthorisationsParameters authorisationRequest = buildUpdateConsentPsuDataReq();
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();

        ConsentAuthorisationsParameters mappedUpdatePsuDataRequest = new ConsentAuthorisationsParameters();
        when(consentPsuDataMapper.mapToUpdateConsentPsuDataReq(authorisationRequest, processorResponse))
            .thenReturn(mappedUpdatePsuDataRequest);

        AuthorisationProcessorResponse actualResponse = decoupledAisAuthorizationService.updateConsentPsuData(authorisationRequest, processorResponse);

        assertThat(actualResponse).isEqualTo(processorResponse);
        verify(aisConsentService).updateConsentAuthorisation(mappedUpdatePsuDataRequest);
    }

    @Test
    void updateConsentPsuData_errorResponse() {
        ConsentAuthorisationsParameters authorisationRequest = buildUpdateConsentPsuDataReq();
        AuthorisationProcessorResponse processorResponse = buildAuthorisationProcessorResponseWithError();

        AuthorisationProcessorResponse actualResponse = decoupledAisAuthorizationService.updateConsentPsuData(authorisationRequest, processorResponse);

        assertThat(actualResponse).isEqualTo(processorResponse);
        verify(aisConsentService, never()).updateConsentAuthorisation(any());
    }

    @Test
    void getAccountConsentAuthorizationById_success() {
        // Given
        when(authorisationService.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(ACCOUNT_CONSENT_AUTHORIZATION));

        // When
        Optional<Authorisation> actualResponse = decoupledAisAuthorizationService.getConsentAuthorizationById(AUTHORISATION_ID);

        // Then
        assertThat(actualResponse).isPresent().contains(ACCOUNT_CONSENT_AUTHORIZATION);
    }

    @Test
    void getAccountConsentAuthorizationById_wrongIds_fail() {
        // Given
        when(authorisationService.getAuthorisationById(WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Authorisation> actualResponse = decoupledAisAuthorizationService.getConsentAuthorizationById(WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getAuthorisationScaStatus_success() {
        // Given
        when(consentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actualResponse).isPresent().contains(SCA_STATUS);
    }

    @Test
    void getAuthorisationScaStatus_wrongIds_fail() {
        // Given
        when(consentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getScaApproachServiceType_success() {
        // When
        ScaApproach actualResponse = decoupledAisAuthorizationService.getScaApproachServiceType();

        // Then
        assertThat(actualResponse).isEqualTo(SCA_APPROACH);
    }

    private static CreateConsentAuthorizationResponse buildCreateConsentAuthorizationResponse() {
        CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();
        resp.setConsentId(CONSENT_ID);
        resp.setAuthorisationId(AUTHORISATION_ID);
        resp.setScaStatus(ScaStatus.RECEIVED);
        resp.setPsuIdData(PSU_DATA);
        resp.setScaApproach(ScaApproach.DECOUPLED);
        resp.setInternalRequestId(INTERNAL_REQUEST_ID);
        return resp;
    }

    private static Authorisation buildAccountConsentAuthorization() {
        Authorisation authorisation = new Authorisation();
        authorisation.setScaStatus(ScaStatus.RECEIVED);
        return authorisation;
    }

    private static AisConsent buildConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        return aisConsent;
    }

    private ConsentAuthorisationsParameters buildUpdateConsentPsuDataReq() {
        ConsentAuthorisationsParameters authorisationRequest = new ConsentAuthorisationsParameters();
        authorisationRequest.setPsuData(PSU_DATA);
        return authorisationRequest;
    }

    private CreateAuthorisationResponse buildCreateAuthorisationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, INTERNAL_REQUEST_ID, PSU_DATA, SCA_APPROACH);
    }

    private AuthorisationProcessorResponse buildAuthorisationProcessorResponseWithError() {
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();
        processorResponse.setErrorHolder(ErrorHolder.builder(ErrorType.AIS_400).build());
        return processorResponse;
    }

    private CreateAuthorisationRequest getTestCreateAuthRequest() {
        CreateAuthorisationRequest consentAuthorization = new CreateAuthorisationRequest();
        consentAuthorization.setScaStatus(SCA_STATUS);
        consentAuthorization.setAuthorisationId(AUTHORISATION_ID);
        consentAuthorization.setPsuData(PSU_DATA);
        consentAuthorization.setScaApproach(SCA_APPROACH);
        return consentAuthorization;
    }
}
