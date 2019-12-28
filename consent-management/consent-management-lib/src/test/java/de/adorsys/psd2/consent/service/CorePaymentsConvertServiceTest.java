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

package de.adorsys.psd2.consent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.consent.api.pis.CmsCommonPayment;
import de.adorsys.psd2.consent.api.pis.CmsCommonPaymentMapper;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.service.mapper.CmsCorePaymentMapper;
import de.adorsys.psd2.core.payment.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.core.payment.model.PaymentInitiationJson;
import de.adorsys.psd2.core.payment.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CmsCorePaymentMapper.class, Xs2aObjectMapper.class})
public class CorePaymentsConvertServiceTest {
    private CorePaymentsConvertService corePaymentsConvertService;

    @Autowired
    private CmsCorePaymentMapper cmsCorePaymentMapper;

    @Autowired
    private Xs2aObjectMapper xs2aObjectMapper;

    private CmsCommonPaymentMapper cmsCommonPaymentMapper;

    private JsonReader jsonReader = new JsonReader();
    private PisPayment pisPayment;
    private CmsCommonPayment cmsCommonPayment;

    @Before
    public void setUp() {
        pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        cmsCommonPayment = new CmsCommonPayment("payments");

        cmsCommonPaymentMapper = mock(CmsCommonPaymentMapper.class);
        corePaymentsConvertService = new CorePaymentsConvertService(cmsCorePaymentMapper, xs2aObjectMapper, cmsCommonPaymentMapper);
    }

    @Test
    public void buildPaymentData_singlePayment() throws JsonProcessingException {
        byte[] actual = corePaymentsConvertService.buildPaymentData(Collections.singletonList(pisPayment), PaymentType.SINGLE);

        PaymentInitiationJson expectedPaymentInitiationJson = jsonReader.getObjectFromFile("json/service/mapper/payment-initiation-resp.json", PaymentInitiationJson.class);
        byte[] expected = xs2aObjectMapper.writeValueAsBytes(expectedPaymentInitiationJson);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void buildPaymentData_periodicPayment() throws JsonProcessingException {
        byte[] actual = corePaymentsConvertService.buildPaymentData(Collections.singletonList(pisPayment), PaymentType.PERIODIC);

        PeriodicPaymentInitiationJson expectedPeriodicPaymentInitiationJson = jsonReader.getObjectFromFile("json/service/mapper/periodic-payment-initiation-resp.json", PeriodicPaymentInitiationJson.class);
        byte[] expected = xs2aObjectMapper.writeValueAsBytes(expectedPeriodicPaymentInitiationJson);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void buildPaymentData_bulkPayment() throws JsonProcessingException {
        byte[] actual = corePaymentsConvertService.buildPaymentData(Collections.singletonList(pisPayment), PaymentType.BULK);

        BulkPaymentInitiationJson expectedBulkPaymentInitiationJson = jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initiation-resp.json", BulkPaymentInitiationJson.class);
        byte[] expected = xs2aObjectMapper.writeValueAsBytes(expectedBulkPaymentInitiationJson);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void expandCommonPaymentWithCorePayment_singlePayment() {
        corePaymentsConvertService = new CorePaymentsConvertService(null, null, cmsCommonPaymentMapper);
        cmsCommonPayment.setPaymentType(PaymentType.SINGLE);

        corePaymentsConvertService.expandCommonPaymentWithCorePayment(cmsCommonPayment);

        verify(cmsCommonPaymentMapper, times(1)).mapToCmsSinglePayment(cmsCommonPayment);
    }

    @Test
    public void expandCommonPaymentWithCorePayment_periodicPayment() {
        cmsCommonPayment.setPaymentType(PaymentType.PERIODIC);

        corePaymentsConvertService.expandCommonPaymentWithCorePayment(cmsCommonPayment);

        verify(cmsCommonPaymentMapper, times(1)).mapToCmsPeriodicPayment(cmsCommonPayment);
    }

    @Test
    public void expandCommonPaymentWithCorePayment_bulkPayment() {
        cmsCommonPayment.setPaymentType(PaymentType.BULK);

        corePaymentsConvertService.expandCommonPaymentWithCorePayment(cmsCommonPayment);

        verify(cmsCommonPaymentMapper, times(1)).mapToCmsBulkPayment(cmsCommonPayment);
    }
}
