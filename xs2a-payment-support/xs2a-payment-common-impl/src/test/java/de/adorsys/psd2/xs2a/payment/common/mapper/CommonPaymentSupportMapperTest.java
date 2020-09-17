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

package de.adorsys.psd2.xs2a.payment.common.mapper;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiCommonPayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

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
