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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.AUTHORISATION_EXTERNAL_ID_ATTRIBUTE;
import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.INSTANCE_ID_ATTRIBUTE;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute;

@Service
public class AuthorisationSpecification {
    public Specification<AuthorisationEntity> byExternalIdAndInstanceId(String externalId, String instanceId) {
        return Optional.of(Specification.<AuthorisationEntity>where(provideSpecificationForEntityAttribute(AUTHORISATION_EXTERNAL_ID_ATTRIBUTE, externalId)))
                   .map(s -> s.and(provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId)))
                   .orElse(null);
    }
}
