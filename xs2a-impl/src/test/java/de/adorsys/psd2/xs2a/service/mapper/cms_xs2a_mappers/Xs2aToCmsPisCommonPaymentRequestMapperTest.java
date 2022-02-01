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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

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
