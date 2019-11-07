/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceRemoteTest {

    private static final String URL = "http://ais/consent/transactions/save";
    private static final String CONSENT_ID = "consent ID";
    private static final String RESOURCE_ID = "resource ID";
    private static final int NUMBER_OF_TRANSACTIONS = 5;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AisConsentRemoteUrls remoteAisConsentUrls;

    @InjectMocks
    private AccountServiceRemote accountServiceRemote;

    @Test
    public void saveNumberOfTransactions() {
        // Given
        when(remoteAisConsentUrls.saveNumberOfTransactions()).thenReturn(URL);
        when(restTemplate.exchange(URL, HttpMethod.PUT, new HttpEntity<>(NUMBER_OF_TRANSACTIONS), Boolean.class, CONSENT_ID, RESOURCE_ID))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        // When
        boolean actualResponse = accountServiceRemote.saveNumberOfTransactions(CONSENT_ID, RESOURCE_ID, NUMBER_OF_TRANSACTIONS);

        // Then
        assertTrue(actualResponse);
    }
}
