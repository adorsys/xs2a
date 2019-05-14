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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.ConsentController;
import de.adorsys.psd2.xs2a.web.link.CreateConsentLinks;
import de.adorsys.psd2.xs2a.web.link.UpdateConsentLinks;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Aspect
@Component
public class ConsentAspect extends AbstractLinkAspect<ConsentController> {
    private final ScaApproachResolver scaApproachResolver;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final RedirectLinkBuilder redirectLinkBuilder;

    public ConsentAspect(ScaApproachResolver scaApproachResolver,
                         MessageService messageService,
                         AuthorisationMethodDecider authorisationMethodDecider,
                         RedirectLinkBuilder redirectLinkBuilder,
                         AspspProfileService aspspProfileService) {
        super(messageService, aspspProfileService);
        this.scaApproachResolver = scaApproachResolver;
        this.authorisationMethodDecider = authorisationMethodDecider;
        this.redirectLinkBuilder = redirectLinkBuilder;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ConsentService.createAccountConsentsWithResponse(..)) && args( request, psuData, explicitPreferred, tppRedirectUri)", returning = "result", argNames = "result,request,psuData,explicitPreferred,tppRedirectUri")
    public ResponseObject<CreateConsentResponse> invokeCreateAccountConsentAspect(ResponseObject<CreateConsentResponse> result, CreateConsentReq request, PsuIdData psuData, boolean explicitPreferred, TppRedirectUri tppRedirectUri) {
        if (!result.hasError()) {

            CreateConsentResponse body = result.getBody();
            boolean isExplicitMethod = authorisationMethodDecider.isExplicitMethod(explicitPreferred, body.isMultilevelScaRequired());
            body.setLinks(new CreateConsentLinks(getHttpUrl(), scaApproachResolver, body, redirectLinkBuilder,
                                                 isExplicitMethod,
                                                 psuData, getScaRedirectFlow()));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ConsentService.createAisAuthorisation(..)) && args( psuData,  consentId,  password)", returning = "result", argNames = "result, psuData,  consentId,  password")
    public ResponseObject<AuthorisationResponse> invokeCreateConsentPsuDataAspect(ResponseObject<AuthorisationResponse> result, PsuIdData psuData, String consentId, String password) {
        if (!result.hasError()) {
            if (result.getBody() instanceof UpdateConsentPsuDataResponse) {
                UpdateConsentPsuDataResponse body = (UpdateConsentPsuDataResponse) result.getBody();
                body.setLinks(buildLinksForUpdateConsentResponse(body));
            }
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ConsentService.updateConsentPsuData(..)) && args(updatePsuData)", returning = "result", argNames = "result,updatePsuData")
    public ResponseObject<UpdateConsentPsuDataResponse> invokeUpdateConsentPsuDataAspect(ResponseObject<UpdateConsentPsuDataResponse> result, UpdateConsentPsuDataReq updatePsuData) {
        if (!result.hasError()) {
            UpdateConsentPsuDataResponse body = result.getBody();
            body.setLinks(buildLinksForUpdateConsentResponse(body));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildLinksForUpdateConsentResponse(UpdateConsentPsuDataResponse response) {
        return Optional.ofNullable(response.getScaStatus())
                   .map(status -> new UpdateConsentLinks(getHttpUrl(), scaApproachResolver, response))
                   .orElse(null);
    }
}
