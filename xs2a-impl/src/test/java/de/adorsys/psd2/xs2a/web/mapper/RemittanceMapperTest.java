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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.RemittanceInformationStructured;
import de.adorsys.psd2.xs2a.core.pis.Remittance;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiRemittance;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class RemittanceMapperTest {
    private final JsonReader jsonReader = new JsonReader();
    private final RemittanceMapper remittanceMapper = Mappers.getMapper(RemittanceMapper.class);

    @Test
    void mapToRemittanceInformationStructured() {
        Remittance remittance = getRemittanceFromFile(Remittance.class);
        RemittanceInformationStructured remittanceInformationStructured = remittanceMapper.mapToRemittanceInformationStructured(remittance);
        RemittanceInformationStructured expectedRemittanceInformationStructured = getRemittanceFromFile(RemittanceInformationStructured.class);
        assertEquals(expectedRemittanceInformationStructured, remittanceInformationStructured);
    }

    @Test
    void mapToRemittanceInformationStructured_isNull_returnNull() {
        RemittanceInformationStructured  remittanceInformationStructured = remittanceMapper.mapToRemittanceInformationStructured(null);
        assertNull(remittanceInformationStructured);
    }

    @Test
    void mapToRemittance_isNull_returnsNull() {
        Remittance remittance = remittanceMapper.mapToRemittance((RemittanceInformationStructured) null);
        assertNull(remittance);
    }

    @Test
    void mapToSpiRemittance_isNull_returnsNull() {
        SpiRemittance spiRemittance = remittanceMapper.mapToSpiRemittance(null);
        assertNull(spiRemittance);
    }

    @Test
    void mapToRemittance_Spi_isNull_returns_null() {
        SpiRemittance spiRemittance = null;
        Remittance remittance = remittanceMapper.mapToRemittance(spiRemittance);
        assertNull(remittance);
    }

    @Test
    void mapToToRemittance() {
        RemittanceInformationStructured remittanceInformationStructured = getRemittanceFromFile(RemittanceInformationStructured.class);
        Remittance remittance = remittanceMapper.mapToRemittance(remittanceInformationStructured);
        Remittance expectedRemittance = getRemittanceFromFile(Remittance.class);
        assertEquals(expectedRemittance, remittance);
    }

    @Test
    void mapToSpiRemitance() {
        Remittance remittance = getRemittanceFromFile(Remittance.class);
        SpiRemittance spiRemittance = remittanceMapper.mapToSpiRemittance(remittance);
        SpiRemittance expectedSpiRemittance = getRemittanceFromFile(SpiRemittance.class);
        assertEquals(expectedSpiRemittance, spiRemittance);
    }

    @Test
    void mapToRemittance() {
        SpiRemittance spiRemittance = getRemittanceFromFile(SpiRemittance.class);
        Remittance remittance = remittanceMapper.mapToRemittance(spiRemittance);
        Remittance expectedRemittance = getRemittanceFromFile(Remittance.class);
        assertEquals(expectedRemittance, remittance);
    }

    private <R> R getRemittanceFromFile(Class<R> clazz) {
        return jsonReader.getObjectFromFile("json/service/mapper/remittance.json", clazz);
    }
}
