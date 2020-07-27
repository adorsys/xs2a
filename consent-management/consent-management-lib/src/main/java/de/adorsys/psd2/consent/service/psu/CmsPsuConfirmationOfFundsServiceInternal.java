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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsConsent;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsResponse;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsAuthorisation;
import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsService;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.ConfirmationOfFundsConsentSpecification;
import de.adorsys.psd2.consent.service.authorisation.CmsConsentAuthorisationServiceInternal;
import de.adorsys.psd2.consent.service.mapper.CmsConfirmationOfFundsMapper;
import de.adorsys.psd2.consent.service.mapper.CmsPsuAuthorisationMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsPsuConfirmationOfFundsServiceInternal implements CmsPsuConfirmationOfFundsService {
    private final ConsentJpaRepository consentJpaRepository;
    private final CmsConsentAuthorisationServiceInternal consentAuthorisationService;
    private final ConfirmationOfFundsConsentSpecification confirmationOfFundsConsentSpecification;
    private final CmsConfirmationOfFundsMapper consentMapper;
    private final CmsPsuConsentServiceInternal cmsPsuConsentServiceInternal;
    private final CmsPsuAuthorisationMapper cmsPsuAuthorisationMapper;

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String consentId,
                                             @NotNull String authorisationId, @NotNull ScaStatus status,
                                             @NotNull String instanceId, AuthenticationDataHolder authenticationDataHolder) throws AuthorisationIsExpiredException {
        Optional<ConsentEntity> actualConsent = getActualConsent(consentId, instanceId);

        if (actualConsent.isEmpty()) {
            log.info("Consent ID: [{}]. Update of authorisation status failed, because consent either has finalised status or not found", consentId);
            return false;
        }

        return consentAuthorisationService.getAuthorisationByAuthorisationId(authorisationId, instanceId)
                   .map(authorisation -> consentAuthorisationService.updateScaStatusAndAuthenticationData(status, authorisation, authenticationDataHolder))
                   .orElseGet(() -> {
                       log.info("Authorisation ID [{}], Instance ID: [{}]. Update authorisation status failed, because authorisation not found",
                                authorisationId, instanceId);
                       return false;
                   });
    }

    @Override
    @Transactional
    public boolean updatePsuDataInConsent(@NotNull PsuIdData psuIdData, @NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException {
        return consentAuthorisationService.getAuthorisationByAuthorisationId(authorisationId, instanceId)
                   .map(auth -> cmsPsuConsentServiceInternal.updatePsuData(auth, psuIdData, ConsentType.PIIS_ASPSP))
                   .orElseGet(() -> {
                       log.info("Authorisation ID [{}], Instance ID: [{}]. Update PSU  in consent failed, because authorisation not found",
                                authorisationId, instanceId);
                       return false;
                   });
    }

    @Override
    @Transactional
    public Optional<CmsConfirmationOfFundsResponse> checkRedirectAndGetConsent(String redirectId, String instanceId) throws RedirectUrlIsExpiredException {
        Optional<AuthorisationEntity> optionalAuthorisation = consentAuthorisationService.getAuthorisationByRedirectId(redirectId, instanceId);

        if (optionalAuthorisation.isPresent()) {
            AuthorisationEntity authorisation = optionalAuthorisation.get();
            return createCmsConsentResponseFromAuthorisation(authorisation, redirectId);
        }

        log.info("Authorisation ID [{}]. Check redirect URL and get consent failed, because authorisation not found or has finalised status",
                 redirectId);
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<CmsPsuConfirmationOfFundsAuthorisation> getAuthorisationByAuthorisationId(@NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException {
        Optional<CmsPsuConfirmationOfFundsAuthorisation> cmsPsuConfirmationOfFundsAuthorisation = consentAuthorisationService.getAuthorisationByAuthorisationId(authorisationId, instanceId)
                                                                                                      .map(cmsPsuAuthorisationMapper::mapToCmsPsuConfirmationOfFundsAuthorisation);
        if (cmsPsuConfirmationOfFundsAuthorisation.isEmpty()) {
            log.info("Authorisation ID [{}], Instance ID: [{}]. Get authorisation failed, because authorisation not found", authorisationId, instanceId);
        }

        return cmsPsuConfirmationOfFundsAuthorisation;
    }

    private Optional<ConsentEntity> getActualConsent(String consentId, String instanceId) {
        return consentJpaRepository.findOne(confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId))
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private Optional<CmsConfirmationOfFundsResponse> createCmsConsentResponseFromAuthorisation(AuthorisationEntity authorisation, String redirectId) {
        Optional<ConsentEntity> consentOptional = consentJpaRepository.findByExternalId(authorisation.getParentExternalId());
        if (consentOptional.isEmpty()) {
            log.info("Authorisation ID [{}]. Check redirect URL and get consent failed in createCmsConsentResponseFromAuthorisation method, because PIIS consent is null",
                     redirectId);
            return Optional.empty();
        }

        ConsentEntity consent = consentOptional.get();

        CmsConfirmationOfFundsConsent cmsConfirmationOfFundsConsent = mapToCmsConsentWithAuthorisations(consent);
        return Optional.of(new CmsConfirmationOfFundsResponse(cmsConfirmationOfFundsConsent, redirectId, authorisation.getTppOkRedirectUri(),
                                                              authorisation.getTppNokRedirectUri()));
    }

    private CmsConfirmationOfFundsConsent mapToCmsConsentWithAuthorisations(ConsentEntity consentEntity) {
        List<AuthorisationEntity> authorisations =
            consentAuthorisationService.getAuthorisationsByParentExternalId(consentEntity.getExternalId());
        return consentMapper.mapToCmsConfirmationOfFundsConsent(consentEntity, authorisations);
    }
}
