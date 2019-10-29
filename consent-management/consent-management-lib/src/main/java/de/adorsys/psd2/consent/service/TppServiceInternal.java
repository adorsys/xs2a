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

import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TppServiceInternal implements TppService {
    private final TppInfoRepository tppInfoRepository;

    @Value("${cms.service.instance-id:UNDEFINED}")
    private String serviceInstanceId;

    @Override
    @Transactional
    public boolean updateTppInfo(@NotNull TppInfo tppInfo) {
        Optional<TppInfoEntity> tppInfoEntityOptional = tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(tppInfo.getAuthorisationNumber(), serviceInstanceId);
        if (!tppInfoEntityOptional.isPresent()) {
            return false;
        }

        TppInfoEntity tppInfoEntity = tppInfoEntityOptional.get();
        tppInfoEntity.setTppRoles(tppInfo.getTppRoles());
        tppInfoRepository.save(tppInfoEntity);

        return true;
    }
}
