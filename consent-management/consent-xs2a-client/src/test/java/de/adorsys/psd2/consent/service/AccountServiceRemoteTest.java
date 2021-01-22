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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ais.UpdateTransactionParametersRequest;
import de.adorsys.psd2.consent.config.AccountRemoteUrls;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceRemoteTest {
    private static final String URL = "http://ais/consent/transactions/save";
    private static final String CONSENT_ID = "consent ID";
    private static final String RESOURCE_ID = "resource ID";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AccountRemoteUrls accountRemoteUrls;

    @InjectMocks
    private AccountServiceRemote accountServiceRemote;

    @Test
    void saveNumberOfTransactions() {
        // Given
        when(accountRemoteUrls.saveTransactionParameters()).thenReturn(URL);
        UpdateTransactionParametersRequest updateTransactionParametersRequest = new UpdateTransactionParametersRequest(5, 1, BookingStatus.BOOKED);
        when(restTemplate.exchange(URL, HttpMethod.PUT, new HttpEntity<>(updateTransactionParametersRequest), Boolean.class, CONSENT_ID, RESOURCE_ID))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        // When
        boolean actualResponse = accountServiceRemote.saveTransactionParameters(CONSENT_ID, RESOURCE_ID, updateTransactionParametersRequest);

        // Then
        assertTrue(actualResponse);
    }
}
