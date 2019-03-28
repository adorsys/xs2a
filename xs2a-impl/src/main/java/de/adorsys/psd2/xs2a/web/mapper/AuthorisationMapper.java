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
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.model.ScaStatusResponse;
import de.adorsys.psd2.model.StartScaprocessResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.REDIRECT;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorisationMapper {
    private final CoreObjectsMapper coreObjectsMapper;
    private final ScaApproachResolver scaApproachResolver;
    private final RedirectLinkBuilder redirectLinkBuilder;
    private final AspspProfileService aspspProfileService;
    private final HrefLinkMapper hrefLinkMapper;

    public StartScaprocessResponse mapToStartScaProcessResponse(
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse) {
        return Optional.ofNullable(createConsentAuthorizationResponse)
                   .map(csar -> {
                       boolean redirectApproachUsed = scaApproachResolver.resolveScaApproach() == REDIRECT;
                       String link = redirectApproachUsed
                                         ? redirectLinkBuilder.buildConsentScaRedirectLink(csar.getConsentId(), csar.getAuthorizationId())
                                         : createUpdateConsentsPsuDataLink(csar);
                       return new StartScaprocessResponse()
                                  .scaStatus(coreObjectsMapper.mapToModelScaStatus(createConsentAuthorizationResponse.getScaStatus()))
                                  ._links(hrefLinkMapper.mapToLinksMap(csar.getResponseLinkType().getValue(), link));
                   })
                   .orElse(null);
    }

    private String createUpdateConsentsPsuDataLink(CreateConsentAuthorizationResponse csar) {
        URI uri = linkTo(methodOn(ConsentApi.class)._updateConsentsPsuData(null, csar.getConsentId(), csar.getAuthorizationId(), null, null, null, null, null, null, null, null, null,
                                                                           null, null, null, null, null, null, null, null, null)).toUri();

        UriComponentsBuilder uriComponentsBuilder = aspspProfileService.getAspspSettings().isForceXs2aBaseUrl()
                                                        ? UriComponentsBuilder.fromHttpUrl(aspspProfileService.getAspspSettings().getXs2aBaseUrl()).path(uri.getPath())
                                                        : UriComponentsBuilder.fromUri(uri);
        return uriComponentsBuilder.toUriString();
    }

    public @NotNull ScaStatusResponse mapToScaStatusResponse(@NotNull ScaStatus scaStatus) {
        return new ScaStatusResponse().scaStatus(coreObjectsMapper.mapToModelScaStatus(scaStatus));
    }
}
