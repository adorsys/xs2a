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
