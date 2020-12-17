/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.integration.aspsp;

import de.adorsys.psd2.consent.ConsentManagementStandaloneApp;
import de.adorsys.psd2.consent.config.WebConfig;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.integration.UrlBuilder;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.TERMINATED_BY_ASPSP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"api-integration-test"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = ConsentManagementStandaloneApp.class)
@ContextConfiguration(classes = WebConfig.class)
class CmsAspspPiisControllerIT {

    private static final String TPP_AUTHORISATION_NUMBER = "PSDDE-FAKENCA-87B2AC";
    private static final String CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String INSTANCE_ID = "bank-instance-id";
    private static final Long ID = 111L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsentJpaRepository consentJpaRepository;
    @MockBean
    private TppInfoRepository tppInfoRepository;

    @SpyBean
    private PiisConsentEntitySpecification piisConsentEntitySpecification;

    private final JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders;
    private PsuIdData psuIdData;
    private AccountReference accountReference;
    private ConsentEntity consentEntity;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add("psu-id", "PSU ID");
        httpHeaders.add("psu-id-type", "PSU ID TYPE");
        httpHeaders.add("psu-corporate-id", "PSU CORPORATE ID");
        httpHeaders.add("psu-corporate-id-type", "PSU CORPORATE ID TYPE");
        httpHeaders.add("PSU-IP-Address", "1.1.1.1");
        httpHeaders.add("instance-id", INSTANCE_ID);

        psuIdData = jsonReader.getObjectFromFile("json/consent/integration/aspsp/psu-id-data.json", PsuIdData.class);
        accountReference = jsonReader.getObjectFromFile("json/consent/integration/aspsp/account-reference.json", AccountReference.class);

        consentEntity = jsonReader.getObjectFromFile("json/consent/integration/aspsp/consent-entity.json", ConsentEntity.class);
        consentEntity.setData(jsonReader.getBytesFromFile("json/consent/integration/aspsp/piis-consent-data.json"));
    }

    @Test
    void createConsent() throws Exception {
        given(consentJpaRepository.findAll(any(Specification.class))).willReturn(Collections.emptyList());
        given(tppInfoRepository.findByAuthorisationNumber(TPP_AUTHORISATION_NUMBER)).willReturn(Optional.empty());

        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setId(ID);
        consentEntity.setExternalId(CONSENT_ID);
        given(consentJpaRepository.save(any(ConsentEntity.class))).willReturn(consentEntity);

        MockHttpServletRequestBuilder requestBuilder = post(UrlBuilder.createPiisConsentUrl())
                                                           .content(jsonReader.getStringFromFile("json/consent/integration/aspsp/create-piis-consent-request.json"));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/aspsp/expect/create-piis-consent-response.json")));

        verify(piisConsentEntitySpecification).byPsuIdDataAndAuthorisationNumberAndAccountReferenceAndInstanceId(psuIdData, TPP_AUTHORISATION_NUMBER, accountReference, INSTANCE_ID);
    }

    @Test
    void getConsentsForPsu() throws Exception {
        given(consentJpaRepository.findAll(any(Specification.class), any(Pageable.class)))
            .willReturn(new PageImpl<>(Collections.singletonList(consentEntity), PageRequest.of(0, 20), 1));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getPiisConsentsByPsuUrl());
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/aspsp/expect/cms-piis-account-consent.json")));

        verify(piisConsentEntitySpecification).byPsuDataAndInstanceId(psuIdData, INSTANCE_ID);
    }

    @Test
    void terminateConsent() throws Exception {
        assertNotEquals(TERMINATED_BY_ASPSP, consentEntity.getConsentStatus());
        given(consentJpaRepository.findOne(any(Specification.class))).willReturn(Optional.of(consentEntity));

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.getPiisTerminateConsentUrl(CONSENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("true"));

        verify(piisConsentEntitySpecification).byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID);
        assertEquals(TERMINATED_BY_ASPSP, consentEntity.getConsentStatus());
    }
}
