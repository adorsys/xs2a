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
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationService;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.ScaMethod;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.ScaMethodMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.adorsys.psd2.consent.api.CmsError.LOGICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AisAuthorisationServiceInternal implements AisConsentAuthorisationService {
    private final AisConsentJpaRepository aisConsentJpaRepository;
    private final AisConsentAuthorisationRepository aisConsentAuthorisationRepository;
    private final AisConsentMapper consentMapper;
    private final PsuDataMapper psuDataMapper;
    private final AspspProfileService aspspProfileService;
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    private final ScaMethodMapper scaMethodMapper;
    private final CmsPsuService cmsPsuService;

    /**
     * Create consent authorisation
     *
     * @param consentId id of consent
     * @param request   needed parameters for creating consent authorisation
     * @return CreateAisConsentAuthorizationResponse object with authorisation id and scaStatus
     */
    @Override
    @Transactional
    public CmsResponse<CreateAisConsentAuthorizationResponse> createAuthorizationWithResponse(String consentId, AisConsentAuthorizationRequest request) {
        Optional<CreateAisConsentAuthorizationResponse> responseOptional = aisConsentJpaRepository.findByExternalId(consentId)
                                                                               .filter(con -> !con.getConsentStatus().isFinalisedStatus())
                                                                               .map(aisConsent -> {
                                                                                   closePreviousAuthorisationsByPsu(aisConsent.getAuthorizations(), request.getPsuData());
                                                                                   AisConsentAuthorization newAuthorisation = saveNewAuthorization(aisConsent, request);

                                                                                   return new CreateAisConsentAuthorizationResponse(newAuthorisation.getExternalId(), newAuthorisation.getScaStatus(), aisConsent.getInternalRequestId(), request.getPsuData());
                                                                               });

        if (responseOptional.isPresent()) {
            return CmsResponse.<CreateAisConsentAuthorizationResponse>builder()
                       .payload(responseOptional.get())
                       .build();
        }

        log.info("Consent ID: [{}]. Created authorisation failed, because consent is not found", consentId);
        return CmsResponse.<CreateAisConsentAuthorizationResponse>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    /**
     * Get consent authorization
     *
     * @param consentId       id of consent
     * @param authorizationId id of authorisation session
     * @return AisConsentAuthorizationResponse
     */
    @Override
    public CmsResponse<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        boolean consentPresent = aisConsentJpaRepository.findByExternalId(consentId)
                                     .filter(c -> !c.getConsentStatus().isFinalisedStatus())
                                     .isPresent();

        if (consentPresent) {
            Optional<AisConsentAuthorizationResponse> authorisationResponse = aisConsentAuthorisationRepository.findByExternalId(authorizationId)
                                                                                  .map(consentMapper::mapToAisConsentAuthorizationResponse);
            if (authorisationResponse.isPresent()) {
                return CmsResponse.<AisConsentAuthorizationResponse>builder()
                           .payload(authorisationResponse.get())
                           .build();
            }
        }

        log.info("Consent ID: [{}], Authorisation ID: [{}]. Get account consent authorisation failed, because consent is not found",
                 consentId, authorizationId);
        return CmsResponse.<AisConsentAuthorizationResponse>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    /**
     * Gets list of consent authorisation IDs by consent ID
     *
     * @param consentId id of consent
     * @return Gets list of consent authorisation IDs
     */
    @Override
    public CmsResponse<List<String>> getAuthorisationsByConsentId(String consentId) {
        Optional<List<String>> authorisationsListOptional = aisConsentJpaRepository.findByExternalId(consentId)
                                                                .map(cst -> cst.getAuthorizations().stream()
                                                                                .map(AisConsentAuthorization::getExternalId)
                                                                                .collect(Collectors.toList()));

        if (authorisationsListOptional.isPresent()) {
            return CmsResponse.<List<String>>builder()
                       .payload(authorisationsListOptional.get())
                       .build();
        }

        log.info("Consent ID: [{}]. Get the list of authorisation IDs failed, because consent is not found",
                 consentId);
        return CmsResponse.<List<String>>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        Optional<AisConsent> consentOptional = aisConsentJpaRepository.findByExternalId(consentId);
        if (!consentOptional.isPresent()) {
            log.info("Consent ID: [{}], Authorisation ID: [{}]. Get authorisation SCA status failed, because consent is not found",
                     consentId, authorisationId);
            return CmsResponse.<ScaStatus>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        AisConsent consent = consentOptional.get();
        if (aisConsentConfirmationExpirationService.isConsentConfirmationExpired(consent)) {
            aisConsentConfirmationExpirationService.updateConsentOnConfirmationExpiration(consent);
            log.info("Consent ID: [{}], Authorisation ID: [{}]. Get authorisation SCA status failed, because consent is expired",
                     consentId, authorisationId);
            return CmsResponse.<ScaStatus>builder()
                       .payload(ScaStatus.FAILED)
                       .build();
        }

        Optional<AisConsentAuthorization> authorisation = findAuthorisationInConsent(authorisationId, consent);
        if (authorisation.isPresent()) {
            return CmsResponse.<ScaStatus>builder()
                       .payload(authorisation.get().getScaStatus())
                       .build();
        }
        return CmsResponse.<ScaStatus>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        Optional<AisConsentAuthorization> authorisationOptional = aisConsentAuthorisationRepository.findByExternalId(authorisationId);

        Optional<Boolean> isDecoupledOptional = authorisationOptional.map(a -> a.getAvailableScaMethods()
                                                                                   .stream()
                                                                                   .filter(m -> Objects.equals(m.getAuthenticationMethodId(), authenticationMethodId))
                                                                                   .anyMatch(ScaMethod::isDecoupled));
        if (isDecoupledOptional.isPresent()) {
            return CmsResponse.<Boolean>builder()
                       .payload(isDecoupledOptional.get())
                       .build();
        }

        log.info("Authorisation ID: [{}]. Get authorisation method decoupled status failed, because consent authorisation is not found",
                 authorisationId);
        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        Optional<AisConsentAuthorization> authorisationOptional = aisConsentAuthorisationRepository.findByExternalId(authorisationId);

        if (!authorisationOptional.isPresent()) {
            log.info(" Authorisation ID: [{}]. Save authentication methods failed, because authorisation is not found", authorisationId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        AisConsentAuthorization authorisation = authorisationOptional.get();

        authorisation.setAvailableScaMethods(scaMethodMapper.mapToScaMethods(methods));
        aisConsentAuthorisationRepository.save(authorisation);
        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
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
    public CmsResponse<Boolean> updateConsentAuthorization(String authorisationId, AisConsentAuthorizationRequest request) {
        Optional<AisConsentAuthorization> aisConsentAuthorizationOptional = aisConsentAuthorisationRepository.findByExternalId(authorisationId);

        if (!aisConsentAuthorizationOptional.isPresent()) {
            log.info("Authorisation ID: [{}]. Update consent authorisation failed, because consent authorisation is not found",
                     authorisationId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        AisConsentAuthorization aisConsentAuthorisation = aisConsentAuthorizationOptional.get();
        PsuIdData psuDataFromRequest = request.getPsuData();
        closePreviousAuthorisationsByPsu(aisConsentAuthorisation, psuDataFromRequest);

        if (aisConsentAuthorisation.getScaStatus().isFinalisedStatus()) {
            log.info("Authorisation ID: [{}], SCA status: [{}]. Update consent authorisation failed, because consent authorisation has finalised status",
                     authorisationId, aisConsentAuthorisation.getScaStatus().getValue());
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        if (ScaStatus.RECEIVED == aisConsentAuthorisation.getScaStatus()) {
            PsuData psuRequest = psuDataMapper.mapToPsuData(psuDataFromRequest);

            if (!cmsPsuService.isPsuDataRequestCorrect(psuRequest, aisConsentAuthorisation.getPsuData())) {
                log.info("Authorisation ID: [{}], SCA status: [{}]. Update consent authorisation failed, because psu data request does not match stored psu data",
                         authorisationId, aisConsentAuthorisation.getScaStatus().getValue());
                return CmsResponse.<Boolean>builder()
                           .payload(false)
                           .build();
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
            aisConsentAuthorisation.setAuthenticationMethodId(request.getAuthenticationMethodId());
        }

        aisConsentAuthorisation.setScaStatus(request.getScaStatus());
        aisConsentAuthorisation = aisConsentAuthorisationRepository.save(aisConsentAuthorisation);

        return CmsResponse.<Boolean>builder()
                   .payload(aisConsentAuthorisation.getExternalId() != null)
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateConsentAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        Optional<AisConsentAuthorization> aisConsentAuthorisationOptional = aisConsentAuthorisationRepository.findByExternalId(authorisationId);

        if (!aisConsentAuthorisationOptional.isPresent()) {
            log.info("Authorisation ID: [{}]. Update consent authorisation failed, because consent authorisation is not found",
                     authorisationId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        AisConsentAuthorization aisConsentAuthorisation = aisConsentAuthorisationOptional.get();
        aisConsentAuthorisation.setScaStatus(scaStatus);
        aisConsentAuthorisationRepository.save(aisConsentAuthorisation);

        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        Optional<AisConsentAuthorization> aisConsentAuthorisationOptional = aisConsentAuthorisationRepository.findByExternalId(authorisationId);

        if (!aisConsentAuthorisationOptional.isPresent()) {
            log.info("Authorisation ID: [{}]. Update SCA approach failed, because consent authorisation is not found",
                     authorisationId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        AisConsentAuthorization aisConsentAuthorisation = aisConsentAuthorisationOptional.get();

        aisConsentAuthorisation.setScaApproach(scaApproach);
        aisConsentAuthorisationRepository.save(aisConsentAuthorisation);
        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    @Override
    public CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationID) {
        Optional<AuthorisationScaApproachResponse> approachResponseOptional = aisConsentAuthorisationRepository.findByExternalId(authorisationID)
                                                                                  .map(a -> new AuthorisationScaApproachResponse(a.getScaApproach()));

        if (approachResponseOptional.isPresent()) {
            return CmsResponse.<AuthorisationScaApproachResponse>builder()
                       .payload(approachResponseOptional.get())
                       .build();
        }

        return CmsResponse.<AuthorisationScaApproachResponse>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    private AisConsentAuthorization saveNewAuthorization(AisConsent aisConsent, AisConsentAuthorizationRequest request) {
        AisConsentAuthorization consentAuthorization = new AisConsentAuthorization();
        Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(psuDataMapper.mapToPsuData(request.getPsuData()), aisConsent.getPsuDataList());

        ScaStatus scaStatus = request.getScaStatus();

        if (psuDataOptional.isPresent()) {
            PsuData psuData = psuDataOptional.get();
            aisConsent.setPsuDataList(cmsPsuService.enrichPsuData(psuData, aisConsent.getPsuDataList()));
            consentAuthorization.setPsuData(psuData);
            scaStatus = ScaStatus.PSUIDENTIFIED;
        }

        consentAuthorization.setExternalId(UUID.randomUUID().toString());
        consentAuthorization.setConsent(aisConsent);
        consentAuthorization.setScaStatus(scaStatus);
        consentAuthorization.setRedirectUrlExpirationTimestamp(OffsetDateTime.now().plus(aspspProfileService.getAspspSettings().getCommon().getRedirectUrlExpirationTimeMs(), ChronoUnit.MILLIS));
        consentAuthorization.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plus(aspspProfileService.getAspspSettings().getCommon().getAuthorisationExpirationTimeMs(), ChronoUnit.MILLIS));
        consentAuthorization.setScaApproach(request.getScaApproach());
        TppRedirectUri redirectURIs = request.getTppRedirectURIs();
        AuthorisationTemplateEntity authorisationTemplate = aisConsent.getAuthorisationTemplate();
        consentAuthorization.setTppOkRedirectUri(StringUtils.defaultIfBlank(redirectURIs.getUri(), authorisationTemplate.getRedirectUri()));
        consentAuthorization.setTppNokRedirectUri(StringUtils.defaultIfBlank(redirectURIs.getNokUri(), authorisationTemplate.getNokRedirectUri()));
        return aisConsentAuthorisationRepository.save(consentAuthorization);
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

        aisConsentAuthorisationRepository.saveAll(aisConsentAuthorisations);
    }

    private AisConsentAuthorization makeAuthorisationFailedAndExpired(AisConsentAuthorization auth) {
        auth.setScaStatus(ScaStatus.FAILED);
        auth.setRedirectUrlExpirationTimestamp(OffsetDateTime.now());
        return auth;
    }
}
