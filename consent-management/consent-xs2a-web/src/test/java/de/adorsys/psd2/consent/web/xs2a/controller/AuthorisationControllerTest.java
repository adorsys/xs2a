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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.authorisation.AuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthorisationControllerTest {
    private static final String PARENT_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final String AUTHORISATION_ID = "6b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_METHOD_ID = "sms";

    @InjectMocks
    private AuthorisationController controller;

    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;

    private MockMvc mockMvc;
    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter())
                      .build();
    }

    @Test
    void createConsentAuthorisation() throws Exception {
        CreateAuthorisationRequest request = jsonReader.getObjectFromFile("json/controller/create-authorisation-request.json", CreateAuthorisationRequest.class);
        when(authorisationServiceEncrypted.createAuthorisation(new AuthorisationParentHolder(AuthorisationType.CONSENT, PARENT_ID), request))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder()
                            .payload(new CreateAuthorisationResponse(AUTHORISATION_ID, null, null, null, null)).build());

        mockMvc.perform(MockMvcRequestBuilders.post(UriComponentsBuilder.fromPath("/api/v1/CONSENT/5c2d5564-367f-4e03-a621-6bef76fa4208/authorisations")
                                                        .buildAndExpand(AuthorisationType.CONSENT.name().toLowerCase(), AUTHORISATION_ID)
                                                        .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(jsonReader.getStringFromFile("json/controller/create-authorisation-request.json")))
            .andExpect(status().is(HttpStatus.CREATED.value()));
    }

    @Test
    void createConsentAuthorisation_hasError_notFoundHttpStatus() throws Exception {
        CreateAuthorisationRequest request = jsonReader.getObjectFromFile("json/controller/create-authorisation-request.json", CreateAuthorisationRequest.class);
        when(authorisationServiceEncrypted.createAuthorisation(new AuthorisationParentHolder(AuthorisationType.CONSENT, PARENT_ID), request))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder()
                            .error(CmsError.TECHNICAL_ERROR).build());

        mockMvc.perform(MockMvcRequestBuilders.post(UriComponentsBuilder.fromPath("/api/v1/CONSENT/5c2d5564-367f-4e03-a621-6bef76fa4208/authorisations")
                                                        .buildAndExpand(AuthorisationType.CONSENT, AUTHORISATION_ID)
                                                        .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(jsonReader.getStringFromFile("json/controller/create-authorisation-request.json")))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void getAuthorisation() throws Exception {
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(new Authorisation()).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}")
                                                       .buildAndExpand(AUTHORISATION_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void getAuthorisation_hasError_notFoundHttpStatus() throws Exception {
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .error(CmsError.TECHNICAL_ERROR).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}")
                                                       .buildAndExpand(AUTHORISATION_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void updateAuthorisation() throws Exception {
        UpdateAuthorisationRequest request = jsonReader.getObjectFromFile("json/controller/update-authorisation-request.json", UpdateAuthorisationRequest.class);
        Authorisation authorisation = new Authorisation();
        authorisation.setParentId(PARENT_ID);

        when(authorisationServiceEncrypted.updateAuthorisation(AUTHORISATION_ID, request))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisation).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}")
                                                       .buildAndExpand(AUTHORISATION_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(jsonReader.getStringFromFile("json/controller/update-authorisation-request.json")))
            .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void updateAuthorisation_notParentId_notFoundHttpStatus() throws Exception {
        UpdateAuthorisationRequest request = jsonReader.getObjectFromFile("json/controller/update-authorisation-request.json", UpdateAuthorisationRequest.class);
        Authorisation authorisation = new Authorisation();

        when(authorisationServiceEncrypted.updateAuthorisation(AUTHORISATION_ID, request))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisation).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}")
                                                       .buildAndExpand(AUTHORISATION_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(jsonReader.getStringFromFile("json/controller/update-authorisation-request.json")))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void updateAuthorisationStatus() throws Exception {
        when(authorisationServiceEncrypted.updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.RECEIVED))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}/status/{status}")
                                                       .buildAndExpand(AUTHORISATION_ID, ScaStatus.RECEIVED)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void updateAuthorisationStatus_notUpdate_notFoundHttpStatus() throws Exception {
        when(authorisationServiceEncrypted.updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.RECEIVED))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(false).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}/status/{status}")
                                                       .buildAndExpand(AUTHORISATION_ID, ScaStatus.RECEIVED)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void getAuthorisationScaStatus() throws Exception {
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, new AuthorisationParentHolder(AuthorisationType.PIS_CREATION, PARENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder()
                            .payload(ScaStatus.FAILED).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/{authorisation-type}/{parent-id}/authorisations/{authorisation-id}/status")
                                                       .buildAndExpand(AuthorisationType.PIS_CREATION, PARENT_ID, AUTHORISATION_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void getAuthorisationScaStatus_hasError_notFoundHttpStatus() throws Exception {
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, new AuthorisationParentHolder(AuthorisationType.PIS_CREATION, PARENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder()
                            .error(CmsError.TECHNICAL_ERROR).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/{authorisation-type}/{parent-id}/authorisations/{authorisation-id}/status")
                                                       .buildAndExpand(AuthorisationType.PIS_CREATION, PARENT_ID, AUTHORISATION_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void getAuthorisationsByParentId() throws Exception {
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(new AuthorisationParentHolder(AuthorisationType.PIS_CREATION, PARENT_ID)))
            .thenReturn(CmsResponse.<List<String>>builder()
                            .payload(Collections.singletonList(AUTHORISATION_ID)).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/{authorisation-type}/{parent-id}/authorisations")
                                                       .buildAndExpand(AuthorisationType.PIS_CREATION, PARENT_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void getAuthorisationsByParentIds_hasError_notFoundHttpStatus() throws Exception {
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(new AuthorisationParentHolder(AuthorisationType.PIS_CREATION, PARENT_ID)))
            .thenReturn(CmsResponse.<List<String>>builder()
                            .error(CmsError.TECHNICAL_ERROR).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/{authorisation-type}/{parent-id}/authorisations")
                                                       .buildAndExpand(AuthorisationType.PIS_CREATION, PARENT_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void isAuthenticationMethodDecoupled() throws Exception {
        when(authorisationServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHORISATION_METHOD_ID))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}")
                                                       .buildAndExpand(AUTHORISATION_ID, AUTHORISATION_METHOD_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void saveAuthenticationMethods() throws Exception {
        List<CmsScaMethod> cmsScaMethods = jsonReader.getListFromFile("json/controller/cms-sca-methods.json", CmsScaMethod.class);

        when(authorisationServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true).build());

        mockMvc.perform(MockMvcRequestBuilders.post(UriComponentsBuilder.fromPath("/api/v1//authorisations/{authorisation-id}/authentication-methods")
                                                        .buildAndExpand(AUTHORISATION_ID, PARENT_ID)
                                                        .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(jsonReader.getStringFromFile("json/controller/cms-sca-methods.json")))
            .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    void saveAuthenticationMethods_notUpdated_notFoundHttpStatus() throws Exception {
        List<CmsScaMethod> cmsScaMethods = jsonReader.getListFromFile("json/controller/cms-sca-methods.json", CmsScaMethod.class);

        when(authorisationServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(false).build());

        mockMvc.perform(MockMvcRequestBuilders.post(UriComponentsBuilder.fromPath("/api/v1//authorisations/{authorisation-id}/authentication-methods")
                                                        .buildAndExpand(AUTHORISATION_ID, PARENT_ID)
                                                        .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(jsonReader.getStringFromFile("json/controller/cms-sca-methods.json")))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void updateScaApproach() throws Exception {
        when(authorisationServiceEncrypted.updateScaApproach(AUTHORISATION_ID, ScaApproach.EMBEDDED))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}/sca-approach/{sca-approach}")
                                                       .buildAndExpand(AUTHORISATION_ID, ScaApproach.EMBEDDED)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void updateScaApproach_notUpdated_notFoundHttpStatus() throws Exception {
        when(authorisationServiceEncrypted.updateScaApproach(AUTHORISATION_ID, ScaApproach.EMBEDDED))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(false).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}/sca-approach/{sca-approach}")
                                                       .buildAndExpand(AUTHORISATION_ID, ScaApproach.EMBEDDED)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void getAuthorisationScaApproach() throws Exception {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(ScaApproach.REDIRECT)).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}/sca-approach")
                                                       .buildAndExpand(AUTHORISATION_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void getAuthorisationScaApproach_hasError_notFoundHttpStatus() throws Exception {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .error(CmsError.TECHNICAL_ERROR).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/authorisations/{authorisation-id}/sca-approach")
                                                       .buildAndExpand(AUTHORISATION_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }
}
