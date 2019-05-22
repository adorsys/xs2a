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

import de.adorsys.psd2.consent.aspsp.api.tpp.CmsAspspTppService;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.repository.TppStopListRepository;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.consent.service.mapper.TppStopListMapper;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CmsAspspTppServiceInternal implements CmsAspspTppService {
    private final TppStopListRepository stopListRepository;
    private final TppInfoRepository tppInfoRepository;
    private final TppStopListMapper tppStopListMapper;
    private final TppInfoMapper tppInfoMapper;

    @NotNull
    @Override
    public Optional<TppStopListRecord> getTppStopListRecord(@NotNull String tppAuthorisationNumber, @NotNull String nationalAuthorityId, @NotNull String instanceId) {
        Optional<TppStopListEntity> stopListEntityOptional = stopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(tppAuthorisationNumber, nationalAuthorityId, instanceId);
        return stopListEntityOptional.map(tppStopListMapper::mapToTppStopListRecord);
    }

    @Transactional
    @Override
    public boolean blockTpp(@NotNull String tppAuthorisationNumber, @NotNull String nationalAuthorityId, @NotNull String instanceId, @Nullable Duration lockPeriod) {
        Optional<TppStopListEntity> stopListEntityOptional = stopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(tppAuthorisationNumber, nationalAuthorityId, instanceId);

        TppStopListEntity entityToBeBlocked = stopListEntityOptional
                                                  .orElseGet(() -> {
                                                      TppStopListEntity entity = new TppStopListEntity();
                                                      entity.setTppAuthorisationNumber(tppAuthorisationNumber);
                                                      entity.setNationalAuthorityId(nationalAuthorityId);
                                                      return entity;
                                                  });
        entityToBeBlocked.block(lockPeriod);

        stopListRepository.save(entityToBeBlocked);
        return true;
    }

    @Transactional
    @Override
    public boolean unblockTpp(@NotNull String tppAuthorisationNumber, @NotNull String nationalAuthorityId, @NotNull String instanceId) {
        Optional<TppStopListEntity> stopListEntityOptional = stopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(tppAuthorisationNumber, nationalAuthorityId, instanceId);

        if (stopListEntityOptional.isPresent()) {
            TppStopListEntity entityToBeUnblocked = stopListEntityOptional.get();
            entityToBeUnblocked.unblock();

            stopListRepository.save(entityToBeUnblocked);
        }
        return true;
    }

    @NotNull
    @Override
    public Optional<TppInfo> getTppInfo(@NotNull String tppId, @NotNull String instanceId) {
        Optional<TppInfoEntity> tppInfoEntityOptional = tppInfoRepository.findFirstByAuthorisationNumberAndInstanceIdOrderByIdDesc(tppId, instanceId);
        return tppInfoEntityOptional.map(tppInfoMapper::mapToTppInfo);
    }
}
