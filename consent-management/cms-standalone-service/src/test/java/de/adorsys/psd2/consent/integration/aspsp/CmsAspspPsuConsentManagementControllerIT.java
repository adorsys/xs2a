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

package de.adorsys.psd2.consent.integration.aspsp;

import de.adorsys.psd2.consent.ConsentManagementStandaloneApp;
import de.adorsys.psd2.consent.config.WebConfig;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.integration.UrlBuilder;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.ConsentSpecification;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"api-integration-test"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = ConsentManagementStandaloneApp.class)
@ContextConfiguration(classes = WebConfig.class)
class CmsAspspPsuConsentManagementControllerIT {

    private static final String INSTANCE_ID = "bank-instance-id";
    private static final String ACCOUNT_ID = "123-DEDE89370400440532013000-EUR";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsentJpaRepository consentJpaRepository;
    @SpyBean
    private ConsentSpecification consentSpecification;

    private final JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders;
    private PsuIdData psuIdData;
    private ConsentEntity consentEntity;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add("account-id", ACCOUNT_ID);
        httpHeaders.add("psu-id", "PSU ID");
        httpHeaders.add("psu-id-type", "PSU ID TYPE");
        httpHeaders.add("psu-corporate-id", "PSU CORPORATE ID");
        httpHeaders.add("psu-corporate-id-type", "PSU CORPORATE ID TYPE");
        httpHeaders.add("PSU-IP-Address", "1.1.1.1");
        httpHeaders.add("instance-id", INSTANCE_ID);

        psuIdData = jsonReader.getObjectFromFile("json/consent/integration/aspsp/psu-id-data.json", PsuIdData.class);

        consentEntity = jsonReader.getObjectFromFile("json/consent/integration/aspsp/consent-entity.json", ConsentEntity.class);
        consentEntity.setData(jsonReader.getBytesFromFile("json/consent/integration/ais/ais-consent-data.json"));
    }

    @Test
    void closeAllConsents() throws Exception {
        given(consentJpaRepository.findAll(any(Specification.class))).willReturn(Collections.singletonList(consentEntity));
        assertNotEquals(ConsentStatus.REVOKED_BY_PSU, consentEntity.getConsentStatus());

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.closeAllConsentsUrl());
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("true"));

        verify(consentSpecification).byPsuIdDataAndAspspAccountIdAndInstanceId(psuIdData, ACCOUNT_ID, INSTANCE_ID);

        assertEquals(ConsentStatus.REVOKED_BY_PSU, consentEntity.getConsentStatus());
    }

    @Test
    void closeAllConsents_badRequest() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.closeAllConsentsUrl());
        httpHeaders.remove("account-id");
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("false"));

        verify(consentSpecification, never()).byPsuIdDataAndAspspAccountIdAndInstanceId(any(PsuIdData.class), anyString(), anyString());
        verify(consentJpaRepository, never()).save(any(ConsentEntity.class));
    }
}
