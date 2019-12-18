/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class Xs2aToSpiPsuDataMapperTest {
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
    public void mapToSpiPsuData_WithAdditionalPsuData() {
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
    public void mapToSpiPsuData_WithoutAdditionalPsuData() {
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
    public void mapToSpiPsuData_WithoutPsuData() {
        //Given
        SpiPsuData spiPsuDataExpected = buildEmptySpiPsuData();
        //When
        SpiPsuData spiPsuDataActual = xs2aToSpiPsuDataMapper.mapToSpiPsuData(null);
        //Then
        assertNotNull(spiPsuDataActual);
        Assertions.assertThat(spiPsuDataExpected).isEqualToComparingFieldByFieldRecursively(spiPsuDataActual);
    }

    @Test
    public void mapToSpiPsuDataList() {
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
    public void mapToSpiPsuDataList_psuIdDataListIsNull() {
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
