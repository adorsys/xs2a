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

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.xs2a.reader.JsonReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Xs2aToSpiPaymentInfoMapperTest {
    private static final String PSU_ID = "psu Id";
    private static final String PSU_ID_TYPE = "psuId Type";
    private static final String PSU_CORPORATE_ID = "psu Corporate Id";
    private static final String PSU_CORPORATE_ID_TYPE = "psuCorporate Id Type";
    private static final String PSU_IP_ADDRESS = "psuIp Address";
    private static final List<PsuIdData> psuDataList = Collections.singletonList(new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS));
    private static final List<SpiPsuData> spiPsuDataList = Collections.singletonList(SpiPsuData.builder()
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

    @Test
    void mapToSpiPaymentInfoSuccess() {
        //Given
        CommonPayment commonPayment = buildCommonPayment();
        when(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(psuDataList)).thenReturn(spiPsuDataList);

        //When
        SpiPaymentInfo spiPaymentInfo = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPayment);
        SpiPaymentInfo spiPaymentInfoExpected = buildSpiPaymentInfo();
        //Then
        Assertions.assertThat(spiPaymentInfo).isEqualToComparingFieldByFieldRecursively(spiPaymentInfoExpected);
    }

    @Test
    void mapToSpiPaymentInfo_CommonPaymentData_Success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData();
        when(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(psuDataList)).thenReturn(spiPsuDataList);

        //When
        SpiPaymentInfo spiPaymentInfo = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPaymentData);
        SpiPaymentInfo spiPaymentInfoExpected = buildSpiPaymentInfo();
        //Then
        Assertions.assertThat(spiPaymentInfo).isEqualToComparingFieldByFieldRecursively(spiPaymentInfoExpected);
    }

    @Test
    void xs2aToSpiPaymentInfo_mapToSpiPaymentRequest() {
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

