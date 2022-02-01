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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Xs2aToSpiPsuDataMapperTest {
    private final static String PSU_ID = "psuId";
    private final static String PSU_ID_TYPE = "psuIdType";
    private final static String PSU_CORPORATE_ID = "psuCorporateId";
    private final static String PSU_CORPORATE_TYPE = "psuCorporateIdType";
    private final static String PSU_IP_ADDRESS = "IP Address";
    private static final String PSU_IP_PORT = "psuIpPort";
    private static final String PSU_USER_AGENT = "psuUserAgent";
    private static final String PSU_GEO_LOCATION = "psuGeoLocation";
    private static final String PSU_ACCEPT = "psuAccept";
    private static final String PSU_ACCEPT_CHARSET = "psuAcceptCharset";
    private static final String PSU_ACCEPT_ENCODING = "psuAcceptEncoding";
    private static final String PSU_ACCEPT_LANGUAGE = "psuAcceptLanguage";
    private static final String PSU_HTTP_METHOD = "psuHttpMethod";
    private static final UUID PSU_DEVICE_ID = UUID.randomUUID();

    private final static Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper = new Xs2aToSpiPsuDataMapper();

    @Test
    void mapToSpiPsuData_WithAdditionalPsuData() {
        //Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_TYPE, PSU_IP_ADDRESS, buildAdditionalPsuData());
        SpiPsuData spiPsuDataExpected = buildSpiPsuDataWithAdditionalPsuData();
        //When
        SpiPsuData spiPsuDataActual = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData);
        //Then
        assertNotNull(spiPsuDataActual);
        Assertions.assertThat(spiPsuDataExpected).isEqualToComparingFieldByFieldRecursively(spiPsuDataActual);
    }

    @Test
    void mapToSpiPsuData_WithoutAdditionalPsuData() {
        //Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_TYPE, PSU_IP_ADDRESS);
        SpiPsuData spiPsuDataExpected = buildSpiPsuDataWithoutAdditionalPsuData();
        //When
        SpiPsuData spiPsuDataActual = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuIdData);
        //Then
        assertNotNull(spiPsuDataActual);
        Assertions.assertThat(spiPsuDataExpected).isEqualToComparingFieldByFieldRecursively(spiPsuDataActual);
    }

    @Test
    void mapToSpiPsuData_WithoutPsuData() {
        //Given
        SpiPsuData spiPsuDataExpected = buildEmptySpiPsuData();
        //When
        SpiPsuData spiPsuDataActual = xs2aToSpiPsuDataMapper.mapToSpiPsuData(null);
        //Then
        assertNotNull(spiPsuDataActual);
        Assertions.assertThat(spiPsuDataExpected).isEqualToComparingFieldByFieldRecursively(spiPsuDataActual);
    }

    @Test
    void mapToSpiPsuDataList() {
        //Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_TYPE, PSU_IP_ADDRESS);
        SpiPsuData spiPsuDataExpected = buildSpiPsuDataWithoutAdditionalPsuData();
        //When
        List<SpiPsuData> spiPsuDataListActual = xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(Collections.singletonList(psuIdData));
        //Then
        assertNotNull(spiPsuDataListActual);
        assertFalse(spiPsuDataListActual.isEmpty());
        Assertions.assertThat(spiPsuDataExpected).isEqualToComparingFieldByFieldRecursively(spiPsuDataListActual.get(0));
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


    private AdditionalPsuIdData buildAdditionalPsuData() {
        return new AdditionalPsuIdData(PSU_IP_PORT, PSU_USER_AGENT, PSU_GEO_LOCATION, PSU_ACCEPT, PSU_ACCEPT_CHARSET, PSU_ACCEPT_ENCODING, PSU_ACCEPT_LANGUAGE, PSU_HTTP_METHOD, PSU_DEVICE_ID);
    }

    private SpiPsuData buildEmptySpiPsuData() {
        return SpiPsuData.builder().build();
    }

    private SpiPsuData buildSpiPsuDataWithoutAdditionalPsuData() {
        return SpiPsuData.builder()
                   .psuId(PSU_ID)
                   .psuIdType(PSU_ID_TYPE)
                   .psuCorporateId(PSU_CORPORATE_ID)
                   .psuCorporateIdType(PSU_CORPORATE_TYPE)
                   .psuIpAddress(PSU_IP_ADDRESS)
                   .build();
    }

    private SpiPsuData buildSpiPsuDataWithAdditionalPsuData() {
        return SpiPsuData.builder()
                   .psuId(PSU_ID)
                   .psuIdType(PSU_ID_TYPE)
                   .psuCorporateId(PSU_CORPORATE_ID)
                   .psuCorporateIdType(PSU_CORPORATE_TYPE)
                   .psuIpAddress(PSU_IP_ADDRESS)
                   .psuIpPort(PSU_IP_PORT)
                   .psuUserAgent(PSU_USER_AGENT)
                   .psuGeoLocation(PSU_GEO_LOCATION)
                   .psuAccept(PSU_ACCEPT)
                   .psuAcceptCharset(PSU_ACCEPT_CHARSET)
                   .psuAcceptEncoding(PSU_ACCEPT_ENCODING)
                   .psuAcceptLanguage(PSU_ACCEPT_LANGUAGE)
                   .psuHttpMethod(PSU_HTTP_METHOD)
                   .psuDeviceId(PSU_DEVICE_ID)
                   .build();
    }
}
