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
