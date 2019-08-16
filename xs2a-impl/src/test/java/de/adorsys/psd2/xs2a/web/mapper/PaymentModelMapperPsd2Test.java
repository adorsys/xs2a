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
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.model.PaymentInitiationStatusResponse200Json;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.GetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CoreObjectsMapper.class, TppRedirectUriMapper.class, ObjectMapper.class,
    HrefLinkMapper.class, StandardPaymentProductsResolver.class, ScaMethodsMapperImpl.class, Xs2aAddressMapperImpl.class,
    RemittanceMapperImpl.class, PurposeCodeMapperImpl.class})
public class PaymentModelMapperPsd2Test {
    private static final String PAYMENT_ID = "594ef79c-d785-41ec-9b14-2ea3a7ae2c7b";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.ACCP;
    private static final boolean FUNDS_AVAILABLE = true;
    private static final GetPaymentStatusResponse PAYMENT_STATUS_RESPONSE = new GetPaymentStatusResponse(TRANSACTION_STATUS, FUNDS_AVAILABLE);

    private PaymentModelMapperPsd2 mapper;
    @Autowired
    private CoreObjectsMapper coreObjectsMapper;
    @Autowired
    private TppRedirectUriMapper tppRedirectUriMapper;
    @Autowired
    private HrefLinkMapper hrefLinkMapper;
    @Autowired
    private StandardPaymentProductsResolver standardPaymentProductsResolver;
    @Autowired
    private ScaMethodsMapper scaMethodsMapper;
    @Autowired
    private Xs2aAddressMapper xs2aAddressMapper;
    @Autowired
    private RemittanceMapper remittanceMapper;
    @Autowired
    private PurposeCodeMapper purposeCodeMapper;

    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        mapper = new PaymentModelMapperPsd2(coreObjectsMapper, null, tppRedirectUriMapper,
                                            null, hrefLinkMapper, standardPaymentProductsResolver,
                                            scaMethodsMapper, xs2aAddressMapper, remittanceMapper, purposeCodeMapper);
    }

    @Test
    public void mapToPaymentCancellationRequest() {
        PisPaymentCancellationRequest actualPaymentCancellationRequest = mapper.mapToPaymentCancellationRequest(PAYMENT_PRODUCT, PaymentType.SINGLE.getValue(), PAYMENT_ID, Boolean.TRUE, "ok_url", "nok_url");

        PisPaymentCancellationRequest expected = jsonReader.getObjectFromFile("json/service/mapper/payment-cancellation-request.json", PisPaymentCancellationRequest.class);
        assertEquals(expected, actualPaymentCancellationRequest);
    }

    @Test
    public void mapToStatusResponse_ShouldMapCorrectly(){
        PaymentInitiationStatusResponse200Json response = mapper.mapToStatusResponse(PAYMENT_STATUS_RESPONSE);
        assertEquals(response.getTransactionStatus(), de.adorsys.psd2.model.TransactionStatus.ACCP);
        assertEquals(response.getFundsAvailable(), true);
    }
}
