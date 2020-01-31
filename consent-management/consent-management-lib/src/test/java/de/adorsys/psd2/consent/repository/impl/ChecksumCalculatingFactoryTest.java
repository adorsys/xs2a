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

package de.adorsys.psd2.consent.repository.impl;

import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingService;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingServiceV1;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingServiceV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChecksumCalculatingFactoryTest {
    private static final byte[] CHECKSUM = Base64.getDecoder().decode(getCorrectChecksum());
    private static final byte[] WRONG_CHECKSUM = "wrong checksum in consent".getBytes();

    @InjectMocks
    private ChecksumCalculatingFactory factory;

    @Mock
    private ChecksumCalculatingServiceV1 v001;

    @Mock
    private ChecksumCalculatingServiceV2 v002;

    @BeforeEach
    void init() {
        when(v001.getVersion()).thenReturn("001");
        when(v002.getVersion()).thenReturn("002");
        factory.init();
    }

    @Test
    void getServiceByChecksum_success() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(CHECKSUM);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(v001.getVersion(), actualResult.get().getVersion());
    }

    @Test
    void getServiceByChecksum_emptyChecksum() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(new byte[0]);

        // Then
        assertFalse(actualResult.isPresent());
    }

    @Test
    void getServiceByChecksum_wrongChecksum() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(WRONG_CHECKSUM);

        // Then
        assertFalse(actualResult.isPresent());
    }

    @Test
    void getServiceByChecksum_nullChecksum() {
        // When
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(null);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(v002.getVersion(), actualResult.get().getVersion());
    }

    private static String getCorrectChecksum() {
        return "MDAxXyVfY1ZQa05GWnV0NjdLTTQ0aEdheHUrWXpKZWNLODN2QTRKY0huMDV3M0hGbUI0alhycmR3S1VEcDRhZEo2aTJMK3dDUU9UOGJiWmNSQ1UrRUpWcDJ5d0E9PV8lX003akNvS1JNc1FWd2svcDhRR0JNWjNvL0VBTDRNQUZ0SStrek9qQUVMNlAxeUJIajh4bHlGa0hUb21FUEYyOEFpMEhyTUlEalQ1NGVydWVwT2ZTdmxBPT0=";
    }
}
