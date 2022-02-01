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

import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
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
@ContextConfiguration(classes = {SpiToXs2aAmountMapperImpl.class})
class SpiToXs2aAmountMapperTest {

    @Autowired
    private SpiToXs2aAmountMapper mapper;

    @Test
    void mapToXs2aAmount() {
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.TEN);
        Xs2aAmount xs2aAmount = mapper.mapToXs2aAmount(spiAmount);

        assertEquals(spiAmount.getCurrency(), xs2aAmount.getCurrency());
        assertEquals(spiAmount.getAmount().toString(), xs2aAmount.getAmount());
    }

    @Test
    void mapToXs2aAmount_nullValue() {
        assertNull(mapper.mapToXs2aAmount(null));
    }
}
