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

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorisationMapper {
    private final CoreObjectsMapper coreObjectsMapper;
    private final HrefLinkMapper hrefLinkMapper;
    private final ScaMethodsMapper scaMethodsMapper;
    private final AuthorisationModelMapper authorisationModelMapper;

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
            return authorisationModelMapper.mapToStartScaProcessResponse((Xs2aCreatePisAuthorisationResponse) body);
        } else if (body instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse) {

            Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisCommonPaymentPsuDataResponse = (Xs2aUpdatePisCommonPaymentPsuDataResponse) body;
            UpdatePsuAuthenticationResponse resp = mapToPisUpdatePsuAuthenticationResponse(updatePisCommonPaymentPsuDataResponse);
            resp.setAuthorisationId(updatePisCommonPaymentPsuDataResponse.getAuthorisationId());
            return resp;
        } else {
            return null;
        }
    }

    public Object mapToAisCreateOrUpdateAuthorisationResponse(ResponseObject<AuthorisationResponse> responseObject) {
        AuthorisationResponse body = responseObject.getBody();
        if (Objects.isNull(body)) {
            return null;
        }

        if (body instanceof CreateConsentAuthorizationResponse) {
            return authorisationModelMapper.mapToStartScaProcessResponse((CreateConsentAuthorizationResponse) body);
        } else if (body instanceof UpdateConsentPsuDataResponse) {

            UpdatePsuAuthenticationResponse resp = mapToAisUpdatePsuAuthenticationResponse((UpdateConsentPsuDataResponse) body);
            resp.setAuthorisationId(body.getAuthorisationId());
            return resp;

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

    public Xs2aCreatePisAuthorisationRequest mapToXs2aCreatePisAuthorisationRequest(PsuIdData psuData, String paymentId, PaymentType paymentService, String paymentProduct, Map body) {
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

    private UpdatePsuAuthenticationResponse buildUpdatePsuAuthenticationResponse(Links links, List<Xs2aAuthenticationObject> availableScaMethods,
                                                                                 Xs2aAuthenticationObject chosenScaMethod, String psuMessage,
                                                                                 de.adorsys.psd2.xs2a.core.sca.ChallengeData challengeData, ScaStatus scaStatus) {
        return new UpdatePsuAuthenticationResponse()
                   ._links(hrefLinkMapper.mapToLinksMap(links))
                   .scaMethods(scaMethodsMapper.mapToScaMethods(availableScaMethods))
                   .chosenScaMethod(mapToChosenScaMethod(chosenScaMethod))
                   .psuMessage(psuMessage)
                   .challengeData(coreObjectsMapper.mapToChallengeData(challengeData))
                   .scaStatus(
                       Optional.ofNullable(scaStatus)
                           .map(s -> de.adorsys.psd2.model.ScaStatus.valueOf(s.name()))
                           .orElse(null)
                   );
    }

    private ChosenScaMethod mapToChosenScaMethod(Xs2aAuthenticationObject xs2aAuthenticationObject) {
        return Optional.ofNullable(xs2aAuthenticationObject)
                   .map(ch -> {
                       ChosenScaMethod method = new ChosenScaMethod();
                       method.setAuthenticationMethodId(ch.getAuthenticationMethodId());
                       method.setAuthenticationType(ch.getAuthenticationType());
                       method.setAuthenticationVersion(ch.getAuthenticationVersion());
                       method.setName(ch.getName());
                       method.setExplanation(ch.getExplanation());
                       return method;
                   }).orElse(null);
    }
}
