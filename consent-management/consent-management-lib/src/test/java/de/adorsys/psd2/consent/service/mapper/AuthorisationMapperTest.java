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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AuthorisationMapperImpl.class, PsuDataMapper.class})
class AuthorisationMapperTest {

    @Autowired
    private AuthorisationMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void prepareAuthorisationEntity() {
        PisCommonPaymentData authorisationParent = jsonReader.getObjectFromFile("json/service/mapper/pis-common-payment-data.json", PisCommonPaymentData.class);
        CreateAuthorisationRequest request = jsonReader.getObjectFromFile("json/service/mapper/create-authorisation-request.json", CreateAuthorisationRequest.class);
        PsuData psuData = jsonReader.getObjectFromFile("json/service/mapper/psu-data.json", PsuData.class);

        AuthorisationEntity actual = mapper.prepareAuthorisationEntity(authorisationParent, request, Optional.of(psuData), AuthorisationType.PIS_CREATION, 100L, 200L);

        AuthorisationEntity expected = jsonReader.getObjectFromFile("json/service/mapper/authorisation-entity.json", AuthorisationEntity.class);
        expected.setExternalId(actual.getExternalId());
        expected.setRedirectUrlExpirationTimestamp(actual.getRedirectUrlExpirationTimestamp());
        expected.setAuthorisationExpirationTimestamp(actual.getAuthorisationExpirationTimestamp());

        assertEquals(expected, actual);
    }

    @Test
    void mapToAuthorisation() {
        // Given
        AuthorisationEntity input = jsonReader.getObjectFromFile("json/service/mapper/authorisation-entity.json", AuthorisationEntity.class);

        // When
        Authorisation actual = mapper.mapToAuthorisation(input);

        // Then
        Authorisation expected = jsonReader.getObjectFromFile("json/service/mapper/authorisation-expected.json", Authorisation.class);
        assertEquals(expected, actual);
    }
}
