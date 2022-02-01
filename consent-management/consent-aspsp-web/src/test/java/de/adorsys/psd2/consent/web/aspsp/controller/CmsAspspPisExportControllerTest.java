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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.CmsAmount;
import de.adorsys.psd2.consent.api.pis.CmsBasePaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsSinglePayment;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService;
import de.adorsys.psd2.consent.web.aspsp.config.ObjectMapperTestConfig;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsAspspPisExportControllerTest {
    private final LocalDate START_DATE = LocalDate.of(2019, 2, 25);
    private final LocalDate END_DATE = LocalDate.of(2020, 7, 22);
    private final String INSTANCE_ID = "UNDEFINED";
    private final String LIST_OF_PIIS_CONSENTS_PATH = "json/pis/list-pis-payment.json";
    private static final Integer PAGE_INDEX = 0;
    private static final Integer ITEMS_PER_PAGE = 20;

    private MockMvc mockMvc;
    private final JsonReader jsonReader = new JsonReader();
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private PsuIdData psuIdData;
    private Collection<CmsBasePaymentResponse> cmsPayments;

    @InjectMocks
    private CmsAspspPisExportController cmsAspspPisExportController;

    @Mock
    private CmsAspspPisExportService cmsAspspPisExportService;

    @BeforeEach
    void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();

        psuIdData = jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);
        CmsBasePaymentResponse cmsPayment = getCmsPayment();
        cmsPayments = Collections.singletonList(cmsPayment);

        httpHeaders.add("psu-id", "marion.mueller");
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Start-Date", START_DATE.toString());
        httpHeaders.add("End-Date", END_DATE.toString());
        httpHeaders.add("instance-id", INSTANCE_ID);

        mockMvc = MockMvcBuilders.standaloneSetup(cmsAspspPisExportController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void getPaymentsByTpp() throws Exception {
        String tppId = "PSDDE-FAKENCA-87B2AC";
        when(cmsAspspPisExportService.exportPaymentsByTpp(tppId, START_DATE, END_DATE, psuIdData, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(new PageData<>(cmsPayments, 0, 20, cmsPayments.size()));

        String EXPORT_PIS_CONSENT_BY_TPP = "/aspsp-api/v1/pis/payments/tpp/PSDDE-FAKENCA-87B2AC";
        mockMvc.perform(get(EXPORT_PIS_CONSENT_BY_TPP)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPisExportService, times(1)).exportPaymentsByTpp(tppId, START_DATE, END_DATE, psuIdData, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);
    }

    @Test
    void getPaymentsByPsu() throws Exception {
        when(cmsAspspPisExportService.exportPaymentsByPsu(psuIdData, START_DATE, END_DATE, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(new PageData<>(cmsPayments, 0, 20, cmsPayments.size()));

        mockMvc.perform(get("/aspsp-api/v1/pis/payments/psu")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPisExportService, times(1)).exportPaymentsByPsu(psuIdData, START_DATE, END_DATE, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);
    }

    @Test
    void getPaymentsByAccountId() throws Exception {
        String accountId = "account_id";
        when(cmsAspspPisExportService.exportPaymentsByAccountId("account_id", START_DATE, END_DATE, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(new PageData<>(cmsPayments, 0, 20, cmsPayments.size()));

        String EXPORT_PIS_CONSENT_BY_ACCOUNT = "/aspsp-api/v1/pis/payments/account/account_id";
        mockMvc.perform(get(EXPORT_PIS_CONSENT_BY_ACCOUNT)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPisExportService, times(1)).exportPaymentsByAccountId(accountId, START_DATE, END_DATE, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);
    }

    private CmsBasePaymentResponse getCmsPayment() {
        String paymentProduct = "paymentProduct";
        CmsSinglePayment result = new CmsSinglePayment(paymentProduct);
        result.setEndToEndIdentification("RI-1234567890");
        result.setInstructedAmount(new CmsAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(1000)));
        result.setCreditorAgent("agent");
        result.setCreditorName("name");
        result.setCreditorAddress(new CmsAddress());
        result.setRemittanceInformationUnstructured("remittanceInformationUnstructured");
        return result;
    }
}
