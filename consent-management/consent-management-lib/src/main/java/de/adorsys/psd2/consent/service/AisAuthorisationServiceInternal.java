/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.ScaMethod;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.ScaMethodMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AisAuthorisationServiceInternal implements AisConsentAuthorisationService {
    private final AisConsentRepository aisConsentRepository;
    private final AisConsentAuthorisationRepository aisConsentAuthorisationRepository;
    private final AisConsentMapper consentMapper;
    private final PsuDataMapper psuDataMapper;
    private final AspspProfileService aspspProfileService;
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    private final ScaMethodMapper scaMethodMapper;
    private final CmsPsuService cmsPsuService;

    /**
     * Create consent authorization
     *
     * @param consentId id of consent
     * @param request   needed parameters for creating consent authorization
     * @return String authorization id
     */
    @Override
    @Transactional
    public Optional<String> createAuthorization(String consentId, AisConsentAuthorizationRequest request) {
        return aisConsentRepository.findByExternalId(consentId)
                   .filter(con -> !con.getConsentStatus().isFinalisedStatus())
                   .map(aisConsent -> {
                       closePreviousAuthorisationsByPsu(aisConsent.getAuthorizations(), request.getPsuData());
                       return saveNewAuthorization(aisConsent, request);
                   });
    }

    /**
     * Get consent authorization
     *
     * @param consentId       id of consent
     * @param authorizationId id of authorisation session
     * @return AisConsentAuthorizationResponse
     */
    @Override
    public Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        boolean consentPresent = aisConsentRepository.findByExternalId(consentId)
                                     .filter(c -> !c.getConsentStatus().isFinalisedStatus())
                                     .isPresent();

        if (consentPresent) {
            return aisConsentAuthorisationRepository.findByExternalId(authorizationId)
                       .map(consentMapper::mapToAisConsentAuthorizationResponse);
        }

        log.info("Consent ID: [{}], Authorisation ID: [{}]. Get account consent authorisation failed, because consent is not found",
                 consentId, authorizationId);
        return Optional.empty();
    }

    /**
     * Gets list of consent authorisation IDs by consent ID
     *
     * @param consentId id of consent
     * @return Gets list of consent authorisation IDs
     */
    @Override
    public Optional<List<String>> getAuthorisationsByConsentId(String consentId) {
        return aisConsentRepository.findByExternalId(consentId)
                   .map(cst -> cst.getAuthorizations().stream()
                                   .map(AisConsentAuthorization::getExternalId)
                                   .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        Optional<AisConsent> consentOptional = aisConsentRepository.findByExternalId(consentId);
        if (!consentOptional.isPresent()) {
            log.info("Consent ID: [{}], Authorisation ID: [{}]. Get authorisation SCA status failed, because consent is not found",
                     consentId, authorisationId);
            return Optional.empty();
        }

        AisConsent consent = consentOptional.get();
        if (aisConsentConfirmationExpirationService.isConsentConfirmationExpired(consent)) {
            aisConsentConfirmationExpirationService.updateConsentOnConfirmationExpiration(consent);
            log.info("Consent ID: [{}], Authorisation ID: [{}]. Get authorisation SCA status failed, because consent is expired",
                     consentId, authorisationId);
            return Optional.of(ScaStatus.FAILED);
        }

        Optional<AisConsentAuthorization> authorisation = findAuthorisationInConsent(authorisationId, consent);
        return authorisation.map(AisConsentAuthorization::getScaStatus);
    }

    @Override
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        Optional<AisConsentAuthorization> authorisationOptional = aisConsentAuthorisationRepository.findByExternalId(authorisationId);

        return authorisationOptional.map(a -> a.getAvailableScaMethods()
                                                  .stream()
                                                  .filter(m -> Objects.equals(m.getAuthenticationMethodId(), authenticationMethodId))
                                                  .anyMatch(ScaMethod::isDecoupled))
                   .orElseGet(() -> {
                       log.info("Authorisation ID: [{}]. Get authorisation method decoupled status failed, because consent authorisation is not found",
                                authorisationId);
                       return false;
                   });
    }

    @Override
    @Transactional
    public boolean saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        Optional<AisConsentAuthorization> authorisationOptional = aisConsentAuthorisationRepository.findByExternalId(authorisationId);

        if (!authorisationOptional.isPresent()) {
            log.info(" Authorisation ID: [{}]. Save authentication methods failed, because authorisation is not found", authorisationId);
            return false;
        }

        AisConsentAuthorization authorisation = authorisationOptional.get();

        authorisation.setAvailableScaMethods(scaMethodMapper.mapToScaMethods(methods));
        aisConsentAuthorisationRepository.save(authorisation);
        return true;
    }

    /**
     * Update consent authorization
     *
     * @param authorisationId id of authorisation session
     * @param request         needed parameters for updating consent authorization
     * @return boolean
     */
    @Override
    @Transactional
    public boolean updateConsentAuthorization(String authorisationId, AisConsentAuthorizationRequest request) {
        Optional<AisConsentAuthorization> aisConsentAuthorizationOptional = aisConsentAuthorisationRepository.findByExternalId(authorisationId);

        if (!aisConsentAuthorizationOptional.isPresent()) {
            log.info("Authorisation ID: [{}]. Update consent authorisation failed, because consent authorisation is not found",
                     authorisationId);
            return false;
        }

        AisConsentAuthorization aisConsentAuthorisation = aisConsentAuthorizationOptional.get();
        PsuIdData psuDataFromRequest = request.getPsuData();
        closePreviousAuthorisationsByPsu(aisConsentAuthorisation, psuDataFromRequest);

        if (aisConsentAuthorisation.getScaStatus().isFinalisedStatus()) {
            log.info("Authorisation ID: [{}], SCA status: [{}]. Update consent authorisation failed, because consent authorisation has finalised status",
                     authorisationId, aisConsentAuthorisation.getScaStatus().getValue());
            return false;
        }

        if (ScaStatus.STARTED == aisConsentAuthorisation.getScaStatus()) {
            PsuData psuRequest = psuDataMapper.mapToPsuData(psuDataFromRequest);

            if (!cmsPsuService.isPsuDataRequestCorrect(psuRequest, aisConsentAuthorisation.getPsuData())) {
                log.info("Authorisation ID: [{}], SCA status: [{}]. Update consent authorisation failed, because psu data request does not match stored psu data",
                         authorisationId, aisConsentAuthorisation.getScaStatus().getValue());
                return false;
            }

            AisConsent aisConsent = aisConsentAuthorisation.getConsent();
            Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(psuRequest, aisConsent.getPsuDataList());

            if (psuDataOptional.isPresent()) {
                PsuData psuData = psuDataOptional.get();
                aisConsent.setPsuDataList(cmsPsuService.enrichPsuData(psuData, aisConsent.getPsuDataList()));
                aisConsentAuthorisation.setPsuData(psuData);
            }

            aisConsentAuthorisation.setConsent(aisConsent);
        }

        if (ScaStatus.SCAMETHODSELECTED == request.getScaStatus()) {
            // TODO refactor logic and don't save tan and password data in plain text https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/390
            aisConsentAuthorisation.setAuthenticationMethodId(request.getAuthenticationMethodId());
        }

        aisConsentAuthorisation.setScaStatus(request.getScaStatus());
        aisConsentAuthorisation = aisConsentAuthorisationRepository.save(aisConsentAuthorisation);

        return aisConsentAuthorisation.getExternalId() != null;
    }

    @Override
    @Transactional
    public boolean updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        Optional<AisConsentAuthorization> aisConsentAuthorisationOptional = aisConsentAuthorisationRepository.findByExternalId(authorisationId);

        if (!aisConsentAuthorisationOptional.isPresent()) {
            log.info("Authorisation ID: [{}]. Update SCA approach failed, because consent authorisation is not found",
                     authorisationId);
            return false;
        }

        AisConsentAuthorization aisConsentAuthorisation = aisConsentAuthorisationOptional.get();

        aisConsentAuthorisation.setScaApproach(scaApproach);
        aisConsentAuthorisationRepository.save(aisConsentAuthorisation);
        return true;
    }

    private String saveNewAuthorization(AisConsent aisConsent, AisConsentAuthorizationRequest request) {
        AisConsentAuthorization consentAuthorization = new AisConsentAuthorization();
        Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(psuDataMapper.mapToPsuData(request.getPsuData()), aisConsent.getPsuDataList());

        if (psuDataOptional.isPresent()) {
            PsuData psuData = psuDataOptional.get();
            aisConsent.setPsuDataList(cmsPsuService.enrichPsuData(psuData, aisConsent.getPsuDataList()));
            consentAuthorization.setPsuData(psuData);
        }

        consentAuthorization.setExternalId(UUID.randomUUID().toString());
        consentAuthorization.setConsent(aisConsent);
        consentAuthorization.setScaStatus(request.getScaStatus());
        consentAuthorization.setRedirectUrlExpirationTimestamp(OffsetDateTime.now().plus(aspspProfileService.getAspspSettings().getRedirectUrlExpirationTimeMs(), ChronoUnit.MILLIS));
        consentAuthorization.setScaApproach(request.getScaApproach());
        return aisConsentAuthorisationRepository.save(consentAuthorization).getExternalId();
    }

    private Optional<AisConsentAuthorization> findAuthorisationInConsent(String authorisationId, AisConsent consent) {
        return consent.getAuthorizations()
                   .stream()
                   .filter(auth -> auth.getExternalId().equals(authorisationId))
                   .findFirst();
    }

    private void closePreviousAuthorisationsByPsu(AisConsentAuthorization authorisation, PsuIdData psuIdData) {
        AisConsent consent = authorisation.getConsent();

        List<AisConsentAuthorization> previousAuthorisations = consent.getAuthorizations().stream()
                                                                   .filter(a -> !a.getExternalId().equals(authorisation.getExternalId()))
                                                                   .collect(Collectors.toList());

        closePreviousAuthorisationsByPsu(previousAuthorisations, psuIdData);
    }

    private void closePreviousAuthorisationsByPsu(List<AisConsentAuthorization> authorisations, PsuIdData psuIdData) {
        PsuData psuData = psuDataMapper.mapToPsuData(psuIdData);

        if (Objects.isNull(psuData)
                || psuData.isEmpty()) {
            log.info("Close previous authorisations by psu failed, because psuData is not allowed");
            return;
        }

        List<AisConsentAuthorization> aisConsentAuthorisations = authorisations
                                                                     .stream()
                                                                     .filter(auth -> Objects.nonNull(auth.getPsuData()) && auth.getPsuData().contentEquals(psuData))
                                                                     .map(this::makeAuthorisationFailedAndExpired)
                                                                     .collect(Collectors.toList());

        aisConsentAuthorisationRepository.save(aisConsentAuthorisations);
    }

    private AisConsentAuthorization makeAuthorisationFailedAndExpired(AisConsentAuthorization auth) {
        auth.setScaStatus(ScaStatus.FAILED);
        auth.setRedirectUrlExpirationTimestamp(OffsetDateTime.now());
        return auth;
    }
}
