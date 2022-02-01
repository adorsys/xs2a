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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.authorisation.AisAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAuthenticationObjectToCmsScaMethodMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aAuthorisationServiceTest {

    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String AUTHENTICATION_METHOD_ID = "19ff-4b5a-8188";

    @InjectMocks
    private Xs2aAuthorisationService authorisationService;

    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @Spy
    private Xs2aAuthenticationObjectToCmsScaMethodMapper xs2aAuthenticationObjectToCmsScaMethodMapper =
        new Xs2aAuthenticationObjectToCmsScaMethodMapper();

    private JsonReader jsonReader = new JsonReader();

    @Test
    void createAuthorisation() {
        CreateAuthorisationRequest request = jsonReader.getObjectFromFile("json/service/mapper/consent/create-authorisation-request.json", CreateAuthorisationRequest.class);
        CreateAuthorisationResponse response = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/create-pis-authorisation-response.json",
                                                                            CreateAuthorisationResponse.class);

        when(authorisationServiceEncrypted.createAuthorisation(new AisAuthorisationParentHolder(CONSENT_ID), request))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().payload(response).build());

        Optional<CreateAuthorisationResponse> actual = authorisationService.createAuthorisation(request, CONSENT_ID, AuthorisationType.CONSENT);

        assertTrue(actual.isPresent());
        assertEquals(response, actual.get());
    }

    @Test
    void createAuthorisation_hasError() {
        CreateAuthorisationRequest request = jsonReader.getObjectFromFile("json/service/mapper/consent/create-authorisation-request.json", CreateAuthorisationRequest.class);

        when(authorisationServiceEncrypted.createAuthorisation(new AisAuthorisationParentHolder(CONSENT_ID), request))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        Optional<CreateAuthorisationResponse> actual = authorisationService.createAuthorisation(request, CONSENT_ID, AuthorisationType.CONSENT);

        assertTrue(actual.isEmpty());
    }

    @Test
    void getAuthorisationById() {
        Authorisation authorisation = new Authorisation();
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder().payload(authorisation).build());

        Optional<Authorisation> actual = authorisationService.getAuthorisationById(AUTHORISATION_ID);

        assertTrue(actual.isPresent());
        assertEquals(authorisation, actual.get());
    }

    @Test
    void getAuthorisationById_hasError() {
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder().error(CmsError.TECHNICAL_ERROR).build());

        Optional<Authorisation> actual = authorisationService.getAuthorisationById(AUTHORISATION_ID);

        assertTrue(actual.isEmpty());
    }

    @Test
    void updateScaApproach() {
        authorisationService.updateScaApproach(AUTHORISATION_ID, ScaApproach.REDIRECT);
        verify(authorisationServiceEncrypted, times(1)).updateScaApproach(AUTHORISATION_ID, ScaApproach.REDIRECT);
    }

    @Test
    void saveAuthenticationMethods() {
        List<AuthenticationObject> methods = Collections.singletonList(new AuthenticationObject());
        List<CmsScaMethod> cmsScaMethods = xs2aAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(methods);

        when(authorisationServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods))
            .thenReturn(CmsResponse.<Boolean>builder().payload(Boolean.TRUE).build());

        assertTrue(authorisationService.saveAuthenticationMethods(AUTHORISATION_ID, methods));
    }

    @Test
    void saveAuthenticationMethods_isNotSuccessful() {
        List<AuthenticationObject> methods = Collections.singletonList(new AuthenticationObject());
        List<CmsScaMethod> cmsScaMethods = xs2aAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(methods);

        when(authorisationServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods))
            .thenReturn(CmsResponse.<Boolean>builder().payload(Boolean.FALSE).build());

        assertFalse(authorisationService.saveAuthenticationMethods(AUTHORISATION_ID, methods));
    }

    @Test
    void updateAuthorisationStatus() {
        authorisationService.updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.RECEIVED);
        verify(authorisationServiceEncrypted, times(1)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.RECEIVED);
    }

    @Test
    void isAuthenticationMethodDecoupled() {
        when(authorisationServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(Boolean.TRUE).build());

        assertTrue(authorisationService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID));
    }

    @Test
    void isAuthenticationMethodDecoupled_isNotSuccessful() {
        when(authorisationServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(Boolean.FALSE).build());

        assertFalse(authorisationService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID));
    }

    @Test
    void getAuthorisationScaApproach() {
        AuthorisationScaApproachResponse payload = new AuthorisationScaApproachResponse(ScaApproach.REDIRECT);
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().payload(payload).build());

        Optional<AuthorisationScaApproachResponse> actual = authorisationService.getAuthorisationScaApproach(AUTHORISATION_ID);

        assertTrue(actual.isPresent());
        assertEquals(payload, actual.get());
    }

    @Test
    void getAuthorisationScaApproach_hasError() {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        Optional<AuthorisationScaApproachResponse> actual = authorisationService.getAuthorisationScaApproach(AUTHORISATION_ID);

        assertTrue(actual.isEmpty());
    }

    @Test
    void getAuthorisationSubResources() {
        List<String> subResources = Collections.singletonList("subResource1");
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<List<String>>builder().payload(subResources).build());

        Optional<List<String>> actual = authorisationService.getAuthorisationSubResources(CONSENT_ID, AuthorisationType.CONSENT);

        assertTrue(actual.isPresent());
        assertEquals(subResources, actual.get());
    }

    @Test
    void getAuthorisationSubResources_hasError() {
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<List<String>>builder().error(CmsError.TECHNICAL_ERROR).build());

        Optional<List<String>> actual = authorisationService.getAuthorisationSubResources(CONSENT_ID, AuthorisationType.CONSENT);

        assertTrue(actual.isEmpty());
    }

    @Test
    void updateAuthorisation() {
        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        authorisationService.updateAuthorisation(request, AUTHORISATION_ID);
        verify(authorisationServiceEncrypted, times(1)).updateAuthorisation(AUTHORISATION_ID, request);
    }

    @Test
    void getAuthorisationScaStatus() {
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(ScaStatus.PSUIDENTIFIED).build());

        Optional<ScaStatus> actual = authorisationService.getAuthorisationScaStatus(AUTHORISATION_ID, CONSENT_ID, AuthorisationType.CONSENT);

        assertTrue(actual.isPresent());
        assertEquals(ScaStatus.PSUIDENTIFIED, actual.get());
    }

    @Test
    void getAuthorisationScaStatus_hasError() {
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        Optional<ScaStatus> actual = authorisationService.getAuthorisationScaStatus(AUTHORISATION_ID, CONSENT_ID, AuthorisationType.CONSENT);

        assertTrue(actual.isEmpty());
    }
}
