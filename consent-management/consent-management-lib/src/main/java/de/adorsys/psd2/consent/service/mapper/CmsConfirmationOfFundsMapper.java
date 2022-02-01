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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsAuthorisation;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentTppInformationEntity;
import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CmsConfirmationOfFundsMapper {
    private final PsuDataMapper psuDataMapper;
    private final TppInfoMapper tppInfoMapper;
    private final AuthorisationTemplateMapper authorisationTemplateMapper;
    private final ConsentDataMapper consentDataMapper;

    public CmsConfirmationOfFundsConsent mapToCmsConfirmationOfFundsConsent(ConsentEntity consent, List<AuthorisationEntity> authorisations) {
        ConsentTppInformationEntity tppInformation = consent.getTppInformation();
        PiisConsentData piisConsentData = consentDataMapper.mapToPiisConsentData(consent.getData());

        return new CmsConfirmationOfFundsConsent(
            consent.getExternalId(),
            getAccountReference(consent),
            consent.getValidUntil(),
            consent.getExpireDate(),
            consent.getLastActionDate(),
            consent.getConsentStatus(),
            tppInformation.isTppRedirectPreferred(),
            psuDataMapper.mapToPsuIdDataList(consent.getPsuDataList()),
            tppInfoMapper.mapToTppInfo(tppInformation.getTppInfo()),
            authorisationTemplateMapper.mapToAuthorisationTemplate(consent.getAuthorisationTemplate()),
            consent.isMultilevelScaRequired(),
            consent.getCreationTimestamp(),
            consent.getStatusChangeTimestamp(),
            mapToAuthorisations(authorisations),
            piisConsentData.getCardNumber(),
            piisConsentData.getCardExpiryDate(),
            piisConsentData.getCardInformation(),
            piisConsentData.getRegistrationInformation()
        );
    }

    private AccountReference getAccountReference(ConsentEntity consent) {
        if (consent != null && CollectionUtils.isNotEmpty(consent.getTppAccountAccesses())) {
            TppAccountAccess tppAccountAccess = consent.getTppAccountAccesses().get(0);
            return new AccountReference(tppAccountAccess.getAccountReferenceType(),
                                        tppAccountAccess.getAccountIdentifier(),
                                        tppAccountAccess.getCurrency());
        }
        return null;
    }

    private List<CmsConfirmationOfFundsAuthorisation> mapToAuthorisations(List<AuthorisationEntity> consentAuthorisations) {
        if (CollectionUtils.isEmpty(consentAuthorisations)) {
            return Collections.emptyList();
        }

        return consentAuthorisations.stream()
                   .map(auth -> new CmsConfirmationOfFundsAuthorisation(
                       auth.getExternalId(),
                       psuDataMapper.mapToPsuIdData(auth.getPsuData()),
                       auth.getScaStatus()
                   ))
                   .collect(Collectors.toList());
    }
}
