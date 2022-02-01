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

import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    TppInfoMapperImpl.class, AuthorisationTemplateMapperImpl.class, PsuDataMapper.class,
    ConsentTppInformationMapperImpl.class, CmsConfirmationOfFundsMapper.class, ConsentDataMapper.class
})
class CmsConfirmationOfFundsMapperTest {

    @Autowired
    private CmsConfirmationOfFundsMapper mapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToCmsConfirmationOfFundsConsent() {
        ConsentEntity consent = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-mapper/consent-entity-empty-authorisations.json", ConsentEntity.class);
        List<AuthorisationEntity> authorisations = Collections.singletonList(jsonReader.getObjectFromFile("json/service/mapper/piis-consent-mapper/authorisation-entity.json",
                                                                                                          AuthorisationEntity.class));

        CmsConfirmationOfFundsConsent actual = mapper.mapToCmsConfirmationOfFundsConsent(consent, authorisations);

        CmsConfirmationOfFundsConsent expected = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-mapper/cms-piis-consent-with-authorisations.json", CmsConfirmationOfFundsConsent.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToCmsConfirmationOfFundsConsent_emptyAuthorisations() {
        ConsentEntity consent = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-mapper/consent-entity-empty-authorisations.json", ConsentEntity.class);

        CmsConfirmationOfFundsConsent actual = mapper.mapToCmsConfirmationOfFundsConsent(consent, null);

        CmsConfirmationOfFundsConsent expected = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-mapper/cms-piis-consent-empty-authorisations.json", CmsConfirmationOfFundsConsent.class);
        assertEquals(expected, actual);
    }
}
