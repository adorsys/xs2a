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
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
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
            log.info("Consent ID: [{}], Instance ID: [{}]. Update of authorisation status failed, because consent either has finalised status or not found", consentId, instanceId);
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
    public @NotNull Optional<CmsConfirmationOfFundsConsent> getConsent(@NotNull PsuIdData psuIdData,
                                                                       @NotNull String consentId,
                                                                       @NotNull String instanceId) {
        return consentJpaRepository.findOne(confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId))
                   .map(this::mapToCmsConsentWithAuthorisations);
    }

    @Override
    @Transactional
    public Optional<CmsConfirmationOfFundsResponse> checkRedirectAndGetConsent(String redirectId, String instanceId) throws RedirectUrlIsExpiredException {
        Optional<AuthorisationEntity> optionalAuthorisation = consentAuthorisationService.getAuthorisationByRedirectId(redirectId, instanceId);

        if (optionalAuthorisation.isPresent()) {
            AuthorisationEntity authorisation = optionalAuthorisation.get();
            return createCmsConsentResponseFromAuthorisation(authorisation, redirectId);
        }

        log.info("Authorisation ID [{}], Instance ID: [{}]. Check redirect URL and get consent failed, because authorisation not found or has finalised status",
                 redirectId, instanceId);
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

    @Override
    @Transactional
    public boolean updateConsentStatus(@NotNull String consentId, @NotNull ConsentStatus status, @NotNull String instanceId) {
        Optional<ConsentEntity> consentEntityOptional = getActualConsent(consentId, instanceId);

        if (consentEntityOptional.isEmpty()) {
            log.info("Consent ID: [{}], Instance ID: [{}]. Update of consent status failed, because consent either has finalised status or not found", consentId, instanceId);
            return false;
        }

        ConsentEntity consentEntity = consentEntityOptional.get();
        consentEntity.setConsentStatus(status);
        return true;
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
