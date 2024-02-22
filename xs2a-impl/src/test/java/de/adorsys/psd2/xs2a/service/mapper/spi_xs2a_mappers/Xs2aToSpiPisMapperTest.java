/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.payment.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class Xs2aToSpiPisMapperTest {
    private final Xs2aToSpiPisMapper mapper = new Xs2aToSpiPisMapperImpl();

    @Test
    void mapToSpiPaymentType_null() {
        //When
        SpiPaymentType actual = mapper.mapToSpiPaymentType(null);
        //Then
        assertNull(actual);
    }

    @Test
    void mapToSpiPisExecutionRule_null() {
        //When
        SpiPisExecutionRule actual = mapper.mapToSpiPisExecutionRule(null);
        //Then
        assertNull(actual);
    }

    @Test
    void mapToSpiFrequencyCode_null() {
        //When
        SpiFrequencyCode actual = mapper.mapToSpiFrequencyCode(null);
        //Then
        assertNull(actual);
    }

    @Test
    void mapToSpiPisDayOfExecution_null() {
        //When
        SpiPisDayOfExecution actual = mapper.mapToSpiPisDayOfExecution(null);
        //Then
        assertNull(actual);
    }

    @Test
    void mapToSpiPisPurposeCode_null() {
        //When
        SpiPisPurposeCode actual = mapper.mapToSpiPisPurposeCode(null);
        //Then
        assertNull(actual);
    }

    @ParameterizedTest
    @EnumSource(PaymentType.class)
    void mapToSpiPaymentType(PaymentType paymentType) {
        //When
        SpiPaymentType actual = mapper.mapToSpiPaymentType(paymentType);
        //Then
        assertEquals(paymentType.name(), actual.name());
    }

    @ParameterizedTest
    @EnumSource(PisExecutionRule.class)
    void mapToSpiPisExecutionRule(PisExecutionRule pisExecutionRule) {
        //When
        SpiPisExecutionRule actual = mapper.mapToSpiPisExecutionRule(pisExecutionRule);
        //Then
        assertEquals(pisExecutionRule.name(), actual.name());
    }

    @ParameterizedTest
    @EnumSource(FrequencyCode.class)
    void mapToSpiFrequencyCode(FrequencyCode frequencyCode) {
        //When
        SpiFrequencyCode actual = mapper.mapToSpiFrequencyCode(frequencyCode);
        //Then
        assertEquals(frequencyCode.name(), actual.name());
    }

    @ParameterizedTest
    @EnumSource(PisDayOfExecution.class)
    void mapToSpiPisDayOfExecution(PisDayOfExecution dayOfExecution) {
        //When
        SpiPisDayOfExecution actual = mapper.mapToSpiPisDayOfExecution(dayOfExecution);
        //Then
        assertEquals(dayOfExecution.name(), actual.name());
    }

    @ParameterizedTest
    @EnumSource(PurposeCode.class)
    void mapToSpiPisPurposeCode(PurposeCode purposeCode) {
        //When
        SpiPisPurposeCode actual = mapper.mapToSpiPisPurposeCode(purposeCode);
        //Then
        assertEquals(purposeCode.name(), actual.name());
    }
}
