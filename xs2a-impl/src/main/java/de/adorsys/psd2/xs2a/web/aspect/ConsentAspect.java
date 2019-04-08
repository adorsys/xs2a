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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.ConsentController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Optional;

@Slf4j
@Aspect
@Component
public class ConsentAspect extends AbstractLinkAspect<ConsentController> {
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final RedirectLinkBuilder redirectLinkBuilder;

    public ConsentAspect(ScaApproachResolver scaApproachResolver, MessageService messageService, AuthorisationMethodDecider authorisationMethodDecider, RedirectLinkBuilder redirectLinkBuilder, AspspProfileService aspspProfileService) {
        super(scaApproachResolver, messageService, aspspProfileService);
        this.authorisationMethodDecider = authorisationMethodDecider;
        this.redirectLinkBuilder = redirectLinkBuilder;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ConsentService.createAccountConsentsWithResponse(..)) && args( request, psuData, explicitPreferred, tppRedirectUri)", returning = "result", argNames = "result,request,psuData,explicitPreferred,tppRedirectUri")
    public ResponseObject<CreateConsentResponse> invokeCreateAccountConsentAspect(ResponseObject<CreateConsentResponse> result, CreateConsentReq request, PsuIdData psuData, boolean explicitPreferred, TppRedirectUri tppRedirectUri) {
        if (!result.hasError()) {

            CreateConsentResponse body = result.getBody();
            body.setLinks(buildLinksForConsentResponse(body, explicitPreferred, psuData));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ConsentService.updateConsentPsuData(..)) && args(updatePsuData)", returning = "result", argNames = "result,updatePsuData")
    public ResponseObject<UpdateConsentPsuDataResponse> invokeUpdateConsentPsuDataAspect(ResponseObject<UpdateConsentPsuDataResponse> result, UpdateConsentPsuDataReq updatePsuData) {
        if (!result.hasError()) {
            UpdateConsentPsuDataResponse body = result.getBody();
            body.setLinks(buildLinksForUpdateConsentResponse(body, updatePsuData));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildLinksForConsentResponse(CreateConsentResponse response, boolean explicitPreferred, PsuIdData psuData) {
        String consentId = response.getConsentId();

        Links links = new Links();
        links.setSelf(buildPath("/v1/consents/{consentId}", consentId));
        links.setStatus(buildPath("/v1/consents/{consentId}/status", consentId));

        if (EnumSet.of(ScaApproach.EMBEDDED, ScaApproach.DECOUPLED).contains(scaApproachResolver.resolveScaApproach())) {
            buildLinkForEmbeddedAndDecoupledScaApproach(response, links, explicitPreferred, psuData);
        } else if (ScaApproach.REDIRECT == scaApproachResolver.resolveScaApproach()) {
            if (authorisationMethodDecider.isExplicitMethod(explicitPreferred, response.isMultilevelScaRequired())) {
                links.setStartAuthorisation(buildPath("/v1/consents/{consentId}/authorisations", consentId));
            } else {
                links.setScaRedirect(redirectLinkBuilder.buildConsentScaRedirectLink(consentId, response.getAuthorizationId()));
                links.setScaStatus(buildPath("/v1/consents/{consentId}/authorisations/{authorisation-id}", consentId, response.getAuthorizationId()));
            }
        }

        return links;
    }

    private void buildLinkForEmbeddedAndDecoupledScaApproach(CreateConsentResponse response, Links links, boolean explicitPreferred, PsuIdData psuData) {
        String consentId = response.getConsentId();

        if (authorisationMethodDecider.isExplicitMethod(explicitPreferred, response.isMultilevelScaRequired())) {
            links.setStartAuthorisation(buildPath("/v1/consents/{consentId}/authorisations", consentId));
        } else {
            String path = buildPath("/v1/consents/{consentId}/authorisations/{authorisation-id}", consentId, response.getAuthorizationId());
            if (psuData.isEmpty()) {
                links.setStartAuthorisationWithPsuIdentification(path);
            } else {
                links.setStartAuthorisationWithPsuAuthentication(path);
            }
            links.setScaStatus(buildPath("/v1/consents/{consentId}/authorisations/{authorisation-id}", consentId, response.getAuthorizationId()));
        }
    }

    private Links buildLinksForUpdateConsentResponse(UpdateConsentPsuDataResponse response, UpdateConsentPsuDataReq request) {
        return Optional.ofNullable(response.getScaStatus())
                   .map(status -> {
                       Links links = null;

                       if (status == ScaStatus.PSUAUTHENTICATED) {
                           links = buildLinksForPsuAuthenticatedConsentResponse(request);
                       } else if (status == ScaStatus.SCAMETHODSELECTED) {
                           links = buildLinksForScaMethodSelectedConsentResponse(request);
                       } else if (status == ScaStatus.FINALISED) {
                           links = buildLinksForFinalisedConsentResponse(request);
                       } else if (status == ScaStatus.PSUIDENTIFIED) {
                           links = buildLinksForPsuIdentifiedConsentResponse(request);
                       }

                       return links;
                   })
                   .orElse(null);
    }

    private Links buildLinksForPsuAuthenticatedConsentResponse(UpdateConsentPsuDataReq request) {
        Links links = new Links();
        links.setSelectAuthenticationMethod(buildPath("/v1/consents/{consentId}/authorisations/{authorisation-id}", request.getConsentId(), request.getAuthorizationId()));

        return links;
    }

    private Links buildLinksForScaMethodSelectedConsentResponse(UpdateConsentPsuDataReq request) {
        Links links = new Links();

        if (scaApproachResolver.resolveScaApproach() == ScaApproach.DECOUPLED) {
            links.setScaStatus(buildPath("/v1/consents/{consentId}/authorisations/{authorisation-id}", request.getConsentId(), request.getAuthorizationId()));
        } else {
            links.setAuthoriseTransaction(buildPath("/v1/consents/{consentId}/authorisations/{authorisation-id}", request.getConsentId(), request.getAuthorizationId()));
        }

        return links;
    }

    private Links buildLinksForFinalisedConsentResponse(UpdateConsentPsuDataReq request) {
        Links links = new Links();
        links.setScaStatus(buildPath("/v1/consents/{consentId}/authorisations/{authorisation-id}", request.getConsentId(), request.getAuthorizationId()));

        return links;
    }

    private Links buildLinksForPsuIdentifiedConsentResponse(UpdateConsentPsuDataReq request) {
        Links links = new Links();
        links.setStartAuthorisationWithPsuAuthentication(buildPath("/v1/consents/{consentId}/authorisations/{authorisation-id}", request.getConsentId(), request.getAuthorizationId()));

        return links;
    }
}
