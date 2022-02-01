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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsConsent;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsResponse;
import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsAuthorisation;
import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsService;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsPsuConfirmationOfFundsControllerTest {
    private static final String CONSENT_ID = "6b94ecdd-fd44-4ffb-bd56-d41ee433a792";
    private static final String AUTHORISATION_ID = "70baa209-6a2f-4cc1-8834-d8a819bf5e9f";
    private static final String INSTANCE_ID = "instance id";
    private static final String PSU_ID = "psu id";
    private static final String PSU_ID_TYPE = "psu id type";
    private static final String PSU_CORPORATE_ID = "psu corporate id";
    private static final String PSU_CORPORATE_ID_TYPE = "psu corporate id type";
    private static final String SCA_STATUS_RECEIVED = "RECEIVED";
    private static final String NOK_REDIRECT_URI = "https://everything_is_bad.html";

    private static final HttpHeaders PSU_HEADERS = buildPsuHeaders();
    private static final HttpHeaders INSTANCE_ID_HEADERS = buildInstanceIdHeaders();
    private static final String INSTANCE_ID_HEADER_NAME = "instance-id";
    private static final String PSU_ID_HEADER_NAME = "psu-id";
    private static final String PSU_ID_TYPE_HEADER_NAME = "psu-id-type";
    private static final String PSU_CORPORATE_ID_HEADER_NAME = "psu-corporate-id";
    private static final String PSU_CORPORATE_ID_TYPE_HEADER_NAME = "psu-corporate-id-type";
    private static final byte[] EMPTY_BODY = new byte[0];

    private MockMvc mockMvc;
    private JsonReader jsonReader = new JsonReader();

    @Mock
    private CmsPsuConfirmationOfFundsService cmsPsuConfirmationOfFundsService;

    @InjectMocks
    private CmsPsuConfirmationOfFundsController cmsPsuConfirmationOfFundsController;

    private PsuIdData psuIdData;
    private AuthenticationDataHolder authenticationDataHolder;

    @BeforeEach
    void setUp() {
        Xs2aObjectMapper xs2aObjectMapper = new ObjectMapperConfig().xs2aObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(cmsPsuConfirmationOfFundsController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(xs2aObjectMapper))
                      .build();
        psuIdData = jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);
        authenticationDataHolder = jsonReader.getObjectFromFile("json/authentication-data-holder.json", AuthenticationDataHolder.class);
    }

    @Test
    void updateAuthorisationStatus_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuConfirmationOfFundsService.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder))
            .thenReturn(true);
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/authentication-data-holder.json");

        // When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuConfirmationOfFundsService).updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder);
    }

    @Test
    void updateAuthorisationStatus_withFalseServiceResponse_shouldReturnBadRequest() throws Exception {
        // Given
        when(cmsPsuConfirmationOfFundsService.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder))
            .thenReturn(false);
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/authentication-data-holder.json");

        // When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuConfirmationOfFundsService).updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder);
    }

    @Test
    void updateAuthorisationStatus_withInvalidScaStatus_shouldReturnBadRequest() throws Exception {
        // Given
        String invalidScaStatus = "invalid SCA status";
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/authentication-data-holder.json");

        // When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, invalidScaStatus)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuConfirmationOfFundsService, never()).updateAuthorisationStatus(any(), anyString(), anyString(), any(), anyString(), any());
    }

    @Test
    void updateAuthorisationStatus_onExpiredAuthorisationException_shouldReturnNokLink() throws Exception {
        // Given
        when(cmsPsuConfirmationOfFundsService.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder))
            .thenThrow(new AuthorisationIsExpiredException(NOK_REDIRECT_URI));
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/authentication-data-holder.json");
        String timeoutResponse = jsonReader.getStringFromFile("json/ais/response/ais-consent-timeout.json");

        // When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(timeoutResponse));

        // Then
        verify(cmsPsuConfirmationOfFundsService).updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder);
    }

    @Test
    void getConsentIdByRedirectId() throws Exception {
        // Given
        String cmsConfirmationOfFundsResponseJson = jsonReader.getStringFromFile("json/piis/response/confirmation-of-funds-response.json");
        CmsConfirmationOfFundsResponse cmsConfirmationOfFundsResponse = jsonReader.getObjectFromString(cmsConfirmationOfFundsResponseJson, CmsConfirmationOfFundsResponse.class);
        when(cmsPsuConfirmationOfFundsService.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.of(cmsConfirmationOfFundsResponse));

        // When
        mockMvc.perform(get("/psu-api/v2/piis/consent/redirect/{redirect-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsConfirmationOfFundsResponseJson));

        // Then
        verify(cmsPsuConfirmationOfFundsService).checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getConsentIdByRedirectId_redirectUrlIsExpired_requestTimeout() throws Exception {
        // Given
        when(cmsPsuConfirmationOfFundsService.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID))
            .thenThrow(new RedirectUrlIsExpiredException(NOK_REDIRECT_URI));
        String timeoutResponse = jsonReader.getStringFromFile("json/piis/response/piis-consent-timeout.json");

        // When
        mockMvc.perform(get("/psu-api/v2/piis/consent/redirect/{redirect-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(timeoutResponse));

        // Then
        verify(cmsPsuConfirmationOfFundsService).checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getConsentIdByRedirectId_notFound() throws Exception {
        // Given
        when(cmsPsuConfirmationOfFundsService.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        // When
        mockMvc.perform(get("/psu-api/v2/piis/consent/redirect/{redirect-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuConfirmationOfFundsService).checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getAuthorisationByAuthorisationId_shouldReturnOk() throws Exception {
        // Given
        String fileName = "json/piis/response/cms-psu-authorisation-piis.json";
        CmsPsuConfirmationOfFundsAuthorisation cmsPsuConfirmationOfFundsAuthorisation = jsonReader.getObjectFromFile(fileName, CmsPsuConfirmationOfFundsAuthorisation.class);
        String content = jsonReader.getStringFromFile(fileName);
        when(cmsPsuConfirmationOfFundsService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.of(cmsPsuConfirmationOfFundsAuthorisation));

        // When
        mockMvc.perform(get("/psu-api/v2/piis/consent/authorisation/{authorisation-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .content(content))
            .andExpect(status().isOk());

        // Then
        verify(cmsPsuConfirmationOfFundsService).getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getAuthorisationByAuthorisationId_shouldReturnBadRequest() throws Exception {
        when(cmsPsuConfirmationOfFundsService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        // When
        mockMvc.perform(get("/psu-api/v2/piis/consent/authorisation/{authorisation-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isBadRequest());

        // Then
        verify(cmsPsuConfirmationOfFundsService).getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getAuthorisationByAuthorisationId_onExpiredAuthorisationException_shouldReturnRequestTimeout() throws Exception {
        // Given
        when(cmsPsuConfirmationOfFundsService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenThrow(new AuthorisationIsExpiredException(NOK_REDIRECT_URI));
        String timeoutResponse = jsonReader.getStringFromFile("json/ais/response/ais-consent-timeout.json");

        // When
        mockMvc.perform(get("/psu-api/v2/piis/consent/authorisation/{authorisation-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(timeoutResponse));

        // Then
        verify(cmsPsuConfirmationOfFundsService).getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID);
    }

    @ParameterizedTest
    @EnumSource(ConsentStatus.class)
    void updateConsentStatus_shouldReturnOk(ConsentStatus consentStatus) throws Exception {
        //Given
        when(cmsPsuConfirmationOfFundsService.updateConsentStatus(CONSENT_ID, consentStatus, INSTANCE_ID))
            .thenReturn(true);
        //When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/status/{status}", CONSENT_ID, consentStatus.toString())
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        // Then
        verify(cmsPsuConfirmationOfFundsService).updateConsentStatus(CONSENT_ID, consentStatus, INSTANCE_ID);
    }

    @Test
    void updateConsentStatus_withInvalidStatus_shouldReturnBadRequest() throws Exception {
        // Given
        String invalidStatus = "invalid status";
        // When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/status/{status}", CONSENT_ID, invalidStatus)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        // Then
        verify(cmsPsuConfirmationOfFundsService, never()).updateConsentStatus(anyString(), any(ConsentStatus.class), anyString());
    }

    @Test
    void updateConsentStatus_consentNotFound_shouldReturnOk() throws Exception {
        //Given
        ConsentStatus consentStatus = ConsentStatus.VALID;
        when(cmsPsuConfirmationOfFundsService.updateConsentStatus(CONSENT_ID, consentStatus, INSTANCE_ID))
            .thenReturn(false);
        //When
        mockMvc.perform(put("/psu-api/v2/piis/consent/{consent-id}/status/{status}", CONSENT_ID, consentStatus)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        // Then
        verify(cmsPsuConfirmationOfFundsService).updateConsentStatus(CONSENT_ID, consentStatus, INSTANCE_ID);
    }

    @Test
    void getConsentByConsentId() throws Exception {
        // Given
        String cmsConfirmationOfFundsConsentJson = jsonReader.getStringFromFile("json/piis/response/cms-confirmation-of-funds-consent.json");
        CmsConfirmationOfFundsConsent cmsConfirmationOfFundsConsent = jsonReader.getObjectFromString(cmsConfirmationOfFundsConsentJson, CmsConfirmationOfFundsConsent.class);
        when(cmsPsuConfirmationOfFundsService.getConsent(psuIdData, CONSENT_ID, INSTANCE_ID))
            .thenReturn(Optional.of(cmsConfirmationOfFundsConsent));

        // When
        mockMvc.perform(get("/psu-api/v2/piis/consent/{consent-id}", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsConfirmationOfFundsConsentJson));

        // Then
        verify(cmsPsuConfirmationOfFundsService).getConsent(psuIdData, CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void getConsentByConsentId_consentIsNotFound() throws Exception {
        // Given
        when(cmsPsuConfirmationOfFundsService.getConsent(psuIdData, CONSENT_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        // When
        mockMvc.perform(get("/psu-api/v2/piis/consent/{consent-id}", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuConfirmationOfFundsService).getConsent(psuIdData, CONSENT_ID, INSTANCE_ID);
    }

    private static HttpHeaders buildInstanceIdHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(INSTANCE_ID_HEADER_NAME, INSTANCE_ID);
        return httpHeaders;
    }

    private static HttpHeaders buildPsuHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(PSU_ID_HEADER_NAME, PSU_ID);
        httpHeaders.add(PSU_ID_TYPE_HEADER_NAME, PSU_ID_TYPE);
        httpHeaders.add(PSU_CORPORATE_ID_HEADER_NAME, PSU_CORPORATE_ID);
        httpHeaders.add(PSU_CORPORATE_ID_TYPE_HEADER_NAME, PSU_CORPORATE_ID_TYPE);
        return httpHeaders;
    }
}
