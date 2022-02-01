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

import com.fasterxml.jackson.databind.SerializationFeature;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.UpdateAisConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AisConsentControllerTest {
    private static final String ENCRYPTED_CONSENT_ID = "encrypted consent id";
    private static final String ACTION_ENDPOINT = UriComponentsBuilder.fromPath("/api/v1/ais/consent/action").toUriString();
    private static final String UPDATE_ACCOUNT_ACCESS_ENDPOINT = UriComponentsBuilder.fromPath("/api/v1/ais/consent/{encrypted-consent-id}/access")
                                                                     .buildAndExpand(ENCRYPTED_CONSENT_ID)
                                                                     .toUriString();
    private static final JsonReader JSON_READER = new JsonReader();
    private static final AisConsentActionRequest AIS_CONSENT_ACTION_REQUEST = JSON_READER.getObjectFromFile("json/controller/ais-consent-action-request.json", AisConsentActionRequest.class);
    private static final AccountAccess ACCOUNT_ACCESS = JSON_READER.getObjectFromFile("json/controller/account-access.json", AccountAccess.class);

    @InjectMocks
    private AisConsentController aisConsentController;

    @Mock
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aisConsentController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void saveConsentActionLog_Success() throws Exception {
        //Given
        when(aisConsentServiceEncrypted.checkConsentAndSaveActionLog(AIS_CONSENT_ACTION_REQUEST))
            .thenReturn(CmsResponse.<CmsResponse.VoidResponse>builder().build());
        //When
        mockMvc.perform(MockMvcRequestBuilders.post(ACTION_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(JSON_READER.writeValueAsString(AIS_CONSENT_ACTION_REQUEST)))
            //Then
            .andExpect(status().isOk());
    }

    @Test
    void saveConsentActionLog_WrongChecksumException() throws Exception {
        //Given
        when(aisConsentServiceEncrypted.checkConsentAndSaveActionLog(AIS_CONSENT_ACTION_REQUEST))
            .thenThrow(WrongChecksumException.class);
        //When
        mockMvc.perform(MockMvcRequestBuilders.post(ACTION_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(JSON_READER.writeValueAsString(AIS_CONSENT_ACTION_REQUEST)))
            //Then
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(JSON_READER.writeValueAsString(CmsError.CHECKSUM_ERROR)));
    }

    @Test
    void updateAccountAccess_Success() throws Exception {
        //Given
        CmsConsent cmsConsent = JSON_READER.getObjectFromFile("json/controller/cms-consent.json", CmsConsent.class);
        UpdateAisConsentResponse updateAisConsentResponse = new UpdateAisConsentResponse(cmsConsent, null);
        when(aisConsentServiceEncrypted.updateAspspAccountAccess(ENCRYPTED_CONSENT_ID, ACCOUNT_ACCESS))
            .thenReturn(CmsResponse.<CmsConsent>builder().payload(cmsConsent).build());
        //When
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ACCOUNT_ACCESS_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(JSON_READER.writeValueAsString(ACCOUNT_ACCESS)))
            //Then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(JSON_READER.writeValueAsString(updateAisConsentResponse)));
    }

    @Test
    void updateAccountAccess_WrongChecksumException() throws Exception {
        //Given
        when(aisConsentServiceEncrypted.updateAspspAccountAccess(ENCRYPTED_CONSENT_ID, ACCOUNT_ACCESS)).thenThrow(WrongChecksumException.class);
        //When
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ACCOUNT_ACCESS_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(JSON_READER.writeValueAsString(ACCOUNT_ACCESS)))
            //Then
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(JSON_READER.writeValueAsString(CmsError.CHECKSUM_ERROR)));
    }

    @Test
    void updateAccountAccess_logicalError() throws Exception {
        //Given
        CmsResponse<CmsConsent> cmsResponse = CmsResponse.<CmsConsent>builder().error(CmsError.LOGICAL_ERROR).build();
        when(aisConsentServiceEncrypted.updateAspspAccountAccess(ENCRYPTED_CONSENT_ID, ACCOUNT_ACCESS)).thenReturn(cmsResponse);
        //When
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ACCOUNT_ACCESS_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(JSON_READER.writeValueAsString(ACCOUNT_ACCESS)))
            //Then
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(JSON_READER.writeValueAsString(cmsResponse.getError())));
    }

    private Xs2aObjectMapper getXs2aObjectMapper() {
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
        xs2aObjectMapper.findAndRegisterModules();
        xs2aObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return xs2aObjectMapper;
    }
}
