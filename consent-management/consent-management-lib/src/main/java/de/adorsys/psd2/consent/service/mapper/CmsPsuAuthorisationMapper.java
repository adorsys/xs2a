/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CmsPsuAuthorisationMapper {

    @Mapping(target = "psuId", source = "psuData.psuId")
    @Mapping(target = "authorisationId", source = "externalId")
    @Mapping(target = "authorisationType", source = "authorizationType")
    CmsPsuAuthorisation mapToCmsPsuAuthorisationPis(PisAuthorization pisAuthorization);

    @Mapping(target = "psuId", source = "psuData.psuId")
    @Mapping(target = "authorisationId", source = "externalId")
    @Mapping(target = "authorisationType", ignore = true)
    @Mapping(target = "tppOkRedirectUri", source = "consent.tppInfo.redirectUri")
    @Mapping(target = "tppNokRedirectUri", source = "consent.tppInfo.nokRedirectUri")
    CmsPsuAuthorisation mapToCmsPsuAuthorisationAis(AisConsentAuthorization consentAuthorization);

    @AfterMapping
    default void mapToCmsPsuAuthorisationPisAfterMapping(PisAuthorization pisAuthorization,
                                                         @MappingTarget CmsPsuAuthorisation cmsPsuAuthorisation) {
        TppInfoEntity tppInfo = pisAuthorization.getPaymentData().getTppInfo();

        boolean isPaymentCreated = pisAuthorization.getAuthorizationType() == PaymentAuthorisationType.CREATED;
        cmsPsuAuthorisation.setTppOkRedirectUri(isPaymentCreated ? tppInfo.getRedirectUri() : tppInfo.getCancelRedirectUri());
        cmsPsuAuthorisation.setTppNokRedirectUri(isPaymentCreated ? tppInfo.getNokRedirectUri() : tppInfo.getCancelNokRedirectUri());
    }
}
