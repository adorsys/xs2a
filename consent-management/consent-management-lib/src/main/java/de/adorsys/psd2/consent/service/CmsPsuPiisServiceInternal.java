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

import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.psu.api.CmsPsuPiisService;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsPsuPiisServiceInternal implements CmsPsuPiisService {
    private final PiisConsentRepository piisConsentRepository;
    private final PiisConsentMapper piisConsentMapper;
    private final PsuDataMapper psuDataMapper;

    @Override
    public @NotNull Optional<PiisConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId) {
        return piisConsentRepository.findByExternalId(consentId)
                   .filter(con -> isPsuIdDataContentEquals(con, psuIdData))
                   .map(piisConsentMapper::mapToPiisConsent);
    }

    @Override
    public @NotNull List<PiisConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData) {
        return piisConsentRepository.findByPsuDataPsuId(psuIdData.getPsuId()).stream()
                   .filter(con -> isPsuIdDataContentEquals(con, psuIdData))
                   .map(piisConsentMapper::mapToPiisConsent)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean revokeConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId) {
        Optional<PiisConsentEntity> piisConsentEntity = piisConsentRepository.findByExternalId(consentId)
                                                            .filter(con -> isPsuIdDataContentEquals(con, psuIdData))
                                                            .filter(con -> !con.getConsentStatus().isFinalisedStatus());

        return piisConsentEntity.isPresent()
                   && revokeConsent(piisConsentEntity.get());
    }

    private boolean isPsuIdDataContentEquals(PiisConsentEntity piisConsentEntity, PsuIdData psuIdData) {
        PsuIdData psuIdDataMapped = psuDataMapper.mapToPsuIdData(piisConsentEntity.getPsuData());
        return Optional.ofNullable(psuIdDataMapped)
                   .map(psu -> psu.contentEquals(psuIdData))
                   .orElse(false);
    }

    private boolean revokeConsent(PiisConsentEntity consent) {
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(ConsentStatus.REVOKED_BY_PSU);
        piisConsentRepository.save(consent);
        return true;
    }
}
