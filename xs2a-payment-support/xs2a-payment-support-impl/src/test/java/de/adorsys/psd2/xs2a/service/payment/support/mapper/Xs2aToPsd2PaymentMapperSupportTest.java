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

package de.adorsys.psd2.xs2a.service.payment.support.mapper;

import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Xs2aToPsd2PaymentMapperSupportTest {
    private Xs2aToPsd2PaymentMapperSupport xs2aToPsd2PaymentMapperSupport = new Xs2aToPsd2PaymentMapperSupport();

    private JsonReader jsonReader = new JsonReader();

    @Test
    public void mapToPaymentInitiationJson() {
        // Given
        PaymentInitiationJson expectedPaymentInitiation = jsonReader.getObjectFromFile("json/support/mapper/single-payment-initiation.json", PaymentInitiationJson.class);
        SinglePayment xs2aPayment = jsonReader.getObjectFromFile("json/support/mapper/xs2a-single-payment.json", SinglePayment.class);

        // When
        PaymentInitiationJson actual = xs2aToPsd2PaymentMapperSupport.mapToPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    public void mapToPaymentInitiationJson_emptyObject() {
        // Given
        PaymentInitiationJson expectedPaymentInitiation = new PaymentInitiationJson();
        SinglePayment xs2aPayment = new SinglePayment();

        // When
        PaymentInitiationJson actual = xs2aToPsd2PaymentMapperSupport.mapToPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    public void mapToPaymentInitiationJson_null() {
        // When
        PaymentInitiationJson actual = xs2aToPsd2PaymentMapperSupport.mapToPaymentInitiationJson(null);

        // Then
        assertNull(actual);
    }

    @Test
    public void mapToPeriodicPaymentInitiationJson() {
        // Given
        PeriodicPaymentInitiationJson expectedPaymentInitiation = jsonReader.getObjectFromFile("json/support/mapper/periodic-payment-initiation.json", PeriodicPaymentInitiationJson.class);
        PeriodicPayment xs2aPayment = jsonReader.getObjectFromFile("json/support/mapper/xs2a-periodic-payment.json", PeriodicPayment.class);

        // When
        PeriodicPaymentInitiationJson actual = xs2aToPsd2PaymentMapperSupport.mapToPeriodicPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    public void mapToPeriodicPaymentInitiationJson_emptyObject() {
        // Given
        PeriodicPaymentInitiationJson expectedPaymentInitiation = new PeriodicPaymentInitiationJson();
        PeriodicPayment xs2aPayment = new PeriodicPayment();

        // When
        PeriodicPaymentInitiationJson actual = xs2aToPsd2PaymentMapperSupport.mapToPeriodicPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    public void mapToPeriodicPaymentInitiationJson_null() {
        // When
        PeriodicPaymentInitiationJson actual = xs2aToPsd2PaymentMapperSupport.mapToPeriodicPaymentInitiationJson(null);

        // Then
        assertNull(actual);
    }

    @Test
    public void mapToBulkPaymentInitiationJson() {
        // Given
        BulkPaymentInitiationJson expectedPaymentInitiation = jsonReader.getObjectFromFile("json/support/mapper/bulk-payment-initiation.json", BulkPaymentInitiationJson.class);
        BulkPayment xs2aPayment = jsonReader.getObjectFromFile("json/support/mapper/xs2a-bulk-payment.json", BulkPayment.class);

        // When
        BulkPaymentInitiationJson actual = xs2aToPsd2PaymentMapperSupport.mapToBulkPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    public void mapToBulkPaymentInitiationJson_emptyObject() {
        // Given
        BulkPaymentInitiationJson expectedPaymentInitiation = new BulkPaymentInitiationJson();
        BulkPayment xs2aPayment = new BulkPayment();

        // When
        BulkPaymentInitiationJson actual = xs2aToPsd2PaymentMapperSupport.mapToBulkPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    public void mapToBulkPaymentInitiationJson_null() {
        // When
        BulkPaymentInitiationJson actual = xs2aToPsd2PaymentMapperSupport.mapToBulkPaymentInitiationJson(null);

        // Then
        assertNull(actual);
    }
}
