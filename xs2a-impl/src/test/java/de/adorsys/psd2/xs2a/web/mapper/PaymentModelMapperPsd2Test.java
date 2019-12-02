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

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.PaymentInitiationStatusResponse200Json;
import de.adorsys.psd2.model.PaymentInitiationWithStatusResponse;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.pis.GetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapperImpl;
import de.adorsys.psd2.xs2a.service.mapper.AmountModelMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CoreObjectsMapper.class, TppRedirectUriMapper.class, Xs2aObjectMapper.class,
    HrefLinkMapper.class, StandardPaymentProductsResolver.class, ScaMethodsMapperImpl.class, Xs2aAddressMapperImpl.class,
    RemittanceMapperImpl.class, PurposeCodeMapperImpl.class})
public class PaymentModelMapperPsd2Test {
    private static final String PAYMENT_ID = "594ef79c-d785-41ec-9b14-2ea3a7ae2c7b";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.ACCP;
    private static final boolean FUNDS_AVAILABLE = true;
    private static final GetPaymentStatusResponse PAYMENT_STATUS_RESPONSE = new GetPaymentStatusResponse(TRANSACTION_STATUS, FUNDS_AVAILABLE, MediaType.APPLICATION_JSON, null);
    private static final String AMOUNT = "100";
    private static final List<NotificationSupportedMode> NOTIFICATION_MODES = Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST);

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
    @MockBean
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    private JsonReader jsonReader = new JsonReader();
    private AccountModelMapper accountModelMapper;
    private AmountModelMapper amountModelMapper;

    @Before
    public void setUp() {
        accountModelMapper = new AccountModelMapperImpl();
        amountModelMapper = new AmountModelMapper(null);
        mapper = new PaymentModelMapperPsd2(coreObjectsMapper, accountModelMapper, tppRedirectUriMapper,
                                            amountModelMapper, hrefLinkMapper, standardPaymentProductsResolver,
                                            scaMethodsMapper, xs2aAddressMapper, remittanceMapper, purposeCodeMapper);
        when(aspspProfileServiceWrapper.getNotificationSupportedModes()).thenReturn(NOTIFICATION_MODES);
    }

    @Test
    public void mapToPaymentCancellationRequest() {
        PisPaymentCancellationRequest actualPaymentCancellationRequest = mapper.mapToPaymentCancellationRequest(PAYMENT_PRODUCT, PaymentType.SINGLE.getValue(), PAYMENT_ID, Boolean.TRUE, "ok_url", "nok_url");

        PisPaymentCancellationRequest expected = jsonReader.getObjectFromFile("json/service/mapper/payment-cancellation-request.json", PisPaymentCancellationRequest.class);
        assertEquals(expected, actualPaymentCancellationRequest);
    }

    @Test
    public void mapToStatusResponse_ShouldMapCorrectly() {
        PaymentInitiationStatusResponse200Json response = mapper.mapToStatusResponseJson(PAYMENT_STATUS_RESPONSE);
        assertEquals(de.adorsys.psd2.model.TransactionStatus.ACCP, response.getTransactionStatus());
        assertEquals(true, response.getFundsAvailable());
    }

    @Test
    public void mapToStatusResponseRaw_shouldReturnBytesFromResponse() {
        // Given
        byte[] rawPaymentStatusBody = "some raw body".getBytes();
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TRANSACTION_STATUS, FUNDS_AVAILABLE, MediaType.APPLICATION_XML, rawPaymentStatusBody);

        // When
        byte[] actual = mapper.mapToStatusResponseRaw(getPaymentStatusResponse);

        // Then
        assertEquals(rawPaymentStatusBody, actual);
    }

    @Test
    public void mapToGetPaymentResponse_SinglePayment() {
        SinglePayment inputData = getSpiSingle();
        PaymentInitiationWithStatusResponse actualResponse = (PaymentInitiationWithStatusResponse) mapper.mapToGetPaymentResponse(inputData, PaymentType.SINGLE, "sepa-credit-transfers");
        PaymentInitiationWithStatusResponse expectedResponse = jsonReader.getObjectFromFile("json/service/mapper/payment-initiation-with-status-response.json", PaymentInitiationWithStatusResponse.class);
        assertEquals(expectedResponse, actualResponse);
    }

    private SinglePayment getSpiSingle() {
        SinglePayment spiPayment = new SinglePayment();
        spiPayment.setEndToEndIdentification("WBG-123456789");
        spiPayment.setDebtorAccount(getSpiAccountReference());
        spiPayment.setInstructedAmount(new Xs2aAmount(Currency.getInstance("EUR"), AMOUNT));
        spiPayment.setCreditorAccount(getSpiAccountReference());
        spiPayment.setCreditorAgent("AAAADEBBXXX");
        spiPayment.setCreditorName("WBG");
        spiPayment.setCreditorAddress(getXs2aAddress());
        spiPayment.setRemittanceInformationUnstructured("Ref. Number WBG-1222");
        spiPayment.setRequestedExecutionDate(LocalDate.of(2025, 5, 5));
        spiPayment.setTransactionStatus(TransactionStatus.RCVD);
        return spiPayment;
    }

    private AccountReference getSpiAccountReference() {
        return jsonReader.getObjectFromFile("json/service/mapper/spi-account-reference.json", AccountReference.class);
    }

    private Xs2aAddress getXs2aAddress() {
        return jsonReader.getObjectFromFile("json/service/mapper/xs2a-address.json", Xs2aAddress.class);
    }
}
