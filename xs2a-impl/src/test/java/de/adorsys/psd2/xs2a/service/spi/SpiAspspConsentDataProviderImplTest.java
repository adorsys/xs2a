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

package de.adorsys.psd2.xs2a.service.spi;

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpiAspspConsentDataProviderImplTest {
    private static final String SOME_CONSENT_ID = "someConsentId";
    private static final byte[] SOME_DATA = "some data".getBytes();
    private static final AspspConsentData SOME_CONSENT_DATA = new AspspConsentData(SOME_DATA, SOME_CONSENT_ID);

    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    @InjectMocks
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;

    @Mock
    private AspspDataService aspspDataService;

    @BeforeEach
    void setUp() {
        spiAspspConsentDataProvider =
            spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(SOME_CONSENT_ID);
    }

    @Test
    void loadDataCallsAspspDataService() {
        when(aspspDataService.readAspspConsentData(anyString()))
            .thenReturn(Optional.of(SOME_CONSENT_DATA));

        byte[] readData = spiAspspConsentDataProvider.loadAspspConsentData();

        assertArrayEquals(SOME_DATA, readData);
        verify(aspspDataService).readAspspConsentData(SOME_CONSENT_ID);
    }

    @Test
    void savingTheSameCallsNoRealUpdate() {
        when(aspspDataService.readAspspConsentData(anyString()))
            .thenReturn(Optional.of(SOME_CONSENT_DATA));

        byte[] readData = spiAspspConsentDataProvider.loadAspspConsentData();
        verify(aspspDataService).readAspspConsentData(SOME_CONSENT_ID);

        spiAspspConsentDataProvider.updateAspspConsentData(readData);
        verifyNoMoreInteractions(aspspDataService);
    }

    @Test
    void doubleUpdateDoesntCallRealUpdate() {
        byte[] bytes = "some another data".getBytes();
        spiAspspConsentDataProvider.updateAspspConsentData(bytes);
        verify(aspspDataService).updateAspspConsentData(new AspspConsentData(bytes, SOME_CONSENT_ID));

        spiAspspConsentDataProvider.updateAspspConsentData(bytes);
        verifyNoMoreInteractions(aspspDataService);

        spiAspspConsentDataProvider.clearAspspConsentData();
        verify(aspspDataService).deleteAspspConsentData(SOME_CONSENT_ID);

        spiAspspConsentDataProvider.updateAspspConsentData(new byte[0]);
        verifyNoMoreInteractions(aspspDataService);
    }

    @Test
    void settingEmptyArrayRemovesData() {
        when(aspspDataService.readAspspConsentData(anyString()))
            .thenReturn(Optional.of(SOME_CONSENT_DATA));

        byte[] bytes = "some another data".getBytes();
        spiAspspConsentDataProvider.updateAspspConsentData(bytes);
        verify(aspspDataService).updateAspspConsentData(new AspspConsentData(bytes, SOME_CONSENT_ID));
        reset(aspspDataService);

        spiAspspConsentDataProvider.updateAspspConsentData(new byte[0]);
        verify(aspspDataService, never()).updateAspspConsentData(any(AspspConsentData.class));
        verify(aspspDataService).deleteAspspConsentData(SOME_CONSENT_ID);
    }
}
