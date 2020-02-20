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

package de.adorsys.psd2.consent.service.sha;


import de.adorsys.psd2.consent.service.sha.v3.AisChecksumCalculatingServiceV3;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChecksumCalculatingFactoryTest {
    private static final byte[] CHECKSUM_AIS_V3 = getCorrectChecksum().getBytes();
    private static final byte[] WRONG_CHECKSUM = "wrong checksum in consent".getBytes();
    private static final ConsentType AIS_TYPE = ConsentType.AIS;

    @InjectMocks
    private ChecksumCalculatingFactory factory;

    @Mock
    private AisChecksumCalculatingServiceV3 aisV3;

    @Mock
    private NoProcessingChecksumService noProcessingChecksumService;

    @BeforeEach
    void init() {
        when(aisV3.getVersion()).thenReturn("003");
        factory.init();
    }

    @Test
    void getServiceByChecksum_ais_v3_success() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(CHECKSUM_AIS_V3, AIS_TYPE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(aisV3.getVersion(), actualResult.get().getVersion());
    }

    @Test
    void getServiceByChecksum_ais_noProcessingV001_success() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(getCorrectChecksumV001().getBytes(), AIS_TYPE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(noProcessingChecksumService.getVersion(), actualResult.get().getVersion());
    }

    @Test
    void getServiceByChecksum_ais_noProcessingV002_success() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(getCorrectChecksumV002().getBytes(), AIS_TYPE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(noProcessingChecksumService.getVersion(), actualResult.get().getVersion());
    }

    @Test
    void getServiceByChecksum_ais_v3_null() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(null, null);

        // Then
        assertFalse(actualResult.isPresent());
    }

    @Test
    void getServiceByChecksum_emptyChecksum() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(new byte[0], AIS_TYPE);

        // Then
        assertFalse(actualResult.isPresent());
    }

    @Test
    void getServiceByChecksum_wrongChecksum() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(WRONG_CHECKSUM, AIS_TYPE);

        // Then
        assertFalse(actualResult.isPresent());
    }

    @Test
    void getServiceByChecksum_nullChecksum() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(null, AIS_TYPE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(aisV3.getVersion(), actualResult.get().getVersion());
    }

    private static String getCorrectChecksum() {
        return "003_%_dsuFMYCrZd1YWY7+3/zF7mgrO0PFjhkHn9foi2ylWZOzCWRaUBXNBXkllfmnQ8JXLFEZk3Ta7l+jbdRHHkYT0Q==_%_eyJpYmFuIjoidDg2OTRsdXd1RUkvQTRQM1NvYkh5c0NhMVRqdjJFbEk4cXltWjkwK3duN2o4cXdMcnBOck5VQWFpbWF2RlZ6OE0vZEhFbUlsbzZJNEZ5VGpaNUdIU3c9PSIsIm1hc2tlZFBhbiI6Ild6TG9rYjM1cXFaMElkcVdFZ09PSEtDSEtFMVg1dDY1amxQMURRREJ1UkQya2VJUDVrYmhUMFRKQ3YwWFQ0Sk9ueGxkYWljTzY2Tk9ZcFBsY1JhdmhnPT0ifQ==";
    }

    private static String getCorrectChecksumV001() {
        return "001_%_old_deprecated_data";
    }

    private static String getCorrectChecksumV002() {
        return "002_%_old_deprecated_data";
    }
}
