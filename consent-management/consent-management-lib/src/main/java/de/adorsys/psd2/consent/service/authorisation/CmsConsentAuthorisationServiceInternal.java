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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.specification.AuthorisationSpecification;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsConsentAuthorisationServiceInternal {
    private final AuthorisationRepository authorisationRepository;
    private final AuthorisationSpecification authorisationSpecification;

    public Optional<AuthorisationEntity> getAuthorisationByExternalId(@NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException {
        Optional<AuthorisationEntity> authorisation = authorisationRepository.findOne(authorisationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId));

        if (authorisation.isPresent() && !authorisation.get().isAuthorisationNotExpired()) {
            log.info("Authorisation ID [{}], Instance ID: [{}]. Authorisation is expired", authorisationId, instanceId);
            throw new AuthorisationIsExpiredException(authorisation.get().getTppNokRedirectUri());
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

    private void enrichAuthorisationWithAuthenticationData(AuthorisationEntity authorisation, AuthenticationDataHolder authenticationDataHolder) {
        if (authenticationDataHolder.getAuthenticationData() != null) {
            authorisation.setScaAuthenticationData(authenticationDataHolder.getAuthenticationData());
        }
        if (authenticationDataHolder.getAuthenticationMethodId() != null) {
            authorisation.setAuthenticationMethodId(authenticationDataHolder.getAuthenticationMethodId());
        }
    }
}
