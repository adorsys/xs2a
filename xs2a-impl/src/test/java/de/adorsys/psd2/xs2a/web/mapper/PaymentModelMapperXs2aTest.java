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

package de.adorsys.psd2.xs2a.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.Validation;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {PaymentModelMapperImpl.class, Xs2aAddressMapperImpl.class})
public class PaymentModelMapperXs2aTest {

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    private PaymentModelMapperXs2a paymentModelMapperXs2a;
    private PaymentInitiationParameters paymentInitiationParameters;
    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        paymentInitiationParameters = new PaymentInitiationParameters();

        ValueValidatorService validatorService = new ValueValidatorService(
            Validation.buildDefaultValidatorFactory().getValidator());
        paymentModelMapperXs2a = new PaymentModelMapperXs2a(new ObjectMapper(), validatorService,
            null, null, paymentModelMapper);
    }

    @Test
    public void mapToXs2aPayment_PeriodicPayment() {
        paymentInitiationParameters.setPaymentType(PaymentType.PERIODIC);

        PeriodicPaymentInitiationJson periodicPaymentInitiationJson =
            jsonReader.getObjectFromFile("json/service/mapper/periodic-payment-initiation.json",
                PeriodicPaymentInitiationJson.class);
        PeriodicPayment actualPeriodicPayment = (PeriodicPayment) paymentModelMapperXs2a.mapToXs2aPayment(
            periodicPaymentInitiationJson, paymentInitiationParameters);

        PeriodicPayment expectedPeriodicPayment = jsonReader.getObjectFromFile("json/service/mapper/expected-periodic-payment-initiation.json",
            PeriodicPayment.class);
        assertEquals(expectedPeriodicPayment, actualPeriodicPayment);
    }

    @Test
    public void mapToXs2aPayment_SinglePayment() {
        paymentInitiationParameters.setPaymentType(PaymentType.SINGLE);

        PaymentInitiationJson singlePaymentInitiationJson =
            jsonReader.getObjectFromFile("json/service/mapper/single-payment-initiation.json",
                PaymentInitiationJson.class);
        SinglePayment actualSinglePayment = (SinglePayment) paymentModelMapperXs2a.mapToXs2aPayment(
            singlePaymentInitiationJson, paymentInitiationParameters);

        SinglePayment expectedSinglePayment = jsonReader.getObjectFromFile("json/service/mapper/expected-single-payment-initiation.json",
            SinglePayment.class);
        assertEquals(expectedSinglePayment, actualSinglePayment);
    }

    @Test
    public void mapToXs2aPayment_BulkPayment() {
        paymentInitiationParameters.setPaymentType(PaymentType.BULK);

        BulkPaymentInitiationJson bulkPaymentInitiationJson =
            jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initiation.json",
                BulkPaymentInitiationJson.class);
        BulkPayment actualBulkPayment = (BulkPayment) paymentModelMapperXs2a.mapToXs2aPayment(
            bulkPaymentInitiationJson, paymentInitiationParameters);

        BulkPayment expectedSinglePayment = jsonReader.getObjectFromFile("json/service/mapper/expected-bulk-payment-initiation.json",
            BulkPayment.class);
        assertEquals(expectedSinglePayment, actualBulkPayment);
    }

}
