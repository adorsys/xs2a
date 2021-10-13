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

import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class CmsConsentMapper {
    private final AuthorisationTemplateMapper authorisationTemplateMapper;
    private final ConsentTppInformationMapper consentTppInformationMapper;
    private final PsuDataMapper psuDataMapper;
    private final AuthorisationMapper authorisationMapper;
    private final AccessMapper accessMapper;

    public List<CmsConsent> mapToCmsConsents(List<ConsentEntity> entities, Map<String, List<AuthorisationEntity>> authorisation, Map<String, Map<String, Integer>> usages) {
        return entities.stream()
                   .map(entity -> mapToCmsConsent(entity, authorisation.get(entity.getExternalId()), usages.get(entity.getExternalId())))
                   .collect(Collectors.toList());
    }

    public CmsConsent mapToCmsConsent(ConsentEntity entity, List<AuthorisationEntity> authorisations, Map<String, Integer> usages) {
        CmsConsent cmsConsent = new CmsConsent();
        cmsConsent.setId(entity.getExternalId());
        cmsConsent.setConsentData(entity.getData());
        cmsConsent.setChecksum(entity.getChecksum());
        cmsConsent.setConsentStatus(entity.getConsentStatus());
        cmsConsent.setConsentType(ConsentType.getByValue(entity.getConsentType()));
        cmsConsent.setTppInformation(consentTppInformationMapper.mapToConsentTppInformation(entity.getTppInformation()));
        cmsConsent.setAuthorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplate(entity.getAuthorisationTemplate()));
        cmsConsent.setInternalRequestId(entity.getInternalRequestId());
        cmsConsent.setFrequencyPerDay(entity.getFrequencyPerDay());
        cmsConsent.setValidUntil(entity.getValidUntil());
        cmsConsent.setPsuIdDataList(psuDataMapper.mapToPsuIdDataList(entity.getPsuDataList()));
        cmsConsent.setRecurringIndicator(entity.isRecurringIndicator());
        cmsConsent.setMultilevelScaRequired(entity.isMultilevelScaRequired());
        cmsConsent.setExpireDate(entity.getExpireDate());
        cmsConsent.setLastActionDate(entity.getLastActionDate());
        cmsConsent.setAuthorisations(authorisationMapper.mapToAuthorisations(authorisations));
        cmsConsent.setUsages(usages);
        cmsConsent.setTppAccountAccesses(accessMapper.mapTppAccessesToAccountAccess(entity.getTppAccountAccesses(),
                                                                                    entity.getOwnerNameType(),
                                                                                    entity.getTrustedBeneficiariesType()));
        cmsConsent.setAspspAccountAccesses(accessMapper.mapAspspAccessesToAccountAccess(entity.getAspspAccountAccesses(),
                                                                                        entity.getOwnerNameType(),
                                                                                        entity.getTrustedBeneficiariesType()));
        cmsConsent.setInstanceId(entity.getInstanceId());
        cmsConsent.setSigningBasketBlocked(entity.isSigningBasketBlocked());
        cmsConsent.setSigningBasketAuthorised(entity.isSigningBasketAuthorised());
        return cmsConsent;
    }

    public ConsentEntity mapToNewConsentEntity(CmsConsent cmsConsent) {
        ConsentEntity entity = new ConsentEntity();
        entity.setData(cmsConsent.getConsentData());
        entity.setChecksum(cmsConsent.getChecksum());
        entity.setExternalId(UUID.randomUUID().toString());
        entity.setConsentStatus(cmsConsent.getConsentStatus());
        entity.setConsentType(cmsConsent.getConsentType().getName());
        entity.setFrequencyPerDay(cmsConsent.getFrequencyPerDay());
        entity.setMultilevelScaRequired(cmsConsent.isMultilevelScaRequired());
        entity.setRequestDateTime(OffsetDateTime.now());
        entity.setValidUntil(cmsConsent.getValidUntil());
        entity.setExpireDate(cmsConsent.getExpireDate());
        entity.setPsuDataList(psuDataMapper.mapToPsuDataList(cmsConsent.getPsuIdDataList(), cmsConsent.getInstanceId()));
        entity.getPsuDataList().forEach(p -> p.setInstanceId(cmsConsent.getInstanceId()));
        entity.setAuthorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplateEntity(cmsConsent.getAuthorisationTemplate()));
        entity.setRecurringIndicator(cmsConsent.isRecurringIndicator());
        entity.setLastActionDate(LocalDate.now());
        entity.setInternalRequestId(cmsConsent.getInternalRequestId());
        entity.setTppInformation(consentTppInformationMapper.mapToConsentTppInformationEntity(cmsConsent.getTppInformation()));
        AccountAccess tppAccountAccesses = cmsConsent.getTppAccountAccesses();
        entity.setTppAccountAccesses(accessMapper.mapToTppAccountAccess(entity, tppAccountAccesses));
        entity.setAspspAccountAccesses(accessMapper.mapToAspspAccountAccess(entity, cmsConsent.getAspspAccountAccesses()));
        entity.setInstanceId(cmsConsent.getInstanceId());

        AdditionalInformationAccess additionalInformationAccess = tppAccountAccesses.getAdditionalInformationAccess();
        if (additionalInformationAccess != null) {
            entity.setOwnerNameType(AdditionalAccountInformationType.findTypeByList(additionalInformationAccess.getOwnerName()));
            entity.setTrustedBeneficiariesType(AdditionalAccountInformationType.findTypeByList(additionalInformationAccess.getTrustedBeneficiaries()));
        }
        return entity;
    }
}
