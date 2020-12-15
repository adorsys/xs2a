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

import static org.junit.jupiter.api.Assertions.*;
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
