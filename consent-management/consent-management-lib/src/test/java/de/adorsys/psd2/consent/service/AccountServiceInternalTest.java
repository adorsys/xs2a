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
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceInternalTest {

    private static final String CONSENT_ID = "fa6e687b-1ac9-4b1a-9c74-357c35c82ba1";
    private static final String RESOURCE_ID = "LGCGDC4KTx0tgnpZGYTTr8";

    @InjectMocks
    private AccountServiceInternal accountServiceInternal;

    @Mock
    private AisConsentTransactionRepository aisConsentTransactionRepository;

    @Mock
    private ConsentJpaRepository consentJpaRepository;

    @Test
    void saveNumberOfTransactions_shouldFail() {
        // Given
        when(consentJpaRepository.findByExternalId(CONSENT_ID)).thenReturn(Optional.empty());
        UpdateTransactionParametersRequest updateTransactionParametersRequest = new UpdateTransactionParametersRequest(5, 1, BookingStatus.BOOKED);

        // When
        boolean result = accountServiceInternal.saveTransactionParameters(CONSENT_ID, RESOURCE_ID, updateTransactionParametersRequest);

        // Then
        assertFalse(result);
        verify(aisConsentTransactionRepository, never()).save(any(AisConsentTransaction.class));
    }

    @Test
    void saveNumberOfTransactions_success() {
        // Given
        when(consentJpaRepository.findByExternalId(CONSENT_ID)).thenReturn(Optional.of(new ConsentEntity()));
        UpdateTransactionParametersRequest updateTransactionParametersRequest = new UpdateTransactionParametersRequest(5, 1, BookingStatus.BOOKED);

        // When
        boolean result = accountServiceInternal.saveTransactionParameters(CONSENT_ID, RESOURCE_ID, updateTransactionParametersRequest);

        // Then
        assertTrue(result);
        verify(aisConsentTransactionRepository, times(1)).save(any(AisConsentTransaction.class));
    }
}
