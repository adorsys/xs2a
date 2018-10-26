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

package de.adorsys.aspsp.xs2a.service.mapper.consent;

import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.Xs2aTppRole;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.psd2.consent.api.CmsTppInfo;
import de.adorsys.psd2.consent.api.CmsTppRole;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class Xs2aPisConsentMapper {

    public Optional<Xsa2CreatePisConsentAuthorisationResponse> mapToXsa2CreatePisConsentAuthorizationResponse(CreatePisConsentAuthorisationResponse response, PaymentType paymentType) {
        return Optional.of(new Xsa2CreatePisConsentAuthorisationResponse(response.getAuthorizationId(), ScaStatus.RECEIVED, paymentType));
    }

    public Optional<Xs2aCreatePisConsentCancellationAuthorisationResponse> mapToXs2aCreatePisConsentCancellationAuthorisationResponse(CreatePisConsentAuthorisationResponse response, PaymentType paymentType) {
        return Optional.of(new Xs2aCreatePisConsentCancellationAuthorisationResponse(response.getAuthorizationId(), ScaStatus.RECEIVED, paymentType));
    }

    public Optional<Xs2aPaymentCancellationAuthorisationSubResource> mapToXs2aPaymentCancellationAuthorisationSubResource(String authorisationId) {
        return Optional.of(new Xs2aPaymentCancellationAuthorisationSubResource(Collections.singletonList(authorisationId)));
    }

    public Optional<Xs2aUpdatePisConsentPsuDataResponse> mapToXs2aUpdatePisConsentPsuDataResponse(UpdatePisConsentPsuDataResponse response) {
        return Optional.ofNullable(response)
                   .map(r -> new Xs2aUpdatePisConsentPsuDataResponse(getScaStatus(r), r.getAvailableScaMethods()));
    }

    public Xs2aPisConsent mapToXs2aPisConsent(CreatePisConsentResponse response, PsuIdData psuData) {
        return new Xs2aPisConsent(response.getConsentId(), psuData);
    }

    public CmsTppInfo mapToCmsTppInfo(TppInfo pisTppInfo) {
        return Optional.ofNullable(pisTppInfo)
                   .map(tpp -> {
                       CmsTppInfo tppInfo = new CmsTppInfo();

                       tppInfo.setAuthorisationNumber(tpp.getAuthorisationNumber());
                       tppInfo.setTppName(tpp.getTppName());
                       tppInfo.setTppRoles(mapToCmsTppRoles(tpp.getTppRoles()));
                       tppInfo.setAuthorityId(tpp.getAuthorityId());
                       tppInfo.setAuthorityName(tpp.getAuthorityName());
                       tppInfo.setCountry(tpp.getCountry());
                       tppInfo.setOrganisation(tpp.getOrganisation());
                       tppInfo.setOrganisationUnit(tpp.getOrganisationUnit());
                       tppInfo.setCity(tpp.getCity());
                       tppInfo.setState(tpp.getState());
                       tppInfo.setRedirectUri(tpp.getRedirectUri());
                       tppInfo.setNokRedirectUri(tpp.getNokRedirectUri());

                       return tppInfo;
                   }).orElse(null);
    }


    private List<CmsTppRole> mapToCmsTppRoles(List<Xs2aTppRole> tppRoles) {
        return Optional.ofNullable(tppRoles)
                   .map(roles -> roles.stream()
                                     .map(role -> CmsTppRole.valueOf(role.name()))
                                     .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private String getScaStatus(UpdatePisConsentPsuDataResponse response) {
        return Optional.ofNullable(response.getScaStatus())
                   .map(Enum::name)
                   .orElse(null);
    }
}
