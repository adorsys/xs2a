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
