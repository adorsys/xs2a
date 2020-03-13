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

package de.adorsys.psd2.xs2a.web.validator.body.payment.mapper;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapperImpl;
import de.adorsys.psd2.xs2a.web.mapper.RemittanceMapperImpl;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentMapper.class, Xs2aObjectMapper.class, PurposeCodeMapperImpl.class, RemittanceMapperImpl.class})
class PaymentMapperTest {
    @Autowired
    private PaymentMapper paymentMapper;
    @Autowired
    private Xs2aObjectMapper xs2aObjectMapper;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        xs2aObjectMapper.findAndRegisterModules();
    }

    @Test
    void mapToSinglePayment() {
        PaymentInitiationJson paymentInitiationJson =
            jsonReader.getObjectFromFile("json/web/validator/body/payment/mapper/single-payment-initiation.json", PaymentInitiationJson.class);
        SinglePayment expectedPayment = jsonReader.getObjectFromFile("json/web/validator/body/payment/mapper/xs2a-single-payment.json", SinglePayment.class);

        SinglePayment actualPayment = paymentMapper.mapToSinglePayment(paymentInitiationJson);

        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToPeriodicPayment() {
        PeriodicPaymentInitiationJson paymentInitiationJson =
            jsonReader.getObjectFromFile("json/web/validator/body/payment/mapper/periodic-payment-initiation.json", PeriodicPaymentInitiationJson.class);
        PeriodicPayment expectedPayment = jsonReader.getObjectFromFile("json/web/validator/body/payment/mapper/xs2a-periodic-payment.json", PeriodicPayment.class);

        PeriodicPayment actualPayment = paymentMapper.mapToPeriodicPayment(paymentInitiationJson);

        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToBulkPayment() {
        BulkPaymentInitiationJson paymentInitiationJson =
            jsonReader.getObjectFromFile("json/web/validator/body/payment/mapper/bulk-payment-initiation.json", BulkPaymentInitiationJson.class);
        BulkPayment expectedPayment = jsonReader.getObjectFromFile("json/web/validator/body/payment/mapper/xs2a-bulk-payment.json", BulkPayment.class);

        BulkPayment actualPayment = paymentMapper.mapToBulkPayment(paymentInitiationJson);

        assertEquals(expectedPayment, actualPayment);
    }
}
