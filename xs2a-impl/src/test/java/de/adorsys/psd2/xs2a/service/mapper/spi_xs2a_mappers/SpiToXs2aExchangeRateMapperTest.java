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

import de.adorsys.psd2.xs2a.domain.Xs2aExchangeRate;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiExchangeRate;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aExchangeRateMapperImpl.class})
class SpiToXs2aExchangeRateMapperTest {

    @Autowired
    private SpiToXs2aExchangeRateMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private SpiExchangeRate spiExchangeRate;
    private Xs2aExchangeRate expectedXs2aExchangeRate;

    @BeforeEach
    void setUp() {
        spiExchangeRate = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-exchange-rate.json",
                                                       SpiExchangeRate.class);
        expectedXs2aExchangeRate = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-exchange-rate.json",
                                                                Xs2aExchangeRate.class);
    }

    @Test
    void mapToExchangeRate() {
        Xs2aExchangeRate xs2aExchangeRate = mapper.mapToExchangeRate(spiExchangeRate);
        assertEquals(expectedXs2aExchangeRate, xs2aExchangeRate);
    }

    @Test
    void mapToExchangeRate_nullValue() {
        assertNull(mapper.mapToExchangeRate(null));
    }

    @Test
    void mapToExchangeRateList() {
        List<Xs2aExchangeRate> xs2aExchangeRateList = mapper.mapToExchangeRateList(Collections.singletonList(spiExchangeRate));

        assertEquals(1, xs2aExchangeRateList.size());
        assertEquals(expectedXs2aExchangeRate, xs2aExchangeRateList.get(0));
    }
}
