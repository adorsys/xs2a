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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.service.mapper.AuthorisationMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthorisationService {
    protected final AuthorisationMapper authorisationMapper;
    protected final AuthorisationRepository authorisationRepository;

    public List<AuthorisationEntity> findAllByParentExternalIdAndType(String parentId, AuthorisationType authorisationType) {
        return authorisationRepository.findAllByParentExternalIdAndType(parentId, authorisationType);
    }

    public Optional<AuthorisationEntity> findByExternalIdAndType(String authorisationId, AuthorisationType authorisationType) {
        return authorisationRepository.findByExternalIdAndType(authorisationId, authorisationType);
    }

    public AuthorisationEntity prepareAuthorisationEntity(Authorisable authorisationParent, CreateAuthorisationRequest request,
                                                          Optional<PsuData> psuDataOptional, AuthorisationType authorisationType,
                                                          long redirectUrlExpirationTimeMs, long authorisationExpirationTimeMs) {
        return authorisationMapper.prepareAuthorisationEntity(authorisationParent, request, psuDataOptional, authorisationType,
                                                              redirectUrlExpirationTimeMs, authorisationExpirationTimeMs);
    }

    public AuthorisationEntity save(AuthorisationEntity entity) {
        return authorisationRepository.save(entity);
    }
}
