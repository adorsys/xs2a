/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
