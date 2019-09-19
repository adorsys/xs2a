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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aToCmsPisCommonPaymentRequestMapperTest {
    private final static JsonReader jsonReader = new JsonReader();
    private final static String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private final static byte[] PAYMENT_DATA = "test".getBytes();

    private SinglePayment singlePayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-single-payment.json", SinglePayment.class);

    @InjectMocks
    private Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;
    @Mock
    private Xs2aRemittanceMapper xs2aRemittanceMapper;

    @Test
    public void mapToPisPaymentInfo() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/payment-initiation-parameters.json", PaymentInitiationParameters.class);
        TppInfo tppInfo = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/tpp-info.json", TppInfo.class);
        PaymentInitiationResponse response = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/payment-initiation-response.json", SinglePaymentInitiationResponse.class);

        PisPaymentInfo expected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/pis-payment-info.json", PisPaymentInfo.class);
        expected.setPaymentData(PAYMENT_DATA);

        // When
        PisPaymentInfo actual = xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(paymentInitiationParameters, tppInfo, response, PAYMENT_DATA);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToCmsSinglePisCommonPaymentRequest() {
        // Given
        SinglePayment singlePayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-single-payment.json", SinglePayment.class);
        PisCommonPaymentRequest expected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/pis-common-payment-request.json", PisCommonPaymentRequest.class);

        // When
        PisCommonPaymentRequest actual = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsSinglePisCommonPaymentRequest(singlePayment, PAYMENT_PRODUCT);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToCmsSinglePisCommonPaymentRequestWithoutCreditorAddress() {
        // Given
        SinglePayment singlePayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-single-payment-without-creditor-address.json", SinglePayment.class);
        PisCommonPaymentRequest expected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/pis-common-payment-request-without-creditor-address.json", PisCommonPaymentRequest.class);

        // When
        PisCommonPaymentRequest actual = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsSinglePisCommonPaymentRequest(singlePayment, PAYMENT_PRODUCT);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToCmsPeriodicPisCommonPaymentRequest() {
        // Given
        PeriodicPayment periodicPayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-periodic-payment.json", PeriodicPayment.class);
        PisCommonPaymentRequest expected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/pis-common-payment-request-periodic.json", PisCommonPaymentRequest.class);

        // When
        PisCommonPaymentRequest actual = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsPeriodicPisCommonPaymentRequest(periodicPayment, PAYMENT_PRODUCT);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToListPisPayment() {
        //Given
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setPayments(Collections.singletonList(singlePayment));
        //When
        PisCommonPaymentRequest pisCommonPaymentRequest = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsBulkPisCommonPaymentRequest(bulkPayment, PAYMENT_PRODUCT);
        //Then
        List<PisPayment> payments = pisCommonPaymentRequest.getPayments();
        PisPayment pisPaymentActual = payments.get(0);
        PisPayment pisPaymentExpected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/pis-payment.json", PisPayment.class);
        assertEquals(pisPaymentExpected, pisPaymentActual);
    }

    @Test
    public void mapToListPisPayment_BatchBookingPreferred_True() {
        //Given
        BulkPayment bulkPayment = buildBulkPayment(Boolean.TRUE);
        //When
        PisCommonPaymentRequest pisCommonPaymentRequest = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsBulkPisCommonPaymentRequest(bulkPayment, PAYMENT_PRODUCT);
        //Then
        List<PisPayment> payments = pisCommonPaymentRequest.getPayments();
        PisPayment pisPayment = payments.get(0);
        assertEquals(bulkPayment.getBatchBookingPreferred(), pisPayment.getBatchBookingPreferred());
    }

    @Test
    public void mapToListPisPayment_BatchBookingPreferred_False() {
        //Given
        BulkPayment bulkPayment = buildBulkPayment(Boolean.FALSE);
        //When
        PisCommonPaymentRequest pisCommonPaymentRequest = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsBulkPisCommonPaymentRequest(bulkPayment, PAYMENT_PRODUCT);
        //Then
        List<PisPayment> payments = pisCommonPaymentRequest.getPayments();
        PisPayment pisPayment = payments.get(0);
        assertEquals(bulkPayment.getBatchBookingPreferred(), pisPayment.getBatchBookingPreferred());
    }

    public BulkPayment buildBulkPayment(Boolean batchBookingPreferred) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setBatchBookingPreferred(batchBookingPreferred);
        bulkPayment.setPayments(Collections.singletonList(singlePayment));
        return bulkPayment;
    }
}
