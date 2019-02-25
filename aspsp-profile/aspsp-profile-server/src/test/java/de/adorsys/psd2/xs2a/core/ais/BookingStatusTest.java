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

package de.adorsys.psd2.xs2a.core.ais;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BookingStatusTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void forValue_lowerCase() throws IOException {
        //Given
        String bookingStatusJson = "{ \"bookingStatus\": \"booked\" }";
        Container expectedResult = new Container(BookingStatus.BOOKED);

        //When
        Container actualResult = objectMapper.readValue(bookingStatusJson, Container.class);

        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void forValue_upperCase() throws IOException {
        //Given
        String bookingStatusJson = "{ \"bookingStatus\": \"BOOKED\" }";
        Container expectedResult = new Container(BookingStatus.BOOKED);

        //When
        Container actualResult = objectMapper.readValue(bookingStatusJson, Container.class);

        //Then
        assertEquals(expectedResult, actualResult);
    }
}

@Data
@AllArgsConstructor
class Container {
    private BookingStatus bookingStatus;
}
