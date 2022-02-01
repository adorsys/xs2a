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

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aPaymentInfoMapperImpl.class})
class SpiToXs2aPaymentInfoMapperTest {
    private static final String PAYMENT_ID = "2Cixxv85Or_qoBBh_d7VTZC0M8PwzR5IGzsJuT-jYHNOMR1D7n69vIF46RgFd7Zn_=_bS6p6XvTWI";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();
    private static final byte[] PAYMENT_DATA = "data".getBytes();

    @Autowired
    private SpiToXs2aPaymentInfoMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aPaymentInfo() {
        SpiPaymentInfo spiPaymentInfo = new SpiPaymentInfo(PAYMENT_PRODUCT);
        spiPaymentInfo.setPaymentId(PAYMENT_ID);
        spiPaymentInfo.setPaymentType(PaymentType.SINGLE);
        spiPaymentInfo.setStatus(TransactionStatus.ACSP);
        spiPaymentInfo.setStatusChangeTimestamp(OFFSET_DATE_TIME);
        spiPaymentInfo.setPsuDataList(Collections.singletonList(SpiPsuData.builder()
                                                                    .psuId("psuId")
                                                                    .psuIdType("")
                                                                    .psuCorporateId("")
                                                                    .psuCorporateIdType("")
                                                                    .psuIpAddress("")
                                                                    .psuIpPort("")
                                                                    .psuUserAgent("")
                                                                    .psuGeoLocation("")
                                                                    .psuAccept("")
                                                                    .psuAcceptCharset("")
                                                                    .psuAcceptEncoding("")
                                                                    .psuAcceptLanguage("")
                                                                    .psuHttpMethod("")
                                                                    .psuDeviceId(UUID.randomUUID())
                                                                    .build()));
        spiPaymentInfo.setPaymentData(PAYMENT_DATA);

        CommonPayment commonPayment = mapper.mapToXs2aPaymentInfo(spiPaymentInfo);

        CommonPayment expectedCommonPayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-payment-info.json", CommonPayment.class);
        expectedCommonPayment.setStatusChangeTimestamp(OFFSET_DATE_TIME);
        assertEquals(expectedCommonPayment, commonPayment);
    }

    @Test
    void mapToXs2aPaymentInfo_nullValue() {
        CommonPayment commonPayment = mapper.mapToXs2aPaymentInfo(null);
        assertNull(commonPayment);
    }
}
