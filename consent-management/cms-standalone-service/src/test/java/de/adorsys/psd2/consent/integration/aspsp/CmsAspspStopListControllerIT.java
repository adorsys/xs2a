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
import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.consent.integration.UrlBuilder;
import de.adorsys.psd2.consent.repository.TppStopListRepository;
import de.adorsys.psd2.xs2a.core.tpp.TppStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"api-integration-test"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = ConsentManagementStandaloneApp.class)
@ContextConfiguration(classes = WebConfig.class)
class CmsAspspStopListControllerIT {

    private static final String TPP_AUTHORISATION_NUMBER = "12345987";
    private static final String INSTANCE_ID = "bank-instance-id";
    private static final Long LOCK_PERIOD = 2000L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TppStopListRepository stopListRepository;

    private final JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add("tpp-authorisation-number", TPP_AUTHORISATION_NUMBER);
        httpHeaders.add("instance-id", INSTANCE_ID);
        httpHeaders.add("lock-period", LOCK_PERIOD.toString());
    }

    @Test
    void getTppStopListRecord() throws Exception {
        TppStopListEntity tppStopListEntity = jsonReader.getObjectFromFile("json/consent/integration/aspsp/tpp-stop-list-entity.json", TppStopListEntity.class);
        given(stopListRepository.findByTppAuthorisationNumberAndInstanceId(TPP_AUTHORISATION_NUMBER, INSTANCE_ID))
            .willReturn(Optional.of(tppStopListEntity));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getTppStopListRecordUrl());
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/aspsp/expect/tpp-stop-list-record.json")));
    }

    @Test
    void blockTpp() throws Exception {
        TppStopListEntity tppStopListEntity = jsonReader.getObjectFromFile("json/consent/integration/aspsp/tpp-stop-list-entity.json", TppStopListEntity.class);
        assertNotEquals(TppStatus.BLOCKED, tppStopListEntity.getStatus());

        given(stopListRepository.findByTppAuthorisationNumberAndInstanceId(TPP_AUTHORISATION_NUMBER, INSTANCE_ID))
            .willReturn(Optional.of(tppStopListEntity));
        given(stopListRepository.save(tppStopListEntity)).willReturn(tppStopListEntity);

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.blockTppUrl());
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("true"));

        assertEquals(TppStatus.BLOCKED, tppStopListEntity.getStatus());
        assertNotNull(tppStopListEntity.getBlockingExpirationTimestamp());
    }

    @Test
    void unblockTpp() throws Exception {
        TppStopListEntity tppStopListEntity = jsonReader.getObjectFromFile("json/consent/integration/aspsp/blocked-tpp-stop-list-entity.json", TppStopListEntity.class);
        assertEquals(TppStatus.BLOCKED, tppStopListEntity.getStatus());

        given(stopListRepository.findByTppAuthorisationNumberAndInstanceId(TPP_AUTHORISATION_NUMBER, INSTANCE_ID))
            .willReturn(Optional.of(tppStopListEntity));
        given(stopListRepository.save(tppStopListEntity)).willReturn(tppStopListEntity);

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.unblockTppUrl());
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("true"));

        assertEquals(TppStatus.ENABLED, tppStopListEntity.getStatus());
        assertNull(tppStopListEntity.getBlockingExpirationTimestamp());
    }
}
