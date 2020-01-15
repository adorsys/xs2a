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


package de.adorsys.psd2.xs2a.integration;

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.logger.context.RequestInfo;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles({"integration-test", "mock-qwac"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(classes = {
    WebConfig.class,
    Xs2aInterfaceConfig.class
})
public class LoggingContextServiceIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String CREATE_CONSENT_REQUEST_JSON_PATH = "/json/account/req/DedicatedConsent.json";

    private HttpHeaders httpHeadersImplicit = new HttpHeaders();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LoggingContextService loggingContextService;

    @Before
    public void init() {
        httpHeadersImplicit.add("Content-Type", "application/json");
        httpHeadersImplicit.add("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");

        when(loggingContextService.getRequestInformation()).thenReturn(new RequestInfo(null, null));
    }

    @Test
    public void loggingContextService_shouldStoreInfoBeforeGetting() throws Exception {
        String requestUrl = UrlBuilder.buildConsentCreation();
        MockHttpServletRequestBuilder requestBuilder = post(requestUrl);
        requestBuilder.headers(httpHeadersImplicit);
        requestBuilder.content(resourceToString(CREATE_CONSENT_REQUEST_JSON_PATH, UTF_8));

        // When
        mockMvc.perform(requestBuilder);

        //Then
        InOrder inOrder = inOrder(loggingContextService);
        inOrder.verify(loggingContextService).storeRequestInformation(any());
        inOrder.verify(loggingContextService).getRequestInformation();
    }
}
