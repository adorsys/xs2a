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

package de.adorsys.psd2.consent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.consent.api.pis.CmsCommonPayment;
import de.adorsys.psd2.consent.api.pis.CmsCommonPaymentMapper;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.service.mapper.CmsAddressMapperImpl;
import de.adorsys.psd2.consent.service.mapper.CmsCorePaymentMapper;
import de.adorsys.psd2.core.payment.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.core.payment.model.PaymentInitiationJson;
import de.adorsys.psd2.core.payment.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CmsCorePaymentMapper.class, CmsAddressMapperImpl.class})
class CorePaymentsConvertServiceTest {
    private CorePaymentsConvertService corePaymentsConvertService;

    @Autowired
    private CmsCorePaymentMapper cmsCorePaymentMapper;

    private Xs2aObjectMapper xs2aObjectMapper;
    private CmsCommonPaymentMapper cmsCommonPaymentMapper;
    private PaymentMapperResolver paymentMapperResolver;

    private final JsonReader jsonReader = new JsonReader();
    private PisPayment pisPayment;
    private CmsCommonPayment cmsCommonPayment;

    @BeforeEach
    void setUp() {
        ObjectMapperConfig objectMapperConfig = new ObjectMapperConfig();
        xs2aObjectMapper = objectMapperConfig.xs2aObjectMapper();

        pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        cmsCommonPayment = new CmsCommonPayment("payments");

        cmsCommonPaymentMapper = mock(CmsCommonPaymentMapper.class);

        paymentMapperResolver = mock(PaymentMapperResolver.class);
        when(paymentMapperResolver.getCmsCommonPaymentMapper(any())).thenReturn(cmsCommonPaymentMapper);
        corePaymentsConvertService = new CorePaymentsConvertService(cmsCorePaymentMapper, xs2aObjectMapper, paymentMapperResolver);
    }

    @Test
    void buildPaymentData_singlePayment() throws JsonProcessingException {
        byte[] actual = corePaymentsConvertService.buildPaymentData(Collections.singletonList(pisPayment), PaymentType.SINGLE);

        PaymentInitiationJson expectedPaymentInitiationJson = jsonReader.getObjectFromFile("json/service/mapper/payment-initiation-resp.json", PaymentInitiationJson.class);
        byte[] expected = xs2aObjectMapper.writeValueAsBytes(expectedPaymentInitiationJson);
        assertArrayEquals(expected, actual);
    }

    @Test
    void buildPaymentData_singlePayment_emptyPayments() {
        // When
        byte[] actual = corePaymentsConvertService.buildPaymentData(Collections.emptyList(), PaymentType.SINGLE);

        // Then
        assertArrayEquals(new byte[0], actual);
    }

    @Test
    void buildPaymentData_periodicPayment() throws JsonProcessingException {
        byte[] actual = corePaymentsConvertService.buildPaymentData(Collections.singletonList(pisPayment), PaymentType.PERIODIC);

        PeriodicPaymentInitiationJson expectedPeriodicPaymentInitiationJson = jsonReader.getObjectFromFile("json/service/mapper/periodic-payment-initiation-resp.json", PeriodicPaymentInitiationJson.class);
        byte[] expected = xs2aObjectMapper.writeValueAsBytes(expectedPeriodicPaymentInitiationJson);
        assertArrayEquals(expected, actual);
    }

    @Test
    void buildPaymentData_periodicPayment_emptyPayments() {
        // When
        byte[] actual = corePaymentsConvertService.buildPaymentData(Collections.emptyList(), PaymentType.PERIODIC);

        // Then
        assertArrayEquals(new byte[0], actual);
    }

    @Test
    void buildPaymentData_bulkPayment() throws JsonProcessingException {
        byte[] actual = corePaymentsConvertService.buildPaymentData(Collections.singletonList(pisPayment), PaymentType.BULK);

        BulkPaymentInitiationJson expectedBulkPaymentInitiationJson = jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initiation-resp.json", BulkPaymentInitiationJson.class);
        byte[] expected = xs2aObjectMapper.writeValueAsBytes(expectedBulkPaymentInitiationJson);
        assertArrayEquals(expected, actual);
    }

    @Test
    void buildPaymentData_bulkPayment_emptyPayments() {
        // When
        byte[] actual = corePaymentsConvertService.buildPaymentData(Collections.emptyList(), PaymentType.BULK);

        // Then
        assertArrayEquals(new byte[0], actual);
    }

    @Test
    void expandCommonPaymentWithCorePayment_singlePayment() {
        corePaymentsConvertService = new CorePaymentsConvertService(null, null, paymentMapperResolver);
        cmsCommonPayment.setPaymentType(PaymentType.SINGLE);

        corePaymentsConvertService.expandCommonPaymentWithCorePayment(cmsCommonPayment);

        verify(cmsCommonPaymentMapper, times(1)).mapToCmsSinglePayment(cmsCommonPayment);
    }

    @Test
    void expandCommonPaymentWithCorePayment_periodicPayment() {
        cmsCommonPayment.setPaymentType(PaymentType.PERIODIC);

        corePaymentsConvertService.expandCommonPaymentWithCorePayment(cmsCommonPayment);

        verify(cmsCommonPaymentMapper, times(1)).mapToCmsPeriodicPayment(cmsCommonPayment);
    }

    @Test
    void expandCommonPaymentWithCorePayment_bulkPayment() {
        cmsCommonPayment.setPaymentType(PaymentType.BULK);

        corePaymentsConvertService.expandCommonPaymentWithCorePayment(cmsCommonPayment);

        verify(cmsCommonPaymentMapper, times(1)).mapToCmsBulkPayment(cmsCommonPayment);
    }
}
