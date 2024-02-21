/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaApproach;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class Xs2aToSpiAuthorizationMapperTest {
    private final Xs2aToSpiAuthorizationMapper mapper = new Xs2aToSpiAuthorizationMapperImpl();

    @Test
    void mapToSpiScaStatus_null() {
        SpiScaStatus actual = mapper.mapToSpiScaStatus(null);
        assertNull(actual);
    }

    @Test
    void mapToSpiScaApproach_null() {
        SpiScaApproach actual = mapper.mapToSpiScaApproach(null);
        assertNull(actual);
    }

    @ParameterizedTest
    @EnumSource(ScaStatus.class)
    void mapToSpiScaStatus(ScaStatus scaStatus) {
        SpiScaStatus actual = mapper.mapToSpiScaStatus(scaStatus);
        assertEquals(actual.name(), scaStatus.name());
    }

    @ParameterizedTest
    @EnumSource(ScaApproach.class)
    void mapToSpiScaApproach(ScaApproach scaApproach) {
        SpiScaApproach actual = mapper.mapToSpiScaApproach(scaApproach);
        assertEquals(actual.name(), scaApproach.name());
    }

}
