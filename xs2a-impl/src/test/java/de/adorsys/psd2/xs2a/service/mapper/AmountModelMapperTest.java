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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AmountModelMapper.class, TestMapperConfiguration.class})
class AmountModelMapperTest {

    @Autowired
    private AmountModelMapper amountModelMapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aAmount_null() {
        Xs2aAmount actual = amountModelMapper.mapToXs2aAmount(null);
        assertThat(actual).isNull();
    }

    @Test
    void mapToAmount_null() {
        Amount actual = amountModelMapper.mapToAmount(null);
        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aAmount_ok() {
        Amount input = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json", Amount.class);

        Xs2aAmount actual = amountModelMapper.mapToXs2aAmount(input);

        Xs2aAmount expected = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json", Xs2aAmount.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToAmount_ok() {
        Xs2aAmount input = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json", Xs2aAmount.class);

        Amount actual = amountModelMapper.mapToAmount(input);

        Amount expected = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json", Amount.class);

        assertThat(actual).isEqualTo(expected);
    }
}
