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

import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.model.Balance;
import de.adorsys.psd2.model.BalanceList;
import de.adorsys.psd2.model.BalanceType;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import de.adorsys.psd2.xs2a.service.mapper.AmountModelMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BalanceMapperImpl.class,
    BalanceMapperTest.TestConfiguration.class})
class BalanceMapperTest {
    private final JsonReader jsonReader = new JsonReader();

    @Autowired
    private BalanceMapper mapper;
    @Autowired
    private OffsetDateTimeMapper offsetDateTimeMapper;
    @Autowired
    private AmountModelMapper amountModelMapper;

    @Configuration
    static class TestConfiguration {
        @Bean
        public AmountModelMapper mockAmountModelMapper() {
            return mock(AmountModelMapper.class);
        }

        @Bean
        public OffsetDateTimeMapper mockOffsetDateTimeMapper() {
            return mock(OffsetDateTimeMapper.class);
        }
    }

    @Test
    void mapToBalanceType() {
        Stream.of(de.adorsys.psd2.xs2a.domain.BalanceType.values()) //Given
            .map(mapper::mapToBalanceType) //When
            .forEach(Assertions::assertNotNull); //Then
    }

    @Test
    void mapToBalanceType_null() {
        BalanceType actual = mapper.mapToBalanceType(null);
        assertThat(actual).isNull();
    }

    @Test
    void mapToBalance_null() {
        Balance actual = mapper.mapToBalance(null);
        assertThat(actual).isNull();
    }

    @Test
    void mapToBalanceList_nullBalances() {
        BalanceList actual = mapper.mapToBalanceList(null);
        assertThat(actual).isNull();
    }

    @Test
    void mapToBalanceList_emptyBalances() {
        BalanceList actual = mapper.mapToBalanceList(Collections.emptyList());
        assertThat(actual).isNull();
    }

    @Test
    void mapToBalanceList_nonEmptyBalances() {
        //Given
        BalanceList expected = jsonReader
            .getObjectFromFile("json/service/mapper/balance-mapper/balance-list-expected.json", BalanceList.class);
        OffsetDateTime offset = OffsetDateTime.of(2018, 3, 31, 12, 16, 16, 0, ZoneOffset.UTC);
        //When
        when(amountModelMapper.mapToAmount(any())).thenReturn(getTestAmount());
        when(offsetDateTimeMapper.mapToOffsetDateTime(any())).thenReturn(offset);
        //Then
        BalanceList actual = mapper.mapToBalanceList(getTestBalances());
        assertThat(actual).isEqualTo(expected);
    }

    private List<Xs2aBalance> getTestBalances() {
        Xs2aBalance firstBalance = jsonReader
            .getObjectFromFile("json/service/mapper/balance-mapper/xs2abalance-expected.json", Xs2aBalance.class);
        List<Xs2aBalance> balances = new ArrayList<>();
        balances.add(firstBalance);
        return balances;
    }

    private Amount getTestAmount() {
        Amount amount = new Amount();
        amount.amount("55");
        amount.currency("EUR");
        return amount;
    }
}
