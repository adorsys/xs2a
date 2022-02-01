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

package de.adorsys.psd2.consent.service.sha;


import de.adorsys.psd2.consent.service.sha.impl.AisChecksumCalculatingServiceV3;
import de.adorsys.psd2.consent.service.sha.impl.AisChecksumCalculatingServiceV4;
import de.adorsys.psd2.consent.service.sha.impl.AisChecksumCalculatingServiceV5;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ChecksumCalculatingFactory.class, AisChecksumCalculatingServiceV3.class,
    AisChecksumCalculatingServiceV4.class, AisChecksumCalculatingServiceV5.class, NoProcessingChecksumService.class})
class ChecksumCalculatingFactoryTest {
    private static final byte[] WRONG_CHECKSUM = "wrong checksum in consent".getBytes();
    private static final ConsentType AIS_TYPE = ConsentType.AIS;

    @Autowired
    private ChecksumCalculatingFactory factory;

    @Autowired
    private AisChecksumCalculatingServiceV3 aisV3;
    @Autowired
    private AisChecksumCalculatingServiceV4 aisV4;
    @Autowired
    private AisChecksumCalculatingServiceV5 aisV5;
    @Autowired
    private NoProcessingChecksumService noProcessingChecksumService;

    @Test
    void getServiceByChecksum_ais_v3_success() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(getCorrectChecksum("003"), AIS_TYPE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(aisV3, actualResult.get());
    }

    @Test
    void getServiceByChecksum_ais_v4_success() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(getCorrectChecksum("004"), AIS_TYPE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(aisV4, actualResult.get());
    }

    @Test
    void getServiceByChecksum_ais_v5_success() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(getCorrectChecksum("005"), AIS_TYPE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(aisV5, actualResult.get());
    }

    @Test
    void getServiceByChecksum_ais_nextVersion_success() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(getCorrectChecksum("006"), AIS_TYPE);

        // Then
        assertFalse(actualResult.isPresent());
    }

    @Test
    void getServiceByChecksum_ais_noProcessingV001_success() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(getCorrectChecksum("001"), AIS_TYPE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(noProcessingChecksumService, actualResult.get());
    }

    @Test
    void getServiceByChecksum_ais_noProcessingV002_success() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(getCorrectChecksum("002"), AIS_TYPE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(noProcessingChecksumService, actualResult.get());
    }

    @Test
    void getServiceByChecksum_nullChecksumAndConsentType() {
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
        assertEquals(aisV5.getVersion(), actualResult.get().getVersion());
    }

    private static byte[] getCorrectChecksum(String version) {
        String checksum = version + "_%_dsuFMYCrZd1YWY7+3/zF7mgrO0PFjhkHn9foi2ylWZOzCWRaUBXNBXkllfmnQ8JXLFEZk3Ta7l+jbdRHHkYT0Q==_%_eyJpYmFuIjoidDg2OTRsdXd1RUkvQTRQM1NvYkh5c0NhMVRqdjJFbEk4cXltWjkwK3duN2o4cXdMcnBOck5VQWFpbWF2RlZ6OE0vZEhFbUlsbzZJNEZ5VGpaNUdIU3c9PSIsIm1hc2tlZFBhbiI6Ild6TG9rYjM1cXFaMElkcVdFZ09PSEtDSEtFMVg1dDY1amxQMURRREJ1UkQya2VJUDVrYmhUMFRKQ3YwWFQ0Sk9ueGxkYWljTzY2Tk9ZcFBsY1JhdmhnPT0ifQ==";
        return checksum.getBytes();
    }
}
