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

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.PaymentModelMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RawToXs2aPaymentMapperTest {
    private static final String SINGLE_PAYMENT_PSD2_JSON_PATH = "json/support/mapper/single-payment-initiation.json";
    private static final String SINGLE_PAYMENT_XS2A_JSON_PATH = "json/support/mapper/xs2a-single-payment.json";
    private static final String PERIODIC_PAYMENT_PSD2_JSON_PATH = "json/support/mapper/periodic-payment-initiation.json";
    private static final String PERIODIC_PAYMENT_XS2A_JSON_PATH = "json/support/mapper/xs2a-periodic-payment.json";
    private static final String BULK_PAYMENT_PSD2_JSON_PATH = "json/support/mapper/bulk-payment-initiation.json";
    private static final String BULK_PAYMENT_XS2A_JSON_PATH = "json/support/mapper/xs2a-bulk-payment.json";
    private static final byte[] MALFORMED_PAYMENT_BODY = "malformed body".getBytes();

    @Mock
    private PaymentModelMapper paymentModelMapper;
    private final Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

    private RawToXs2aPaymentMapper rawToXs2aPaymentMapper;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        xs2aObjectMapper.findAndRegisterModules();
        rawToXs2aPaymentMapper = new RawToXs2aPaymentMapper(paymentModelMapper, xs2aObjectMapper);
    }

    @Test
    void mapToSinglePayment() {
        // Given
        PaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(SINGLE_PAYMENT_PSD2_JSON_PATH, PaymentInitiationJson.class);
        SinglePayment xs2aSinglePayment = jsonReader.getObjectFromFile(SINGLE_PAYMENT_XS2A_JSON_PATH, SinglePayment.class);
        when(paymentModelMapper.mapToXs2aPayment(paymentInitiationJson)).thenReturn(xs2aSinglePayment);

        byte[] paymentBody = jsonReader.getBytesFromFile(SINGLE_PAYMENT_PSD2_JSON_PATH);
        SinglePayment expectedSinglePayment = jsonReader.getObjectFromFile(SINGLE_PAYMENT_XS2A_JSON_PATH, SinglePayment.class);
        expectedSinglePayment.setPaymentData(paymentBody);

        // When
        SinglePayment actual = rawToXs2aPaymentMapper.mapToSinglePayment(paymentBody);

        // Then
        assertEquals(expectedSinglePayment, actual);
    }

    @Test
    void mapToSinglePayment_null() {
        // When
        SinglePayment actual = rawToXs2aPaymentMapper.mapToSinglePayment(null);

        // Then
        assertNull(actual);
        verify(paymentModelMapper, never()).mapToXs2aPayment(any(PaymentInitiationJson.class));
    }

    @Test
    void mapToSinglePayment_malformedBody() {
        // When
        SinglePayment actual = rawToXs2aPaymentMapper.mapToSinglePayment(MALFORMED_PAYMENT_BODY);

        // Then
        assertNull(actual);
        verify(paymentModelMapper, never()).mapToXs2aPayment(any(PaymentInitiationJson.class));
    }

    @Test
    void mapToSinglePayment_nullSinglePayment() {
        // Given
        PaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(SINGLE_PAYMENT_PSD2_JSON_PATH, PaymentInitiationJson.class);
        when(paymentModelMapper.mapToXs2aPayment(paymentInitiationJson)).thenReturn(null);

        byte[] paymentBody = jsonReader.getBytesFromFile(SINGLE_PAYMENT_PSD2_JSON_PATH);

        // When
        SinglePayment actual = rawToXs2aPaymentMapper.mapToSinglePayment(paymentBody);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToPeriodicPayment() {
        // Given
        PeriodicPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(PERIODIC_PAYMENT_PSD2_JSON_PATH, PeriodicPaymentInitiationJson.class);
        PeriodicPayment xs2aPeriodicPayment = jsonReader.getObjectFromFile(PERIODIC_PAYMENT_XS2A_JSON_PATH, PeriodicPayment.class);
        when(paymentModelMapper.mapToXs2aPayment(paymentInitiationJson)).thenReturn(xs2aPeriodicPayment);

        byte[] paymentBody = jsonReader.getBytesFromFile(PERIODIC_PAYMENT_PSD2_JSON_PATH);
        PeriodicPayment expectedPeriodicPayment = jsonReader.getObjectFromFile(PERIODIC_PAYMENT_XS2A_JSON_PATH, PeriodicPayment.class);
        expectedPeriodicPayment.setPaymentData(paymentBody);

        // When
        PeriodicPayment actual = rawToXs2aPaymentMapper.mapToPeriodicPayment(paymentBody);

        // Then
        assertEquals(expectedPeriodicPayment, actual);
    }

    @Test
    void mapToPeriodicPayment_null() {
        // When
        PeriodicPayment actual = rawToXs2aPaymentMapper.mapToPeriodicPayment(null);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToPeriodicPayment_malformedBody() {
        // When
        SinglePayment actual = rawToXs2aPaymentMapper.mapToPeriodicPayment(MALFORMED_PAYMENT_BODY);

        // Then
        assertNull(actual);
        verify(paymentModelMapper, never()).mapToXs2aPayment(any(PeriodicPaymentInitiationJson.class));
    }

    @Test
    void mapToPeriodicPayment_nullPeriodicPayment() {
        // Given
        PeriodicPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(PERIODIC_PAYMENT_PSD2_JSON_PATH, PeriodicPaymentInitiationJson.class);
        when(paymentModelMapper.mapToXs2aPayment(paymentInitiationJson)).thenReturn(null);

        byte[] paymentBody = jsonReader.getBytesFromFile(PERIODIC_PAYMENT_PSD2_JSON_PATH);

        // When
        PeriodicPayment actual = rawToXs2aPaymentMapper.mapToPeriodicPayment(paymentBody);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToBulkPayment() {
        // Given
        BulkPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(BULK_PAYMENT_PSD2_JSON_PATH, BulkPaymentInitiationJson.class);
        BulkPayment xs2aBulkPayment = jsonReader.getObjectFromFile(BULK_PAYMENT_XS2A_JSON_PATH, BulkPayment.class);
        when(paymentModelMapper.mapToXs2aPayment(paymentInitiationJson)).thenReturn(xs2aBulkPayment);

        byte[] paymentBody = jsonReader.getBytesFromFile(BULK_PAYMENT_PSD2_JSON_PATH);
        BulkPayment expectedBulkPayment = jsonReader.getObjectFromFile(BULK_PAYMENT_XS2A_JSON_PATH, BulkPayment.class);
        expectedBulkPayment.setPaymentData(paymentBody);

        // When
        BulkPayment actual = rawToXs2aPaymentMapper.mapToBulkPayment(paymentBody);

        // Then
        assertEquals(expectedBulkPayment, actual);
    }

    @Test
    void mapToBulkPayment_null() {
        // When
        BulkPayment actual = rawToXs2aPaymentMapper.mapToBulkPayment(null);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToBulkPayment_malformedBody() {
        // When
        BulkPayment actual = rawToXs2aPaymentMapper.mapToBulkPayment(MALFORMED_PAYMENT_BODY);

        // Then
        assertNull(actual);
        verify(paymentModelMapper, never()).mapToXs2aPayment(any(BulkPaymentInitiationJson.class));
    }

    @Test
    void mapToBulkPayment_nullBulkPayment() {
        // Given
        BulkPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(BULK_PAYMENT_PSD2_JSON_PATH, BulkPaymentInitiationJson.class);
        when(paymentModelMapper.mapToXs2aPayment(paymentInitiationJson)).thenReturn(null);

        byte[] paymentBody = jsonReader.getBytesFromFile(BULK_PAYMENT_PSD2_JSON_PATH);

        // When
        BulkPayment actual = rawToXs2aPaymentMapper.mapToBulkPayment(paymentBody);

        // Then
        assertNull(actual);
    }
}
