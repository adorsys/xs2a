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

import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.consent.repository.TppStopListRepository;
import de.adorsys.psd2.xs2a.core.tpp.TppStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TppStopListScheduleTask {
    private final TppStopListRepository tppStopListRepository;

    @Scheduled(cron = "${stoplist.cron.expression}")
    @Transactional
    public void unblockTppIfBlockingExpired() {
        log.info("Tpp Stop List schedule task is run!");

        List<TppStopListEntity> blockedWithExpirationTpps = tppStopListRepository.findAllByStatusAndBlockingExpirationTimestampLessThanEqual(TppStatus.BLOCKED, OffsetDateTime.now());
        List<TppStopListEntity> unblockedTpps = unblockTpps(blockedWithExpirationTpps);

        if (!unblockedTpps.isEmpty()) {
            tppStopListRepository.save(unblockedTpps);
        }
    }

    private List<TppStopListEntity> unblockTpps(List<TppStopListEntity> blockedWithExpirationTpps) {
        return blockedWithExpirationTpps.stream()
                   .map(this::unblockTpp)
                   .collect(Collectors.toList());
    }

    private TppStopListEntity unblockTpp(TppStopListEntity tppStopListEntity) {
        tppStopListEntity.unblock();
        return tppStopListEntity;
    }
}
