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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.BalanceType;
import de.adorsys.psd2.xs2a.service.mapper.AmountModelMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BalanceMapperImpl.class,
    BalanceMapperTest.TestConfiguration.class})
class BalanceMapperTest {
    @Autowired
    private BalanceMapper mapper;
    @Autowired
    private OffsetDateTimeMapper offsetDateTimeMapper;
    @Autowired
    private AmountModelMapper amountModelMapper;

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
}
