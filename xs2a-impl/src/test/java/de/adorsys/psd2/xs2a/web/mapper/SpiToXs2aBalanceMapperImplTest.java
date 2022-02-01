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

import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aBalanceMapperImpl;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiBalanceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class SpiToXs2aBalanceMapperImplTest {
    private SpiToXs2aBalanceMapperImpl spiToXs2aBalanceMapper = new SpiToXs2aBalanceMapperImpl();

    @Test
    void mapToSpiBalanceType() {
        Stream.of(SpiBalanceType.values()) //Given
            .map(spiToXs2aBalanceMapper::mapToSpiBalanceType) //When
            .forEach(Assertions::assertNotNull); //Then
    }
}
