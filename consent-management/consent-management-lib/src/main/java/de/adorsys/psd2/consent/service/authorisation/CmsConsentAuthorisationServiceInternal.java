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

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.specification.AuthorisationSpecification;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsConsentAuthorisationServiceInternal {
    private final AuthorisationRepository authorisationRepository;
    private final AuthorisationSpecification authorisationSpecification;

    public Optional<AuthorisationEntity> getAuthorisationByAuthorisationId(@NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException {
        Optional<AuthorisationEntity> authorisation = authorisationRepository.findOne(authorisationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId));

        if (authorisation.isPresent() && !authorisation.get().isAuthorisationNotExpired()) {
            log.info("Authorisation ID [{}], Instance ID: [{}]. Authorisation is expired", authorisationId, instanceId);
            throw new AuthorisationIsExpiredException(authorisation.get().getTppNokRedirectUri());
        }
        return authorisation;
    }

    public Optional<AuthorisationEntity> getAuthorisationByRedirectId(String redirectId, String instanceId) throws RedirectUrlIsExpiredException {
        Optional<AuthorisationEntity> authorisation = authorisationRepository.findOne(authorisationSpecification.byExternalIdAndInstanceId(redirectId, instanceId));

        if (authorisation.isPresent() && !authorisation.get().isRedirectUrlNotExpired()) {
            log.info("Authorisation ID [{}], Instance ID: [{}]. Check redirect URL and get consent failed, because authorisation is expired",
                     redirectId, instanceId);
            authorisation.get().setScaStatus(ScaStatus.FAILED);
            throw new RedirectUrlIsExpiredException(authorisation.get().getTppNokRedirectUri());
        }
        return authorisation;
    }

    public boolean updateScaStatusAndAuthenticationData(@NotNull ScaStatus status, AuthorisationEntity authorisation, AuthenticationDataHolder authenticationDataHolder) {
        if (authorisation.getScaStatus().isFinalisedStatus()) {
            log.info("Authorisation ID [{}], SCA status [{}]. Update authorisation status failed in updateScaStatusAndAuthenticationData method because authorisation has finalised status.", authorisation.getExternalId(),
                     authorisation.getScaStatus().getValue());
            return false;
        }
        authorisation.setScaStatus(status);

        if (authenticationDataHolder != null) {
            enrichAuthorisationWithAuthenticationData(authorisation, authenticationDataHolder);
        }

        return true;
    }

    public List<AuthorisationEntity> getAuthorisationsByParentExternalId(String externalId) {
        return authorisationRepository.findAllByParentExternalIdAndType(externalId, AuthorisationType.CONSENT);
    }

    private void enrichAuthorisationWithAuthenticationData(AuthorisationEntity authorisation, AuthenticationDataHolder authenticationDataHolder) {
        if (authenticationDataHolder.getAuthenticationData() != null) {
            authorisation.setScaAuthenticationData(authenticationDataHolder.getAuthenticationData());
        }
        if (authenticationDataHolder.getAuthenticationMethodId() != null) {
            authorisation.setAuthenticationMethodId(authenticationDataHolder.getAuthenticationMethodId());
        }
    }
}
