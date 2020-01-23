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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.domain.RedirectIdHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectIdServiceTest {
    private static final String AUTHORISATION_ID = "authorisation id";

    @Mock
    private RedirectIdHolder redirectIdHolder;
    @InjectMocks
    private RedirectIdService redirectIdService;

    @Test
    void generateRedirectId_shouldReturnAuthorisationId() {
        // When
        String redirectId = redirectIdService.generateRedirectId(AUTHORISATION_ID);

        // Then
        assertEquals(AUTHORISATION_ID, redirectId);
    }

    @Test
    void generateRedirectId_shouldStoreIdInHolder() {
        // When
        redirectIdService.generateRedirectId(AUTHORISATION_ID);

        // Then
        verify(redirectIdHolder).setRedirectId(eq(AUTHORISATION_ID));
    }

    @Test
    void getRedirectId_shouldReturnIdFromHolder() {
        // Given
        when(redirectIdHolder.getRedirectId()).thenReturn(AUTHORISATION_ID);

        // When
        String redirectId = redirectIdService.getRedirectId();

        // Then
        //noinspection ResultOfMethodCallIgnored
        verify(redirectIdHolder).getRedirectId();
        assertEquals(AUTHORISATION_ID, redirectId);
    }

    @Test
    void getRedirectId_withNoIdInHolder_shouldReturnEmpty() {
        // When
        String redirectId = redirectIdService.getRedirectId();

        // Then
        //noinspection ResultOfMethodCallIgnored
        verify(redirectIdHolder).getRedirectId();
        assertNull(redirectId);
    }
}
