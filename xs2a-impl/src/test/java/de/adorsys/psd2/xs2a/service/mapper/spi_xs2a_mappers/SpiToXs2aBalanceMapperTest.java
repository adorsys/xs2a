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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.BalanceType;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiBalanceType;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aBalanceMapperImpl.class, SpiToXs2aAmountMapperImpl.class})
class SpiToXs2aBalanceMapperTest {

    @Autowired
    private SpiToXs2aBalanceMapper mapper;

    private final JsonReader jsonReader = new JsonReader();
    private SpiAccountBalance spiAccountBalance;
    private Xs2aBalance expectedBalance;

    @BeforeEach
    void setUp() {
        spiAccountBalance = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-balance.json",
                                                         SpiAccountBalance.class);
        expectedBalance = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-balance.json",
                                                       Xs2aBalance.class);
    }

    @Test
    void mapToXs2aBalance() {
        //When
        Xs2aBalance actual = mapper.mapToXs2aBalance(spiAccountBalance);

        //Then
        assertThat(actual).isEqualTo(expectedBalance);
    }

    @Test
    void mapToXs2aBalance_nullValue() {
        //When
        Xs2aBalance actual = mapper.mapToXs2aBalance(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aBalanceList() {
        //When
        List<Xs2aBalance> actual = mapper.mapToXs2aBalanceList(Collections.singletonList(spiAccountBalance));

        //Then
        assertThat(actual)
            .asList()
            .isNotEmpty()
            .hasSize(1)
            .contains(expectedBalance);
    }

    @Test
    void mapToXs2aBalanceList_nullValue() {
        //When
        List<Xs2aBalance> actual = mapper.mapToXs2aBalanceList(null);

        //Then
        assertThat(actual)
            .isEmpty();
    }

    @Test
    void mapToSpiBalanceType_nullInput() {
        //When
        BalanceType actual = mapper.mapToSpiBalanceType(null);

        //Then
        assertThat(actual).isNull();
    }

    @ParameterizedTest
    @EnumSource(SpiBalanceType.class)
    void mapToSpiBalanceType(SpiBalanceType spiBalanceType) {
        //When
        BalanceType actual = mapper.mapToSpiBalanceType(spiBalanceType);

        //Then
        assertThat(actual.name()).isEqualTo(spiBalanceType.name());
    }
}
