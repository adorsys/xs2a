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

package de.adorsys.psd2.xs2a.web.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.DayOfExecution;
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeriodicPaymentJsonPartConverterTest {
    private static final String SERIALISED_BODY = "properly serialised body";
    private static final String MALFORMED_SERIALISED_BODY = "malformed body";

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;

    @InjectMocks
    private PeriodicPaymentJsonPartConverter periodicPaymentJsonPartConverter;

    @Test
    void convert_withCorrectBody_shouldReturnObject() throws IOException {
        // Given
        when(xs2aObjectMapper.readValue(SERIALISED_BODY, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class))
            .thenReturn(buildPeriodicPaymentJson());

        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson expected = buildPeriodicPaymentJson();

        // When
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson actualResult = periodicPaymentJsonPartConverter.convert(SERIALISED_BODY);

        // Then
        verify(xs2aObjectMapper).readValue(SERIALISED_BODY, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class);
        assertEquals(expected, actualResult);
    }

    @Test
    void convert_withMalformedBody_shouldReturnNull() throws JsonProcessingException {
        // Given
        when(xs2aObjectMapper.readValue(MALFORMED_SERIALISED_BODY, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class))
            .thenReturn(null);

        // When
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson actualResult = periodicPaymentJsonPartConverter.convert(MALFORMED_SERIALISED_BODY);

        // Then
        assertNull(actualResult);
    }

    @Test
    void convert_withNullString_shouldReturnNull() {
        // When
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson actualResult = periodicPaymentJsonPartConverter.convert(null);

        // Then
        assertNull(actualResult);
    }

    private PeriodicPaymentInitiationXmlPart2StandingorderTypeJson buildPeriodicPaymentJson() {
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson periodicPaymentJson = new PeriodicPaymentInitiationXmlPart2StandingorderTypeJson();
        periodicPaymentJson.setDayOfExecution(DayOfExecution._2);
        periodicPaymentJson.startDate(LocalDate.of(2019, 4, 9));
        return periodicPaymentJson;
    }
}
