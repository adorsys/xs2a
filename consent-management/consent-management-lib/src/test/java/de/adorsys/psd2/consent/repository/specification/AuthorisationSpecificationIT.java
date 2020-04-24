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

package de.adorsys.psd2.consent.repository.specification;

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.integration.test.BaseTest;
import de.adorsys.psd2.integration.test.TestDBConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestDBConfiguration.class,
    initializers = {AuthorisationSpecificationIT.Initializer.class})
class AuthorisationSpecificationIT extends BaseTest {

    private static final String EXTERNAL_ID = "bdb36439-0e07-4037-a8a1-171c969fb0d7";
    private static final String INSTANCE_ID = "UNDEFINED";

    @Autowired
    private AuthorisationSpecification authorisationSpecification;

    @Autowired
    private AuthorisationRepository authorisationRepository;

    @Test
    void byExternalIdAndInstanceId() {
        AuthorisationEntity authorisationEntity = jsonReader.getObjectFromFile("json/specification/authorisation-entity.json", AuthorisationEntity.class);
        authorisationRepository.save(authorisationEntity);

        Optional<AuthorisationEntity> actual = authorisationRepository.findOne(
            authorisationSpecification.byExternalIdAndInstanceId(
                EXTERNAL_ID,
                INSTANCE_ID
            ));

        assertTrue(actual.isPresent());
        assertEquals(authorisationEntity.getExternalId(), actual.get().getExternalId());
        assertEquals(authorisationEntity.getInstanceId(), actual.get().getInstanceId());
    }
}
