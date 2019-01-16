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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.api.ConsentApi;
import de.adorsys.psd2.model.ScaStatusResponse;
import de.adorsys.psd2.model.StartScaprocessResponse;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Service
@RequiredArgsConstructor
public class AuthorisationMapper {
    private final CoreObjectsMapper coreObjectsMapper;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final RedirectLinkBuilder redirectLinkBuilder;

    public StartScaprocessResponse mapToStartScaProcessResponse(
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse) {
        return Optional.ofNullable(createConsentAuthorizationResponse)
                   .map(csar -> {
                       boolean redirectApproachUsed = aspspProfileService.getScaApproach() == ScaApproach.REDIRECT;
                       String link = redirectApproachUsed
                                         ? redirectLinkBuilder.buildConsentScaRedirectLink(csar.getConsentId(), csar.getAuthorizationId())
                                         : linkTo(methodOn(ConsentApi.class)._updateConsentsPsuData(null, csar.getConsentId(), csar.getAuthorizationId(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null))
                                               .toString();

                       return new StartScaprocessResponse()
                                  .scaStatus(coreObjectsMapper.mapToModelScaStatus(createConsentAuthorizationResponse.getScaStatus()))
                                  ._links(Collections.singletonMap(csar.getResponseLinkType().getValue(), link));
                   })
                   .orElse(null);
    }

    public @NotNull ScaStatusResponse mapToScaStatusResponse(@NotNull ScaStatus scaStatus) {
        return new ScaStatusResponse().scaStatus(coreObjectsMapper.mapToModelScaStatus(scaStatus));
    }
}
