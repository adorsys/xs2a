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
