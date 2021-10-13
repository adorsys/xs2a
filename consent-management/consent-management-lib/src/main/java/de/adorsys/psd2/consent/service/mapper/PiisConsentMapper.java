/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.ais.AdditionalTppInfo;
import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;

@Component
@RequiredArgsConstructor
public class PiisConsentMapper {
    private final PsuDataMapper psuDataMapper;
    private final ConsentDataMapper consentDataMapper;
    private final AccessMapper accessMapper;

    public List<CmsPiisConsent> mapToCmsPiisConsentList(List<ConsentEntity> consentEntities) {
        return consentEntities.stream()
                   .map(this::mapToCmsPiisConsent)
                   .collect(Collectors.toList());
    }

    public CmsPiisConsent mapToCmsPiisConsent(ConsentEntity consentEntity) {
        PiisConsentData piisConsentData = consentDataMapper.mapToPiisConsentData(consentEntity.getData());
        AccountReference accountReference = accessMapper.mapToAccountReference(consentEntity.getAspspAccountAccesses().get(0));
        return new CmsPiisConsent(consentEntity.getExternalId(),
                                  consentEntity.isRecurringIndicator(),
                                  consentEntity.getRequestDateTime(),
                                  consentEntity.getLastActionDate(),
                                  consentEntity.getValidUntil(),
                                  psuDataMapper.mapToPsuIdData(consentEntity.getPsuDataList().get(0)),
                                  consentEntity.getConsentStatus(),
                                  accountReference,
                                  consentEntity.getCreationTimestamp(),
                                  consentEntity.getInstanceId(),
                                  piisConsentData.getCardNumber(),
                                  piisConsentData.getCardExpiryDate(),
                                  piisConsentData.getCardInformation(),
                                  piisConsentData.getRegistrationInformation(),
                                  consentEntity.getStatusChangeTimestamp(),
                                  consentEntity.getTppInformation().getTppInfo().getAuthorisationNumber());
    }

    public ConsentEntity mapToPiisConsentEntity(PsuIdData psuIdData, TppInfoEntity tppInfoEntity, CreatePiisConsentRequest request,
                                                String instanceId) {
        ConsentEntity consent = new ConsentEntity();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setConsentStatus(VALID);
        consent.setRequestDateTime(OffsetDateTime.now());
        consent.setValidUntil(request.getValidUntil());
        consent.setLastActionDate(LocalDate.now());
        consent.getPsuDataList().add(psuDataMapper.mapToPsuData(psuIdData, instanceId));
        consent.getAspspAccountAccesses().add(accessMapper.mapToAspspAccountAccess(consent, request.getAccount()));
        consent.getTppInformation().setTppInfo(tppInfoEntity);
        consent.getTppInformation().setAdditionalInfo(AdditionalTppInfo.NONE);
        PiisConsentData consentData = new PiisConsentData(request.getCardNumber(), request.getCardExpiryDate(),
                                                          request.getCardInformation(), request.getRegistrationInformation());
        consent.setData(consentDataMapper.getBytesFromConsentData(consentData));
        consent.setConsentType(ConsentType.PIIS_ASPSP.toString());
        consent.setInstanceId(instanceId);
        return consent;
    }
}
