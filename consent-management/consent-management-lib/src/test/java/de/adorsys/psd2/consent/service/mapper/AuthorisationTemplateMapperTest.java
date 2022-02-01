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

import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AuthorisationTemplateMapperImpl.class})
class AuthorisationTemplateMapperTest {

    @Autowired
    private AuthorisationTemplateMapper mapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToAuthorisationTemplate_success() {
        AuthorisationTemplateEntity authorisationTemplateEntity = jsonReader.getObjectFromFile("json/service/mapper/authorisation-template-entity.json", AuthorisationTemplateEntity.class);

        AuthorisationTemplate actualAuthorisationTemplate = mapper.mapToAuthorisationTemplate(authorisationTemplateEntity);

        AuthorisationTemplate expectedAuthorisationTemplate = jsonReader.getObjectFromFile("json/service/mapper/authorisation-template.json", AuthorisationTemplate.class);
        assertEquals(expectedAuthorisationTemplate, actualAuthorisationTemplate);
    }

    @Test
    void mapToAuthorisationTemplate_shouldNotMapRedirectUri() {
        AuthorisationTemplateEntity authorisationTemplateEntity = jsonReader.getObjectFromFile("json/service/mapper/authorisation-template-entity.json", AuthorisationTemplateEntity.class);
        authorisationTemplateEntity.setRedirectUri(null);
        authorisationTemplateEntity.setCancelRedirectUri(null);

        AuthorisationTemplate actualAuthorisationTemplate = mapper.mapToAuthorisationTemplate(authorisationTemplateEntity);

        AuthorisationTemplate expectedAuthorisationTemplate = jsonReader.getObjectFromFile("json/service/mapper/authorisation-template.json", AuthorisationTemplate.class);
        expectedAuthorisationTemplate.setTppRedirectUri(null);
        expectedAuthorisationTemplate.setCancelTppRedirectUri(null);
        assertEquals(expectedAuthorisationTemplate, actualAuthorisationTemplate);
    }

    @Test
    void mapToAuthorisationTemplate_nullValue() {
        AuthorisationTemplate authorisationTemplate = mapper.mapToAuthorisationTemplate(null);
        assertNull(authorisationTemplate);
    }

    @Test
    void mapToAuthorisationTemplateEntity() {
        //Given
        AuthorisationTemplate authorisationTemplate = jsonReader.getObjectFromFile("json/service/mapper/authorisation-template.json", AuthorisationTemplate.class);
        AuthorisationTemplateEntity expectedAuthorisationTemplateEntity = jsonReader.getObjectFromFile("json/service/mapper/authorisation-template-entity.json", AuthorisationTemplateEntity.class);
        //When
        AuthorisationTemplateEntity authorisationTemplateEntity = mapper.mapToAuthorisationTemplateEntity(authorisationTemplate);
        //Then
        assertEquals(expectedAuthorisationTemplateEntity, authorisationTemplateEntity);
    }

    @Test
    void mapToAuthorisationTemplateEntity_tppRedirectUriAreNull() {
        //Given
        AuthorisationTemplate authorisationTemplate = new AuthorisationTemplate();
        AuthorisationTemplateEntity expectedAuthorisationTemplateEntity = new AuthorisationTemplateEntity();
        //When
        AuthorisationTemplateEntity authorisationTemplateEntity = mapper.mapToAuthorisationTemplateEntity(authorisationTemplate);
        //Then
        assertEquals(expectedAuthorisationTemplateEntity, authorisationTemplateEntity);
    }

    @Test
    void mapToAuthorisationTemplateEntity_nullValue() {
        AuthorisationTemplateEntity authorisationTemplateEntity = mapper.mapToAuthorisationTemplateEntity(null);
        assertNull(authorisationTemplateEntity);
    }
}
