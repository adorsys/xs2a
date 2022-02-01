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

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CmsToXs2APaymentSupportMapperTest {
    private static final byte[] PAYMENT_BODY = "some payment body".getBytes();
    private static final String PAYMENT_ID =
        "2Cixxv85Or_qoBBh_d7VTZC0M8PwzR5IGzsJuT-jYHNOMR1D7n69vIF46RgFd7Zn_=_bS6p6XvTWI";
    private static final TransactionStatus PAYMENT_STATUS = TransactionStatus.ACCP;
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP =
        OffsetDateTime.of(2019, 12, 27, 12, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime CREATION_TIMESTAMP =
        OffsetDateTime.of(2019, 12, 27, 14, 0, 0, 0, ZoneOffset.UTC);
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";

    @Mock
    private RawToXs2aPaymentMapper rawToXs2aPaymentMapper;

    @InjectMocks
    private CmsToXs2aPaymentSupportMapper cmsToXs2APaymentSupportMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToSinglePayment() {
        // Given
        SinglePayment rawSinglePayment = jsonReader.getObjectFromFile("json/support/mapper/raw-xs2a-single-payment.json", SinglePayment.class);
        when(rawToXs2aPaymentMapper.mapToSinglePayment(PAYMENT_BODY)).thenReturn(rawSinglePayment);

        SinglePayment expectedSinglePayment = jsonReader.getObjectFromFile("json/support/mapper/enriched-xs2a-single-payment.json", SinglePayment.class);
        CommonPaymentData commonPaymentData = buildCommonPaymentData();

        // When
        SinglePayment actual = cmsToXs2APaymentSupportMapper.mapToSinglePayment(commonPaymentData);

        // Then
        assertEquals(expectedSinglePayment, actual);
    }

    @Test
    void mapToSinglePayment_null() {
        // When
        SinglePayment actual = cmsToXs2APaymentSupportMapper.mapToSinglePayment(null);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToPeriodicPayment() {
        // Given
        PeriodicPayment rawPeriodicPayment = jsonReader.getObjectFromFile("json/support/mapper/raw-xs2a-periodic-payment.json", PeriodicPayment.class);
        when(rawToXs2aPaymentMapper.mapToPeriodicPayment(PAYMENT_BODY)).thenReturn(rawPeriodicPayment);

        PeriodicPayment expectedPeriodicPayment = jsonReader.getObjectFromFile("json/support/mapper/enriched-xs2a-periodic-payment.json", PeriodicPayment.class);
        CommonPaymentData commonPaymentData = buildCommonPaymentData();

        // When
        PeriodicPayment actual = cmsToXs2APaymentSupportMapper.mapToPeriodicPayment(commonPaymentData);

        // Then
        assertEquals(expectedPeriodicPayment, actual);
    }

    @Test
    void mapToPeriodicPayment_null() {
        // When
        PeriodicPayment actual = cmsToXs2APaymentSupportMapper.mapToPeriodicPayment(null);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToBulkPayment() {
        // Given
        BulkPayment rawBulkPayment = jsonReader.getObjectFromFile("json/support/mapper/raw-xs2a-bulk-payment.json", BulkPayment.class);
        when(rawToXs2aPaymentMapper.mapToBulkPayment(PAYMENT_BODY)).thenReturn(rawBulkPayment);

        BulkPayment expectedBulkPayment = jsonReader.getObjectFromFile("json/support/mapper/enriched-xs2a-bulk-payment.json", BulkPayment.class);
        CommonPaymentData commonPaymentData = buildCommonPaymentData();

        // When
        BulkPayment actual = cmsToXs2APaymentSupportMapper.mapToBulkPayment(commonPaymentData);

        // Then
        assertEquals(expectedBulkPayment, actual);
    }

    @Test
    void mapToBulkPayment_nullPayments() {
        // Given
        BulkPayment rawBulkPayment = jsonReader.getObjectFromFile("json/support/mapper/raw-xs2a-bulk-payment.json", BulkPayment.class);
        rawBulkPayment.setPayments(null);
        when(rawToXs2aPaymentMapper.mapToBulkPayment(PAYMENT_BODY)).thenReturn(rawBulkPayment);

        BulkPayment expectedBulkPayment = jsonReader.getObjectFromFile("json/support/mapper/enriched-xs2a-bulk-payment.json", BulkPayment.class);
        expectedBulkPayment.setPayments(null);
        CommonPaymentData commonPaymentData = buildCommonPaymentData();

        // When
        BulkPayment actual = cmsToXs2APaymentSupportMapper.mapToBulkPayment(commonPaymentData);

        // Then
        assertEquals(expectedBulkPayment, actual);
    }

    @Test
    void mapToBulkPayment_null() {
        // When
        BulkPayment actual = cmsToXs2APaymentSupportMapper.mapToBulkPayment(null);

        // Then
        assertNull(actual);
    }

    private CommonPaymentData buildCommonPaymentData() {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setPaymentData(PAYMENT_BODY);
        pisCommonPaymentResponse.setExternalId(PAYMENT_ID);
        pisCommonPaymentResponse.setTransactionStatus(PAYMENT_STATUS);
        pisCommonPaymentResponse.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        pisCommonPaymentResponse.setCreationTimestamp(CREATION_TIMESTAMP);
        pisCommonPaymentResponse.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return pisCommonPaymentResponse;
    }
}
