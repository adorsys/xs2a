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

package de.adorsys.psd2.xs2a.service.validator.pis.payment.raw;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.PaymentModelMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentAccountReferenceExtractorTest {
    private static final String SINGLE_PAYMENT_JSON_PATH = "json/service/validator/pis/payment/raw/single-payment.json";
    private static final String SINGLE_PAYMENT_XS2A_JSON_PATH = "json/service/validator/pis/payment/raw/xs2a-single-payment.json";
    private static final String PERIODIC_PAYMENT_JSON_PATH = "json/service/validator/pis/payment/raw/periodic-payment.json";
    private static final String PERIODIC_PAYMENT_XS2A_JSON_PATH = "json/service/validator/pis/payment/raw/xs2a-periodic-payment.json";
    private static final String BULK_PAYMENT_JSON_PATH = "json/service/validator/pis/payment/raw/bulk-payment.json";
    private static final String BULK_PAYMENT_XS2A_JSON_PATH = "json/service/validator/pis/payment/raw/xs2a-bulk-payment.json";
    private static final String MALFORMED_BODY_JSON_PATH = "json/service/validator/pis/payment/raw/malformed-body.json";
    private Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
    private JsonReader jsonReader = new JsonReader();

    @Mock
    private PaymentModelMapper paymentModelMapper;
    @Mock
    private RequestProviderService requestProviderService;

    private PaymentAccountReferenceExtractor paymentAccountReferenceExtractor;

    @BeforeEach
    void setUp() {
        xs2aObjectMapper.findAndRegisterModules();

        paymentAccountReferenceExtractor = new PaymentAccountReferenceExtractor(xs2aObjectMapper, paymentModelMapper);
    }

    @Test
    void extractAccountReferences_singlePayment() {
        // Given
        byte[] rawBody = jsonReader.getBytesFromFile(SINGLE_PAYMENT_JSON_PATH);
        PaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile(SINGLE_PAYMENT_JSON_PATH, PaymentInitiationJson.class);
        SinglePayment xs2aSinglePayment = jsonReader.getObjectFromFile(SINGLE_PAYMENT_XS2A_JSON_PATH, SinglePayment.class);

        when(paymentModelMapper.mapToXs2aPayment(paymentInitiationJson)).thenReturn(xs2aSinglePayment);

        Set<AccountReference> expected = new HashSet<>();
        expected.add(new AccountReference(null, null, "DE52500105173911841934", null, "1111", "23456xxxxxx1234", "0172/1111111", Currency.getInstance("EUR"), null));
        expected.add(new AccountReference(null, null, "DE15500105172295759744", null, "1111", "23456xxxxxx1234", "0172/1111111", Currency.getInstance("EUR"), null));

        // When
        Set<AccountReference> accountReferences = paymentAccountReferenceExtractor.extractAccountReferences(rawBody, PaymentType.SINGLE);

        // Then
        assertEquals(expected, accountReferences);
    }

    @Test
    void extractAccountReferences_periodicPayment() {
        // Given
        byte[] rawBody = jsonReader.getBytesFromFile(PERIODIC_PAYMENT_JSON_PATH);
        PeriodicPaymentInitiationJson periodicPaymentInitiationJson = jsonReader.getObjectFromFile(PERIODIC_PAYMENT_JSON_PATH, PeriodicPaymentInitiationJson.class);
        PeriodicPayment xs2aPeriodicPayment = jsonReader.getObjectFromFile(PERIODIC_PAYMENT_XS2A_JSON_PATH, PeriodicPayment.class);

        when(paymentModelMapper.mapToXs2aPayment(periodicPaymentInitiationJson)).thenReturn(xs2aPeriodicPayment);

        Set<AccountReference> expected = new HashSet<>();
        expected.add(new AccountReference(null, null, "DE89370400440532013000", null, "1111", "23456xxxxxx1234", "0172/1111111", Currency.getInstance("EUR"), null));
        expected.add(new AccountReference(null, null, "LU280019400644750000", null, "1111", "23456xxxxxx1234", "0172/1111111", Currency.getInstance("EUR"), null));

        // When
        Set<AccountReference> accountReferences = paymentAccountReferenceExtractor.extractAccountReferences(rawBody, PaymentType.PERIODIC);

        // Then
        assertEquals(expected, accountReferences);
    }

    @Test
    void extractAccountReferences_bulkPayment() {
        // Given
        byte[] rawBody = jsonReader.getBytesFromFile(BULK_PAYMENT_JSON_PATH);
        BulkPaymentInitiationJson bulkPaymentInitiationJson = jsonReader.getObjectFromFile(BULK_PAYMENT_JSON_PATH, BulkPaymentInitiationJson.class);
        BulkPayment xs2aBulkPayment = jsonReader.getObjectFromFile(BULK_PAYMENT_XS2A_JSON_PATH, BulkPayment.class);

        when(paymentModelMapper.mapToXs2aPayment(bulkPaymentInitiationJson)).thenReturn(xs2aBulkPayment);

        Set<AccountReference> expected = new HashSet<>();
        expected.add(new AccountReference(null, null, "DE52500105173911841934", null, "1111", "23456xxxxxx1234", "0172/1111111", Currency.getInstance("EUR"), null));
        expected.add(new AccountReference(null, null, "DE15500105172295759744", null, "1111", "23456xxxxxx1234", "0172/1111111", Currency.getInstance("EUR"), null));

        // When
        Set<AccountReference> accountReferences = paymentAccountReferenceExtractor.extractAccountReferences(rawBody, PaymentType.BULK);

        // Then
        assertEquals(expected, accountReferences);
    }

    @Test
    void extractAccountReferences_malformedBody() {
        // Given
        byte[] rawBody = jsonReader.getBytesFromFile(MALFORMED_BODY_JSON_PATH);

        // When
        Set<AccountReference> accountReferences = paymentAccountReferenceExtractor.extractAccountReferences(rawBody, PaymentType.SINGLE);

        // Then
        assertTrue(accountReferences.isEmpty());
    }
}
