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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.StartScaprocessResponse;
import de.adorsys.psd2.model.UpdatePsuAuthenticationResponse;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Slf4j
@Mapper(componentModel = "spring", uses = {CoreObjectsMapper.class, ChosenScaMethodMapper.class, ScaMethodsMapper.class})
public abstract class AuthorisationModelMapper {
    @Autowired
    protected HrefLinkMapper hrefLinkMapper;
    @Autowired
    protected ScaMethodsMapper scaMethodsMapper;
    @Autowired
    protected TppMessageGenericMapper tppMessageGenericMapper;

    public Object mapToStartOrUpdateCancellationResponse(CancellationAuthorisationResponse cancellationAuthorisationResponse) {
        if (Objects.isNull(cancellationAuthorisationResponse)) {
            return null;
        }

        AuthorisationResponseType authorisationResponseType = cancellationAuthorisationResponse.getAuthorisationResponseType();

        if (authorisationResponseType == AuthorisationResponseType.START) {
            return mapToStartScaProcessResponseCancellation((Xs2aCreatePisCancellationAuthorisationResponse) cancellationAuthorisationResponse);
        } else if (authorisationResponseType == AuthorisationResponseType.UPDATE) {
            return mapToUpdatePsuAuthenticationResponse((Xs2aUpdatePisCommonPaymentPsuDataResponse) cancellationAuthorisationResponse);
        } else {
            throw new IllegalArgumentException("Unknown authorisation response type: " + authorisationResponseType);
        }
    }

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "challengeData", ignore = true)
    @Mapping(target = "chosenScaMethod", ignore = true)
    @Mapping(target = "scaMethods", ignore = true)
    @Mapping(target = "tppMessages", expression = "java(tppMessageGenericMapper.mapToTppMessageGenericList(xs2aResponse.getTppMessageInformation()))")
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(xs2aResponse.getLinks()))")
    public abstract StartScaprocessResponse mapToStartScaProcessResponse(Xs2aCreatePisAuthorisationResponse xs2aResponse);

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "challengeData", ignore = true)
    @Mapping(target = "chosenScaMethod", ignore = true)
    @Mapping(target = "scaMethods", ignore = true)
    @Mapping(target = "tppMessages", expression = "java(tppMessageGenericMapper.mapToTppMessageGenericList(xs2aResponse.getTppMessageInformation()))")
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(xs2aResponse.getLinks()))")
    public abstract StartScaprocessResponse mapToStartScaProcessResponse(CreateConsentAuthorizationResponse xs2aResponse);

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "challengeData", ignore = true)
    @Mapping(target = "chosenScaMethod", ignore = true)
    @Mapping(target = "scaMethods", ignore = true)
    @Mapping(target = "tppMessages", expression = "java(tppMessageGenericMapper.mapToTppMessageGenericList(xs2aResponse.getTppMessageInformation()))")
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(xs2aResponse.getLinks()))")
    public abstract StartScaprocessResponse mapToStartScaProcessResponseCancellation(Xs2aCreatePisCancellationAuthorisationResponse xs2aResponse);

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(xs2aResponse.getLinks()))")
    @Mapping(target = "scaMethods", source = "availableScaMethods")
    public abstract UpdatePsuAuthenticationResponse mapToUpdatePsuAuthenticationResponse(Xs2aUpdatePisCommonPaymentPsuDataResponse xs2aResponse);
}
