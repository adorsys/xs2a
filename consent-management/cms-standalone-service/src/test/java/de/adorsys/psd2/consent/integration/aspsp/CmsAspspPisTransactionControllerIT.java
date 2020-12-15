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
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.integration.UrlBuilder;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.PisCommonPaymentDataSpecification;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"api-integration-test"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = ConsentManagementStandaloneApp.class)
@ContextConfiguration(classes = WebConfig.class)
class CmsAspspPisTransactionControllerIT {

    private static final String INSTANCE_ID = "bank-instance-id";
    private static final String PAYMENT_ID = "cea9dda3-5154-420d-b1a7-6b4798fccb4b";
    private static final String STATUS = TransactionStatus.PATC.name();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @SpyBean
    private PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;

    private final JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders;
    private PisCommonPaymentData pisCommonPaymentData;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add("instance-id", INSTANCE_ID);

        pisCommonPaymentData = jsonReader.getObjectFromFile("json/consent/integration/aspsp/common-payment-data.json", PisCommonPaymentData.class);
        pisCommonPaymentData.getPayments().forEach(p -> p.setPaymentData(pisCommonPaymentData));
    }

    @Test
    void updatePaymentStatus() throws Exception {
        assertFalse(pisCommonPaymentData.isMultilevelScaRequired());

        given(pisCommonPaymentDataRepository.findOne(any(Specification.class))).willReturn(Optional.of(pisCommonPaymentData));
        given(pisCommonPaymentDataRepository.save(pisCommonPaymentData)).willReturn(pisCommonPaymentData);

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.updatePaymentStatusUrl(PAYMENT_ID, STATUS));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(pisCommonPaymentDataSpecification).byPaymentIdAndInstanceId(PAYMENT_ID, INSTANCE_ID);
        verify(pisCommonPaymentDataRepository).save(pisCommonPaymentData);

        assertTrue(pisCommonPaymentData.isMultilevelScaRequired());
    }
}
