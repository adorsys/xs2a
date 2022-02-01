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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PsuDataMapperTest {
    private final static JsonReader jsonReader = new JsonReader();
    @Spy
    private PsuDataMapper psuDataMapper;

    @Test
    void mapToPsuDataList() {
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        PsuData expected = jsonReader.getObjectFromFile("json/service/mapper/psu-data.json", PsuData.class);
        List<PsuData> expectedList = Collections.singletonList(expected);

        List<PsuData> psuDataList = psuDataMapper.mapToPsuDataList(Collections.singletonList(psuIdData), DEFAULT_SERVICE_INSTANCE_ID);

        verify(psuDataMapper, times(1)).mapToPsuData(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
        assertEquals(expectedList, psuDataList);
    }

    @Test
    void mapToPsuIdDataList() {
        PsuData psuData = jsonReader.getObjectFromFile("json/service/mapper/psu-data.json", PsuData.class);
        PsuIdData expected = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        List<PsuIdData> expectedList = Collections.singletonList(expected);

        List<PsuIdData> psuDataList = psuDataMapper.mapToPsuIdDataList(Collections.singletonList(psuData));

        verify(psuDataMapper, times(1)).mapToPsuIdData(psuData);
        assertEquals(expectedList, psuDataList);
    }

    @Test
    void mapToPsuData_WithAdditionalPsuIdData() {
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        PsuData expected = jsonReader.getObjectFromFile("json/service/mapper/psu-data.json", PsuData.class);

        PsuData psuData = psuDataMapper.mapToPsuData(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        assertEquals(expected, psuData);
    }

    @Test
    void mapToPsuIdData_WithAdditionalPsuData() {
        PsuData psuData = jsonReader.getObjectFromFile("json/service/mapper/psu-data.json", PsuData.class);
        PsuIdData expected = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);

        PsuIdData psuIdData = psuDataMapper.mapToPsuIdData(psuData);

        assertEquals(expected, psuIdData);
    }

    @Test
    void mapToPsuData_WithoutAdditionalPsuIdData() {
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data-with-additional-psu-id-data.json", PsuIdData.class);
        PsuData expected = jsonReader.getObjectFromFile("json/service/mapper/psu-data-with-additional-psu-data.json", PsuData.class);

        PsuData psuData = psuDataMapper.mapToPsuData(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        assertEquals(expected, psuData);
    }

    @Test
    void mapToPsuIdData_WithoutAdditionalPsuData() {
        PsuData psuData = jsonReader.getObjectFromFile("json/service/mapper/psu-data-with-additional-psu-data.json", PsuData.class);
        PsuIdData expected = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data-with-additional-psu-id-data.json", PsuIdData.class);

        PsuIdData psuIdData = psuDataMapper.mapToPsuIdData(psuData);

        assertEquals(expected, psuIdData);
    }
}
