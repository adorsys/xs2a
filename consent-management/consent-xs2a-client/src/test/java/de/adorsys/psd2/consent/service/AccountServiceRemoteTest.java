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
