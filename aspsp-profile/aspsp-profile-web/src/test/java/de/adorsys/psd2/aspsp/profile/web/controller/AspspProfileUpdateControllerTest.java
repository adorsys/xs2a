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

package de.adorsys.psd2.aspsp.profile.web.controller;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileUpdateService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AspspProfileUpdateControllerTest {
    @Mock
    private AspspProfileUpdateService aspspProfileUpdateService;

    @InjectMocks
    private AspspProfileUpdateController aspspProfileUpdateController;

    @ParameterizedTest
    @ValueSource(strings = {"redirect", "REDIRECT", " REDIRECT "})
    void updateScaApproach(String requestedScaApproach) {
        // Given
        ScaApproach scaApproach = ScaApproach.REDIRECT;

        // When
        ResponseEntity<Void> actualResponse = aspspProfileUpdateController.updateScaApproach(Collections.singletonList(requestedScaApproach), "");

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());

        verify(aspspProfileUpdateService).updateScaApproaches(Collections.singletonList(scaApproach), "");
    }

    @Test
    void updateScaApproach_withInvalidApproach_shouldThrowException() {
        // Given
        String requestedScaApproach = "invalid value";
        List<String> approaches = Collections.singletonList(requestedScaApproach);

        // When
        assertThrows(
            IllegalArgumentException.class,
            () -> aspspProfileUpdateController.updateScaApproach(approaches, "")
        );

        verify(aspspProfileUpdateService, never()).updateScaApproaches(anyList(), eq(""));
    }

    @Test
    void updateAspspSettings() {
        // Given
        AspspSettings aspspSettings = AspspSettingsBuilder.buildAspspSettings();

        // When
        ResponseEntity<Void> actualResponse = aspspProfileUpdateController.updateAspspSettings(aspspSettings, "");

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());

        verify(aspspProfileUpdateService).updateAspspSettings(aspspSettings, "");
    }
}
