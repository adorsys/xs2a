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
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

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

    public Authorisations mapToAuthorisations(Xs2aAuthorisationSubResources xs2AAuthorisationSubResources) {
        Authorisations authorisations = new Authorisations();
        AuthorisationsList authorisationsList = new AuthorisationsList();
        authorisationsList.addAll(xs2AAuthorisationSubResources.getAuthorisationIds());
        authorisations.setAuthorisationIds(authorisationsList);
        return authorisations;
    }

    public Object mapToPisCreateOrUpdateAuthorisationResponse(ResponseObject responseObject) {
        Object body = responseObject.getBody();
        if (Objects.isNull(body)) {
            return null;
        }

        if (body instanceof Xs2aCreatePisAuthorisationResponse) {

            return mapToStartScaProcessResponseFromPis((Xs2aCreatePisAuthorisationResponse) body);
        } else if (body instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse) {

            return mapToPisUpdatePsuAuthenticationResponse((Xs2aUpdatePisCommonPaymentPsuDataResponse) body);
        } else {
            return null;
        }
    }

    public Object mapToAisCreateOrUpdateAuthorisationResponse(ResponseObject responseObject) {
        Object body = responseObject.getBody();
        if (Objects.isNull(body)) {
            return null;
        }

        if (body instanceof CreateConsentAuthorizationResponse) {

            return mapToStartScaProcessResponse((CreateConsentAuthorizationResponse) body);
        } else if (body instanceof UpdateConsentPsuDataResponse) {

            return mapToAisUpdatePsuAuthenticationResponse((UpdateConsentPsuDataResponse) body);
        } else {
            return null;
        }
    }

    public @NotNull ScaStatusResponse mapToScaStatusResponse(@NotNull ScaStatus scaStatus) {
        return new ScaStatusResponse().scaStatus(coreObjectsMapper.mapToModelScaStatus(scaStatus));
    }

    public UpdatePsuAuthenticationResponse mapToPisUpdatePsuAuthenticationResponse(Xs2aUpdatePisCommonPaymentPsuDataResponse response) {
        return Optional.ofNullable(response)
                   .map(r -> buildUpdatePsuAuthenticationResponse(r.getLinks(),
                                                                  r.getAvailableScaMethods(),
                                                                  r.getChosenScaMethod(),
                                                                  r.getPsuMessage(),
                                                                  r.getChallengeData(),
                                                                  r.getScaStatus()))
                   .orElse(null);
    }

    public UpdatePsuAuthenticationResponse mapToAisUpdatePsuAuthenticationResponse(UpdateConsentPsuDataResponse response) {
        return Optional.ofNullable(response)
                   .map(r -> buildUpdatePsuAuthenticationResponse(r.getLinks(),
                                                                  r.getAvailableScaMethods(),
                                                                  r.getChosenScaMethod(),
                                                                  r.getPsuMessage(),
                                                                  r.getChallengeData(),
                                                                  r.getScaStatus()))
                   .orElse(null);
    }

    public Xs2aCreatePisAuthorisationRequest mapToXs2aCreatePisAuthorisationRequest(PsuIdData psuData, String paymentId, String paymentService, String paymentProduct, Map body) {
        return new Xs2aCreatePisAuthorisationRequest(
            paymentId,
            psuData,
            paymentProduct,
            paymentService,
            mapToPasswordFromBody(body));
    }

    public String mapToPasswordFromBody(Map body) {
        return Optional.ofNullable(body)
                   .filter(bdy -> !bdy.isEmpty())
                   .map(bdy -> bdy.get("psuData"))
                   .map(o -> (LinkedHashMap<String, String>) o)
                   .map(psuDataMap -> psuDataMap.get("password"))
                   .orElse(null);
    }

    private StartScaprocessResponse mapToStartScaProcessResponseFromPis(Xs2aCreatePisAuthorisationResponse response) {
        return Optional.ofNullable(response)
                   .map(r -> new StartScaprocessResponse()
                                 .scaStatus(coreObjectsMapper.mapToModelScaStatus(r.getScaStatus()))
                                 .authorisationId(r.getAuthorisationId())
                                 ._links(hrefLinkMapper.mapToLinksMap(r.getLinks())))
                   .orElse(null);
    }

    private StartScaprocessResponse mapToStartScaProcessResponse(
        CreateConsentAuthorizationResponse createResponse) {
        return Optional.ofNullable(createResponse)
                   .map(csar -> {
                       boolean redirectApproachUsed = scaApproachResolver.resolveScaApproach() == REDIRECT;
                       String link = redirectApproachUsed
                                         ? redirectLinkBuilder.buildConsentScaRedirectLink(csar.getConsentId(), csar.getAuthorizationId())
                                         : createUpdateConsentsPsuDataLink(csar);
                       return new StartScaprocessResponse()
                                  .scaStatus(coreObjectsMapper.mapToModelScaStatus(csar.getScaStatus()))
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

    private UpdatePsuAuthenticationResponse buildUpdatePsuAuthenticationResponse(Links links, List<Xs2aAuthenticationObject> availableScaMethods,
                                                                                 Xs2aAuthenticationObject chosenScaMethod, String psuMessage,
                                                                                 de.adorsys.psd2.xs2a.core.sca.ChallengeData challengeData, ScaStatus scaStatus) {
        return new UpdatePsuAuthenticationResponse()
                   ._links(hrefLinkMapper.mapToLinksMap(links))
                   .scaMethods(getAvailableScaMethods(availableScaMethods))
                   .chosenScaMethod(mapToChosenScaMethod(chosenScaMethod))
                   .psuMessage(psuMessage)
                   .challengeData(coreObjectsMapper.mapToChallengeData(challengeData))
                   .scaStatus(
                       Optional.ofNullable(scaStatus)
                           .map(s -> de.adorsys.psd2.model.ScaStatus.valueOf(s.name()))
                           .orElse(null)
                   );
    }

    private ScaMethods getAvailableScaMethods(List<Xs2aAuthenticationObject> availableScaMethods) {
        ScaMethods scaMethods = new ScaMethods();
        if (CollectionUtils.isNotEmpty(availableScaMethods)) {
            availableScaMethods.forEach(a -> scaMethods.add(new AuthenticationObject()
                                                                .authenticationMethodId(a.getAuthenticationMethodId())
                                                                .authenticationType(AuthenticationType.fromValue(a.getAuthenticationType()))
                                                                .authenticationVersion(a.getAuthenticationVersion())
                                                                .name(a.getName())
                                                                .explanation(a.getExplanation())));
        }
        return scaMethods;
    }

    private ChosenScaMethod mapToChosenScaMethod(Xs2aAuthenticationObject xs2aAuthenticationObject) {
        return Optional.ofNullable(xs2aAuthenticationObject)
                   .map(ch -> {
                       ChosenScaMethod method = new ChosenScaMethod();
                       method.setAuthenticationMethodId(ch.getAuthenticationMethodId());
                       method.setAuthenticationType(AuthenticationType.fromValue(ch.getAuthenticationType()));
                       method.setAuthenticationVersion(ch.getAuthenticationVersion());
                       method.setName(ch.getName());
                       method.setExplanation(ch.getExplanation());
                       return method;
                   }).orElse(null);
    }
}
