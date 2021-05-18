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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.TrustedBeneficiariesList;
import de.adorsys.psd2.model.TrustedBeneficiary;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiariesList;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapperImpl;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TrustedBeneficiariesModelMapperImpl.class, Xs2aAddressMapperImpl.class})
class TrustedBeneficiariesModelMapperTest {

    @Autowired
    private TrustedBeneficiariesModelMapper trustedBeneficiariesModelMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToTrustedBeneficiaries() {
        // Given
        TrustedBeneficiary expected = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/trusted-beneficiaries.json",
                                                                   TrustedBeneficiary.class);
        Xs2aTrustedBeneficiaries input = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-trusted-beneficiaries.json",
                                                                      Xs2aTrustedBeneficiaries.class);

        // When
        TrustedBeneficiary actual = trustedBeneficiariesModelMapper.mapToTrustedBeneficiaries(input);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToTrustedBeneficiariesList() {
        // Given
        TrustedBeneficiariesList expected = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/trusted-beneficiaries-list.json",
                                                                         TrustedBeneficiariesList.class);
        Xs2aTrustedBeneficiariesList input = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-trusted-beneficiaries-list.json",
                                                                          Xs2aTrustedBeneficiariesList.class);

        // When
        TrustedBeneficiariesList actual = trustedBeneficiariesModelMapper.mapToTrustedBeneficiariesList(input);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToTrustedBeneficiaries_null() {
        // When
        TrustedBeneficiary actual = trustedBeneficiariesModelMapper.mapToTrustedBeneficiaries(null);

        // Then
        assertThat(actual).isNull();
    }

    @Test
    void accountReference_isNull() {
        // Given
        Xs2aTrustedBeneficiaries input = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-trusted-beneficiaries-accountReference-isNull.json",
                                                                      Xs2aTrustedBeneficiaries.class);

        // When
        TrustedBeneficiary actual = trustedBeneficiariesModelMapper.mapToTrustedBeneficiaries(input);

        TrustedBeneficiary expected = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/trusted-beneficiaries-accountReference-null.json",
                                                                   TrustedBeneficiary.class);

        // Then
        assertThat(actual).isEqualTo(expected);
    }
}
