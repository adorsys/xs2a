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
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;

import java.util.List;
import java.util.Optional;

public interface AuthService {
    Optional<Authorisable> getNotFinalisedAuthorisationParent(String parentId);

    Optional<Authorisable> getAuthorisationParent(String parentId);

    AuthorisationEntity saveAuthorisation(CreateAuthorisationRequest request, Authorisable authorisationParent);

    AuthorisationEntity doUpdateAuthorisation(AuthorisationEntity authorisationEntity, UpdateAuthorisationRequest updateAuthorisationRequest);

    List<AuthorisationEntity> getAuthorisationsByParentId(String parentId);

    Optional<AuthorisationEntity> getAuthorisationById(String authorisationId);

    Authorisable checkAndUpdateOnConfirmationExpiration(Authorisable authorisable);

    boolean isConfirmationExpired(Authorisable object);

    Authorisable updateOnConfirmationExpiration(Authorisable object);
}
