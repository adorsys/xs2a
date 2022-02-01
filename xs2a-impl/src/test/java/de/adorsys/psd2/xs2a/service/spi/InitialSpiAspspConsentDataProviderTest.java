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
