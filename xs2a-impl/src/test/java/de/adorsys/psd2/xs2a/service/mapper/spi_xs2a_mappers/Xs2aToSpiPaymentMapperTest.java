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
