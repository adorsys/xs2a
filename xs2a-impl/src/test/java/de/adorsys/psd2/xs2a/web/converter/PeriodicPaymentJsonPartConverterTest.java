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

package de.adorsys.psd2.xs2a.web.converter;

import de.adorsys.psd2.model.DayOfExecution;
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PeriodicPaymentJsonPartConverterTest {
    private static final String SERIALISED_BODY = "properly serialised body";
    private static final String MALFORMED_SERIALISED_BODY = "malformed body";

    @Mock
    private JsonConverter jsonConverter;

    @InjectMocks
    private PeriodicPaymentJsonPartConverter periodicPaymentJsonPartConverter;

    @Before
    public void setUp() {
        when(jsonConverter.toObject(SERIALISED_BODY, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class))
            .thenReturn(Optional.of(buildPeriodicPaymentJson()));
        when(jsonConverter.toObject(MALFORMED_SERIALISED_BODY, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class))
            .thenReturn(Optional.empty());
    }

    @Test
    public void convert_withCorrectBody_shouldReturnObject() {
        // Given
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson expected = buildPeriodicPaymentJson();

        // When
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson actualResult = periodicPaymentJsonPartConverter.convert(SERIALISED_BODY);

        // Then
        verify(jsonConverter).toObject(SERIALISED_BODY, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class);
        assertEquals(expected, actualResult);
    }

    @Test
    public void convert_withMalformedBody_shouldReturnNull() {
        // When
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson actualResult = periodicPaymentJsonPartConverter.convert(MALFORMED_SERIALISED_BODY);

        // Then
        assertNull(actualResult);
    }

    @Test
    public void convert_withNullString_shouldReturnNull() {
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
