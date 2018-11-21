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

import de.adorsys.psd2.consent.api.piis.PiisConsentTppAccessType;
import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService;
import de.adorsys.psd2.consent.aspsp.api.piis.PiisConsent;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.AccountReferenceMapper;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.TERMINATED_BY_ASPSP;

@Service
@RequiredArgsConstructor
public class CmsAspspPiisServiceInternal implements CmsAspspPiisService {
    private final PiisConsentRepository piisConsentRepository;
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
        PiisConsentEntity consent = buildPiisConsent(psuIdData, tppInfo, accounts, validUntil, allowedFrequencyPerDay);
        consent.setExternalId(UUID.randomUUID().toString());
        PiisConsentEntity saved = piisConsentRepository.save(consent);
        return saved.getId() != null
                   ? Optional.ofNullable(saved.getExternalId())
                   : Optional.empty();
    }

    @Override
    public @NotNull List<PiisConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData) {
        return piisConsentRepository.findByPsuDataPsuId(psuIdData.getPsuId()).stream()
                   .map(piisConsentMapper::mapToPiisConsent)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean terminateConsent(@NotNull String consentId) {
        Optional<PiisConsentEntity> entityOptional = piisConsentRepository.findByExternalId(consentId);

        if (!entityOptional.isPresent()) {
            return false;
        }

        PiisConsentEntity entity = entityOptional.get();
        entity.setLastActionDate(LocalDate.now());
        entity.setConsentStatus(TERMINATED_BY_ASPSP);
        return piisConsentRepository.save(entity) != null;
    }

    private PiisConsentEntity buildPiisConsent(PsuIdData psuIdData,
                                               TppInfo tppInfo,
                                               List<AccountReference> accounts,
                                               LocalDate validUntil,
                                               int allowedFrequencyPerDay) {
        PiisConsentEntity consent = new PiisConsentEntity();
        consent.setConsentStatus(RECEIVED);
        consent.setRequestDateTime(OffsetDateTime.now());
        consent.setExpireDate(validUntil);
        consent.setPsuData(psuDataMapper.mapToPsuData(psuIdData));
        consent.setTppInfo(tppInfoMapper.mapToTppInfoEntity(tppInfo));
        consent.setAccounts(accountReferenceMapper.mapToAccountReferenceEntityList(accounts));
        PiisConsentTppAccessType accessType = tppInfo != null
                                                  ? PiisConsentTppAccessType.SINGLE_TPP
                                                  : PiisConsentTppAccessType.ALL_TPP;
        consent.setTppAccessType(accessType);
        consent.setAllowedFrequencyPerDay(allowedFrequencyPerDay);
        return consent;
    }
}
