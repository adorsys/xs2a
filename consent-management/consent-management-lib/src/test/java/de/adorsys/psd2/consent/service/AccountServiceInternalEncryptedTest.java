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
