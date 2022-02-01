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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.domain.RedirectIdHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        verify(redirectIdHolder).setRedirectId(AUTHORISATION_ID);
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
