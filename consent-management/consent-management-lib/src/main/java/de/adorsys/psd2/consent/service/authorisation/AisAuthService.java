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

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.consent.service.mapper.AuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AisAuthService extends CmsAuthorisationService<ConsentEntity> {
    private ConsentJpaRepository consentJpaRepository;

    @Autowired
    public AisAuthService(CmsPsuService cmsPsuService, PsuDataMapper psuDataMapper, AspspProfileService aspspProfileService,
                          AuthorisationRepository authorisationRepository,
                          AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService,
                          ConsentJpaRepository aisConsentJpaRepository,
                          AuthorisationMapper authorisationMapper) {
        super(cmsPsuService, psuDataMapper, aspspProfileService, authorisationMapper, authorisationRepository, aisConsentConfirmationExpirationService);
        this.consentJpaRepository = aisConsentJpaRepository;
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
