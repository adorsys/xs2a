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
