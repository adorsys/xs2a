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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.ChallengeData;
import de.adorsys.psd2.model.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreObjectsMapper.class})
class CoreObjectsMapperTest {
    private final JsonReader jsonReader = new JsonReader();

    @Autowired
    private CoreObjectsMapper mapper;

    @Test
    void mapToModelScaStatus() {
        Stream.of(de.adorsys.psd2.xs2a.core.sca.ScaStatus.values())
            .forEach(scaStatus -> assertEquals(ScaStatus.valueOf(scaStatus.name()), mapper.mapToModelScaStatus(scaStatus)));
    }

    @Test
    void mapToChallengeData_nullValue() {
        //When
        ChallengeData actual = mapper.mapToChallengeData(null);
        //Then
        assertNull(actual);
    }

    @Test
    void mapToChallengeData_nonNullValue() {
        //Given
        de.adorsys.psd2.xs2a.core.sca.ChallengeData inputData = jsonReader
            .getObjectFromFile("json/service/mapper/core-objects-mapper/challenge-data.json", de.adorsys.psd2.xs2a.core.sca.ChallengeData.class);
        ChallengeData expected = jsonReader
            .getObjectFromFile("json/service/mapper/core-objects-mapper/challenge-data-expected.json", ChallengeData.class);
        //When
        ChallengeData actual = mapper.mapToChallengeData(inputData);
        //Then
        assertEquals(expected, actual);
    }
}
