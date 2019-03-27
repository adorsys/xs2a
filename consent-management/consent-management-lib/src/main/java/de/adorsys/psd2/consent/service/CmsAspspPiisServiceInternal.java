/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.AccountReferenceMapper;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.piis.PiisConsentTppAccessType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.TERMINATED_BY_ASPSP;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsAspspPiisServiceInternal implements CmsAspspPiisService {
    private final PiisConsentRepository piisConsentRepository;
    private final PiisConsentEntitySpecification piisConsentEntitySpecification;
    private final PiisConsentMapper piisConsentMapper;
    private final PsuDataMapper psuDataMapper;
    private final TppInfoMapper tppInfoMapper;
    private final AccountReferenceMapper accountReferenceMapper;

    @Override
    @Transactional
    public Optional<String> createConsent(@NotNull PsuIdData psuIdData,
                                          @Nullable TppInfo tppInfo,
                                          @NotNull List<AccountReference> accounts,
                                          @NotNull LocalDate validUntil,
                                          int allowedFrequencyPerDay) {
        CreatePiisConsentRequest request = new CreatePiisConsentRequest();
        request.setTppInfo(tppInfo);
        request.setAccounts(accounts);
        request.setValidUntil(validUntil);
        request.setAllowedFrequencyPerDay(allowedFrequencyPerDay);

        return createConsent(psuIdData, request);
    }

    @Override
    @Transactional
    public Optional<String> createConsent(@NotNull PsuIdData psuIdData, @NotNull CreatePiisConsentRequest request) {
        if (isInvalidConsentCreationRequest(psuIdData, request)) {
            log.info("Consent cannot be created, because request contains no allowed tppInfo or empty psuIdData or empty accounts or validUntil or cardExpiryDate in the past");
            return Optional.empty();
        }

        PiisConsentEntity consent = buildPiisConsent(psuIdData, request);
        PiisConsentEntity saved = piisConsentRepository.save(consent);

        if (saved.getId() != null) {
            return Optional.ofNullable(saved.getExternalId());
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
        Optional<PiisConsentEntity> entityOptional = Optional.ofNullable(piisConsentRepository.findOne(piisConsentEntitySpecification.byConsentIdAndInstanceId(consentId, instanceId)));

        if (!entityOptional.isPresent()) {
            log.info("Consent ID: [{}], Instance ID: [{}]. Consent cannot be terminated, because not found by consentId and instanceId",
                     consentId, instanceId);
            return false;
        }

        PiisConsentEntity entity = entityOptional.get();
        entity.setLastActionDate(LocalDate.now());
        entity.setConsentStatus(TERMINATED_BY_ASPSP);
        piisConsentRepository.save(entity);

        return true;
    }

    private PiisConsentEntity buildPiisConsent(PsuIdData psuIdData, CreatePiisConsentRequest request) {
        PiisConsentEntity consent = new PiisConsentEntity();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setConsentStatus(VALID);
        consent.setRequestDateTime(OffsetDateTime.now());
        consent.setExpireDate(request.getValidUntil());
        consent.setPsuData(psuDataMapper.mapToPsuData(psuIdData));
        consent.setTppInfo(tppInfoMapper.mapToTppInfoEntity(request.getTppInfo()));
        consent.setAccounts(accountReferenceMapper.mapToAccountReferenceEntityList(request.getAccounts()));
        PiisConsentTppAccessType accessType = request.getTppInfo() != null
                                                  ? PiisConsentTppAccessType.SINGLE_TPP
                                                  : PiisConsentTppAccessType.ALL_TPP;
        consent.setTppAccessType(accessType);
        consent.setAllowedFrequencyPerDay(request.getAllowedFrequencyPerDay());
        consent.setCardNumber(request.getCardNumber());
        consent.setCardExpiryDate(request.getCardExpiryDate());
        consent.setCardInformation(request.getCardInformation());
        consent.setRegistrationInformation(request.getRegistrationInformation());
        return consent;
    }

    private boolean isInvalidConsentCreationRequest(@NotNull PsuIdData psuIdData, CreatePiisConsentRequest request) {
        boolean invalidTpp = request.getTppInfo() != null
                                 && request.getTppInfo().isNotValid();

        return invalidTpp
                   || psuIdData.isEmpty()
                   || CollectionUtils.isEmpty(request.getAccounts())
                   || request.getValidUntil() == null
                   || request.getValidUntil().isBefore(LocalDate.now())
                   || request.getCardExpiryDate() != null && request.getCardExpiryDate().isBefore(LocalDate.now());
    }
}
