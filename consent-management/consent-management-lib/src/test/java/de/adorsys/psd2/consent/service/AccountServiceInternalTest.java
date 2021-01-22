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
