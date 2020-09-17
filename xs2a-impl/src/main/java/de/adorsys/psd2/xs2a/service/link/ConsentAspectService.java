/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aConfirmationOfFundsResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.ConsentController;
import de.adorsys.psd2.xs2a.web.link.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConsentAspectService extends BaseAspectService<ConsentController> {

    private ScaApproachResolver scaApproachResolver;
    private AuthorisationMethodDecider authorisationMethodDecider;
    private RedirectLinkBuilder redirectLinkBuilder;
    private RedirectIdService redirectIdService;
    private RequestProviderService requestProviderService;

    @Autowired
    public ConsentAspectService(ScaApproachResolver scaApproachResolver,
                                AuthorisationMethodDecider authorisationMethodDecider,
                                RedirectLinkBuilder redirectLinkBuilder,
                                AspspProfileServiceWrapper aspspProfileServiceWrapper,
                                RedirectIdService redirectIdService,
                                RequestProviderService requestProviderService) {
        super(aspspProfileServiceWrapper);
        this.scaApproachResolver = scaApproachResolver;
        this.authorisationMethodDecider = authorisationMethodDecider;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.redirectIdService = redirectIdService;
        this.requestProviderService = requestProviderService;
    }

    public ResponseObject<CreateConsentResponse> invokeCreateAccountConsentAspect(ResponseObject<CreateConsentResponse> result,
                                                                                  boolean explicitPreferred) {
        if (!result.hasError()) {

            CreateConsentResponse body = result.getBody();
            boolean explicitMethod = authorisationMethodDecider.isExplicitMethod(explicitPreferred, body.isMultilevelScaRequired());
            boolean signingBasketModeActive = authorisationMethodDecider.isSigningBasketModeActive(explicitPreferred);

            body.setLinks(new CreateConsentLinks(getHttpUrl(), scaApproachResolver, body, redirectLinkBuilder,
                                                 redirectIdService,
                                                 explicitMethod, signingBasketModeActive,
                                                 isAuthorisationConfirmationRequestMandated(),
                                                 requestProviderService.getInstanceId()));
        }
        return result;
    }

    public ResponseObject<AuthorisationResponse> invokeCreateConsentPsuDataAspect(ResponseObject<AuthorisationResponse> result) {
        if (!result.hasError()) {
            if (result.getBody() instanceof UpdateConsentPsuDataResponse) {
                UpdateConsentPsuDataResponse body = (UpdateConsentPsuDataResponse) result.getBody();
                body.setLinks(buildLinksForUpdateConsentResponse(body));
            } else if (result.getBody() instanceof CreateConsentAuthorizationResponse) {
                CreateConsentAuthorizationResponse body = (CreateConsentAuthorizationResponse) result.getBody();
                body.setLinks(new CreateAisAuthorisationLinks(getHttpUrl(), body, scaApproachResolver, redirectLinkBuilder,
                                                              redirectIdService, isAuthorisationConfirmationRequestMandated(),
                                                              requestProviderService.getInstanceId()));
            }
        }
        return result;
    }

    public ResponseObject<UpdateConsentPsuDataResponse> invokeUpdateConsentPsuDataAspect(ResponseObject<UpdateConsentPsuDataResponse> result) {
        if (!result.hasError()) {
            UpdateConsentPsuDataResponse body = result.getBody();
            body.setLinks(buildLinksForUpdateConsentResponse(body));
        }
        return result;
    }

    private Links buildLinksForUpdateConsentResponse(UpdateConsentPsuDataResponse response) {
        return Optional.ofNullable(response.getScaStatus())
                   .map(status -> new UpdateAisConsentLinksImpl(getHttpUrl(), scaApproachResolver, response))
                   .orElse(null);
    }

    public ResponseObject<UpdateConsentPsuDataResponse> invokeUpdatePiisConsentPsuDataAspect(ResponseObject<UpdateConsentPsuDataResponse> result) {
        if (!result.hasError()) {
            UpdateConsentPsuDataResponse body = result.getBody();
            body.setLinks(buildLinksForUpdatePiisConsentResponse(body));
        }
        return result;
    }

    private Links buildLinksForUpdatePiisConsentResponse(UpdateConsentPsuDataResponse response) {
        return Optional.ofNullable(response.getScaStatus())
                   .map(status -> new UpdatePiisConsentLinksImpl(getHttpUrl(), scaApproachResolver, response))
                   .orElse(null);
    }

    public ResponseObject<Xs2aConfirmationOfFundsResponse> createPiisConsentWithResponse(ResponseObject<Xs2aConfirmationOfFundsResponse> result, boolean explicitPreferred) {
        if (!result.hasError()) {

            Xs2aConfirmationOfFundsResponse body = result.getBody();
            boolean explicitMethod = authorisationMethodDecider.isExplicitMethod(explicitPreferred, body.isMultilevelScaRequired());
            boolean signingBasketModeActive = authorisationMethodDecider.isSigningBasketModeActive(explicitPreferred);

            body.setLinks(new CreatePiisConsentLinks(getHttpUrl(), scaApproachResolver, body, redirectLinkBuilder,
                                                     redirectIdService,
                                                     explicitMethod, signingBasketModeActive,
                                                     isAuthorisationConfirmationRequestMandated(),
                                                     requestProviderService.getInstanceId()));
        }
        return result;
    }

    public ResponseObject<AuthorisationResponse> invokeCreatePiisAuthorisationAspect(ResponseObject<AuthorisationResponse> result) {
        if (!result.hasError()) {
            if (result.getBody() instanceof UpdateConsentPsuDataResponse) {
                UpdateConsentPsuDataResponse body = (UpdateConsentPsuDataResponse) result.getBody();
                body.setLinks(buildLinksForUpdateConsentResponse(body));
            } else if (result.getBody() instanceof CreateConsentAuthorizationResponse) {
                CreateConsentAuthorizationResponse body = (CreateConsentAuthorizationResponse) result.getBody();
                body.setLinks(new CreatePiisAuthorisationLinks(getHttpUrl(), body, scaApproachResolver, redirectLinkBuilder,
                                                               redirectIdService, isAuthorisationConfirmationRequestMandated(),
                                                               requestProviderService.getInstanceId()));
            }
        }
        return result;
    }
}
