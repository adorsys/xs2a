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

import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsPsuAisControllerTest {
    private static final String CONSENT_ID = "6b94ecdd-fd44-4ffb-bd56-d41ee433a792";
    private static final String AUTHORISATION_ID = "70baa209-6a2f-4cc1-8834-d8a819bf5e9f";
    private static final String PSU_ID = "psu id";
    private static final String PSU_ID_TYPE = "psu id type";
    private static final String PSU_CORPORATE_ID = "psu corporate id";
    private static final String PSU_CORPORATE_ID_TYPE = "psu corporate id type";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, null);
    private static final String INSTANCE_ID = "instance id";
    private static final String SCA_STATUS_RECEIVED = "RECEIVED";
    private static final String NOK_REDIRECT_URI = "https://everything_is_bad.html";
    private static final String METHOD_ID = "SMS";
    private static final String AUTHENTICATION_DATA = "123456";
    private static final AuthenticationDataHolder AUTHENTICATION_DATA_HOLDER = new AuthenticationDataHolder(METHOD_ID, AUTHENTICATION_DATA);

    private static final String INSTANCE_ID_HEADER_NAME = "instance-id";
    private static final String PSU_ID_HEADER_NAME = "psu-id";
    private static final String PSU_ID_TYPE_HEADER_NAME = "psu-id-type";
    private static final String PSU_CORPORATE_ID_HEADER_NAME = "psu-corporate-id";
    private static final String PSU_CORPORATE_ID_TYPE_HEADER_NAME = "psu-corporate-id-type";
    private static final String STATUS_PARAM = "status";
    private static final HttpHeaders PSU_HEADERS = buildPsuHeaders();
    private static final HttpHeaders INSTANCE_ID_HEADERS = buildInstanceIdHeaders();
    private static final byte[] EMPTY_BODY = new byte[0];

    private MockMvc mockMvc;
    private final JsonReader jsonReader = new JsonReader();

    @Mock
    private CmsPsuAisService cmsPsuAisService;

    @InjectMocks
    private CmsPsuAisController cmsPsuAisController;

    @BeforeEach
    void setUp() {
        Xs2aObjectMapper xs2aObjectMapper = new ObjectMapperConfig().xs2aObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(cmsPsuAisController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(xs2aObjectMapper))
                      .build();
    }

    @Test
    void updatePsuDataInConsent_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.updatePsuDataInConsent(PSU_ID_DATA, AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(true);
        String psuIdDataContent = jsonReader.getStringFromFile("json/ais/request/psu-id-data.json");

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/psu-data", CONSENT_ID, AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(psuIdDataContent))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).updatePsuDataInConsent(PSU_ID_DATA, AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void updatePsuDataInConsent_withFalseServiceResponse_shouldReturnBadRequest() throws Exception {
        // Given
        when(cmsPsuAisService.updatePsuDataInConsent(PSU_ID_DATA, AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(false);
        String psuIdDataContent = jsonReader.getStringFromFile("json/ais/request/psu-id-data.json");

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/psu-data", CONSENT_ID, AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(psuIdDataContent))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).updatePsuDataInConsent(PSU_ID_DATA, AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void updatePsuDataInConsent_onExpiredAuthorisationException_shouldReturnNokLink() throws Exception {
        // Given
        when(cmsPsuAisService.updatePsuDataInConsent(PSU_ID_DATA, AUTHORISATION_ID, INSTANCE_ID))
            .thenThrow(new AuthorisationIsExpiredException(NOK_REDIRECT_URI));
        String psuIdDataContent = jsonReader.getStringFromFile("json/ais/request/psu-id-data.json");
        String timeoutResponse = jsonReader.getStringFromFile("json/ais/response/ais-consent-timeout.json");

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/psu-data", CONSENT_ID, AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(psuIdDataContent))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(timeoutResponse));

        // Then
        verify(cmsPsuAisService).updatePsuDataInConsent(PSU_ID_DATA, AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.updateAuthorisationStatus(PSU_ID_DATA, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER))
            .thenReturn(true);
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/ais/request/authentication-data-holder.json");

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).updateAuthorisationStatus(PSU_ID_DATA, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER);
    }

    @Test
    void updateAuthorisationStatus_withValidRequestAndLowercaseScaStatus_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.updateAuthorisationStatus(PSU_ID_DATA, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER))
            .thenReturn(true);
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/ais/request/authentication-data-holder.json");
        String lowercaseScaStatus = SCA_STATUS_RECEIVED.toLowerCase();

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, lowercaseScaStatus)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).updateAuthorisationStatus(PSU_ID_DATA, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER);
    }

    @Test
    void updateAuthorisationStatus_withFalseServiceResponse_shouldReturnBadRequest() throws Exception {
        // Given
        when(cmsPsuAisService.updateAuthorisationStatus(PSU_ID_DATA, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER))
            .thenReturn(false);
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/ais/request/authentication-data-holder.json");

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).updateAuthorisationStatus(PSU_ID_DATA, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER);
    }

    @Test
    void updateAuthorisationStatus_withInvalidScaStatus_shouldReturnBadRequest() throws Exception {
        // Given
        String invalidScaStatus = "invalid SCA status";
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/ais/request/authentication-data-holder.json");

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, invalidScaStatus)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService, never()).updateAuthorisationStatus(any(), anyString(), anyString(), any(), anyString(), any());
    }

    @Test
    void updateAuthorisationStatus_onExpiredAuthorisationException_shouldReturnNokLink() throws Exception {
        // Given
        when(cmsPsuAisService.updateAuthorisationStatus(PSU_ID_DATA, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER))
            .thenThrow(new AuthorisationIsExpiredException(NOK_REDIRECT_URI));
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/ais/request/authentication-data-holder.json");
        String timeoutResponse = jsonReader.getStringFromFile("json/ais/response/ais-consent-timeout.json");

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}", CONSENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(timeoutResponse));

        // Then
        verify(cmsPsuAisService).updateAuthorisationStatus(PSU_ID_DATA, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER);
    }

    @Test
    void confirmConsent_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.confirmConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(true);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/confirm-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));

        // Then
        verify(cmsPsuAisService).confirmConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void confirmConsent_withFalseServiceResponse_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.confirmConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(false);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/confirm-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));

        // Then
        verify(cmsPsuAisService).confirmConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void confirmConsent_onChecksumException_shouldReturnBadRequest() throws Exception {
        // Given
        when(cmsPsuAisService.confirmConsent(CONSENT_ID, INSTANCE_ID))
            .thenThrow(WrongChecksumException.class);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/confirm-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).confirmConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void rejectConsent_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.rejectConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(true);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/reject-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));

        // Then
        verify(cmsPsuAisService).rejectConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void rejectConsent_withFalseServiceResponse_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.rejectConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(false);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/reject-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));

        // Then
        verify(cmsPsuAisService).rejectConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void rejectConsent_onChecksumException_shouldReturnBadRequest() throws Exception {
        // Given
        when(cmsPsuAisService.rejectConsent(CONSENT_ID, INSTANCE_ID))
            .thenThrow(WrongChecksumException.class);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/reject-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).rejectConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void getConsentsForPsu_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        String aisConsentListString = jsonReader.getStringFromFile("json/ais/response/ais-consent-list.json");
        List<CmsAisAccountConsent> cmsAisAccountConsentList = jsonReader.getListFromString(aisConsentListString, CmsAisAccountConsent.class);
        when(cmsPsuAisService.getConsentsForPsuAndAdditionalTppInfo(PSU_ID_DATA, INSTANCE_ID, null, null,
                                                                    null, null, null))
            .thenReturn(cmsAisAccountConsentList);

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/consents")
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(aisConsentListString));

        // Then
        verify(cmsPsuAisService).getConsentsForPsuAndAdditionalTppInfo(PSU_ID_DATA, INSTANCE_ID, null, null, null, null, null);
    }

    @Test
    void getConsentsForPsu_withFalseServiceResponse_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.getConsentsForPsuAndAdditionalTppInfo(PSU_ID_DATA, INSTANCE_ID, null, Arrays.asList("RECEIVED", "VALID"), null, null, null))
            .thenReturn(Collections.emptyList());

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/consents")
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .param(STATUS_PARAM, "RECEIVED,VALID")
        )
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        // Then
        verify(cmsPsuAisService).getConsentsForPsuAndAdditionalTppInfo(PSU_ID_DATA, INSTANCE_ID, null, Arrays.asList("RECEIVED", "VALID"), null, null, null);
    }

    @Test
    void revokeConsent_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.revokeConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(true);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/revoke-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));

        // Then
        verify(cmsPsuAisService).revokeConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void revokeConsent_withFalseServiceResponse_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.revokeConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(false);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/revoke-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));

        // Then
        verify(cmsPsuAisService).revokeConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void revokeConsent_onChecksumException_shouldReturnBadRequest() throws Exception {
        // Given
        when(cmsPsuAisService.revokeConsent(CONSENT_ID, INSTANCE_ID))
            .thenThrow(WrongChecksumException.class);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/revoke-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).revokeConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void authorisePartiallyConsent_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.authorisePartiallyConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(true);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorise-partially-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));

        // Then
        verify(cmsPsuAisService).authorisePartiallyConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void authorisePartiallyConsent_withFalseServiceResponse_shouldReturnOk() throws Exception {
        // Given
        when(cmsPsuAisService.authorisePartiallyConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(false);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorise-partially-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));

        // Then
        verify(cmsPsuAisService).authorisePartiallyConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void authorisePartiallyConsent_onChecksumException_shouldReturnBadRequest() throws Exception {
        // Given
        when(cmsPsuAisService.authorisePartiallyConsent(CONSENT_ID, INSTANCE_ID))
            .thenThrow(WrongChecksumException.class);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/authorise-partially-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).authorisePartiallyConsent(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void getConsentIdByRedirectId_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        String cmsAisConsentResponseJson = jsonReader.getStringFromFile("json/ais/response/cms-ais-consent-response.json");
        CmsAisConsentResponse cmsAisConsentResponse = jsonReader.getObjectFromString(cmsAisConsentResponseJson, CmsAisConsentResponse.class);
        when(cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.of(cmsAisConsentResponse));

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/redirect/{redirect-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsAisConsentResponseJson));

        // Then
        verify(cmsPsuAisService).checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getConsentIdByRedirectId_withEmptyServiceResponse_shouldReturnNotFound() throws Exception {
        // Given
        when(cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/redirect/{redirect-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getConsentIdByRedirectId_onExpiredRedirectUrlException_shouldReturnNokLink() throws Exception {
        // Given
        when(cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID))
            .thenThrow(new RedirectUrlIsExpiredException(NOK_REDIRECT_URI));
        String cmsAisConsentResponseJson = jsonReader.getStringFromFile("json/ais/response/ais-consent-timeout.json");

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/redirect/{redirect-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(cmsAisConsentResponseJson));

        // Then
        verify(cmsPsuAisService).checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getConsentByConsentId_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        String aisConsentString = jsonReader.getStringFromFile("json/ais/response/ais-consent.json");
        CmsAisAccountConsent cmsAisAccountConsent = jsonReader.getObjectFromString(aisConsentString, CmsAisAccountConsent.class);
        when(cmsPsuAisService.getConsent(PSU_ID_DATA, CONSENT_ID, INSTANCE_ID))
            .thenReturn(Optional.of(cmsAisAccountConsent));

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/{consent-id}", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(aisConsentString));

        // Then
        verify(cmsPsuAisService).getConsent(PSU_ID_DATA, CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void getConsentByConsentId_withEmptyServiceResponse_shouldReturnNotFound() throws Exception {
        // Given
        when(cmsPsuAisService.getConsent(PSU_ID_DATA, CONSENT_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/{consent-id}", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).getConsent(PSU_ID_DATA, CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void getAuthorisationByAuthorisationId_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        String cmsPsuAuthorisationJson = jsonReader.getStringFromFile("json/ais/response/cms-psu-authorisation.json");
        CmsPsuAuthorisation cmsPsuAuthorisation = jsonReader.getObjectFromString(cmsPsuAuthorisationJson, CmsPsuAuthorisation.class);
        when(cmsPsuAisService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.of(cmsPsuAuthorisation));

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/authorisation/{authorisation-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsPsuAuthorisationJson));

        // Then
        verify(cmsPsuAisService).getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getAuthorisationByAuthorisationId_withEmptyServiceResponse_shouldReturnBadRequest() throws Exception {
        // Given
        when(cmsPsuAisService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/authorisation/{authorisation-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void putAccountAccessInConsent_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        String cmsAisConsentAccessRequestJson = jsonReader.getStringFromFile("json/ais/request/cms-ais-consent-access-request.json");
        CmsAisConsentAccessRequest cmsAisConsentAccessRequest = jsonReader.getObjectFromString(cmsAisConsentAccessRequestJson, CmsAisConsentAccessRequest.class);
        when(cmsPsuAisService.updateAccountAccessInConsent(CONSENT_ID, cmsAisConsentAccessRequest, INSTANCE_ID))
            .thenReturn(true);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/save-access", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cmsAisConsentAccessRequestJson))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).updateAccountAccessInConsent(CONSENT_ID, cmsAisConsentAccessRequest, INSTANCE_ID);
    }

    @Test
    void putAccountAccessInConsent_withFalseServiceResponse_shouldReturnNotFound() throws Exception {
        // Given
        String cmsAisConsentAccessRequestJson = jsonReader.getStringFromFile("json/ais/request/cms-ais-consent-access-request.json");
        CmsAisConsentAccessRequest cmsAisConsentAccessRequest = jsonReader.getObjectFromString(cmsAisConsentAccessRequestJson, CmsAisConsentAccessRequest.class);
        when(cmsPsuAisService.updateAccountAccessInConsent(CONSENT_ID, cmsAisConsentAccessRequest, INSTANCE_ID))
            .thenReturn(false);

        // When
        mockMvc.perform(put("/psu-api/v1/ais/consent/{consent-id}/save-access", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cmsAisConsentAccessRequestJson))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).updateAccountAccessInConsent(CONSENT_ID, cmsAisConsentAccessRequest, INSTANCE_ID);
    }

    @Test
    void psuDataAuthorisations_withValidRequest_shouldReturnOk() throws Exception {
        // Given
        String cmsAisPsuDataAuthorisationListJson = jsonReader.getStringFromFile("json/ais/response/cms-ais-psu-data-authorisation-list.json");
        CmsAisPsuDataAuthorisation cmsAisPsuDataAuthorisation = new CmsAisPsuDataAuthorisation(PSU_ID_DATA, AUTHORISATION_ID, ScaStatus.RECEIVED);
        when(cmsPsuAisService.getPsuDataAuthorisations(CONSENT_ID, INSTANCE_ID, null, null))
            .thenReturn(Optional.of(Collections.singletonList(cmsAisPsuDataAuthorisation)));

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/{consent-id}/authorisation/psus", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsAisPsuDataAuthorisationListJson));

        // Then
        verify(cmsPsuAisService).getPsuDataAuthorisations(CONSENT_ID, INSTANCE_ID, null, null);
    }

    @Test
    void psuDataAuthorisations_withEmptyServiceResponse_shouldReturnNotFound() throws Exception {
        // Given
        when(cmsPsuAisService.getPsuDataAuthorisations(CONSENT_ID, INSTANCE_ID, null, null))
            .thenReturn(Optional.empty());

        // When
        mockMvc.perform(get("/psu-api/v1/ais/consent/{consent-id}/authorisation/psus", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));

        // Then
        verify(cmsPsuAisService).getPsuDataAuthorisations(CONSENT_ID, INSTANCE_ID, null, null);
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
