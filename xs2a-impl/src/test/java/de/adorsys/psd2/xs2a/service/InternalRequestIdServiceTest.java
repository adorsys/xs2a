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

import de.adorsys.psd2.xs2a.domain.InternalRequestIdHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalRequestIdServiceTest {
    private static final UUID INTERNAL_REQUEST_ID = UUID.fromString("b571c834-4eb1-468f-91b0-f5e83589bc22");

    @Mock
    private InternalRequestIdHolder internalRequestIdHolder;
    @InjectMocks
    private InternalRequestIdService internalRequestIdService;

    @Test
    void getInternalRequestId_shouldReturnIdFromHolder() {
        // Given
        when(internalRequestIdHolder.getInternalRequestId()).thenReturn(INTERNAL_REQUEST_ID);

        // When
        UUID actualInternalRequestId = internalRequestIdService.getInternalRequestId();

        // Then
        //noinspection ResultOfMethodCallIgnored
        verify(internalRequestIdHolder).getInternalRequestId();
        assertEquals(INTERNAL_REQUEST_ID, actualInternalRequestId);
    }

    @Test
    void getInternalRequestId_withNullIdInHolder_shouldGenerateId() {
        // When
        UUID actualInternalRequestId = internalRequestIdService.getInternalRequestId();

        // Then
        assertNotNull(actualInternalRequestId);
        verify(internalRequestIdHolder).setInternalRequestId(any(UUID.class));
    }
}
