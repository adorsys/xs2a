/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaApproach;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SpiToXs2aAuthorizationMapperImplTest {
    private final SpiToXs2aAuthorizationMapper mapper = new SpiToXs2aAuthorizationMapperImpl();

    @Test
    void mapToScaStatus_null() {
        ScaStatus actual = mapper.mapToScaStatus(null);
        assertNull(actual);
    }

    @Test
    void mapToScaApproach_null() {
        ScaApproach actual = mapper.mapToScaApproach(null);
        assertNull(actual);
    }

    @ParameterizedTest
    @EnumSource(SpiScaStatus.class)
    void mapToScaStatus(SpiScaStatus spiScaStatus) {
        ScaStatus actual = mapper.mapToScaStatus(spiScaStatus);
        assertEquals(actual.name(), spiScaStatus.name());
    }

    @ParameterizedTest
    @EnumSource(SpiScaApproach.class)
    void mapToScaApproach(SpiScaApproach spiScaApproach) {
        ScaApproach actual = mapper.mapToScaApproach(spiScaApproach);
        assertEquals(actual.name(), spiScaApproach.name());
    }
}
