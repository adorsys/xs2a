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
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"api-integration-test"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = ConsentManagementStandaloneApp.class)
@ContextConfiguration(classes = WebConfig.class)
class CmsAspspAisExportControllerIT {

    private static final String TPP_AUTHORISATION_NUMBER = "12345987";
    private static final String START_DATE = "2010-01-01";
    private static final String END_DATE = "2030-01-01";
    private static final String INSTANCE_ID = "bank-instance-id";
    private static final String ACCOUNT_ID = "123-DEDE89370400440532013000-EUR";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsentJpaRepository consentJpaRepository;
    @MockBean
    private AuthorisationRepository authorisationRepository;
    @MockBean
    private AisConsentUsageRepository aisConsentUsageRepository;
    @SpyBean
    private AisConsentSpecification aisConsentSpecification;

    private final JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders;
    private PsuIdData psuIdData;
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
        httpHeaders.add("start-date", START_DATE);
        httpHeaders.add("end-date", END_DATE);
        httpHeaders.add("instance-id", INSTANCE_ID);

        psuIdData = jsonReader.getObjectFromFile("json/consent/integration/aspsp/psu-id-data.json", PsuIdData.class);

        consentEntity = jsonReader.getObjectFromFile("json/consent/integration/aspsp/consent-entity.json", ConsentEntity.class);
        consentEntity.setData(jsonReader.getBytesFromFile("json/consent/integration/ais/ais-consent-data.json"));

        given(consentJpaRepository.findAll(any(Specification.class), any(Pageable.class)))
            .willReturn(new PageImpl<>(Collections.singletonList(consentEntity), PageRequest.of(0, 20), 1));
        given(authorisationRepository.findAllByParentExternalIdAndType(consentEntity.getExternalId(), AuthorisationType.CONSENT))
            .willReturn(Collections.emptyList());
        given(aisConsentUsageRepository.findReadByConsentAndUsageDate(eq(consentEntity), any(LocalDate.class)))
            .willReturn(Collections.emptyList());
    }

    @Test
    void getConsentsByTpp() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getConsentsByTppUrl(TPP_AUTHORISATION_NUMBER));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/aspsp/expect/cms-ais-account-consent.json")));

        verify(aisConsentSpecification).byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, LocalDate.parse(START_DATE), LocalDate.parse(END_DATE), psuIdData, INSTANCE_ID);
    }

    @Test
    void getConsentsByPsu() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getConsentsByPsuUrl());
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/aspsp/expect/cms-ais-account-consent.json")));

        verify(aisConsentSpecification).byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, LocalDate.parse(START_DATE), LocalDate.parse(END_DATE), INSTANCE_ID);
    }

    @Test
    void getConsentsByAccount() throws Exception {
        ZoneOffset currentOffset = OffsetDateTime.now().getOffset();
        given(consentJpaRepository.findAllWithPagination(Collections.singleton(ConsentType.AIS.getName()), ACCOUNT_ID,
                                                         OffsetDateTime.of(LocalDate.parse(START_DATE), LocalTime.MIN, currentOffset),
                                                         OffsetDateTime.of(LocalDate.parse(END_DATE), LocalTime.MAX, currentOffset),
                                                         INSTANCE_ID, PageRequest.of(0, 20)))
            .willReturn(new PageImpl<>(Collections.singletonList(consentEntity), PageRequest.of(0, 20), 1));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getConsentsByAccountUrl(ACCOUNT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/aspsp/expect/cms-ais-account-consent.json")));
    }
}
