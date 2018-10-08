/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.aspsp.xs2a.domain.Xs2aAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.springframework.stereotype.Component;

@Component
public class SpiToXs2aAmountMapper {
    public Xs2aAmount mapToXs2aAmount(SpiAmount spiAmount) {
        Xs2aAmount amount = new Xs2aAmount();

        amount.setAmount(spiAmount.getAmount().toString());
        amount.setCurrency(spiAmount.getCurrency());

        return amount;
    }
}
