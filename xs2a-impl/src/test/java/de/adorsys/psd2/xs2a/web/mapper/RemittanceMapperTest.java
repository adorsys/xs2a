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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.RemittanceInformationStructured;
import de.adorsys.psd2.model.RemittanceInformationStructuredArray;
import de.adorsys.psd2.model.RemittanceInformationStructuredMax140;
import de.adorsys.psd2.model.RemittanceInformationUnstructuredArray;
import de.adorsys.psd2.xs2a.core.pis.Remittance;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiRemittance;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

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
        RemittanceInformationStructured remittanceInformationStructured = remittanceMapper.mapToRemittanceInformationStructured(null);
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
    void mapToToRemittanceFromRemittance140_null() {
        Remittance remittance = remittanceMapper.mapToRemittance((RemittanceInformationStructuredMax140) null);
        assertNull(remittance);
    }

    @Test
    void mapToToRemittanceFromRemittance140() {
        RemittanceInformationStructuredMax140 remittanceInformationStructuredMax140 = getRemittanceFromFile(RemittanceInformationStructuredMax140.class);
        Remittance remittance = remittanceMapper.mapToRemittance(remittanceInformationStructuredMax140);
        Remittance expectedRemittance = getRemittanceFromFile(Remittance.class);
        assertEquals(expectedRemittance, remittance);
    }

    @Test
    void mapToRemittanceUnstructuredList_null() {
        List<String> remittances = remittanceMapper.mapToRemittanceUnstructuredList(null);
        assertNull(remittances);
    }

    @Test
    void mapToRemittanceUnstructuredList() {
        RemittanceInformationUnstructuredArray remittances = getRemittanceUnstructuredArray();
        List<String> expected = List.of("remittance1", "remittance2");
        List<String> actual = remittanceMapper.mapToRemittanceUnstructuredList(remittances);
        assertEquals(actual, expected);
    }

    @Test
    void mapToRemittanceArray_null() {
        List<Remittance> remittances = remittanceMapper.mapToRemittanceArray((List<SpiRemittance>) null);
        assertNull(remittances);
    }

    @Test
    void mapToRemittanceArray() {
        List<SpiRemittance> remittances = getRemittanceStructuredArray(SpiRemittance.class);
        List<Remittance> expected = List.of(getRemittanceFromFile(Remittance.class), getRemittanceFromFile(Remittance.class));
        List<Remittance> actual = remittanceMapper.mapToRemittanceArray(remittances);
        assertEquals(actual, expected);
    }

    @Test
    void mapToRemittanceArrayRemittanceInfoUnstructured_null() {
        List<Remittance> remittances = remittanceMapper.mapToRemittanceArray((RemittanceInformationStructuredArray) null);
        assertNull(remittances);
    }

    @Test
    void mapToRemittanceArrayRemittanceInfoUnstructured() {
        RemittanceInformationStructuredArray remittances = getRemittanceStructuredArray();
        List<Remittance> expected = List.of(getRemittanceFromFile(Remittance.class), getRemittanceFromFile(Remittance.class));
        List<Remittance> actual = remittanceMapper.mapToRemittanceArray(remittances);
        assertEquals(actual, expected);
    }

    @Test
    void mapToSpiRemittanceArray_null() {
        List<SpiRemittance> remittances = remittanceMapper.mapToSpiRemittanceArray((List<Remittance>) null);
        assertNull(remittances);
    }

    @Test
    void mapToSpiRemittanceArray() {
        List<Remittance> remittances = getRemittanceStructuredArray(Remittance.class);
        List<SpiRemittance> expected = List.of(getRemittanceFromFile(SpiRemittance.class), getRemittanceFromFile(SpiRemittance.class));
        List<SpiRemittance> actual = remittanceMapper.mapToSpiRemittanceArray(remittances);
        assertEquals(actual, expected);
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

    private RemittanceInformationUnstructuredArray getRemittanceUnstructuredArray() {
        RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray = new RemittanceInformationUnstructuredArray();
        remittanceInformationUnstructuredArray.add("remittance1");
        remittanceInformationUnstructuredArray.add("remittance2");
        return remittanceInformationUnstructuredArray;
    }

    private RemittanceInformationStructuredArray getRemittanceStructuredArray() {
        RemittanceInformationStructuredArray remittanceInformationStructuredArray = new RemittanceInformationStructuredArray();
        remittanceInformationStructuredArray.add(getRemittanceFromFile(RemittanceInformationStructured.class));
        remittanceInformationStructuredArray.add(getRemittanceFromFile(RemittanceInformationStructured.class));
        return remittanceInformationStructuredArray;
    }

    private <R> List<R> getRemittanceStructuredArray(Class<R> clazz) {
        return List.of(getRemittanceFromFile(clazz), getRemittanceFromFile(clazz));
    }
}
