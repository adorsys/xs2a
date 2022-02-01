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
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EmbeddedAisAuthorizationServiceTest {
    private static final ScaStatus STARTED_SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaStatus STARTED_XS2A_SCA_STATUS = ScaStatus.RECEIVED;
    private static final String PSU_ID = "Test psuId";
    private static final PsuIdData PSU_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final String CONSENT_ID = "Test consentId";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "Test authorisationId";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;

    @InjectMocks
    private EmbeddedAisAuthorizationService authorizationService;

    @Mock
    private Xs2aAuthorisationService authorisationService;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aConsentService consentService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private AisScaStageAuthorisationFactory scaStageAuthorisationFactory;
    @Mock
    private ConsentAuthorisationsParameters updateConsentPsuDataRequest;
    @Mock
    private Authorisation authorisation;
    @Mock
    private UpdateConsentPsuDataResponse updateConsentPsuDataResponse;
    @Mock
    private AisConsent consent;
    @Mock
    private Xs2aConsentAuthorisationMapper xs2aConsentAuthorisationMapper;

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
        Optional<CreateConsentAuthorizationResponse> actualResponse = authorizationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getAccountConsentAuthorizationById_success() {
        // Given
        when(authorisationService.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(authorisation));

        // When
        Optional<Authorisation> actualResponse = authorizationService.getConsentAuthorizationById(AUTHORISATION_ID);

        // Then
        assertThat(actualResponse).isPresent().contains(authorisation);
    }

    @Test
    void getAccountConsentAuthorizationById_wrongId_fail() {
        // Given
        when(authorisationService.getAuthorisationById(WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Authorisation> actualResponse = authorizationService.getConsentAuthorizationById(WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getScaApproachServiceType_success() {
        // When
        ScaApproach actualResponse = authorizationService.getScaApproachServiceType();

        // Then
        assertThat(actualResponse).isEqualTo(ScaApproach.EMBEDDED);
    }

    @Test
    void createConsentAuthorization_Success() {
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .psuData(PSU_DATA)
                                                                            .consentId(CONSENT_ID)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .scaStatus(STARTED_XS2A_SCA_STATUS)
                                                                            .build();
        when(xs2aConsentAuthorisationMapper.mapToAuthorisationRequest(AUTHORISATION_ID, SCA_STATUS, PSU_DATA, SCA_APPROACH)).thenReturn(getTestCreateAuthRequest());
        when(consentService.createConsentAuthorisation(CONSENT_ID, getTestCreateAuthRequest()))
            .thenReturn(Optional.of(buildCreateAuthorisationResponse()));
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(consent));

        Optional<CreateConsentAuthorizationResponse> actualResponseOptional = authorizationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);

        assertThat(actualResponseOptional).isPresent();

        CreateConsentAuthorizationResponse actualResponse = actualResponseOptional.get();

        assertThat(actualResponse.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(actualResponse.getAuthorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(actualResponse.getScaStatus()).isEqualTo(STARTED_SCA_STATUS);
    }


    @Test
    void createConsentAuthorization_wrongId_Failure() {
        //Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .psuData(PSU_DATA)
                                                                            .consentId(WRONG_CONSENT_ID)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .scaStatus(STARTED_XS2A_SCA_STATUS)
                                                                            .build();
        //When
        Optional<CreateConsentAuthorizationResponse> actualResponseOptional = authorizationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);

        // Then
        assertThat(actualResponseOptional).isNotPresent();
    }

    @Test
    void createConsentAuthorizationNoPsuAuthentification_Success() {
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .psuData(PSU_DATA)
                                                                            .consentId(CONSENT_ID)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .scaStatus(STARTED_XS2A_SCA_STATUS)
                                                                            .build();
        when(xs2aConsentAuthorisationMapper.mapToAuthorisationRequest(AUTHORISATION_ID, SCA_STATUS, PSU_DATA, SCA_APPROACH)).thenReturn(getTestCreateAuthRequest());
        when(consentService.createConsentAuthorisation(CONSENT_ID, getTestCreateAuthRequest()))
            .thenReturn(Optional.of(buildCreateAuthorisationResponse()));
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(consent));

        Optional<CreateConsentAuthorizationResponse> actualResponseOptional = authorizationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);

        assertThat(actualResponseOptional).isPresent();

        CreateConsentAuthorizationResponse actualResponse = actualResponseOptional.get();

        assertThat(actualResponse.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(actualResponse.getAuthorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(actualResponse.getScaStatus()).isEqualTo(STARTED_SCA_STATUS);
    }

    @Test
    void createConsentAuthorizationNoPsuIdentification_Success() {

        PsuIdData psuIdData = new PsuIdData(null, null, null, null, null);
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .psuData(psuIdData)
                                                                            .consentId(CONSENT_ID)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .scaStatus(STARTED_XS2A_SCA_STATUS)
                                                                            .build();
        when(xs2aConsentAuthorisationMapper.mapToAuthorisationRequest(AUTHORISATION_ID, STARTED_XS2A_SCA_STATUS, psuIdData, SCA_APPROACH)).thenReturn(getTestCreateAuthRequest());
        when(consentService.createConsentAuthorisation(CONSENT_ID, getTestCreateAuthRequest()))
            .thenReturn(Optional.of(buildCreateAuthorisationResponse()));
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(consent));
        Optional<CreateConsentAuthorizationResponse> actualResponseOptional = authorizationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);

        assertThat(actualResponseOptional).isPresent();

        CreateConsentAuthorizationResponse actualResponse = actualResponseOptional.get();

        assertThat(actualResponse.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(actualResponse.getAuthorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(actualResponse.getScaStatus()).isEqualTo(STARTED_SCA_STATUS);
    }

    @Test
    void getAuthorisationScaStatus_success() {
        when(consentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actual = authorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actual).isPresent().contains(SCA_STATUS);
    }

    @Test
    void getAuthorisationScaStatus_failure_wrongId() {
        // When
        Optional<ScaStatus> actual = authorizationService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actual).isNotPresent();
    }

    private CreateAuthorisationResponse buildCreateAuthorisationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", null, SCA_APPROACH);
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
