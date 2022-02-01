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
