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
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.integration.UrlBuilder;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.PisCommonPaymentDataSpecification;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
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
class CmsAspspPisExportControllerIT {

    private static final String TPP_AUTHORISATION_NUMBER = "12345987";
    private static final String START_DATE = "2010-01-01";
    private static final String END_DATE = "2030-01-01";
    private static final String INSTANCE_ID = "bank-instance-id";
    private static final String ACCOUNT_ID = "123-DEDE89370400440532013000-EUR";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @SpyBean
    private PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;

    private final JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders;
    private PsuIdData psuIdData;

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

        PisCommonPaymentData pisCommonPaymentData = jsonReader.getObjectFromFile("json/consent/integration/aspsp/common-payment-data.json", PisCommonPaymentData.class);
        pisCommonPaymentData.getPayments().forEach(p -> p.setPaymentData(pisCommonPaymentData));
        given(pisCommonPaymentDataRepository.findAll(any(Specification.class), any(Pageable.class)))
            .willReturn(new PageImpl<>(Collections.singletonList(pisCommonPaymentData), PageRequest.of(0, 20), 1));
    }

    @Test
    void getPaymentsByTpp() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getPaymentsByTppUrl(TPP_AUTHORISATION_NUMBER));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/aspsp/expect/cms-payment.json")));

        verify(pisCommonPaymentDataSpecification).byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, LocalDate.parse(START_DATE), LocalDate.parse(END_DATE), psuIdData, INSTANCE_ID);
    }

    @Test
    void getPaymentsByPsu() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getPaymentsByPsuUrl());
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/aspsp/expect/cms-payment.json")));

        verify(pisCommonPaymentDataSpecification).byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, LocalDate.parse(START_DATE), LocalDate.parse(END_DATE), INSTANCE_ID);
    }

    @Test
    void getPaymentsByAccountId() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getPaymentsByAccountUrl(ACCOUNT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/aspsp/expect/cms-payment.json")));

        verify(pisCommonPaymentDataSpecification).byAspspAccountIdAndCreationPeriodAndInstanceId(ACCOUNT_ID, LocalDate.parse(START_DATE), LocalDate.parse(END_DATE), INSTANCE_ID);
    }
}
