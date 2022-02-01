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

package de.adorsys.psd2.xs2a.service.payment.support.mapper;

import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Xs2aToPsd2PaymentSupportMapperImpl.class)
class Xs2AToPsd2PaymentSupportMapperTest {
    @Autowired
    private Xs2aToPsd2PaymentSupportMapper xs2AToPsd2PaymentSupportMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToPaymentInitiationJson() {
        // Given
        PaymentInitiationJson expectedPaymentInitiation = jsonReader.getObjectFromFile("json/support/mapper/single-payment-initiation.json", PaymentInitiationJson.class);
        SinglePayment xs2aPayment = jsonReader.getObjectFromFile("json/support/mapper/xs2a-single-payment.json", SinglePayment.class);

        // When
        PaymentInitiationJson actual = xs2AToPsd2PaymentSupportMapper.mapToPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    void mapToPaymentInitiationJson_emptyObject() {
        // Given
        PaymentInitiationJson expectedPaymentInitiation = new PaymentInitiationJson();
        SinglePayment xs2aPayment = new SinglePayment();

        // When
        PaymentInitiationJson actual = xs2AToPsd2PaymentSupportMapper.mapToPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    void mapToPaymentInitiationJson_null() {
        // When
        PaymentInitiationJson actual = xs2AToPsd2PaymentSupportMapper.mapToPaymentInitiationJson(null);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToPeriodicPaymentInitiationJson() {
        // Given
        PeriodicPaymentInitiationJson expectedPaymentInitiation = jsonReader.getObjectFromFile("json/support/mapper/periodic-payment-initiation.json", PeriodicPaymentInitiationJson.class);
        PeriodicPayment xs2aPayment = jsonReader.getObjectFromFile("json/support/mapper/xs2a-periodic-payment.json", PeriodicPayment.class);

        // When
        PeriodicPaymentInitiationJson actual = xs2AToPsd2PaymentSupportMapper.mapToPeriodicPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    void mapToPeriodicPaymentInitiationJson_emptyObject() {
        // Given
        PeriodicPaymentInitiationJson expectedPaymentInitiation = new PeriodicPaymentInitiationJson();
        PeriodicPayment xs2aPayment = new PeriodicPayment();

        // When
        PeriodicPaymentInitiationJson actual = xs2AToPsd2PaymentSupportMapper.mapToPeriodicPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    void mapToPeriodicPaymentInitiationJson_null() {
        // When
        PeriodicPaymentInitiationJson actual = xs2AToPsd2PaymentSupportMapper.mapToPeriodicPaymentInitiationJson(null);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToBulkPaymentInitiationJson() {
        // Given
        BulkPaymentInitiationJson expectedPaymentInitiation = jsonReader.getObjectFromFile("json/support/mapper/bulk-payment-initiation.json", BulkPaymentInitiationJson.class);
        BulkPayment xs2aPayment = jsonReader.getObjectFromFile("json/support/mapper/xs2a-bulk-payment.json", BulkPayment.class);

        // When
        BulkPaymentInitiationJson actual = xs2AToPsd2PaymentSupportMapper.mapToBulkPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    void mapToBulkPaymentInitiationJson_nullPaymentPart() {
        // Given
        BulkPaymentInitiationJson expectedPaymentInitiation = jsonReader.getObjectFromFile("json/support/mapper/bulk-payment-initiation-null-payment.json", BulkPaymentInitiationJson.class);
        BulkPayment xs2aPayment = jsonReader.getObjectFromFile("json/support/mapper/xs2a-bulk-payment.json", BulkPayment.class);
        xs2aPayment.setPayments(Collections.singletonList(null));

        // When
        BulkPaymentInitiationJson actual = xs2AToPsd2PaymentSupportMapper.mapToBulkPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    void mapToBulkPaymentInitiationJson_emptyObject() {
        // Given
        BulkPaymentInitiationJson expectedPaymentInitiation = new BulkPaymentInitiationJson();
        expectedPaymentInitiation.setPayments(null);
        BulkPayment xs2aPayment = new BulkPayment();

        // When
        BulkPaymentInitiationJson actual = xs2AToPsd2PaymentSupportMapper.mapToBulkPaymentInitiationJson(xs2aPayment);

        // Then
        assertEquals(expectedPaymentInitiation, actual);
    }

    @Test
    void mapToBulkPaymentInitiationJson_null() {
        // When
        BulkPaymentInitiationJson actual = xs2AToPsd2PaymentSupportMapper.mapToBulkPaymentInitiationJson(null);

        // Then
        assertNull(actual);
    }
}
