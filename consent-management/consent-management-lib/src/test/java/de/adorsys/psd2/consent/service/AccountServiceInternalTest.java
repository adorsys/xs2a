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

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceInternalTest {

    private static final String CONSENT_ID = "consent ID";
    private static final String RESOURCE_ID = "resource ID";

    @InjectMocks
    private AccountServiceInternal accountServiceInternal;

    @Mock
    private AisConsentTransactionRepository aisConsentTransactionRepository;

    @Mock
    private AisConsentJpaRepository aisConsentJpaRepository;

    @Test
    public void saveNumberOfTransactions_shouldFail() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(CONSENT_ID)).thenReturn(Optional.empty());

        // When
        boolean result = accountServiceInternal.saveNumberOfTransactions(CONSENT_ID, RESOURCE_ID, 10);

        // Then
        assertFalse(result);
        verify(aisConsentTransactionRepository, never()).save(any(AisConsentTransaction.class));
    }

    @Test
    public void saveNumberOfTransactions_success() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(CONSENT_ID)).thenReturn(Optional.of(new AisConsent()));

        // When
        boolean result = accountServiceInternal.saveNumberOfTransactions(CONSENT_ID, RESOURCE_ID, 10);

        // Then
        assertTrue(result);
        verify(aisConsentTransactionRepository, times(1)).save(any(AisConsentTransaction.class));
    }

}
