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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

@Mapper(componentModel = "spring", uses = {ConsentDataMapper.class, Xs2aAccountConsentAuthorizationMapper.class})
public abstract class Xs2aPiisConsentMapper {
    private static final Integer PIIS_FREQUENCY_PER_DAY = 0;

    @Autowired
    protected ConsentDataMapper consentDataMapper;
    @Autowired
    protected RequestProviderService requestProviderService;

    @Mapping(target = "consentTppInformation", source = "tppInformation")
    public abstract PiisConsent mapToPiisConsent(CmsConsent cmsConsent);

    public CmsConsent mapToCmsConsent(CreatePiisConsentRequest request, PsuIdData psuData, TppInfo tppInfo) {
        PiisConsentData piisConsentData = new PiisConsentData(request.getCardNumber(), request.getCardExpiryDate(), request.getCardInformation(), request.getRegistrationInformation());
        byte[] consentDataInBytes = consentDataMapper.getBytesFromConsentData(piisConsentData);

        CmsConsent cmsConsent = new CmsConsent();
        cmsConsent.setConsentData(consentDataInBytes);

        ConsentTppInformation tppInformation = new ConsentTppInformation();
        tppInformation.setTppInfo(tppInfo);
        tppInformation.setTppRedirectPreferred(requestProviderService.resolveTppRedirectPreferred().orElse(false));
        cmsConsent.setTppInformation(tppInformation);

        AuthorisationTemplate authorisationTemplate = new AuthorisationTemplate();
        String tppRedirectURI = requestProviderService.getTppRedirectURI();
        if (tppRedirectURI != null) {
            TppRedirectUri tppRedirectUri = new TppRedirectUri(tppRedirectURI, requestProviderService.getTppNokRedirectURI());
            authorisationTemplate.setTppRedirectUri(tppRedirectUri);
        }
        cmsConsent.setAuthorisationTemplate(authorisationTemplate);

        cmsConsent.setFrequencyPerDay(PIIS_FREQUENCY_PER_DAY);
        cmsConsent.setInternalRequestId(requestProviderService.getInternalRequestIdString());
        cmsConsent.setPsuIdDataList(Collections.singletonList(psuData));
        cmsConsent.setConsentType(ConsentType.PIIS_TPP);

        AccountAccess accountAccess = new AccountAccess(Collections.singletonList(request.getAccount()),
                                                        Collections.emptyList(),
                                                        Collections.emptyList(),
                                                        null);
        cmsConsent.setTppAccountAccesses(accountAccess);
        cmsConsent.setAspspAccountAccesses(AccountAccess.EMPTY_ACCESS);
        cmsConsent.setConsentStatus(ConsentStatus.RECEIVED);
        cmsConsent.setInstanceId(requestProviderService.getInstanceId());
        return cmsConsent;
    }
}
