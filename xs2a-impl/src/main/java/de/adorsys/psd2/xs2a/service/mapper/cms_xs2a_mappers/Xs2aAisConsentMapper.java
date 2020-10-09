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
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Xs2aAisConsentMapper {
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final Xs2aToSpiAccountAccessMapper xs2aToSpiAccountAccessMapper;
    private final ConsentDataMapper consentDataMapper;
    private final RequestProviderService requestProviderService;
    private final Xs2aAccountConsentAuthorizationMapper xs2aAccountConsentAuthorizationMapper;

    public SpiAccountConsent mapToSpiAccountConsent(AisConsent aisConsent) {
        return Optional.ofNullable(aisConsent)
                   .map(ac -> new SpiAccountConsent(
                            ac.getId(),
                            xs2aToSpiAccountAccessMapper.mapToAccountAccess(ac),
                            ac.isRecurringIndicator(),
                            ac.getValidUntil(),
                            ac.getExpireDate(),
                            ac.getFrequencyPerDay(),
                            ac.getLastActionDate(),
                            ac.getConsentStatus(),
                            ac.isWithBalance(),
                            ac.getConsentTppInformation().isTppRedirectPreferred(),
                            xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(ac.getPsuIdDataList()),
                            ac.getTppInfo(),
                            ac.getAisConsentRequestType(),
                            ac.getStatusChangeTimestamp(),
                            ac.getCreationTimestamp(),
                            ac.getInstanceId(),
                            ac.getConsentType()
                        )
                   )
                   .orElse(null);
    }

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

    public SpiScaConfirmation mapToSpiScaConfirmation(UpdateAuthorisationRequest request, PsuIdData psuData) {
        SpiScaConfirmation accountConfirmation = new SpiScaConfirmation();
        accountConfirmation.setConsentId(request.getBusinessObjectId());
        accountConfirmation.setPsuId(Optional.ofNullable(psuData).map(PsuIdData::getPsuId).orElse(null));
        accountConfirmation.setTanNumber(request.getScaAuthenticationData());
        return accountConfirmation;
    }

    public CmsConsent mapToCmsConsent(CreateConsentReq request, PsuIdData psuData, TppInfo tppInfo, int allowedFrequencyPerDay) {
        CmsConsent cmsConsent = new CmsConsent();

        AisConsentData aisConsentData = new AisConsentData(request.getAvailableAccounts(), request.getAllPsd2(), request.getAvailableAccountsWithBalance(), request.isCombinedServiceIndicator());
        byte[] aisConsentDataBytes = consentDataMapper.getBytesFromConsentData(aisConsentData);
        cmsConsent.setConsentData(aisConsentDataBytes);

        ConsentTppInformation tppInformation = new ConsentTppInformation();
        tppInformation.setTppInfo(tppInfo);
        tppInformation.setTppFrequencyPerDay(request.getFrequencyPerDay());
        tppInformation.setTppNotificationUri(Optional.ofNullable(request.getTppNotificationData())
                                                 .map(TppNotificationData::getTppNotificationUri)
                                                 .orElse(null));
        tppInformation.setTppNotificationSupportedModes(Optional.ofNullable(request.getTppNotificationData())
                                                            .map(TppNotificationData::getNotificationModes)
                                                            .orElse(Collections.emptyList()));
        tppInformation.setTppRedirectPreferred(requestProviderService.resolveTppRedirectPreferred().orElse(false));
        tppInformation.setTppBrandLoggingInformation(request.getTppBrandLoggingInformation());
        cmsConsent.setTppInformation(tppInformation);

        AuthorisationTemplate authorisationTemplate = new AuthorisationTemplate();
        authorisationTemplate.setTppRedirectUri(request.getTppRedirectUri());
        cmsConsent.setAuthorisationTemplate(authorisationTemplate);

        cmsConsent.setFrequencyPerDay(allowedFrequencyPerDay);
        cmsConsent.setInternalRequestId(requestProviderService.getInternalRequestIdString());
        cmsConsent.setValidUntil(request.getValidUntil());
        cmsConsent.setRecurringIndicator(request.isRecurringIndicator());
        cmsConsent.setPsuIdDataList(Collections.singletonList(psuData));
        cmsConsent.setConsentType(ConsentType.AIS);
        cmsConsent.setTppAccountAccesses(request.getAccess());
        cmsConsent.setAspspAccountAccesses(AccountAccess.EMPTY_ACCESS);
        cmsConsent.setConsentStatus(ConsentStatus.RECEIVED);
        cmsConsent.setInstanceId(request.getInstanceId());
        return cmsConsent;
    }

    public AisConsent mapToAisConsent(CmsConsent ais) {
        return Optional.ofNullable(ais)
                   .map(ac -> {
                       AisConsent aisConsent = new AisConsent();
                       aisConsent.setId(ac.getId());
                       aisConsent.setConsentData(consentDataMapper.mapToAisConsentData(ac.getConsentData()));
                       aisConsent.setRecurringIndicator(ac.isRecurringIndicator());
                       aisConsent.setValidUntil(ac.getValidUntil());
                       aisConsent.setExpireDate(ac.getExpireDate());
                       aisConsent.setFrequencyPerDay(ac.getFrequencyPerDay());
                       aisConsent.setLastActionDate(ac.getLastActionDate());
                       aisConsent.setConsentStatus(ac.getConsentStatus());
                       aisConsent.setAuthorisationTemplate(ac.getAuthorisationTemplate());
                       aisConsent.setPsuIdDataList(ac.getPsuIdDataList());
                       aisConsent.setConsentTppInformation(ac.getTppInformation());
                       aisConsent.setMultilevelScaRequired(ac.isMultilevelScaRequired());
                       aisConsent.setAuthorisations(xs2aAccountConsentAuthorizationMapper.mapToAccountConsentAuthorisation(ac.getAuthorisations()));
                       aisConsent.setStatusChangeTimestamp(ac.getStatusChangeTimestamp());
                       aisConsent.setUsages(ac.getUsages());
                       aisConsent.setCreationTimestamp(ac.getCreationTimestamp());
                       aisConsent.setTppAccountAccesses(ais.getTppAccountAccesses());
                       aisConsent.setAspspAccountAccesses(ais.getAspspAccountAccesses());
                       aisConsent.setInstanceId(ac.getInstanceId());
                       aisConsent.setSigningBasketBlocked(ac.isSigningBasketBlocked());
                       aisConsent.setSigningBasketAuthorised(ac.isSigningBasketAuthorised());
                       return aisConsent;
                   })
                   .orElse(null);
    }
}
