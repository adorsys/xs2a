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

package de.adorsys.psd2.consent.web.xs2a.controller;

import com.fasterxml.jackson.databind.SerializationFeature;
import de.adorsys.psd2.consent.api.ais.UpdateTransactionParametersRequest;
import de.adorsys.psd2.consent.api.service.AccountServiceEncrypted;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsAccountControllerTest {
    private static final String ENCRYPTED_CONSENT_ID = "encrypted consent id";
    private static final String RESOURCE_ID = "resource id";
    private static final Integer NUMBER_OF_TRANSACTIONS = 3;
    private static final String SAVE_NUMBER_OF_TRANSACTIONS = UriComponentsBuilder.fromPath("/api/v1/ais/consent/{encrypted-consent-id}/{resource-id}")
                                                                  .buildAndExpand(ENCRYPTED_CONSENT_ID, RESOURCE_ID)
                                                                  .toUriString();
    private static final JsonReader JSON_READER = new JsonReader();

    @InjectMocks
    private CmsAccountController cmsAccountController;
    @Mock
    private AccountServiceEncrypted accountServiceEncrypted;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cmsAccountController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void saveNumberOfTransactions_Success() throws Exception {
        //Given
        UpdateTransactionParametersRequest updateTransactionParametersRequest = new UpdateTransactionParametersRequest(5, 1, BookingStatus.BOOKED);
        when(accountServiceEncrypted.saveTransactionParameters(ENCRYPTED_CONSENT_ID, RESOURCE_ID, updateTransactionParametersRequest))
            .thenReturn(true);
        //When
        mockMvc.perform(MockMvcRequestBuilders.put(SAVE_NUMBER_OF_TRANSACTIONS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(JSON_READER.writeValueAsString(updateTransactionParametersRequest)))
            //Then
            .andExpect(status().isOk());
    }

    @Test
    void saveNumberOfTransactions_ConsentNotFound() throws Exception {
        //Given
        UpdateTransactionParametersRequest updateTransactionParametersRequest = new UpdateTransactionParametersRequest(5, 1, BookingStatus.BOOKED);
        when(accountServiceEncrypted.saveTransactionParameters(ENCRYPTED_CONSENT_ID, RESOURCE_ID, updateTransactionParametersRequest))
            .thenReturn(false);
        //When
        mockMvc.perform(MockMvcRequestBuilders.put(SAVE_NUMBER_OF_TRANSACTIONS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(JSON_READER.writeValueAsString(updateTransactionParametersRequest)))
            //Then
            .andExpect(status().isNotFound());
    }

    private Xs2aObjectMapper getXs2aObjectMapper() {
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
        xs2aObjectMapper.findAndRegisterModules();
        xs2aObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return xs2aObjectMapper;
    }
}
