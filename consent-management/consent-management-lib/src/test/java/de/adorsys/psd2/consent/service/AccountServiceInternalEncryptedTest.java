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

import de.adorsys.psd2.consent.api.ais.UpdateTransactionParametersRequest;
import de.adorsys.psd2.consent.api.service.AccountService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceInternalEncryptedTest {

    private static final String ENCRYPTED_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";

    private static final String RESOURCE_ID = "LGCGDC4KTx0tgnpZGYTTr8";

    @InjectMocks
    private AccountServiceInternalEncrypted accountServiceInternalEncrypted;

    @Mock
    private SecurityDataService securityDataService;

    @Mock
    private AccountService accountService;

    @Test
    void saveTransactionParameters_shouldFail() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());
        UpdateTransactionParametersRequest updateTransactionParametersRequest = new UpdateTransactionParametersRequest(5, 1, BookingStatus.BOOKED);

        // When
        boolean result = accountServiceInternalEncrypted.saveTransactionParameters(ENCRYPTED_CONSENT_ID, RESOURCE_ID, updateTransactionParametersRequest);

        // Then
        assertFalse(result);
        verify(accountService, never()).saveTransactionParameters(CONSENT_ID, RESOURCE_ID, updateTransactionParametersRequest);
    }

    @Test
    void saveTransactionParameters_success() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(CONSENT_ID));
        UpdateTransactionParametersRequest updateTransactionParametersRequest = new UpdateTransactionParametersRequest(5, 1, BookingStatus.BOOKED);
        when(accountService.saveTransactionParameters(CONSENT_ID, RESOURCE_ID, updateTransactionParametersRequest)).thenReturn(Boolean.TRUE);

        // When
        boolean result = accountServiceInternalEncrypted.saveTransactionParameters(ENCRYPTED_CONSENT_ID, RESOURCE_ID, updateTransactionParametersRequest);

        // Then
        assertTrue(result);
        verify(accountService, times(1)).saveTransactionParameters(CONSENT_ID, RESOURCE_ID, updateTransactionParametersRequest);
    }
}
