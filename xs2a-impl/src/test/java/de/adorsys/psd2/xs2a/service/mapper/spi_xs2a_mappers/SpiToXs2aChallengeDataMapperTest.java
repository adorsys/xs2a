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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.OtpFormat;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiOtpFormat;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aChallengeDataMapperImpl.class})
class SpiToXs2aChallengeDataMapperTest {
    @Autowired
    private SpiToXs2aChallengeDataMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void toChallengeData_nullInput() {
        //When
        ChallengeData actual = mapper.toChallengeData(null);
        //Then
        assertNull(actual);
    }

    @Test
    void toChallengeData_validData_otpFormatIsCharacter() {
        ChallengeData expected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/challenge-data-expected.json", ChallengeData.class);
        ChallengeData actual  = mapper.toChallengeData(getTestSpiChallengeData_otpFormatIsCharacters());
        assertEquals(expected, actual);
    }

    @Test
    void toChallengeData_validData_otpFormatIsInteger() {
        ChallengeData actual  = mapper.toChallengeData(getTestSpiChallengeData_otpFormatIsInteger());
        assertEquals(OtpFormat.INTEGER, actual.getOtpFormat());
    }

    @Test
    void toChallengeData_validData_otpFormatIsNull() {
        ChallengeData actual  = mapper.toChallengeData(getTestSpiChallengeData_otpFormatIsNull());
        assertNull(actual.getOtpFormat());
    }

    private SpiChallengeData getTestSpiChallengeData_otpFormatIsCharacters() {
        return new SpiChallengeData(new byte[0],
            List.of("test data"),
            "imagelink",
            10,
            SpiOtpFormat.CHARACTERS,
            "additionalInformation");
    }

    private SpiChallengeData getTestSpiChallengeData_otpFormatIsInteger() {
        return new SpiChallengeData(new byte[0],
            List.of("test data"),
            "imagelink",
            10,
            SpiOtpFormat.INTEGER,
            "additionalInformation");
    }

    private SpiChallengeData getTestSpiChallengeData_otpFormatIsNull() {
        return new SpiChallengeData(new byte[0],
            List.of("test data"),
            "imagelink",
            10,
            null,
            "additionalInformation");
    }
}
