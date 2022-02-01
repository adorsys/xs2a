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

package de.adorsys.psd2.xs2a.core.ais;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingStatusTest {

    private final Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

    @Test
    void forValue_lowerCase() throws IOException {
        //Given
        String bookingStatusJson = "{ \"bookingStatus\": \"booked\" }";
        Container expectedResult = new Container(BookingStatus.BOOKED);

        //When
        Container actualResult = xs2aObjectMapper.readValue(bookingStatusJson, Container.class);

        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void forValue_upperCase() throws IOException {
        //Given
        String bookingStatusJson = "{ \"bookingStatus\": \"BOOKED\" }";
        Container expectedResult = new Container(BookingStatus.BOOKED);

        //When
        Container actualResult = xs2aObjectMapper.readValue(bookingStatusJson, Container.class);

        //Then
        assertEquals(expectedResult, actualResult);
    }
}

@Data
@AllArgsConstructor
class Container {
    private BookingStatus bookingStatus;
}
