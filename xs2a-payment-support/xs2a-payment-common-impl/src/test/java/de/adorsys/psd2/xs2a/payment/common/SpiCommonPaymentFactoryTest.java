/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.payment.common;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.payment.common.mapper.CommonPaymentSupportMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiCommonPayment;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpiCommonPaymentFactoryTest {
    @InjectMocks
    SpiCommonPaymentFactory spiCommonPaymentFactory;
    @Mock
    CommonPaymentSupportMapper  mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void getSpiPayment() {
        // Given
        SpiCommonPayment spiCommonPayment = jsonReader.getObjectFromFile("json/spi-common-payment.json", SpiCommonPayment.class);
        PisCommonPaymentResponse pisCommonPaymentResponse = jsonReader.getObjectFromFile("json/common-payment.json", PisCommonPaymentResponse.class);
        when(mapper.toSpiCommonPayment(pisCommonPaymentResponse)).thenReturn(spiCommonPayment);

        // When
        Optional<SpiPayment> actual = spiCommonPaymentFactory.getSpiPayment(pisCommonPaymentResponse);

        // Then
        assertEquals(Optional.of(spiCommonPayment), actual);
    }
}
