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

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpiToXs2aCancelPaymentMapperImpl.class})
public class SpiToXs2aCancelPaymentMapperTest {
    @Autowired
    private SpiToXs2aCancelPaymentMapper mapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    public void mapToCancelPaymentResponse_success() {
        SpiSinglePayment spiSinglePayment = new SpiSinglePayment("product");
        spiSinglePayment.setPaymentId("2Cixxv85Or_qoBBh_d7VTZC0M8PwzR5IGz");

        SpiPaymentCancellationResponse spiCancelPayment =
            jsonReader.getObjectFromFile("json/service/mapper/single-payment-cancellation-response.json", SpiPaymentCancellationResponse.class);

        PsuIdData psuData =
            jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);

        CancelPaymentResponse actualResponse =
            mapper.mapToCancelPaymentResponse(spiCancelPayment, spiSinglePayment, psuData);

        CancelPaymentResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/cancel-payment-response.json", CancelPaymentResponse.class);

        assertEquals(expectedResponse, actualResponse);
    }

}
