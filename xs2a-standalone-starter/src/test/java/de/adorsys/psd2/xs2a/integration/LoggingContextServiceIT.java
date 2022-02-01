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


package de.adorsys.psd2.xs2a.integration;

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.logger.context.RequestInfo;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles({"integration-test", "mock-qwac"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(classes = {
    WebConfig.class,
    Xs2aInterfaceConfig.class
})
class LoggingContextServiceIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String CREATE_CONSENT_REQUEST_JSON_PATH = "/json/account/req/DedicatedConsent.json";
    private static final String ASPSP_PROFILE_SCA_APPROACHES_JSON_PATH = "json/aspsp-profile/sca-approaches.json";
    private static final String ASPSP_PROFILE_SETTINGS_JSON_PATH = "json/aspsp-profile/aspsp-profile.json";
    private static final String ASPSP_PROFILE_BASE_URL = "http://localhost:48080/api/v1";
    private static final String CMS_BASE_URL = "http://localhost:38080/api/v1";

    private final HttpHeaders httpHeadersImplicit = new HttpHeaders();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LoggingContextService loggingContextService;

    @Qualifier("aspspProfileRestTemplate")
    @Autowired
    private RestTemplate aspspProfileRestTemplate;

    @Qualifier("consentRestTemplate")
    @Autowired
    private RestTemplate consentRestTemplate;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void init() {
        httpHeadersImplicit.add("Content-Type", "application/json");
        httpHeadersImplicit.add("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");

        initMockRestServers();

        when(loggingContextService.getRequestInformation()).thenReturn(new RequestInfo(null, null, null));
    }

    @Test
    void loggingContextService_shouldStoreInfoBeforeGetting() throws Exception {
        String requestUrl = UrlBuilder.buildConsentCreation();
        MockHttpServletRequestBuilder requestBuilder = post(requestUrl);
        requestBuilder.headers(httpHeadersImplicit);
        requestBuilder.content(resourceToString(CREATE_CONSENT_REQUEST_JSON_PATH, UTF_8));

        // When
        mockMvc.perform(requestBuilder);

        //Then
        InOrder inOrder = inOrder(loggingContextService);
        inOrder.verify(loggingContextService).storeRequestInformation(any());
        inOrder.verify(loggingContextService, atLeastOnce()).getRequestInformation();
    }

    private void initMockRestServers() {
        MockRestServiceServer aspspProfileMockRestServer = MockRestServiceServer
                                                               .bindTo(aspspProfileRestTemplate)
                                                               .ignoreExpectOrder(true)
                                                               .build();
        aspspProfileMockRestServer
            .expect(ExpectedCount.manyTimes(), requestTo(ASPSP_PROFILE_BASE_URL + "/aspsp-profile/sca-approaches"))
            .andRespond(withSuccess(jsonReader.getStringFromFile(ASPSP_PROFILE_SCA_APPROACHES_JSON_PATH), MediaType.APPLICATION_JSON));
        aspspProfileMockRestServer
            .expect(ExpectedCount.manyTimes(), requestTo(ASPSP_PROFILE_BASE_URL + "/aspsp-profile"))
            .andRespond(withSuccess(jsonReader.getStringFromFile(ASPSP_PROFILE_SETTINGS_JSON_PATH), MediaType.APPLICATION_JSON));

        MockRestServiceServer cmsMockRestServer = MockRestServiceServer
                                                      .bindTo(consentRestTemplate)
                                                      .ignoreExpectOrder(true)
                                                      .build();
        cmsMockRestServer
            .expect(ExpectedCount.manyTimes(), requestTo(CMS_BASE_URL + "/tpp/stop-list"))
            .andRespond(withSuccess("false", MediaType.APPLICATION_JSON));
    }
}
