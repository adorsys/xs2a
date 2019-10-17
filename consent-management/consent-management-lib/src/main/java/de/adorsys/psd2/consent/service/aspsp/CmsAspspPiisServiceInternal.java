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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.REVOKED_BY_PSU;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.TERMINATED_BY_ASPSP;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsAspspPiisServiceInternal implements CmsAspspPiisService {
    private final PiisConsentRepository piisConsentRepository;
    private final PiisConsentEntitySpecification piisConsentEntitySpecification;
    private final PiisConsentMapper piisConsentMapper;

    @Override
    @Transactional
    public Optional<String> createConsent(@NotNull PsuIdData psuIdData, @NotNull CreatePiisConsentRequest request) {
        if (isInvalidConsentCreationRequest(psuIdData, request)) {
            log.info("Consent cannot be created, because request contains no allowed tppInfo or empty psuIdData or empty accounts or validUntil or cardExpiryDate in the past");
            return Optional.empty();
        }

        closePreviousPiisConsents(psuIdData, request);

        PiisConsentEntity consent = piisConsentMapper.mapToPiisConsentEntity(psuIdData, request);
        PiisConsentEntity savedConsent = piisConsentRepository.save(consent);

        if (savedConsent.getId() != null) {
            return Optional.ofNullable(savedConsent.getExternalId());
        } else {
            log.info("External Consent ID: [{}]. PIIS consent cannot be created, because when saving to DB got null ID",
                     consent.getExternalId());
            return Optional.empty();
        }
    }

    @Override
    public @NotNull List<PiisConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData, @NotNull String instanceId) {
        if (psuIdData.isEmpty()) {
            return Collections.emptyList();
        }

        return piisConsentRepository.findAll(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, instanceId))
                   .stream()
                   .map(piisConsentMapper::mapToPiisConsent)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean terminateConsent(@NotNull String consentId, @NotNull String instanceId) {
        Optional<PiisConsentEntity> entityOptional = piisConsentRepository.findOne(piisConsentEntitySpecification.byConsentIdAndInstanceId(consentId, instanceId));

        if (!entityOptional.isPresent()) {
            log.info("Consent ID: [{}], Instance ID: [{}]. PIIS consent cannot be terminated, because it was not found by consentId and instanceId",
                     consentId, instanceId);
            return false;
        }

        PiisConsentEntity entity = entityOptional.get();
        changeStatusAndLastActionDate(entity, TERMINATED_BY_ASPSP);

        piisConsentRepository.save(entity);

        return true;
    }

    private void closePreviousPiisConsents(PsuIdData psuIdData, CreatePiisConsentRequest request) {
        AccountReference accountReference = request.getAccount();
        Specification<PiisConsentEntity> specification = piisConsentEntitySpecification.byPsuIdDataAndAuthorisationNumberAndAccountReference(psuIdData, request.getTppAuthorisationNumber(), accountReference);

        List<PiisConsentEntity> piisConsentEntities = piisConsentRepository.findAll(specification);
        List<PiisConsentEntity> consentsToRevoke = piisConsentEntities.stream()
                                                       .filter(c -> !c.getConsentStatus().isFinalisedStatus())
                                                       .collect(Collectors.toList());
        consentsToRevoke.forEach(entity -> changeStatusAndLastActionDate(entity, REVOKED_BY_PSU));

        piisConsentRepository.saveAll(consentsToRevoke);
    }

    private void changeStatusAndLastActionDate(PiisConsentEntity piisConsentEntity, ConsentStatus consentStatus) {
        piisConsentEntity.setLastActionDate(LocalDate.now());
        piisConsentEntity.setConsentStatus(consentStatus);
    }

    private boolean isInvalidConsentCreationRequest(@NotNull PsuIdData psuIdData, CreatePiisConsentRequest request) {
        return StringUtils.isBlank(request.getTppAuthorisationNumber())
                   || psuIdData.isEmpty()
                   || request.getAccount() == null
                   || request.getValidUntil() == null
                   || request.getValidUntil().isBefore(LocalDate.now())
                   || request.getCardExpiryDate() != null && request.getCardExpiryDate().isBefore(LocalDate.now());
    }
}
