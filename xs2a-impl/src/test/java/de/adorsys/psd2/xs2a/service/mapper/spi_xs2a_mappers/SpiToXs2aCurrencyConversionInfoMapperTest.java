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
