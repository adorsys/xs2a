/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AspspAccountAccessRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
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
    private final ConsentJpaRepository consentJpaRepository;
    private final TppInfoRepository tppInfoRepository;
    private final PiisConsentEntitySpecification piisConsentEntitySpecification;
    private final PiisConsentMapper piisConsentMapper;
    private final PageRequestBuilder pageRequestBuilder;
    private final AspspAccountAccessRepository aspspAccountAccessRepository;

    @Override
    @Transactional
    public Optional<String> createConsent(@NotNull PsuIdData psuIdData, @NotNull CreatePiisConsentRequest request, @NotNull String instanceId) {
        if (isInvalidConsentCreationRequest(psuIdData, request)) {
            log.info("Consent cannot be created, because request contains no allowed tppInfo or empty psuIdData or empty accounts or validUntil or cardExpiryDate in the past");
            return Optional.empty();
        }

        closePreviousPiisConsents(psuIdData, request, instanceId);

        TppInfoEntity tppInfoEntity = getTppInfoEntity(request.getTppAuthorisationNumber());
        ConsentEntity consent = piisConsentMapper.mapToPiisConsentEntity(psuIdData, tppInfoEntity, request, instanceId);
        ConsentEntity savedConsent = consentJpaRepository.save(consent);
        aspspAccountAccessRepository.saveAll(savedConsent.getAspspAccountAccesses());

        if (savedConsent.getId() != null) {
            return Optional.ofNullable(savedConsent.getExternalId());
        } else {
            log.info("External Consent ID: [{}]. PIIS consent cannot be created, because when saving to DB got null ID",
                     consent.getExternalId());
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public @NotNull PageData<List<CmsPiisConsent>> getConsentsForPsu(@NotNull PsuIdData psuIdData, @NotNull String instanceId,
                                                                     Integer pageIndex, Integer itemsPerPage) {
        if (psuIdData.isEmpty()) {
            log.info("InstanceId: [{}]. Export PIIS consents by psu failed, psuIdData is empty or null.", instanceId);
            return new PageData<>(Collections.emptyList(), 0, itemsPerPage, 0);
        }

        return mapToPageData(consentJpaRepository.findAll(
            piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, instanceId),
            pageRequestBuilder.getPageable(pageIndex, itemsPerPage)));
    }

    @Override
    @Transactional
    public boolean terminateConsent(@NotNull String consentId, @NotNull String instanceId) {
        Optional<ConsentEntity> entityOptional = consentJpaRepository.findOne(piisConsentEntitySpecification.byConsentIdAndInstanceId(consentId, instanceId));

        if (entityOptional.isEmpty()) {
            log.info("Consent ID: [{}], Instance ID: [{}]. PIIS consent cannot be terminated, because it was not found by consentId and instanceId",
                     consentId, instanceId);
            return false;
        }

        ConsentEntity entity = entityOptional.get();
        changeStatusAndLastActionDate(entity, TERMINATED_BY_ASPSP);

        return true;
    }

    private void closePreviousPiisConsents(PsuIdData psuIdData, CreatePiisConsentRequest request, String instanceId) {
        AccountReference accountReference = request.getAccount();
        Specification<ConsentEntity> specification = piisConsentEntitySpecification
                                                         .byPsuIdDataAndAuthorisationNumberAndAccountReferenceAndInstanceId(psuIdData, request.getTppAuthorisationNumber(), accountReference, instanceId);

        List<ConsentEntity> piisConsentEntities = consentJpaRepository.findAll(specification);
        List<ConsentEntity> consentsToRevoke = piisConsentEntities.stream()
                                                   .filter(c -> !c.getConsentStatus().isFinalisedStatus())
                                                   .collect(Collectors.toList());
        consentsToRevoke.forEach(entity -> changeStatusAndLastActionDate(entity, REVOKED_BY_PSU));

        consentJpaRepository.saveAll(consentsToRevoke);
    }

    private void changeStatusAndLastActionDate(ConsentEntity consentEntity, ConsentStatus consentStatus) {
        consentEntity.setLastActionDate(LocalDate.now());
        consentEntity.setConsentStatus(consentStatus);
    }

    private TppInfoEntity getTppInfoEntity(String tppAuthorisationNumber) {
        Optional<TppInfoEntity> tppInfoEntityOptional = tppInfoRepository.findByAuthorisationNumber(tppAuthorisationNumber);
        if (tppInfoEntityOptional.isPresent()) {
            return tppInfoEntityOptional.get();
        }
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setAuthorisationNumber(tppAuthorisationNumber);
        tppInfoEntity.setAuthorityId("UNKNOWN");
        return tppInfoRepository.save(tppInfoEntity);
    }

    private boolean isInvalidConsentCreationRequest(@NotNull PsuIdData psuIdData, CreatePiisConsentRequest request) {
        return StringUtils.isBlank(request.getTppAuthorisationNumber())
                   || psuIdData.isEmpty()
                   || request.getAccount() == null
                   || request.getValidUntil() == null
                   || request.getValidUntil().isBefore(LocalDate.now())
                   || request.getCardExpiryDate() != null && request.getCardExpiryDate().isBefore(LocalDate.now());
    }

    private PageData<List<CmsPiisConsent>> mapToPageData(Page<ConsentEntity> entities) {
        return new PageData<>(entities
                                  .stream()
                                  .map(piisConsentMapper::mapToCmsPiisConsent)
                                  .collect(Collectors.toList()),
                              entities.getPageable().getPageNumber(),
                              entities.getPageable().getPageSize(),
                              entities.getTotalElements());
    }
}
