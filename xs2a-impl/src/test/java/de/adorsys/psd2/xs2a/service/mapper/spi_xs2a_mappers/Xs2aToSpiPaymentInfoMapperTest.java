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

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.xs2a.reader.JsonReader;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aToSpiPaymentInfoMapperTest {
    private static final String PSU_ID = "psu Id";
    private static final String PSU_ID_TYPE = "psuId Type";
    private static final String PSU_CORPORATE_ID = "psu Corporate Id";
    private static final String PSU_CORPORATE_ID_TYPE = "psuCorporate Id Type";
    private static final String PSU_IP_ADDRESS = "psuIp Address";
    private static final List<PsuIdData> psuDataList = Arrays.asList(new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS));
    private static final List<SpiPsuData> spiPsuDataList = Arrays.asList(SpiPsuData.builder()
                                                                             .psuId(PSU_ID)
                                                                             .psuIdType(PSU_ID_TYPE)
                                                                             .psuCorporateId(PSU_CORPORATE_ID)
                                                                             .psuCorporateIdType(PSU_CORPORATE_ID_TYPE)
                                                                             .psuIpAddress(PSU_IP_ADDRESS)
                                                                             .build());

    @InjectMocks
    private Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private Xs2aToSpiPaymentInfo xs2aToSpiPaymentInfo = new Xs2aToSpiPaymentInfo(new Xs2aToSpiPsuDataMapper());

    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        when(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(psuDataList)).thenReturn(spiPsuDataList);
    }

    @Test
    public void mapToSpiPaymentInfoSuccess() {
        //Given
        CommonPayment commonPayment = buildCommonPayment();
        //When
        SpiPaymentInfo spiPaymentInfo = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPayment);
        SpiPaymentInfo spiPaymentInfoExpected = buildSpiPaymentInfo();
        //Then
        Assertions.assertThat(spiPaymentInfo).isEqualToComparingFieldByFieldRecursively(spiPaymentInfoExpected);
    }

    @Test
    public void mapToSpiPaymentInfo_CommonPaymentData_Success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData();
        //When
        SpiPaymentInfo spiPaymentInfo = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPaymentData);
        SpiPaymentInfo spiPaymentInfoExpected = buildSpiPaymentInfo();
        //Then
        Assertions.assertThat(spiPaymentInfo).isEqualToComparingFieldByFieldRecursively(spiPaymentInfoExpected);
    }

    @Test
    public void xs2aToSpiPaymentInfo_mapToSpiPaymentRequest() {
        //Given
        CommonPayment commonPayment = buildCommonPayment();
        //When
        SpiPaymentInfo spiPaymentInfo = xs2aToSpiPaymentInfo.mapToSpiPaymentRequest(commonPayment, commonPayment.getPaymentProduct());
        SpiPaymentInfo spiPaymentInfoExpected = buildSpiPaymentInfo();
        //Then
        Assertions.assertThat(spiPaymentInfo).isEqualToComparingFieldByFieldRecursively(spiPaymentInfoExpected);
    }

    private CommonPaymentData buildCommonPaymentData() {
        return jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/pis-common-payment-response.json", PisCommonPaymentResponse.class);
    }

    private CommonPayment buildCommonPayment() {
        return jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/common-payment.json", CommonPayment.class);
    }

    private SpiPaymentInfo buildSpiPaymentInfo() {
        return jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-payment-info.json", SpiPaymentInfo.class);
    }
}

