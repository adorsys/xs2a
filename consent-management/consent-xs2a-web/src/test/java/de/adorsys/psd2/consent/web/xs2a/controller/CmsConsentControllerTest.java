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
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.consent.web.xs2a.config.ObjectMapperTestConfig;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsConsentControllerTest {
    private static final String EXTERNAL_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";

    private static final String CMS_CONSENT_PATH = "json/controller/cms-consent.json";
    private static final String CONSENT_STATUS_RESPONSE_PATH = "json/controller/consent-status-response.json";
    private static final String CMS_CREATE_CONSENT_RESPONSE_PATH = "json/controller/cms-create-consent-response.json";

    @InjectMocks
    private CmsConsentController cmsConsentController;
    @Mock
    private ConsentServiceEncrypted consentServiceEncrypted;

    private MockMvc mockMvc;
    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();
        mockMvc = MockMvcBuilders.standaloneSetup(cmsConsentController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void createConsent_Success() throws Exception {
        when(consentServiceEncrypted.createConsent(any())).thenReturn(CmsResponse.<CmsCreateConsentResponse>builder()
                                                                          .payload(jsonReader.getObjectFromFile(CMS_CREATE_CONSENT_RESPONSE_PATH, CmsCreateConsentResponse.class))
                                                                          .build());

        mockMvc.perform(MockMvcRequestBuilders.post(UriComponentsBuilder.fromPath("/api/v1/consent").build().toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(jsonReader.getStringFromFile(CMS_CONSENT_PATH)))
            .andExpect(status().is(HttpStatus.CREATED.value()));

    }

    @Test
    void createConsent_throwsChecksumError() throws Exception {
        when(consentServiceEncrypted.createConsent(any())).thenThrow(WrongChecksumException.class);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UriComponentsBuilder.fromPath("/api/v1/consent").build().toUriString())
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .content(jsonReader.getStringFromFile(CMS_CONSENT_PATH)));

        resultActions
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
            .andExpect(content().string('"' + CmsError.CHECKSUM_ERROR.toString() + '"'));
    }

    @Test
    void createConsent_returnsNoContentResponse() throws Exception {
        when(consentServiceEncrypted.createConsent(any())).thenReturn(CmsResponse.<CmsCreateConsentResponse>builder().error(CmsError.LOGICAL_ERROR).build());

        mockMvc.perform(MockMvcRequestBuilders.post(UriComponentsBuilder.fromPath("/api/v1/consent").build().toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(jsonReader.getStringFromFile(CMS_CONSENT_PATH)))
            .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    void getConsentStatus_Success() throws Exception {
        CmsResponse<CmsConsent> cmsConsentCmsResponse = CmsResponse.<CmsConsent>builder().payload(jsonReader.getObjectFromFile(CMS_CONSENT_PATH, CmsConsent.class)).build();
        when(consentServiceEncrypted.getConsentById(EXTERNAL_ID)).thenReturn(cmsConsentCmsResponse);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}")
                                                                                     .buildAndExpand(EXTERNAL_ID)
                                                                                     .toUriString())
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .content(EXTERNAL_ID));

        resultActions.andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(CMS_CONSENT_PATH)));
    }

    @Test
    void getConsentStatus_returnsNotFoundResponse() throws Exception {
        when(consentServiceEncrypted.getConsentById(EXTERNAL_ID)).thenReturn(CmsResponse.<CmsConsent>builder().error(CmsError.LOGICAL_ERROR).build());

        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}")
                                                       .buildAndExpand(EXTERNAL_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(EXTERNAL_ID))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void getConsentStatusById_Success() throws Exception {
        when(consentServiceEncrypted.getConsentStatusById(EXTERNAL_ID)).thenReturn(CmsResponse.<ConsentStatus>builder().payload(ConsentStatus.VALID).build());


        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/status")
                                                                                     .buildAndExpand(EXTERNAL_ID)
                                                                                     .toUriString())
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .content(EXTERNAL_ID));

        resultActions.andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(CONSENT_STATUS_RESPONSE_PATH)));

    }

    @Test
    void getConsentStatusById_returnsNotFoundResponse() throws Exception {
        when(consentServiceEncrypted.getConsentStatusById(EXTERNAL_ID)).thenReturn(CmsResponse.<ConsentStatus>builder().error(CmsError.LOGICAL_ERROR).build());


        mockMvc.perform(MockMvcRequestBuilders.get(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/status")
                                                       .buildAndExpand(EXTERNAL_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(EXTERNAL_ID)).andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void updateConsentStatus_success() throws Exception {
        when(consentServiceEncrypted.updateConsentStatusById(EXTERNAL_ID, ConsentStatus.VALID)).thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/status/{status}")
                                                       .buildAndExpand(EXTERNAL_ID, ConsentStatus.VALID.toString())
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void updateConsentStatus_throwsChecksumError() throws Exception {
        when(consentServiceEncrypted.updateConsentStatusById(EXTERNAL_ID, ConsentStatus.VALID)).thenThrow(WrongChecksumException.class);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/status/{status}")
                                                                                     .buildAndExpand(EXTERNAL_ID, ConsentStatus.VALID.toString())
                                                                                     .toUriString())
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE));

        resultActions
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
            .andExpect(content().string('"' + CmsError.CHECKSUM_ERROR.toString() + '"'));
    }

    @Test
    void updateConsentStatus_returnsNotFoundResponse_withError() throws Exception {
        when(consentServiceEncrypted.updateConsentStatusById(EXTERNAL_ID, ConsentStatus.VALID)).thenReturn(CmsResponse.<Boolean>builder().error(CmsError.LOGICAL_ERROR).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/status/{status}")
                                                       .buildAndExpand(EXTERNAL_ID, ConsentStatus.VALID.toString())
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void updateConsentStatus_returnsNotFoundResponse_withFalseBooleanResponse() throws Exception {
        when(consentServiceEncrypted.updateConsentStatusById(EXTERNAL_ID, ConsentStatus.VALID)).thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/status/{status}")
                                                       .buildAndExpand(EXTERNAL_ID, ConsentStatus.VALID.toString())
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_Success() throws Exception {
        when(consentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_ID)).thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        mockMvc.perform(MockMvcRequestBuilders.delete(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/old-consents")
                                                          .buildAndExpand(EXTERNAL_ID)
                                                          .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    void findAndTerminateOldConsents_Success() throws Exception {
        TerminateOldConsentsRequest request = jsonReader.getObjectFromFile("json/controller/terminate-consent-req.json", TerminateOldConsentsRequest.class);
        when(consentServiceEncrypted.findAndTerminateOldConsents(EXTERNAL_ID, request)).thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/old-consents")
                                                       .buildAndExpand(EXTERNAL_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(jsonReader.getStringFromFile("json/controller/terminate-consent-req.json")))
            .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    void updateMultilevelScaRequired_Success() throws Exception {
        when(consentServiceEncrypted.updateMultilevelScaRequired(EXTERNAL_ID, true)).thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/multilevel-sca")
                                                                                     .buildAndExpand(EXTERNAL_ID)
                                                                                     .toUriString())
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .param("multilevel-sca", "true"));

        resultActions.andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().string("true"));
    }

    @Test
    void updateMultilevelScaRequired_throwsChecksumError() throws Exception {
        when(consentServiceEncrypted.updateMultilevelScaRequired(EXTERNAL_ID, true)).thenThrow(WrongChecksumException.class);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/multilevel-sca")
                                                                                     .buildAndExpand(EXTERNAL_ID)
                                                                                     .toUriString())
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .param("multilevel-sca", "true"));

        resultActions
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
            .andExpect(content().string('"' + CmsError.CHECKSUM_ERROR.toString() + '"'));
    }

    @Test
    void updateMultilevelScaRequired_returnsNotFoundResponse_withError() throws Exception {
        when(consentServiceEncrypted.updateMultilevelScaRequired(EXTERNAL_ID, true)).thenReturn(CmsResponse.<Boolean>builder().error(CmsError.LOGICAL_ERROR).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/multilevel-sca")
                                                       .buildAndExpand(EXTERNAL_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .param("multilevel-sca", "true"))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void updateMultilevelScaRequired_returnsNotFoundResponse_withFalseBooleanResponse() throws Exception {
        when(consentServiceEncrypted.updateMultilevelScaRequired(EXTERNAL_ID, true)).thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        mockMvc.perform(MockMvcRequestBuilders.put(UriComponentsBuilder.fromPath("/api/v1/consent/{encrypted-consent-id}/multilevel-sca")
                                                       .buildAndExpand(EXTERNAL_ID)
                                                       .toUriString())
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .param("multilevel-sca", "true"))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }
}
