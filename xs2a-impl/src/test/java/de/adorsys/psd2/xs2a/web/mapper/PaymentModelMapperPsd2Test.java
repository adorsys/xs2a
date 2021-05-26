/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.tpp.TppAttributes;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aChosenScaMethod;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.exception.WrongPaymentTypeException;
import de.adorsys.psd2.xs2a.service.mapper.AmountModelMapper;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.Validation;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreObjectsMapper.class, TppRedirectUriMapper.class,
    HrefLinkMapper.class, Xs2aObjectMapper.class, ScaMethodsMapperImpl.class, StandardPaymentProductsResolver.class,
    TppMessage201MapperImpl.class, TppMessageGenericMapperImpl.class})
class PaymentModelMapperPsd2Test {
    private static final String PAYMENT_ID = "594ef79c-d785-41ec-9b14-2ea3a7ae2c7b";
    private static final String ENCRYPTED_PAYMENT_ID = "2Cixxv85Or_qoBBh_d7VTZC0M8PwzR5IGzsJuT-jYHNOMR1D7n69vIF46RgFd7Zn_=_bS6p6XvTWI";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_TYPE = "payments";
    private static final String NON_STANDARD_PAYMENT_PRODUCT = "pain.001-sepa-credit-transfers";
    private static final String PSU_MESSAGE = "PSU message";
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.ACCP;
    private static final boolean FUNDS_AVAILABLE = true;
    private static final GetPaymentStatusResponse PAYMENT_STATUS_RESPONSE = new GetPaymentStatusResponse(TRANSACTION_STATUS, FUNDS_AVAILABLE, MediaType.APPLICATION_JSON, null, PSU_MESSAGE, null, null);
    private static final List<NotificationSupportedMode> NOTIFICATION_MODES = Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST);
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("123456789", null, null, null, null);
    private static final String TPP_BRAND_LOGGING_INFORMATION = "tppBrandLoggingInformation";

    private PaymentModelMapperPsd2 mapper;

    @Autowired
    private CoreObjectsMapper coreObjectsMapper;
    @Autowired
    private TppRedirectUriMapper tppRedirectUriMapper;
    @Autowired
    private HrefLinkMapper hrefLinkMapper;
    @Autowired
    private ScaMethodsMapper scaMethodsMapper;
    @Autowired
    private StandardPaymentProductsResolver standardPaymentProductsResolver;
    @Autowired
    private Xs2aObjectMapper xs2aObjectMapper;
    @Autowired
    private TppMessage201Mapper tppMessage201Mapper;
    @Autowired
    private TppMessageGenericMapper tppMessageGenericMapper;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        ValueValidatorService validatorService = new ValueValidatorService(Validation.buildDefaultValidatorFactory().getValidator());
        AmountModelMapper amountModelMapper = new AmountModelMapper(validatorService);
        mapper = new PaymentModelMapperPsd2(coreObjectsMapper, tppRedirectUriMapper, amountModelMapper,
                                            hrefLinkMapper, scaMethodsMapper, standardPaymentProductsResolver,
                                            xs2aObjectMapper, tppMessage201Mapper, tppMessageGenericMapper);
    }

    @Test
    void mapToGetPaymentResponse_standardPayment() {
        CommonPayment payment = new CommonPayment();
        payment.setPaymentData(jsonReader.getBytesFromFile("json/service/mapper/common-payment.json"));
        payment.setTransactionStatus(TransactionStatus.RCVD);
        payment.setPaymentProduct(PAYMENT_PRODUCT);

        Map actual = (Map) mapper.mapToGetPaymentResponse(payment);

        assertEquals(7, actual.size());
        assertEquals(PAYMENT_TYPE, actual.get("paymentType"));
        assertEquals("26bb59a3-2f63-4027-ad38-67d87e59611a", actual.get("aspspAccountId"));
        assertEquals(TransactionStatus.RCVD.name(), actual.get("transactionStatus"));
        assertEquals(ENCRYPTED_PAYMENT_ID, actual.get("paymentId"));
        assertEquals(false, actual.get("transactionFeeIndicator"));
        assertEquals(true, actual.get("multilevelScaRequired"));
        assertTrue(((Map) actual.get("aspspConsentDataProvider")).isEmpty());
    }

    @Test
    void mapToGetPaymentResponse_nonStandardPayment() {
        CommonPayment payment = new CommonPayment();
        payment.setPaymentData(jsonReader.getBytesFromFile("json/service/mapper/common-payment.json"));
        payment.setTransactionStatus(TransactionStatus.RCVD);
        payment.setPaymentProduct(NON_STANDARD_PAYMENT_PRODUCT);

        String actual = (String) mapper.mapToGetPaymentResponse(payment);

        assertEquals(jsonReader.getStringFromFile("json/service/mapper/common-payment.json"), actual);
    }

    @Test
    void mapToGetPaymentResponse_enrich_with_additional_data() {
        CommonPayment payment = new CommonPayment();
        payment.setTransactionStatus(TransactionStatus.RCVD);
        payment.setPaymentProduct(PAYMENT_PRODUCT);
        payment.setPaymentData("incorrectPaymentData".getBytes());

        Object actual = mapper.mapToGetPaymentResponse(payment);

        assertEquals("incorrectPaymentData", actual);
    }

    @Test
    void mapToPaymentCancellationRequest() {
        PisPaymentCancellationRequest actualPaymentCancellationRequest = mapper.mapToPaymentCancellationRequest(PAYMENT_PRODUCT, PaymentType.SINGLE.getValue(), PAYMENT_ID, Boolean.TRUE, "ok_url", "nok_url");

        PisPaymentCancellationRequest expected = jsonReader.getObjectFromFile("json/service/mapper/payment-cancellation-request.json", PisPaymentCancellationRequest.class);
        assertEquals(expected, actualPaymentCancellationRequest);
    }

    @Test
    void mapToStatusResponse_ShouldMapCorrectly() {
        PaymentInitiationStatusResponse200Json response = mapper.mapToStatusResponseJson(PAYMENT_STATUS_RESPONSE);
        assertEquals(de.adorsys.psd2.model.TransactionStatus.ACCP, response.getTransactionStatus());
        assertEquals(true, response.getFundsAvailable());
    }

    @Test
    void mapToStatusResponseRaw_shouldReturnBytesFromResponse() {
        // Given
        byte[] rawPaymentStatusBody = "some raw body".getBytes();
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TRANSACTION_STATUS, FUNDS_AVAILABLE, MediaType.APPLICATION_XML, rawPaymentStatusBody, PSU_MESSAGE, null, null);

        // When
        byte[] actual = mapper.mapToStatusResponseRaw(getPaymentStatusResponse);

        // Then
        assertEquals(rawPaymentStatusBody, actual);
    }

    @Test
    void mapToPaymentInitiationResponse() {
        PaymentInitiationResponse paymentInitiationResponse = mock(PaymentInitiationResponse.class);
        when(paymentInitiationResponse.getTransactionStatus()).thenReturn(TransactionStatus.RCVD);
        when(paymentInitiationResponse.getPaymentId()).thenReturn(PAYMENT_ID);
        when(paymentInitiationResponse.getTransactionFees()).thenReturn(new Xs2aAmount(Currency.getInstance("EUR"), "20.00"));
        when(paymentInitiationResponse.getTransactionFeeIndicator()).thenReturn(true);
        when(paymentInitiationResponse.getScaMethods()).thenReturn(Collections.singletonList(new de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject()));
        when(paymentInitiationResponse.getChallengeData()).thenReturn(new ChallengeData());
        when(paymentInitiationResponse.getLinks()).thenReturn(null);
        when(paymentInitiationResponse.getPsuMessage()).thenReturn("psu message");
        when(paymentInitiationResponse.getCurrencyConversionFee()).thenReturn(new Xs2aAmount(Currency.getInstance("EUR"), "1200"));
        when(paymentInitiationResponse.getEstimatedTotalAmount()).thenReturn(new Xs2aAmount(Currency.getInstance("EUR"), "1300"));
        when(paymentInitiationResponse.getEstimatedInterbankSettlementAmount()).thenReturn(new Xs2aAmount(Currency.getInstance("EUR"), "1500"));


        PaymentInitationRequestResponse201 expected = jsonReader.getObjectFromFile("json/service/mapper/payment-initiation-response-201.json", PaymentInitationRequestResponse201.class);

        PaymentInitationRequestResponse201 actual = mapper.mapToPaymentInitiationResponse(paymentInitiationResponse);

        assertEquals(expected, actual);
    }

    @Test
    void mapToPaymentRequestParameters() {
        TppNotificationData tppNotificationData = new TppNotificationData(NOTIFICATION_MODES, "notification.uri");
        PaymentInitiationParameters expected = new PaymentInitiationParameters();
        expected.setPaymentProduct(PAYMENT_PRODUCT);
        expected.setPaymentType(PaymentType.SINGLE);
        expected.setQwacCertificate("certificate");
        expected.setTppRedirectUri(new TppRedirectUri("ok.uri", "nok.uri"));
        expected.setTppExplicitAuthorisationPreferred(true);
        expected.setPsuData(PSU_ID_DATA);
        expected.setTppNotificationData(tppNotificationData);
        expected.setTppBrandLoggingInformation(TPP_BRAND_LOGGING_INFORMATION);
        TppAttributes tppAttributes = new TppAttributes("certificate".getBytes(), "ok.uri", "nok.uri", true, tppNotificationData, TPP_BRAND_LOGGING_INFORMATION);

        PaymentInitiationParameters actual = mapper.mapToPaymentRequestParameters(PAYMENT_PRODUCT, PaymentType.SINGLE.getValue(), tppAttributes, PSU_ID_DATA, null);

        assertEquals(expected, actual);
    }

    @Test
    void mapToPaymentRequestParameters_incorrect_payment_type_throw_wrong_payment_type_exception() {
        //Given
        TppNotificationData tppNotificationData = new TppNotificationData(NOTIFICATION_MODES, "notification.uri");
        byte[] tpPSignatureCertificate = "certificate".getBytes();
        //Then
        TppAttributes tppAttributes = new TppAttributes(tpPSignatureCertificate, "ok.uri", "nok.uri", true, tppNotificationData, TPP_BRAND_LOGGING_INFORMATION);
        assertThrows(WrongPaymentTypeException.class, () -> mapper.mapToPaymentRequestParameters(PAYMENT_PRODUCT, "incorrect payment type", tppAttributes, PSU_ID_DATA, null));
    }

    @Test
    void mapToPaymentInitiationCancelResponse() {
        CancelPaymentResponse cancelPaymentResponse = new CancelPaymentResponse();
        cancelPaymentResponse.setTransactionStatus(TransactionStatus.CANC);
        de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject authenticationObjec = new de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject();
        authenticationObjec.setAuthenticationMethodId("authenticationMethodId");
        authenticationObjec.setAuthenticationType("authenticationType");
        authenticationObjec.setAuthenticationVersion("authenticationVersion");
        authenticationObjec.setName("name123");
        cancelPaymentResponse.setScaMethods(Collections.singletonList(authenticationObjec));
        Xs2aChosenScaMethod chosenScaMethod = new Xs2aChosenScaMethod();
        chosenScaMethod.setAuthenticationMethodId("authenticationMethodId1");
        chosenScaMethod.setAuthenticationType("authenticationType");
        cancelPaymentResponse.setChosenScaMethod(chosenScaMethod);
        cancelPaymentResponse.setLinks(null);

        PaymentInitiationCancelResponse202 actual = mapper.mapToPaymentInitiationCancelResponse(cancelPaymentResponse);

        PaymentInitiationCancelResponse202 expected = new PaymentInitiationCancelResponse202();
        expected.setTransactionStatus(de.adorsys.psd2.model.TransactionStatus.CANC);
        ScaMethods scaMethods = new ScaMethods();
        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId("authenticationMethodId");
        authenticationObject.setAuthenticationType("authenticationType");
        authenticationObject.setAuthenticationVersion("authenticationVersion");
        authenticationObject.setName("name123");
        scaMethods.add(authenticationObject);
        expected.setScaMethods(scaMethods);
        ChosenScaMethod expectedChosenScaMethod = new ChosenScaMethod();
        expectedChosenScaMethod.setAuthenticationMethodId("authenticationMethodId1");
        expectedChosenScaMethod.setAuthenticationType("authenticationType");
        expected.setChosenScaMethod(expectedChosenScaMethod);
        expected.setLinks(null);

        assertEquals(expected, actual);
    }
}
