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

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aToSpiPaymentMapper.class, Xs2aToSpiPsuDataMapper.class})
class Xs2aToSpiPaymentMapperTest {

    @Autowired
    private Xs2aToSpiPaymentMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToSpiPayment_emptyPayments() {
        PisCommonPaymentResponse pisCommonPaymentResponse = jsonReader.getObjectFromFile("json/service/mapper/pis-common-payment-response.json",
                                                                                         PisCommonPaymentResponse.class);

        SpiPayment actual = mapper.mapToSpiPayment(pisCommonPaymentResponse);

        SpiPaymentInfo expected = jsonReader.getObjectFromFile("json/service/mapper/spi-single-payment2.json",
                                                               SpiPaymentInfo.class);
        assertEquals(expected, actual);
    }
}
