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
