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

package de.adorsys.psd2.consent.service.aspsp;

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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsAspspTppServiceInternal implements CmsAspspTppService {
    private final TppStopListRepository stopListRepository;
    private final TppStopListMapper tppStopListMapper;
    private final TppInfoRepository tppInfoRepository;
    private final TppInfoMapper tppInfoMapper;

    @NotNull
    @Override
    public Optional<TppStopListRecord> getTppStopListRecord(@NotNull String tppAuthorisationNumber, @NotNull String instanceId) {
        Optional<TppStopListEntity> stopListEntityOptional = stopListRepository.findByTppAuthorisationNumberAndInstanceId(tppAuthorisationNumber, instanceId);
        return stopListEntityOptional.map(tppStopListMapper::mapToTppStopListRecord);
    }

    @Transactional
    @Override
    public boolean blockTpp(@NotNull String tppAuthorisationNumber, @NotNull String instanceId, @Nullable Duration lockPeriod) {
        Optional<TppStopListEntity> stopListEntityOptional = stopListRepository.findByTppAuthorisationNumberAndInstanceId(tppAuthorisationNumber, instanceId);

        TppStopListEntity entityToBeBlocked = stopListEntityOptional
                                                  .orElseGet(() -> {
                                                      TppStopListEntity entity = new TppStopListEntity();
                                                      entity.setTppAuthorisationNumber(tppAuthorisationNumber);
                                                      entity.setInstanceId(instanceId);
                                                      return entity;
                                                  });
        entityToBeBlocked.block(lockPeriod);
        if (stopListEntityOptional.isEmpty()) {
            stopListRepository.save(entityToBeBlocked);
        }
        return true;
    }

    @Transactional
    @Override
    public boolean unblockTpp(@NotNull String tppAuthorisationNumber, @NotNull String instanceId) {
        Optional<TppStopListEntity> stopListEntityOptional = stopListRepository.findByTppAuthorisationNumberAndInstanceId(tppAuthorisationNumber, instanceId);

        if (stopListEntityOptional.isPresent()) {
            TppStopListEntity entityToBeUnblocked = stopListEntityOptional.get();
            entityToBeUnblocked.unblock();
        }
        return true;
    }

    @NotNull
    @Override
    public Optional<TppInfo> getTppInfo(@NotNull String tppAuthorisationNumber, @NotNull String instanceId) {
        Optional<TppInfoEntity> tppInfoEntityOptional = tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(tppAuthorisationNumber, instanceId);
        return tppInfoEntityOptional.map(tppInfoMapper::mapToTppInfo);
    }
}
