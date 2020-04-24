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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.payment.create.PisPaymentInfoCreationObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Xs2aToCmsPisCommonPaymentRequestMapperTest {
    private final static JsonReader jsonReader = new JsonReader();
    private final static byte[] PAYMENT_DATA = "test".getBytes();

    private Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper = new Xs2aToCmsPisCommonPaymentRequestMapper();

    @Test
    void mapToPisPaymentInfo() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/payment-initiation-parameters.json", PaymentInitiationParameters.class);
        TppInfo tppInfo = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/tpp-info.json", TppInfo.class);
        PaymentInitiationResponse response = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/payment-initiation-response.json", SinglePaymentInitiationResponse.class);

        PisPaymentInfo expected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/pis-payment-info.json", PisPaymentInfo.class);
        expected.setPaymentData(PAYMENT_DATA);
        expected.setStatusChangeTimestamp(null);

        // When
        PisPaymentInfoCreationObject creationObject = new PisPaymentInfoCreationObject(paymentInitiationParameters, tppInfo, response, PAYMENT_DATA, null, expected.getCreationTimestamp(), expected.getContentType());
        PisPaymentInfo actual = xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(creationObject);

        // Then
        assertEquals(expected, actual);
    }
}
