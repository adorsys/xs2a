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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = {ConsentDataMapper.class, Xs2aAccountConsentAuthorizationMapper.class})
public abstract class Xs2aPiisConsentMapper {
    private static final Integer PIIS_FREQUENCY_PER_DAY = 0;

    @Autowired
    protected ConsentDataMapper consentDataMapper;
    @Autowired
    protected RequestProviderService requestProviderService;

    @Mapping(target = "consentTppInformation", source = "tppInformation")
    public abstract PiisConsent mapToPiisConsent(CmsConsent cmsConsent);

    public UpdateConsentPsuDataReq mapToUpdateConsentPsuDataReq(UpdateAuthorisationRequest request,
                                                                AuthorisationProcessorResponse response) {
        return Optional.ofNullable(response)
                   .map(data -> {
                       UpdateConsentPsuDataReq req = new UpdateConsentPsuDataReq();
                       req.setPsuData(response.getPsuData());
                       req.setConsentId(request.getBusinessObjectId());
                       req.setAuthorizationId(request.getAuthorisationId());
                       req.setAuthenticationMethodId(Optional.ofNullable(data.getChosenScaMethod())
                                                         .map(AuthenticationObject::getAuthenticationMethodId)
                                                         .orElse(null));
                       req.setScaAuthenticationData(request.getScaAuthenticationData());
                       req.setScaStatus(data.getScaStatus());
                       req.setAuthorisationType(AuthorisationType.CONSENT);
                       return req;
                   })
                   .orElse(null);
    }

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
                                                        new AdditionalInformationAccess(Collections.emptyList(),
                                                                                        Collections.emptyList()));
        cmsConsent.setTppAccountAccesses(accountAccess);
        cmsConsent.setAspspAccountAccesses(AccountAccess.EMPTY_ACCESS);
        cmsConsent.setConsentStatus(ConsentStatus.RECEIVED);
        cmsConsent.setInstanceId(requestProviderService.getInstanceId());
        return cmsConsent;
    }
}
