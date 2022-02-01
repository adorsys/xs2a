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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapperImpl;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapperImpl;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentModelMapperImpl.class, Xs2aAddressMapperImpl.class, PurposeCodeMapperImpl.class})
class PaymentModelMapperImplTest {

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    private final JsonReader jsonReader = new JsonReader();

    @ParameterizedTest
    @EnumSource(FrequencyCode.class)
    void frequencyCode_test(FrequencyCode frequencyCode) {

        PeriodicPaymentInitiationJson periodicPaymentInitiationJson = jsonReader.getObjectFromFile("json/service/mapper/periodic-payment-initiation.json", PeriodicPaymentInitiationJson.class);

        periodicPaymentInitiationJson.setFrequency(frequencyCode);

        PeriodicPayment actual = paymentModelMapper.mapToXs2aPayment(periodicPaymentInitiationJson);

        assertThat(actual.getFrequency().name()).isEqualTo(frequencyCode.name());
    }

    @Test
    void mapToXs2aPayment_null() {
        PeriodicPayment actual = paymentModelMapper.mapToXs2aPayment((PeriodicPaymentInitiationJson) null);

        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aPayment_singlePayment_null() {
        SinglePayment actual = paymentModelMapper.mapToXs2aPayment((PaymentInitiationJson) null);

        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aPayment_singlePayment_ok() {
        PaymentInitiationJson input = jsonReader.getObjectFromFile("json/service/mapper/single-payment-initiation.json", PaymentInitiationJson.class);

        SinglePayment actual = paymentModelMapper.mapToXs2aPayment(input);

        SinglePayment expected = jsonReader.getObjectFromFile("json/service/mapper/single-payment-initiation-expected.json", SinglePayment.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToXs2aPayment_bulkPayment_null() {
        BulkPayment actual = paymentModelMapper.mapToXs2aPayment((BulkPaymentInitiationJson) null);

        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aPayment_bulkPayment_ok() {
        BulkPaymentInitiationJson input = jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initiation.json", BulkPaymentInitiationJson.class);

        BulkPayment actual = paymentModelMapper.mapToXs2aPayment(input);

        BulkPayment expected = jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initiation-expected.json", BulkPayment.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToXs2aAmount_null() {
        Xs2aAmount actual = paymentModelMapper.mapToXs2aAmount(null);

        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aAmount_ok() {
        Amount input = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json", Amount.class);

        Xs2aAmount actual = paymentModelMapper.mapToXs2aAmount(input);

        Xs2aAmount expected = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json", Xs2aAmount.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToAccountReference_null() {
        AccountReference actual = paymentModelMapper.mapToAccountReference(null);

        assertThat(actual).isNull();
    }

    @Test
    void mapToAccountReference_ok() {
        de.adorsys.psd2.model.AccountReference input = jsonReader.getObjectFromFile("json/service/mapper/account-reference.json", de.adorsys.psd2.model.AccountReference.class);

        de.adorsys.psd2.xs2a.core.profile.AccountReference actual = paymentModelMapper.mapToAccountReference(input);

        de.adorsys.psd2.xs2a.core.profile.AccountReference expected = jsonReader.getObjectFromFile("json/service/mapper/account-reference.json", de.adorsys.psd2.xs2a.core.profile.AccountReference.class);

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> params() {
        String periodicPaymentFilePath = "json/service/mapper/periodic-payment-initiation.json";
        String periodicPaymentExpectedFilePath = "json/service/mapper/periodic-payment-initiation-expected.json";
        String periodicPaymentExecutionRuleFollowingFilePath = "json/service/mapper/periodic-payment-initiation-executionRule-following.json";
        String periodicPaymentExecutionRuleFollowingExpectedFilePath = "json/service/mapper/periodic-payment-initiation-expected-executionRule-following.json";
        String periodicPaymentExecutionRuleIsNullFilePath = "json/service/mapper/periodic-payment-initiation-variousInfo-isNull.json";
        String periodicPaymentExecutionRuleIsNullExpectedFilePath = "json/service/mapper/periodic-payment-initiation-expected-variousInfo-isNull.json";

        return Stream.of(
            Arguments.arguments(periodicPaymentFilePath, periodicPaymentExpectedFilePath),
            Arguments.arguments(periodicPaymentExecutionRuleFollowingFilePath, periodicPaymentExecutionRuleFollowingExpectedFilePath),
            Arguments.arguments(periodicPaymentExecutionRuleIsNullFilePath, periodicPaymentExecutionRuleIsNullExpectedFilePath)
        );
    }

    @ParameterizedTest
    @MethodSource("params")
    void mapToXs2aPayment(String inputFilePath, String expectedFilePath) {
        PeriodicPaymentInitiationJson input = jsonReader.getObjectFromFile(inputFilePath, PeriodicPaymentInitiationJson.class);
        PeriodicPayment expected = jsonReader.getObjectFromFile(expectedFilePath, PeriodicPayment.class);

        PeriodicPayment actual = paymentModelMapper.mapToXs2aPayment(input);

        assertThat(actual).isEqualTo(expected);
    }
}
