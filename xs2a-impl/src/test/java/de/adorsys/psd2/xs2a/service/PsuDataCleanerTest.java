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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PsuDataCleanerTest {
    private final PsuDataCleaner psuDataCleaner  = new PsuDataCleaner();
    private final PsuIdData PSU_ID_DATA = buildPsuIdData();
    private final PsuIdData PSU_ID_DATA_CLEAR = buildPsuIdDataClean();

    @Test
    void clearPsuData() {
        // When
        PsuIdData actual = psuDataCleaner.clearPsuData(PSU_ID_DATA);

        // Then
        assertThat(actual).isEqualTo(PSU_ID_DATA_CLEAR);
    }

    @Test
    void clearPsuData_null() {
        // When
        PsuIdData actual = psuDataCleaner.clearPsuData(null);

        // Then
        assertThat(actual).isNull();
    }

    private PsuIdData buildPsuIdDataClean() {
        return new PsuIdData(null, null, null, null, "psuIpAddress", null);
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress", buildAdditionalPsuIdData());
    }

    private AdditionalPsuIdData buildAdditionalPsuIdData() {
        return new AdditionalPsuIdData("port", "agent", "location", "accept", "charset", "encoding", "language", "method", UUID.randomUUID());
    }
}
