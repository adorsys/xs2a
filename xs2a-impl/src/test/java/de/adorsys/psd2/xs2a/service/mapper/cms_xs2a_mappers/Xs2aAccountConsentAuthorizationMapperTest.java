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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Xs2aAccountConsentAuthorizationMapperTest {
    private Xs2aAccountConsentAuthorizationMapper xs2aAccountConsentAuthorizationMapper = new Xs2aAccountConsentAuthorizationMapper();
    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToAccountConsentAuthorisation() {
        //Given
        List<Authorisation> authorisations = jsonReader.getListFromFile("json/service/mapper/cms-xs2a-mappers/authorisation-list.json", Authorisation.class);
        List<ConsentAuthorization> expected = jsonReader.getListFromFile("json/service/mapper/cms-xs2a-mappers/account-consent-authorisation-list.json", ConsentAuthorization.class);
        //When
        List<ConsentAuthorization> actual = xs2aAccountConsentAuthorizationMapper.mapToAccountConsentAuthorisation(authorisations);
        //Then
        assertEquals(expected, actual);
    }
}
