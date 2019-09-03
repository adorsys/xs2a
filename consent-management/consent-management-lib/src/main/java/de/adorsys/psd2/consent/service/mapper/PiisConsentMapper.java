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

import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.piis.PiisConsentTppAccessType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;

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
                               accountReferenceMapper.mapToAccountReferenceEntity(piisConsentEntity.getAccount()),
                               piisConsentEntity.getTppAccessType(),
                               piisConsentEntity.getCreationTimestamp(),
                               piisConsentEntity.getInstanceId(),
                               piisConsentEntity.getCardNumber(),
                               piisConsentEntity.getCardExpiryDate(),
                               piisConsentEntity.getCardInformation(),
                               piisConsentEntity.getRegistrationInformation(),
                               piisConsentEntity.getStatusChangeTimestamp(),
                               piisConsentEntity.getTppAuthorisationNumber());
    }

    public List<PiisConsent> mapToPiisConsentList(List<PiisConsentEntity> consentEntities) {
        return consentEntities.stream()
                   .map(this::mapToPiisConsent)
                   .collect(Collectors.toList());
    }

    public PiisConsentEntity mapToPiisConsentEntity(PsuIdData psuIdData, CreatePiisConsentRequest request) {
        PiisConsentEntity consent = new PiisConsentEntity();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setConsentStatus(VALID);
        consent.setRequestDateTime(OffsetDateTime.now());
        consent.setExpireDate(request.getValidUntil());
        consent.setPsuData(psuDataMapper.mapToPsuData(psuIdData));
        consent.setTppInfo(tppInfoMapper.mapToTppInfoEntity(request.getTppInfo()));
        consent.setAccount(accountReferenceMapper.mapToAccountReferenceEntity(request.getAccount()));
        consent.setTppAccessType(getAccessType(request));
        consent.setCardNumber(request.getCardNumber());
        consent.setCardExpiryDate(request.getCardExpiryDate());
        consent.setCardInformation(request.getCardInformation());
        consent.setRegistrationInformation(request.getRegistrationInformation());
        consent.setTppAuthorisationNumber(request.getTppAuthorisationNumber());
        return consent;
    }

    @NotNull
    private PiisConsentTppAccessType getAccessType(CreatePiisConsentRequest request) {
        return request.getTppInfo() != null || StringUtils.isNotBlank(request.getTppAuthorisationNumber()) ?
                   PiisConsentTppAccessType.SINGLE_TPP :
                   PiisConsentTppAccessType.ALL_TPP;
    }
}
