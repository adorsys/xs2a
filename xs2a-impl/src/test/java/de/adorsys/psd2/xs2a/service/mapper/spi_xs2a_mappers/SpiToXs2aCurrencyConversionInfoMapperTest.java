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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.pis.Xs2aCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aCurrencyConversionInfoMapperImpl.class})
class SpiToXs2aCurrencyConversionInfoMapperTest {
    @Autowired
    private SpiToXs2aCurrencyConversionInfoMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void toXs2aCurrencyConversionInfo_nullInput() {
        Xs2aCurrencyConversionInfo actual = mapper.toXs2aCurrencyConversionInfo(null);
        assertNull(actual);
    }

    @Test
    void toXs2aCurrencyConversionInfo_validInput() {
        Xs2aCurrencyConversionInfo expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-currency-conv-info-expected.json", Xs2aCurrencyConversionInfo.class);
        Xs2aCurrencyConversionInfo actual = mapper.toXs2aCurrencyConversionInfo(getTestSpiCurrencyConversionInfo());
        assertEquals(expected, actual);
    }

    @Test
    void toXs2aCurrencyConversionInfo_nullTransactionFees() {
        Xs2aCurrencyConversionInfo actual = mapper.toXs2aCurrencyConversionInfo(getTestSpiCurrencyConversionInfoNullTransactionFees());
        assertNull(actual.getTransactionFees());
    }

    private SpiCurrencyConversionInfo getTestSpiCurrencyConversionInfoNullTransactionFees() {
        return new SpiCurrencyConversionInfo(null,
            new SpiAmount(Currency.getInstance("USD"), BigDecimal.valueOf(22)),
            new SpiAmount(Currency.getInstance("UAH"), BigDecimal.valueOf(33)),
            new SpiAmount(Currency.getInstance("GBP"), BigDecimal.valueOf(44)));
    }

    private SpiCurrencyConversionInfo getTestSpiCurrencyConversionInfo() {
        return new SpiCurrencyConversionInfo(new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(11)),
            new SpiAmount(Currency.getInstance("USD"), BigDecimal.valueOf(22)),
            new SpiAmount(Currency.getInstance("UAH"), BigDecimal.valueOf(33)),
            new SpiAmount(Currency.getInstance("GBP"), BigDecimal.valueOf(44)));
    }
}
