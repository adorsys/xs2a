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

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aAccountAccessMapper.class, SpiToXs2aAccountReferenceMapperImpl.class})
class SpiToXs2aAccountAccessMapperTest {
    private final JsonReader jsonReader = new JsonReader();

    @Autowired
    private SpiToXs2aAccountAccessMapper mapper;


    @Test
    void mapToAccountAccess_nullInput() {
        //When
        Optional<AccountAccess> actual = mapper.mapToAccountAccess(null);

        //Then
        assertThat(actual).isNotPresent();
    }

    @Test
    void mapToAccountAccess() {
        //Given
        SpiAccountAccess spiAccountAccess =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-access.json",
                SpiAccountAccess.class);
        AccountAccess expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/account-access-expected.json",
                AccountAccess.class);

        //When
        Optional<AccountAccess> actual = mapper.mapToAccountAccess(spiAccountAccess);

        //Then
        assertThat(actual)
            .isPresent()
            .contains(expected);
    }

    @Test
    void getAccessForGlobalOrAllAvailableAccountsConsent() {
        //Given
        CreateConsentReq createConsentReq =
            jsonReader.getObjectFromFile("json/service/create-consent-req-with-beneficiaries-and-owner-name.json",
                CreateConsentReq.class);
        AccountAccess expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/account-access-additional-info-expected.json",
                AccountAccess.class);

        //When
        AccountAccess actual = mapper.getAccessForGlobalOrAllAvailableAccountsConsent(createConsentReq);

        //Then
        assertThat(actual)
            .isNotNull()
            .isEqualTo(expected);
    }

    @Test
    void getAccessForGlobalOrAllAvailableAccountsConsent_nullAdditionalInfo() {
        //Given
        CreateConsentReq createConsentReq =
            jsonReader.getObjectFromFile("json/service/create-consent-req-with-empty-additional-info.json",
                CreateConsentReq.class);
        AccountAccess expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/account-access-null-additional-info-expected.json",
                AccountAccess.class);

        //When
        AccountAccess actual = mapper.getAccessForGlobalOrAllAvailableAccountsConsent(createConsentReq);

        //Then
        assertThat(actual)
            .isNotNull()
            .isEqualTo(expected);
    }
}
