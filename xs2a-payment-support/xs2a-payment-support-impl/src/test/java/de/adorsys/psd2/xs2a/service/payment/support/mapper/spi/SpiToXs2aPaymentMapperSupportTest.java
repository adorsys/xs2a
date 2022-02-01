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

package de.adorsys.psd2.xs2a.service.payment.support.mapper.spi;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.Xs2aToPsd2PaymentSupportMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpiToXs2aPaymentMapperSupportTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String XS2A_SINGLE_PAYMENT_JSON_PATH = "json/support/mapper/xs2a-single-payment.json";
    private static final String SINGLE_PAYMENT_INITIATION_JSON_PATH = "json/support/mapper/single-payment-initiation.json";
    private static final String XS2A_PERIODIC_PAYMENT_JSON_PATH = "json/support/mapper/xs2a-periodic-payment.json";
    private static final String PERIODIC_PAYMENT_INITIATION_JSON_PATH = "json/support/mapper/periodic-payment-initiation.json";
    private static final String XS2A_BULK_PAYMENT_JSON_PATH = "json/support/mapper/xs2a-bulk-payment.json";
    private static final String BULK_PAYMENT_INITIATION_JSON_PATH = "json/support/mapper/bulk-payment-initiation.json";

    @Mock
    private SpiToXs2aSinglePaymentMapper spiToXs2aSinglePaymentMapper;
    @Mock
    private SpiToXs2aPeriodicPaymentMapper spiToXs2aPeriodicPaymentMapper;
    @Mock
    private SpiToXs2aBulkPaymentMapper spiToXs2aBulkPaymentMapper;
    @Mock
    private Xs2aToPsd2PaymentSupportMapper xs2aToPsd2PaymentSupportMapper;
    @Mock
    private Xs2aObjectMapper mockXs2aObjectMapper;

    private Xs2aObjectMapper xs2aObjectMapper = new ObjectMapperConfig().xs2aObjectMapper();

    private SpiToXs2aPaymentMapperSupport spiToXs2aPaymentMapperSupport;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        spiToXs2aPaymentMapperSupport = new SpiToXs2aPaymentMapperSupport(spiToXs2aSinglePaymentMapper,
                                                                          spiToXs2aPeriodicPaymentMapper,
                                                                          spiToXs2aBulkPaymentMapper,
                                                                          xs2aToPsd2PaymentSupportMapper,
                                                                          xs2aObjectMapper);
    }

    @Test
    void mapToSinglePayment() throws JsonProcessingException {
        // Given
        SpiSinglePayment spiSinglePayment = new SpiSinglePayment(PAYMENT_PRODUCT);

        SinglePayment xs2aSinglePayment = jsonReader.getObjectFromFile(XS2A_SINGLE_PAYMENT_JSON_PATH, SinglePayment.class);
        when(spiToXs2aSinglePaymentMapper.mapToXs2aSinglePayment(spiSinglePayment)).thenReturn(xs2aSinglePayment);

        PaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(SINGLE_PAYMENT_INITIATION_JSON_PATH, PaymentInitiationJson.class);
        when(xs2aToPsd2PaymentSupportMapper.mapToPaymentInitiationJson(xs2aSinglePayment)).thenReturn(paymentInitiationJson);

        SinglePayment expectedPayment = jsonReader.getObjectFromFile(XS2A_SINGLE_PAYMENT_JSON_PATH, SinglePayment.class);
        expectedPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));

        // When
        SinglePayment actualPayment = spiToXs2aPaymentMapperSupport.mapToSinglePayment(spiSinglePayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToSinglePayment_nullPayment() {
        // When
        SinglePayment actualPayment = spiToXs2aPaymentMapperSupport.mapToSinglePayment(null);

        // Then
        assertNull(actualPayment);
    }

    @Test
    void mapToSinglePayment_nullPsd2Payment() {
        // Given
        SpiSinglePayment spiSinglePayment = new SpiSinglePayment(PAYMENT_PRODUCT);

        SinglePayment xs2aSinglePayment = jsonReader.getObjectFromFile(XS2A_SINGLE_PAYMENT_JSON_PATH, SinglePayment.class);
        when(spiToXs2aSinglePaymentMapper.mapToXs2aSinglePayment(spiSinglePayment)).thenReturn(xs2aSinglePayment);

        SinglePayment expectedPayment = jsonReader.getObjectFromFile(XS2A_SINGLE_PAYMENT_JSON_PATH, SinglePayment.class);
        expectedPayment.setPaymentData(new byte[0]);

        // When
        SinglePayment actualPayment = spiToXs2aPaymentMapperSupport.mapToSinglePayment(spiSinglePayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToSinglePayment_serializationException() throws JsonProcessingException {
        // Given
        SpiToXs2aPaymentMapperSupport spiToXs2aPaymentMapperSupportWithMock =
            new SpiToXs2aPaymentMapperSupport(spiToXs2aSinglePaymentMapper,
                                              spiToXs2aPeriodicPaymentMapper,
                                              spiToXs2aBulkPaymentMapper,
                                              xs2aToPsd2PaymentSupportMapper,
                                              mockXs2aObjectMapper);

        SpiSinglePayment spiSinglePayment = new SpiSinglePayment(PAYMENT_PRODUCT);

        SinglePayment xs2aSinglePayment = jsonReader.getObjectFromFile(XS2A_SINGLE_PAYMENT_JSON_PATH, SinglePayment.class);
        when(spiToXs2aSinglePaymentMapper.mapToXs2aSinglePayment(spiSinglePayment)).thenReturn(xs2aSinglePayment);

        PaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(SINGLE_PAYMENT_INITIATION_JSON_PATH, PaymentInitiationJson.class);
        when(xs2aToPsd2PaymentSupportMapper.mapToPaymentInitiationJson(xs2aSinglePayment)).thenReturn(paymentInitiationJson);

        when(mockXs2aObjectMapper.writeValueAsBytes(any())).thenThrow(JsonProcessingException.class);

        SinglePayment expectedPayment = jsonReader.getObjectFromFile(XS2A_SINGLE_PAYMENT_JSON_PATH, SinglePayment.class);
        expectedPayment.setPaymentData(new byte[0]);

        // When
        SinglePayment actualPayment = spiToXs2aPaymentMapperSupportWithMock.mapToSinglePayment(spiSinglePayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToPeriodicPayment() throws JsonProcessingException {
        // Given
        SpiPeriodicPayment spiPeriodicPayment = new SpiPeriodicPayment(PAYMENT_PRODUCT);

        PeriodicPayment xs2aPeriodicPayment = jsonReader.getObjectFromFile(XS2A_PERIODIC_PAYMENT_JSON_PATH, PeriodicPayment.class);
        when(spiToXs2aPeriodicPaymentMapper.mapToXs2aPeriodicPayment(spiPeriodicPayment)).thenReturn(xs2aPeriodicPayment);

        PeriodicPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(PERIODIC_PAYMENT_INITIATION_JSON_PATH, PeriodicPaymentInitiationJson.class);
        when(xs2aToPsd2PaymentSupportMapper.mapToPeriodicPaymentInitiationJson(xs2aPeriodicPayment)).thenReturn(paymentInitiationJson);

        PeriodicPayment expectedPayment = jsonReader.getObjectFromFile(XS2A_PERIODIC_PAYMENT_JSON_PATH, PeriodicPayment.class);
        expectedPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));

        // When
        PeriodicPayment actualPayment = spiToXs2aPaymentMapperSupport.mapToPeriodicPayment(spiPeriodicPayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToPeriodicPayment_nullPayment() {
        // When
        PeriodicPayment actualPayment = spiToXs2aPaymentMapperSupport.mapToPeriodicPayment(null);

        // Then
        assertNull(actualPayment);
    }

    @Test
    void mapToPeriodicPayment_nullPsd2Payment() {
        // Given
        SpiPeriodicPayment spiPeriodicPayment = new SpiPeriodicPayment(PAYMENT_PRODUCT);

        PeriodicPayment xs2aPeriodicPayment = jsonReader.getObjectFromFile(XS2A_PERIODIC_PAYMENT_JSON_PATH, PeriodicPayment.class);
        when(spiToXs2aPeriodicPaymentMapper.mapToXs2aPeriodicPayment(spiPeriodicPayment)).thenReturn(xs2aPeriodicPayment);

        PeriodicPayment expectedPayment = jsonReader.getObjectFromFile(XS2A_PERIODIC_PAYMENT_JSON_PATH, PeriodicPayment.class);
        expectedPayment.setPaymentData(new byte[0]);

        // When
        PeriodicPayment actualPayment = spiToXs2aPaymentMapperSupport.mapToPeriodicPayment(spiPeriodicPayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToPeriodicPayment_serializationException() throws JsonProcessingException {
        // Given
        SpiToXs2aPaymentMapperSupport spiToXs2aPaymentMapperSupportWithMock =
            new SpiToXs2aPaymentMapperSupport(spiToXs2aSinglePaymentMapper,
                                              spiToXs2aPeriodicPaymentMapper,
                                              spiToXs2aBulkPaymentMapper,
                                              xs2aToPsd2PaymentSupportMapper,
                                              mockXs2aObjectMapper);

        SpiPeriodicPayment spiPeriodicPayment = new SpiPeriodicPayment(PAYMENT_PRODUCT);

        PeriodicPayment xs2aPeriodicPayment = jsonReader.getObjectFromFile(XS2A_PERIODIC_PAYMENT_JSON_PATH, PeriodicPayment.class);
        when(spiToXs2aPeriodicPaymentMapper.mapToXs2aPeriodicPayment(spiPeriodicPayment)).thenReturn(xs2aPeriodicPayment);

        PeriodicPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(PERIODIC_PAYMENT_INITIATION_JSON_PATH, PeriodicPaymentInitiationJson.class);
        when(xs2aToPsd2PaymentSupportMapper.mapToPeriodicPaymentInitiationJson(xs2aPeriodicPayment)).thenReturn(paymentInitiationJson);

        when(mockXs2aObjectMapper.writeValueAsBytes(any())).thenThrow(JsonProcessingException.class);

        PeriodicPayment expectedPayment = jsonReader.getObjectFromFile(XS2A_PERIODIC_PAYMENT_JSON_PATH, PeriodicPayment.class);
        expectedPayment.setPaymentData(new byte[0]);

        // When
        PeriodicPayment actualPayment = spiToXs2aPaymentMapperSupportWithMock.mapToPeriodicPayment(spiPeriodicPayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToBulkPayment() throws JsonProcessingException {
        // Given
        SpiBulkPayment spiBulkPayment = new SpiBulkPayment();

        BulkPayment xs2aBulkPayment = jsonReader.getObjectFromFile(XS2A_BULK_PAYMENT_JSON_PATH, BulkPayment.class);
        when(spiToXs2aBulkPaymentMapper.mapToXs2aBulkPayment(spiBulkPayment)).thenReturn(xs2aBulkPayment);

        BulkPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(BULK_PAYMENT_INITIATION_JSON_PATH, BulkPaymentInitiationJson.class);
        when(xs2aToPsd2PaymentSupportMapper.mapToBulkPaymentInitiationJson(xs2aBulkPayment)).thenReturn(paymentInitiationJson);

        BulkPayment expectedPayment = jsonReader.getObjectFromFile(XS2A_BULK_PAYMENT_JSON_PATH, BulkPayment.class);
        expectedPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));

        // When
        BulkPayment actualPayment = spiToXs2aPaymentMapperSupport.mapToBulkPayment(spiBulkPayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToBulkPayment_nullPayment() {
        // When
        BulkPayment actualPayment = spiToXs2aPaymentMapperSupport.mapToBulkPayment(null);

        // Then
        assertNull(actualPayment);
    }

    @Test
    void mapToBulkPayment_nullPsd2Payment() {
        // Given
        SpiBulkPayment spiBulkPayment = new SpiBulkPayment();

        BulkPayment xs2aBulkPayment = jsonReader.getObjectFromFile(XS2A_BULK_PAYMENT_JSON_PATH, BulkPayment.class);
        when(spiToXs2aBulkPaymentMapper.mapToXs2aBulkPayment(spiBulkPayment)).thenReturn(xs2aBulkPayment);

        BulkPayment expectedPayment = jsonReader.getObjectFromFile(XS2A_BULK_PAYMENT_JSON_PATH, BulkPayment.class);
        expectedPayment.setPaymentData(new byte[0]);

        // When
        BulkPayment actualPayment = spiToXs2aPaymentMapperSupport.mapToBulkPayment(spiBulkPayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToBulkPayment_serializationException() throws JsonProcessingException {
        // Given
        SpiToXs2aPaymentMapperSupport spiToXs2aPaymentMapperSupportWithMock =
            new SpiToXs2aPaymentMapperSupport(spiToXs2aSinglePaymentMapper,
                                              spiToXs2aPeriodicPaymentMapper,
                                              spiToXs2aBulkPaymentMapper,
                                              xs2aToPsd2PaymentSupportMapper,
                                              mockXs2aObjectMapper);

        SpiBulkPayment spiBulkPayment = new SpiBulkPayment();

        BulkPayment xs2aBulkPayment = jsonReader.getObjectFromFile(XS2A_BULK_PAYMENT_JSON_PATH, BulkPayment.class);
        when(spiToXs2aBulkPaymentMapper.mapToXs2aBulkPayment(spiBulkPayment)).thenReturn(xs2aBulkPayment);

        BulkPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(BULK_PAYMENT_INITIATION_JSON_PATH, BulkPaymentInitiationJson.class);
        when(xs2aToPsd2PaymentSupportMapper.mapToBulkPaymentInitiationJson(xs2aBulkPayment)).thenReturn(paymentInitiationJson);

        when(mockXs2aObjectMapper.writeValueAsBytes(any())).thenThrow(JsonProcessingException.class);

        BulkPayment expectedPayment = jsonReader.getObjectFromFile(XS2A_BULK_PAYMENT_JSON_PATH, BulkPayment.class);
        expectedPayment.setPaymentData(new byte[0]);

        // When
        BulkPayment actualPayment = spiToXs2aPaymentMapperSupportWithMock.mapToBulkPayment(spiBulkPayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }
}
