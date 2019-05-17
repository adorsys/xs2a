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

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpiToXs2aPaymentMapperImpl.class})
public class SpiToXs2aPaymentMapperTest {

    @Autowired
    private SpiToXs2aPaymentMapper mapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    public void mapSingleToCommonPaymentInitiateResponse_success() {
        SpiSinglePaymentInitiationResponse singlePaymentInitiationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/single-payment-initial-response.json", SpiSinglePaymentInitiationResponse.class);

        CommonPaymentInitiationResponse actualResponse =
            mapper.mapToCommonPaymentInitiateResponse(singlePaymentInitiationResponse, PaymentType.SINGLE, AspspConsentData.emptyConsentData());

        CommonPaymentInitiationResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/common-payment-initial-response.json", CommonPaymentInitiationResponse.class);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void mapSingleToCommonPaymentInitiateResponse_transactionFeeIndicatorIsNotSupported() {
        SpiSinglePaymentInitiationResponse singlePaymentInitiationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/single-payment-initial-response.json", SpiSinglePaymentInitiationResponse.class);
        singlePaymentInitiationResponse.setSpiTransactionFeeIndicator(null);

        CommonPaymentInitiationResponse actualResponse =
            mapper.mapToCommonPaymentInitiateResponse(singlePaymentInitiationResponse, PaymentType.SINGLE, AspspConsentData.emptyConsentData());

        CommonPaymentInitiationResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/common-payment-initial-response.json", CommonPaymentInitiationResponse.class);
        expectedResponse.setTransactionFeeIndicator(null);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void mapToPaymentInitiateResponse_Single() {
        SpiSinglePaymentInitiationResponse spiSinglePaymentInitiationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/single-payment-initial-response.json", SpiSinglePaymentInitiationResponse.class);

        SinglePaymentInitiationResponse actualResponse = mapper.mapToPaymentInitiateResponse(
            spiSinglePaymentInitiationResponse, AspspConsentData.emptyConsentData());

        SinglePaymentInitiationResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/payment-initial-response.json", SinglePaymentInitiationResponse.class);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void mapToPaymentInitiateResponse_Periodic() {
        SpiPeriodicPaymentInitiationResponse spiPeriodicPaymentInitiationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/periodic-payment-initial-response.json", SpiPeriodicPaymentInitiationResponse.class);

        PeriodicPaymentInitiationResponse actualResponse = mapper.mapToPaymentInitiateResponse(
            spiPeriodicPaymentInitiationResponse, AspspConsentData.emptyConsentData());

        PeriodicPaymentInitiationResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/payment-initial-response.json", PeriodicPaymentInitiationResponse.class);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void mapToPaymentInitiateResponse_Bulk() {
        SpiBulkPaymentInitiationResponse spiBulkPaymentInitiationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initial-response.json", SpiBulkPaymentInitiationResponse.class);

        BulkPaymentInitiationResponse actualResponse = mapper.mapToPaymentInitiateResponse(
            spiBulkPaymentInitiationResponse, AspspConsentData.emptyConsentData());

        BulkPaymentInitiationResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/payment-initial-response.json", BulkPaymentInitiationResponse.class);
        assertEquals(expectedResponse, actualResponse);
    }
}
