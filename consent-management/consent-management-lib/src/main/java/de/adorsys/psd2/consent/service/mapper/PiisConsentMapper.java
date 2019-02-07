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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PiisConsentMapper {
    private final PsuDataMapper psuDataMapper;
    private final TppInfoMapper tppInfoMapper;
    private final AccountReferenceMapper accountReferenceMapper;

    public PiisConsent mapToPiisConsent(PiisConsentEntity piisConsentEntity) {
        return new PiisConsent(piisConsentEntity.getExternalId(),
                               piisConsentEntity.isRecurringIndicator(),
                               piisConsentEntity.getRequestDateTime(),
                               piisConsentEntity.getLastActionDate(),
                               piisConsentEntity.getExpireDate(),
                               psuDataMapper.mapToPsuIdData(piisConsentEntity.getPsuData()),
                               tppInfoMapper.mapToTppInfo(piisConsentEntity.getTppInfo()),
                               piisConsentEntity.getConsentStatus(),
                               accountReferenceMapper.mapToAccountReferenceList(piisConsentEntity.getAccounts()),
                               piisConsentEntity.getTppAccessType(),
                               piisConsentEntity.getAllowedFrequencyPerDay(),
                               piisConsentEntity.getCreationTimestamp(),
                               piisConsentEntity.getInstanceId());
    }

    public List<PiisConsent> mapToPiisConsentList(List<PiisConsentEntity> consentEntities) {
        return consentEntities.stream()
                   .map(this::mapToPiisConsent)
                   .collect(Collectors.toList());
    }
}
