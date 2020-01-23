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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Xs2aToSpiPsuDataMapperTest {
    private final static String PSU_ID = "psuId";
    private final static String PSU_ID_TYPE = "psuIdType";
    private final static String PSU_CORPORATE_ID = "psuCorporateId";
    private final static String PSU_CORPORATE_TYPE = "psuCorporateIdType";
    private final static String PSU_IP_ADDRESS = "IP Address";
    private final static Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper = new Xs2aToSpiPsuDataMapper();

    @Test
    void mapToSpiPsuData_WithPsuIpAddress() {
        //Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_TYPE);
        SpiPsuData spiPsuDataExpected = buildSpiPsuData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_TYPE, PSU_IP_ADDRESS);
        //When
        SpiPsuData spiPsuDataActual = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData, PSU_IP_ADDRESS);
        //Then
        assertNotNull(spiPsuDataActual);
        assertEquals(spiPsuDataExpected, spiPsuDataActual);
    }

    @Test
    void mapToSpiPsuData_WithPsuIpAddress_PsuDataNull() {
        //Given
        SpiPsuData spiPsuDataExpected = buildSpiPsuData(null, null, null, null, PSU_IP_ADDRESS);
        //When
        SpiPsuData spiPsuDataActual = xs2aToSpiPsuDataMapper.mapToSpiPsuData(null, PSU_IP_ADDRESS);
        //Then
        assertNotNull(spiPsuDataActual);
        assertEquals(spiPsuDataExpected, spiPsuDataActual);
    }

    @Test
    void mapToSpiPsuData_WithoutPsuIpAddress() {
        //Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_TYPE);
        SpiPsuData spiPsuDataExpected = buildSpiPsuData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_TYPE, null);
        //When
        SpiPsuData spiPsuDataActual = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData);
        //Then
        assertNotNull(spiPsuDataActual);
        assertEquals(spiPsuDataExpected, spiPsuDataActual);
    }

    @Test
    void mapToSpiPsuData_WithoutPsuIpAddress_PsuDataNull() {
        //Given
        SpiPsuData spiPsuDataExpected = buildSpiPsuData(null, null, null, null, null);
        //When
        SpiPsuData spiPsuDataActual = xs2aToSpiPsuDataMapper.mapToSpiPsuData(null);
        //Then
        assertNotNull(spiPsuDataActual);
        assertEquals(spiPsuDataExpected, spiPsuDataActual);
    }

    @Test
    void mapToSpiPsuDataList() {
        //Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_TYPE);
        SpiPsuData spiPsuDataExpected = buildSpiPsuData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_TYPE, null);
        //When
        List<SpiPsuData> spiPsuDataListActual = xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(Collections.singletonList(psuIdData));
        //Then
        assertNotNull(spiPsuDataListActual);
        assertFalse(spiPsuDataListActual.isEmpty());
        assertEquals(spiPsuDataExpected, spiPsuDataListActual.get(0));
    }

    @Test
    void mapToSpiPsuDataList_psuIdDataListIsNull() {
        //Given
        List<SpiPsuData> spiPsuDataListExpected = Collections.emptyList();
        //When
        List<SpiPsuData> spiPsuDataListActual = xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(null);
        //Then
        assertNotNull(spiPsuDataListActual);
        assertEquals(spiPsuDataListExpected, spiPsuDataListActual);
    }


    private SpiPsuData buildSpiPsuData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String psuIpAddress) {
        return new SpiPsuData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, psuIpAddress);
    }
}
