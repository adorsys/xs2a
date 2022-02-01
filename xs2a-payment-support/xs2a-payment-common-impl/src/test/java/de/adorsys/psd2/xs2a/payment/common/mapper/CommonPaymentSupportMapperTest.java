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

package de.adorsys.psd2.xs2a.payment.common.mapper;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiCommonPayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommonPaymentSupportMapperTest {
    CommonPaymentSupportMapper  mapper = Mappers.getMapper(CommonPaymentSupportMapper.class);
    JsonReader jsonReader = new JsonReader();

    @Test
    void toSpiCommonPayment() {
        // Given
        SpiCommonPayment expected = jsonReader.getObjectFromFile("json/spi-common-payment.json", SpiCommonPayment.class);
        PisCommonPaymentResponse input = jsonReader.getObjectFromFile("json/common-payment.json", PisCommonPaymentResponse.class);

        // When
        SpiCommonPayment actual = mapper.toSpiCommonPayment(input);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    void toSpiCommonPaymentNull() {
        // When
        SpiCommonPayment actual = mapper.toSpiCommonPayment(null);

        // Then
        assertNull(actual);
    }
}
