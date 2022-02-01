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

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aConsentServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String REDIRECT_URI = "request/redirect_uri";
    private static final String NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final CreateAuthorisationRequest AIS_CONSENT_AUTHORISATION_REQUEST = buildAisConsentAuthorisationRequest();

    @InjectMocks
    private Xs2aConsentService xs2aConsentService;

    @Mock
    private Xs2aAuthorisationService authorisationService;
    @Mock
    private Xs2aConsentAuthorisationMapper aisConsentAuthorisationMapper;
    @Mock
    private RequestProviderService requestProviderService;

    @Test
    void createAisConsentAuthorization_success() {
        // Given
        when(authorisationService.createAuthorisation(AIS_CONSENT_AUTHORISATION_REQUEST, CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(Optional.of(buildCreateAisConsentAuthorizationResponse()));

        // When
        Optional<CreateAuthorisationResponse> actualResponse = xs2aConsentService.createConsentAuthorisation(CONSENT_ID, AIS_CONSENT_AUTHORISATION_REQUEST);

        // Then
        assertThat(actualResponse).isPresent().contains(buildCreateAisConsentAuthorizationResponse());
    }

    @Test
    void createAisConsentAuthorization_false() {
        // Given
        CreateAuthorisationRequest request = new CreateAuthorisationRequest();
        when(authorisationService.createAuthorisation(request, CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(Optional.empty());

        // When
        Optional<CreateAuthorisationResponse> actualResponse = xs2aConsentService.createConsentAuthorisation(CONSENT_ID, request);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getAuthorisationScaStatus() {
        xs2aConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        verify(authorisationService, times(1)).getAuthorisationScaStatus(AUTHORISATION_ID, CONSENT_ID, AuthorisationType.CONSENT);
    }

    private static CreateAuthorisationRequest buildAisConsentAuthorisationRequest() {
        CreateAuthorisationRequest consentAuthorization = new CreateAuthorisationRequest();
        consentAuthorization.setPsuData(PSU_DATA);
        consentAuthorization.setScaApproach(SCA_APPROACH);
        return consentAuthorization;
    }

    private static CreateAuthorisationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", null, SCA_APPROACH);
    }
}
