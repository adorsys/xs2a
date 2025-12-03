/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.profile;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.vop.VopAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.vop.VopxReference;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AspspProfileServiceWrapperVopTest {

    @Mock
    private AspspProfileService aspspProfileService;

    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    private AspspSettings aspspSettings;
    private VopAspspProfileSetting vopSettings;

    @BeforeEach
    void setUp() {
        // Create VOP settings with expected values from bank_profile.yml
        VopxReference vopxReference = new VopxReference(
            "NextGenPSD2 XS2A Framework",
            "https://www.berlin-group.org/nextgenpsd2-downloads",
            "1.3.15_2025-06-25"
        );
        vopSettings = new VopAspspProfileSetting(
                vopxReference,
            List.of("DEDICATED", "INTEGRATED"),
            List.of("synchronous-mode", "asynchronous-mode")
        );

        // Create AspspSettings with VOP configuration
        aspspSettings = mock(AspspSettings.class);
        when(aspspSettings.getVop()).thenReturn(vopSettings);

        // Mock the profile service to return our settings
        when(requestProviderService.getInstanceId()).thenReturn(null);
        when(aspspProfileService.getAspspSettings(null)).thenReturn(aspspSettings);
    }

    @Test
    void getXReferenceApiName_shouldReturnConfiguredValue() {
        // When
        String result = aspspProfileServiceWrapper.getXReferenceApiName();

        // Then
        assertNotNull(result);
        assertEquals("NextGenPSD2 XS2A Framework", result);
        System.out.println("[DEBUG_LOG] X-Reference-API-Name: " + result);
    }

    @Test
    void getXReferenceApiDocument_shouldReturnConfiguredValue() {
        // When
        String result = aspspProfileServiceWrapper.getXReferenceApiDocument();

        // Then
        assertNotNull(result);
        assertEquals("https://www.berlin-group.org/nextgenpsd2-downloads", result);
        System.out.println("[DEBUG_LOG] X-Reference-API-Document: " + result);
    }

    @Test
    void getXReferenceApiVersion_shouldReturnConfiguredValue() {
        // When
        String result = aspspProfileServiceWrapper.getXReferenceApiVersion();

        // Then
        assertNotNull(result);
        assertEquals("1.3.15_2025-06-25", result);
        System.out.println("[DEBUG_LOG] X-Reference-API-Version: " + result);
    }

    @Test
    void getXReferenceApiName_shouldReturnNullWhenVopIsNull() {
        // Given
        when(aspspSettings.getVop()).thenReturn(null);

        // When
        String result = aspspProfileServiceWrapper.getXReferenceApiName();

        // Then
        assertNull(result);
        System.out.println("[DEBUG_LOG] VOP is null - returned null gracefully");
    }

    @Test
    void getXReferenceApiDocument_shouldReturnNullWhenVopIsNull() {
        // Given
        when(aspspSettings.getVop()).thenReturn(null);

        // When
        String result = aspspProfileServiceWrapper.getXReferenceApiDocument();

        // Then
        assertNull(result);
        System.out.println("[DEBUG_LOG] VOP is null - returned null gracefully");
    }

    @Test
    void getXReferenceApiVersion_shouldReturnNullWhenVopIsNull() {
        // Given
        when(aspspSettings.getVop()).thenReturn(null);

        // When
        String result = aspspProfileServiceWrapper.getXReferenceApiVersion();

        // Then
        assertNull(result);
        System.out.println("[DEBUG_LOG] VOP is null - returned null gracefully");
    }
}
