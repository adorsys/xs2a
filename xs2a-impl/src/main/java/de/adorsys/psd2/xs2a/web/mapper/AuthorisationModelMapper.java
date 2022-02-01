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
