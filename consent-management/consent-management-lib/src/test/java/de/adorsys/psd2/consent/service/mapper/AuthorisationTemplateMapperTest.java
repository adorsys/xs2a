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
}
