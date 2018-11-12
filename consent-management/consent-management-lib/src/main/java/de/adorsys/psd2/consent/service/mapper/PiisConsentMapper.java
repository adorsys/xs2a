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

import de.adorsys.psd2.consent.api.piis.CmsPiisValidationInfo;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.piis.PiisConsent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PiisConsentMapper {

    public List<CmsPiisValidationInfo> mapToListCmsPiisValidationInfo(List<PiisConsent> consents) {
        return consents.stream()
                   .map(this::mapToCmsPiisValidationInfo)
                   .collect(Collectors.toList());
    }

    private CmsPiisValidationInfo mapToCmsPiisValidationInfo(PiisConsent piisConsent) {
        CmsPiisValidationInfo info = new CmsPiisValidationInfo();
        info.setConsentId(piisConsent.getExternalId());
        info.setExpireDate(piisConsent.getExpireDate());
        info.setConsentStatus(piisConsent.getConsentStatus());
        info.setPiisConsentTppAccessType(piisConsent.getTppAccessType());
        info.setTppInfoId(Optional.ofNullable(piisConsent.getTppInfo()).map(TppInfoEntity::getAuthorisationNumber).orElse(null));
        info.setFrequencyPerDay(piisConsent.getAllowedFrequencyPerDay());
        return info;
    }
}
