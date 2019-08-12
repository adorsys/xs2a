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

import de.adorsys.psd2.model.StartCancellationScaProcessResponse;
import de.adorsys.psd2.model.StartScaprocessResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = CoreObjectsMapper.class)
public abstract class AuthorisationModelMapper {
    @Autowired
    protected HrefLinkMapper hrefLinkMapper;

    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(xs2aResponse.getLinks()))")
    public abstract StartCancellationScaProcessResponse mapToStartCancellationScaProcessResponse(Xs2aCreatePisCancellationAuthorisationResponse xs2aResponse);

    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(xs2aResponse.getLinks()))")
    public abstract StartScaprocessResponse mapToStartScaProcessResponse(Xs2aCreatePisAuthorisationResponse xs2aResponse);

    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(xs2aResponse.getLinks()))")
    public abstract StartScaprocessResponse mapToStartScaProcessResponse(CreateConsentAuthorizationResponse xs2aResponse);
}
