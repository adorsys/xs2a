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

import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aCancelPaymentMapperImpl.class})
class SpiToXs2aCancelPaymentMapperTest {
    private static final String INTERNAL_PAYMENT_ID = "2Cixxv85Or_qoBBh_d7VTZC0M8PwzR5IGz";
    private static final String ENCRYPTED_PAYMENT_ID = "m7yCLQAd0eYCRm0GQlpSUkdX0zMvJvyxMZP5Y9t3_LXc-bUD1r4ipjym9p0cP3rgqC7ZF9NIw_bZTnhXSEbGFg==_=_psGLvQpt9Q";

    @Autowired
    private SpiToXs2aCancelPaymentMapper mapper;
    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToCancelPaymentResponse_success() {
        SpiSinglePayment spiSinglePayment = new SpiSinglePayment("product");
        spiSinglePayment.setPaymentId(INTERNAL_PAYMENT_ID);

        SpiPaymentCancellationResponse spiCancelPayment =
            jsonReader.getObjectFromFile("json/service/mapper/single-payment-cancellation-response.json",
                SpiPaymentCancellationResponse.class);

        CancelPaymentResponse actual =
            mapper.mapToCancelPaymentResponse(spiCancelPayment, spiSinglePayment, ENCRYPTED_PAYMENT_ID);

        CancelPaymentResponse expected =
            jsonReader.getObjectFromFile("json/service/mapper/cancel-payment-response.json",
                CancelPaymentResponse.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToCancelPaymentResponse_nullInput() {
        //When
        CancelPaymentResponse actual =
            mapper.mapToCancelPaymentResponse(null, null, null);

        //Then
        assertThat(actual).isNull();
    }
}
