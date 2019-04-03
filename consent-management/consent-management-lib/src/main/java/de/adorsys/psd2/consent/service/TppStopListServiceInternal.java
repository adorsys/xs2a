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

import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.consent.repository.TppStopListRepository;
import de.adorsys.psd2.xs2a.core.tpp.TppUniqueParamsHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TppStopListServiceInternal implements TppStopListService {
    private final TppStopListRepository tppStopListRepository;

    @Value("${cms.service.instance-id:UNDEFINED}")
    private String serviceInstanceId;

    @Override
    public boolean checkIfTppBlocked(TppUniqueParamsHolder tppUniqueParams) {
        Optional<TppStopListEntity> stopListEntityOptional = tppStopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(tppUniqueParams.getAuthorisationNumber(), tppUniqueParams.getAuthorityId(), serviceInstanceId);

        return stopListEntityOptional
                   .filter(TppStopListEntity::isBlocked)
                   .map(sl -> {
                       log.info("TPP ID: [{}], Authority ID: [{}]. TPP has been blocked, because it's in stop list",
                                tppUniqueParams.getAuthorisationNumber(), tppUniqueParams.getAuthorityId());
                       return true;
                   })
                   .orElse(false);
    }
}
