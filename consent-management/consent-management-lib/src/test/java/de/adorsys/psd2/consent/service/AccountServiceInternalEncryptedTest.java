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

import de.adorsys.psd2.consent.api.service.AccountService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceInternalEncryptedTest {

    private static final String ENCRYPTED_CONSENT_ID = "encrypted consent ID";
    private static final String CONSENT_ID = "consent ID";

    private static final String RESOURCE_ID = "resource ID";

    @InjectMocks
    private AccountServiceInternalEncrypted accountServiceInternalEncrypted;

    @Mock
    private SecurityDataService securityDataService;

    @Mock
    private AccountService accountService;

    @Test
    public void saveNumberOfTransactions_shouldFail() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        boolean result = accountServiceInternalEncrypted.saveNumberOfTransactions(ENCRYPTED_CONSENT_ID, RESOURCE_ID, 10);

        // Then
        assertFalse(result);
        verify(accountService, never()).saveNumberOfTransactions(CONSENT_ID, RESOURCE_ID, 10);
    }

    @Test
    public void saveNumberOfTransactions_success() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(CONSENT_ID));
        when(accountService.saveNumberOfTransactions(CONSENT_ID, RESOURCE_ID, 10)). thenReturn(Boolean.TRUE);

        // When
        boolean result = accountServiceInternalEncrypted.saveNumberOfTransactions(ENCRYPTED_CONSENT_ID, RESOURCE_ID, 10);

        // Then
        assertTrue(result);
        verify(accountService, times(1)).saveNumberOfTransactions(CONSENT_ID, RESOURCE_ID, 10);
    }
}
