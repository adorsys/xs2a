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

    private final JsonReader jsonReader = new JsonReader();

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
