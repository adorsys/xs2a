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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialSpiAspspConsentDataProviderTest {
    @InjectMocks
    private InitialSpiAspspConsentDataProvider initialSpiAspspConsentDataProvider;

    @Mock
    private AspspDataService aspspDataService;

    @Test
    void onSettingEncryptedIdSaveServiceWillBeCalled() {
        when(aspspDataService.updateAspspConsentData(any()))
            .thenReturn(true);

        initialSpiAspspConsentDataProvider.saveWith("Some ID");
        verify(aspspDataService, never()).updateAspspConsentData(any());
        verify(aspspDataService, times(1))
            .deleteAspspConsentData("Some ID");

        reset(aspspDataService);
        initialSpiAspspConsentDataProvider.updateAspspConsentData("Some not empty data".getBytes());
        verify(aspspDataService, times(1))
            .updateAspspConsentData(new AspspConsentData("Some not empty data".getBytes(), "Some ID"));
    }

    @Test
    void dataPutIntoWillBeReadBack() {
        byte[] initialState = initialSpiAspspConsentDataProvider.loadAspspConsentData();
        assertArrayEquals(new byte[0], initialState);

        initialSpiAspspConsentDataProvider.updateAspspConsentData("some data".getBytes());
        byte[] readBack = initialSpiAspspConsentDataProvider.loadAspspConsentData();
        assertArrayEquals("some data".getBytes(), readBack);

        initialSpiAspspConsentDataProvider.clearAspspConsentData();
        byte[] readBackAfterClear = initialSpiAspspConsentDataProvider.loadAspspConsentData();
        assertArrayEquals(new byte[0], readBackAfterClear);
    }

    @Test
    void clearWithoutConsentIdDoesntCallRealUpdate() {
        initialSpiAspspConsentDataProvider.clearAspspConsentData();
        verify(aspspDataService, never())
            .deleteAspspConsentData("Some ID");
    }

    @Test
    void clearWithConsentIdDoesCallUpdate() {
        initialSpiAspspConsentDataProvider.saveWith("Some ID");
        verify(aspspDataService, times(1))
            .deleteAspspConsentData("Some ID");

        reset(aspspDataService);
        initialSpiAspspConsentDataProvider.clearAspspConsentData();
        verify(aspspDataService, times(1))
            .deleteAspspConsentData("Some ID");
    }
}
