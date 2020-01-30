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
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.*;
import de.adorsys.psd2.consent.repository.AisConsentActionRepository;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.repository.AisConsentVerifyingRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.consent.api.CmsError.LOGICAL_ERROR;
import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AisConsentServiceInternal implements AisConsentService {

    private final AisConsentJpaRepository aisConsentJpaRepository;
    private final AisConsentVerifyingRepository aisConsentRepository;
    private final AisConsentActionRepository aisConsentActionRepository;
    private final TppInfoRepository tppInfoRepository;
    private final AisConsentMapper consentMapper;
    private final PsuDataMapper psuDataMapper;
    private final AspspProfileService aspspProfileService;
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    private final TppInfoMapper tppInfoMapper;
    private final CmsPsuService cmsPsuService;
    private final AisConsentUsageService aisConsentUsageService;
    private final AisConsentRequestTypeService aisConsentRequestTypeService;
    private final OneOffConsentExpirationService oneOffConsentExpirationService;

    /**
     * Creates AIS consent.
     *
     * @param request needed parameters for creating AIS consent
     * @return create consent response, containing consent and its encrypted ID
     */
    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CreateAisConsentResponse> createConsent(CreateAisConsentRequest request) throws WrongChecksumException {

        if (request.getAllowedFrequencyPerDay() == null) {
            log.info("TPP ID: [{}]. Consent cannot be created, because request contains no allowed frequency per day",
                     request.getTppInfo().getAuthorisationNumber());
            return CmsResponse.<CreateAisConsentResponse>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }
        AisConsent consent = createConsentFromRequest(request);
        tppInfoRepository.findByAuthorisationNumber(request.getTppInfo().getAuthorisationNumber())
            .ifPresent(consent::setTppInfo);

        AisConsent savedConsent = aisConsentRepository.verifyAndSave(consent);

        if (savedConsent.getId() != null) {
            return CmsResponse.<CreateAisConsentResponse>builder()
                       .payload(new CreateAisConsentResponse(savedConsent.getExternalId(), consentMapper.mapToAisAccountConsent(savedConsent), consent.getTppNotificationContentPreferred()))
                       .build();

        } else {
            log.info("TPP ID: [{}], External Consent ID: [{}]. AIS consent cannot be created, because when saving to DB got null ID",
                     request.getTppInfo().getAuthorisationNumber(), consent.getExternalId());
            return CmsResponse.<CreateAisConsentResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }
    }

    /**
     * Reads status of consent by ID.
     *
     * @param consentId ID of consent
     * @return ConsentStatus
     */
    @Override
    @Transactional
    public CmsResponse<ConsentStatus> getConsentStatusById(String consentId) {
        Optional<ConsentStatus> consentStatusOptional = aisConsentJpaRepository.findByExternalId(consentId)
                                                            .map(aisConsentConfirmationExpirationService::checkAndUpdateOnConfirmationExpiration)
                                                            .map(this::checkAndUpdateOnExpiration)
                                                            .map(AisConsent::getConsentStatus);
        if (consentStatusOptional.isPresent()) {
            return CmsResponse.<ConsentStatus>builder()
                       .payload(consentStatusOptional.get())
                       .build();
        } else {
            log.info("Consent ID: [{}]. Get consent status failed, because consent not found", consentId);
            return CmsResponse.<ConsentStatus>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }
    }

    /**
     * Updates consent status by ID.
     *
     * @param consentId ID of consent
     * @param status    new consent status
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<Boolean> updateConsentStatusById(String consentId, ConsentStatus status) throws WrongChecksumException {
        Optional<AisConsent> consentOptional = getActualAisConsent(consentId);

        if (consentOptional.isPresent()) {
            AisConsent consent = consentOptional.get();
            boolean result = setStatusAndSaveConsent(consent, status);

            return CmsResponse.<Boolean>builder()
                       .payload(result)
                       .build();
        }

        log.info("Consent ID [{}]. Update consent status by ID failed, because consent not found", consentId);
        return CmsResponse.<Boolean>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    /**
     * Reads full information of consent by ID.
     *
     * @param consentId ID of consent
     * @return AisAccountConsent
     */
    @Override
    @Transactional
    public CmsResponse<AisAccountConsent> getAisAccountConsentById(String consentId) {
        Optional<AisAccountConsent> consentOptional = aisConsentJpaRepository.findByExternalId(consentId)
                                                          .map(aisConsentConfirmationExpirationService::checkAndUpdateOnConfirmationExpiration)
                                                          .map(this::checkAndUpdateOnExpiration)
                                                          .map(consentMapper::mapToAisAccountConsent);

        if (consentOptional.isPresent()) {
            return CmsResponse.<AisAccountConsent>builder()
                       .payload(consentOptional.get())
                       .build();
        }

        log.info("Consent ID [{}]. Get consent by ID failed, because consent not found", consentId);
        return CmsResponse.<AisAccountConsent>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    /**
     * Searches the old AIS consents and updates their statuses according to authorisation states and PSU data.
     *
     * @param newConsentId ID of new consent that was created
     * @return true if old consents were updated, false otherwise
     */
    @Override
    @Transactional
    public CmsResponse<Boolean> findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        AisConsent newConsent = aisConsentJpaRepository.findByExternalId(newConsentId)
                                    .orElseThrow(() -> {
                                        log.info("Consent ID: [{}]. Cannot find consent by ID", newConsentId);
                                        return new IllegalArgumentException("Wrong consent ID: " + newConsentId);
                                    });

        if (newConsent.isOneAccessType()) {
            log.info("Consent ID: [{}]. Cannot find old consents, because consent is OneAccessType", newConsentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        if (newConsent.isWrongConsentData()) {
            log.info("Consent ID: [{}]. Find old consents failed, because consent PSU data list is empty or TPP Info is null", newConsentId);
            throw new IllegalArgumentException("Wrong consent data");
        }

        List<PsuData> psuDataList = newConsent.getPsuDataList();
        Set<String> psuIds = psuDataList.stream()
                                 .filter(Objects::nonNull)
                                 .map(PsuData::getPsuId)
                                 .collect(Collectors.toSet());
        TppInfoEntity tppInfo = newConsent.getTppInfo();

        List<AisConsent> oldConsents = aisConsentJpaRepository.findOldConsentsByNewConsentParams(psuIds,
                                                                                                 tppInfo.getAuthorisationNumber(),
                                                                                                 newConsent.getInstanceId(),
                                                                                                 newConsent.getExternalId(),
                                                                                                 EnumSet.of(RECEIVED, PARTIALLY_AUTHORISED, VALID));

        List<AisConsent> oldConsentsWithExactPsuDataLists = oldConsents.stream()
                                                                .distinct()
                                                                .filter(c -> cmsPsuService.isPsuDataListEqual(c.getPsuDataList(), psuDataList))
                                                                .collect(Collectors.toList());

        if (oldConsentsWithExactPsuDataLists.isEmpty()) {
            log.info("Consent ID: [{}]. Cannot find old consents, because consent hasn't exact PSU data lists as old consents", newConsentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        oldConsentsWithExactPsuDataLists.forEach(this::updateStatus);
        aisConsentJpaRepository.saveAll(oldConsentsWithExactPsuDataLists);
        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    /**
     * Saves information about consent usage and consent's sub-resources usage.
     *
     * @param request {@link AisConsentActionRequest} needed parameters for logging usage AIS consent
     */
    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CmsResponse.VoidResponse> checkConsentAndSaveActionLog(AisConsentActionRequest request) throws WrongChecksumException {
        Optional<AisConsent> consentOpt = getActualAisConsent(request.getConsentId());
        if (consentOpt.isPresent()) {
            AisConsent consent = consentOpt.get();
            aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent);
            checkAndUpdateOnExpiration(consent);
            // In this method sonar claims that NPE is possible:
            // https://rules.sonarsource.com/java/RSPEC-3655
            // but we have isPresent in the code before.
            updateAisConsentUsage(consent, request); //NOSONAR
            logConsentAction(consent.getExternalId(), resolveConsentActionStatus(request, consent), request.getTppId()); //NOSONAR
        }

        return CmsResponse.<CmsResponse.VoidResponse>builder()
                   .payload(CmsResponse.voidResponse())
                   .build();
    }

    /**
     * Updates AIS consent account access by ID.
     *
     * @param request   needed parameters for updating AIS consent
     * @param consentId ID of the consent to be updated
     * @return String   consent ID
     */
    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<String> updateAspspAccountAccess(String consentId, AisAccountAccessInfo request) throws WrongChecksumException {
        Optional<AisConsent> consentOptional = aisConsentRepository.getActualAisConsent(consentId);

        if (!consentOptional.isPresent()) {
            log.info("Consent ID [{}]. Update aspsp account access failed, because consent not found",
                     consentId);
            return CmsResponse.<String>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        AisConsent consent = consentOptional.get();
        consent.addAspspAccountAccess(new AspspAccountAccessHolder(request).getAccountAccesses());

        String externalId = aisConsentRepository.verifyAndSave(consent).getExternalId();

        return CmsResponse.<String>builder()
                   .payload(externalId)
                   .build();
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<AisAccountConsent> updateAspspAccountAccessWithResponse(String consentId, AisAccountAccessInfo request) throws WrongChecksumException {
        Optional<AisConsent> consentOptional = aisConsentRepository.getActualAisConsent(consentId);

        if (!consentOptional.isPresent()) {
            log.info("Consent ID [{}]. Update aspsp account access with response failed, because consent not found",
                     consentId);
            return CmsResponse.<AisAccountConsent>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        AisConsent consent = consentOptional.get();

        Set<AspspAccountAccess> aspspAccesses = new AspspAccountAccessHolder(request).getAccountAccesses();

        List<AspspAccountAccess> cmsAccesses = consent.getAspspAccountAccesses();

        consent.setAspspAccountAccesses(getUpdatedAccesses(cmsAccesses, aspspAccesses));

        AisConsent aisConsent = aisConsentRepository.verifyAndUpdate(consent);

        return CmsResponse.<AisAccountConsent>builder()
                   .payload(consentMapper.mapToAisAccountConsent(aisConsent))
                   .build();
    }

    private AspspAccountAccess enrichAccount(Set<AspspAccountAccess> accounts, AspspAccountAccess element) {

        return accounts.stream()
                   .filter(a -> a.getAccountIdentifier().equals(element.getAccountIdentifier())
                                    && a.getAccountReferenceType() == element.getAccountReferenceType()
                                    && a.getCurrency() == element.getCurrency()
                                    && a.getTypeAccess() == (element.getTypeAccess()))
                   .findFirst()
                   .orElse(element);
    }

    private List<AspspAccountAccess> getUpdatedAccesses(List<AspspAccountAccess> cmsAccesses, Set<AspspAccountAccess> requestAccesses) {
        if (CollectionUtils.isEmpty(cmsAccesses)) {
            return new ArrayList<>(requestAccesses);
        }

        Set<AspspAccountAccess> updatedCmsAccesses = new HashSet<>();

        for (AspspAccountAccess access : cmsAccesses) {
            updatedCmsAccesses.add(enrichAccount(requestAccesses, access));
        }

        return new ArrayList<>(updatedCmsAccesses);
    }

    @Override
    public CmsResponse<List<PsuIdData>> getPsuDataByConsentId(String consentId) {
        Optional<List<PsuIdData>> psuIdDataOptional = getActualAisConsent(consentId)
                                                          .map(ac -> psuDataMapper.mapToPsuIdDataList(ac.getPsuDataList()));

        if (psuIdDataOptional.isPresent()) {
            return CmsResponse.<List<PsuIdData>>builder()
                       .payload(psuIdDataOptional.get())
                       .build();
        }

        log.info("Consent ID [{}]. Get psu data by consent id failed, because consent not found",
                 consentId);
        return CmsResponse.<List<PsuIdData>>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<Boolean> updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) throws WrongChecksumException {
        Optional<AisConsent> aisConsentOptional = aisConsentJpaRepository.findByExternalId(consentId);
        if (!aisConsentOptional.isPresent()) {
            log.info("Consent ID: [{}]. Get update multilevel SCA required status failed, because consent authorisation is not found",
                     consentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }
        AisConsent consent = aisConsentOptional.get();
        consent.setMultilevelScaRequired(multilevelScaRequired);

        aisConsentRepository.verifyAndSave(consent);

        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    private AisConsent checkAndUpdateOnExpiration(AisConsent consent) {
        if (consent != null && consent.shouldConsentBeExpired()) {
            return aisConsentConfirmationExpirationService.expireConsent(consent);
        }

        return consent;
    }

    private AisConsent createConsentFromRequest(CreateAisConsentRequest request) {

        AisConsent consent = new AisConsent();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setConsentStatus(RECEIVED);
        consent.setAllowedFrequencyPerDay(request.getAllowedFrequencyPerDay());
        consent.setTppFrequencyPerDay(request.getRequestedFrequencyPerDay());
        consent.setRequestDateTime(LocalDateTime.now());
        consent.setValidUntil(adjustExpireDate(request.getValidUntil()));
        consent.setPsuDataList(psuDataMapper.mapToPsuDataList(Collections.singletonList(request.getPsuData())));
        consent.setTppInfo(tppInfoMapper.mapToTppInfoEntity(request.getTppInfo()));
        AuthorisationTemplateEntity authorisationTemplate = new AuthorisationTemplateEntity();
        TppRedirectUri tppRedirectUri = request.getTppRedirectUri();
        if (tppRedirectUri != null) {
            authorisationTemplate.setRedirectUri(tppRedirectUri.getUri());
            authorisationTemplate.setNokRedirectUri(tppRedirectUri.getNokUri());
        }
        consent.setAuthorisationTemplate(authorisationTemplate);
        consent.addAccountAccess(new TppAccountAccessHolder(request.getAccess())
                                     .getAccountAccesses());
        consent.setRecurringIndicator(request.isRecurringIndicator());
        consent.setTppRedirectPreferred(request.isTppRedirectPreferred());
        consent.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
        consent.setAisConsentRequestType(aisConsentRequestTypeService.getRequestTypeFromAccess(request.getAccess()));
        consent.setAvailableAccounts(request.getAccess().getAvailableAccounts());
        consent.setAllPsd2(request.getAccess().getAllPsd2());
        consent.setAvailableAccountsWithBalance(request.getAccess().getAvailableAccountsWithBalance());
        consent.setLastActionDate(LocalDate.now());
        setAdditionalInformationTypes(consent, request.getAccess().getAccountAdditionalInformationAccess());
        consent.setInternalRequestId(request.getInternalRequestId());
        consent.setTppNotificationUri(request.getTppNotificationUri());
        consent.setTppNotificationContentPreferred(request.getNotificationSupportedModes());

        return consent;
    }

    private void setAdditionalInformationTypes(AisConsent consent, AccountAdditionalInformationAccess info) {
        AdditionalAccountInformationType ownerNameType = info == null
                                                             ? AdditionalAccountInformationType.NONE
                                                             : AdditionalAccountInformationType.findTypeByList(info.getOwnerName());
        consent.setOwnerNameType(ownerNameType);
    }

    private LocalDate adjustExpireDate(LocalDate validUntil) {
        int lifetime = aspspProfileService.getAspspSettings().getAis().getConsentTypes().getMaxConsentValidityDays();
        if (lifetime <= 0) {
            return validUntil;
        }

        //Expire date is inclusive and TPP can access AIS consent from current date
        LocalDate lifeTimeDate = LocalDate.now().plusDays(lifetime - 1L);
        return lifeTimeDate.isBefore(validUntil) ? lifeTimeDate : validUntil;
    }

    private ActionStatus resolveConsentActionStatus(AisConsentActionRequest request, AisConsent consent) {

        if (consent == null) {
            log.info("Consent ID: [{}]. Consent action status resolver received null consent",
                     request.getConsentId());
            return ActionStatus.BAD_PAYLOAD;
        }
        return request.getActionStatus();
    }

    private void logConsentAction(String requestedConsentId, ActionStatus actionStatus, String tppId) {
        AisConsentAction action = new AisConsentAction();
        action.setActionStatus(actionStatus);
        action.setRequestedConsentId(requestedConsentId);
        action.setTppId(tppId);
        action.setRequestDate(LocalDate.now());
        aisConsentActionRepository.save(action);
    }

    private Optional<AisConsent> getActualAisConsent(String consentId) {
        return aisConsentJpaRepository.findByExternalId(consentId)
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private boolean setStatusAndSaveConsent(AisConsent consent, ConsentStatus status) throws WrongChecksumException {
        if (consent.getConsentStatus().isFinalisedStatus()) {
            log.info("Consent ID: [{}], Consent status [{}]. Update consent status by ID failed, because consent status is finalised",
                     consent.getExternalId(), consent.getConsentStatus());
            return false;
        }
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(status);

        AisConsent aisConsent = aisConsentRepository.verifyAndSave(consent);

        return Optional.ofNullable(aisConsent)
                   .isPresent();
    }

    private void updateAisConsentUsage(AisConsent consent, AisConsentActionRequest request) throws WrongChecksumException {
        if (!request.isUpdateUsage()) {
            return;
        }
        aisConsentUsageService.incrementUsage(consent, request);

        if (!consent.isRecurringIndicator() && consent.getAllowedFrequencyPerDay() == 1 && oneOffConsentExpirationService.isConsentExpired(consent)) {
            consent.setConsentStatus(EXPIRED);
        }

        consent.setLastActionDate(LocalDate.now());

        aisConsentRepository.verifyAndSave(consent);
    }

    private void updateStatus(AisConsent aisConsent) {
        aisConsent.setConsentStatus(aisConsent.getConsentStatus() == RECEIVED || aisConsent.getConsentStatus() == PARTIALLY_AUTHORISED
                                        ? REJECTED
                                        : TERMINATED_BY_TPP);
    }
}
