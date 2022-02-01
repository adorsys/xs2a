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

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.ConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AisAuthService extends CmsAuthorisationService<ConsentEntity> {
    private final ConsentJpaRepository consentJpaRepository;

    @Autowired
    public AisAuthService(PsuService psuService, AspspProfileService aspspProfileService,
                          AuthorisationService authorisationService,
                          ConfirmationExpirationService<ConsentEntity> confirmationExpirationService,
                          ConsentJpaRepository consentJpaRepository) {
        super(psuService, aspspProfileService, authorisationService, confirmationExpirationService);
        this.consentJpaRepository = consentJpaRepository;
    }

    @Override
    public Optional<Authorisable> getNotFinalisedAuthorisationParent(String parentId) {
        return consentJpaRepository.findByExternalId(parentId)
                   .filter(con -> !con.getConsentStatus().isFinalisedStatus())
                   .map(con -> con);
    }

    @Override
    public Optional<Authorisable> getAuthorisationParent(String parentId) {
        return consentJpaRepository.findByExternalId(parentId)
                   .map(con -> con);
    }

    @Override
    protected void updateAuthorisable(Object authorisable) {
        consentJpaRepository.save((ConsentEntity) authorisable);
    }

    @Override
    AuthorisationType getAuthorisationType() {
        return AuthorisationType.CONSENT;
    }

    @Override
    ConsentEntity castToParent(Authorisable authorisable) {
        return (ConsentEntity) authorisable;
    }
}
