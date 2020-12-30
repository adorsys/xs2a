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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.core.payment.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.core.payment.model.PaymentInitiationJson;
import de.adorsys.psd2.core.payment.model.PeriodicPaymentInitiationJson;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CmsCorePaymentMapperTest {

    private CmsCorePaymentMapper mapper;
    private final JsonReader jsonReader = new JsonReader();
    private static final CmsAddressMapper cmsAddressMapper = new CmsAddressMapperImpl();

    @BeforeEach
    void setUp() {
        mapper = new CmsCorePaymentMapper(cmsAddressMapper);
    }

    @Test
    void mapToPaymentInitiationJson() {
        PisPayment pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        PaymentInitiationJson actual = mapper.mapToPaymentInitiationJson(pisPayment);

        PaymentInitiationJson expected = jsonReader.getObjectFromFile("json/service/mapper/payment-initiation-resp.json", PaymentInitiationJson.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToPaymentInitiationJsonList_emptyValueValue() {
        assertNull(mapper.mapToPaymentInitiationJson(Collections.emptyList()));
    }

    @Test
    void mapToPaymentInitiationJsonList() {
        PisPayment pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        PaymentInitiationJson actual = mapper.mapToPaymentInitiationJson(Arrays.asList(pisPayment, new PisPayment()));

        PaymentInitiationJson expected = jsonReader.getObjectFromFile("json/service/mapper/payment-initiation-resp.json", PaymentInitiationJson.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToBulkPaymentInitiationJson() {
        PisPayment pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        BulkPaymentInitiationJson actual = mapper.mapToBulkPaymentInitiationJson(Collections.singletonList(pisPayment));

        BulkPaymentInitiationJson expected = jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initiation-resp.json", BulkPaymentInitiationJson.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToBulkPaymentInitiationJson_nullValue() {
        assertNull(mapper.mapToBulkPaymentInitiationJson(null));
    }

    @Test
    void mapToPeriodicPaymentInitiationJson() {
        PisPayment pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        PeriodicPaymentInitiationJson actual = mapper.mapToPeriodicPaymentInitiationJson(Collections.singletonList(pisPayment));

        PeriodicPaymentInitiationJson expected = jsonReader.getObjectFromFile("json/service/mapper/periodic-payment-initiation-resp.json", PeriodicPaymentInitiationJson.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToPeriodicPaymentInitiationJson_nullValue() {
        assertNull(mapper.mapToPeriodicPaymentInitiationJson(null));
    }
}
