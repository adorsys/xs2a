/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpiToXs2aAmountMapperImpl.class})
public class SpiToXs2aAmountMapperTest {

    @Autowired
    private SpiToXs2aAmountMapper mapper;

    @Test
    public void mapToXs2aAmount() {
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.TEN);
        Xs2aAmount xs2aAmount = mapper.mapToXs2aAmount(spiAmount);

        assertEquals(spiAmount.getCurrency(), xs2aAmount.getCurrency());
        assertEquals(spiAmount.getAmount().toString(), xs2aAmount.getAmount());
    }

    @Test
    public void mapToXs2aAmount_nullValue() {
        assertNull(mapper.mapToXs2aAmount(null));
    }
}
