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
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConsentAspectService extends BaseAspectService<ConsentController> {

    private final ScaApproachResolver scaApproachResolver;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final RedirectLinkBuilder redirectLinkBuilder;
    private final RedirectIdService redirectIdService;
    private final RequestProviderService requestProviderService;

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

            LinkParameters linkParameters = LinkParameters.builder()
                .httpUrl(getHttpUrl())
                .isExplicitMethod(explicitMethod)
                .isSigningBasketModeActive(signingBasketModeActive)
                .isAuthorisationConfirmationRequestMandated(isAuthorisationConfirmationRequestMandated())
                .instanceId(requestProviderService.getInstanceId())
                .build();
            body.setLinks(new CreateConsentLinks(linkParameters, scaApproachResolver, body, redirectLinkBuilder,
                                                 redirectIdService,
                                                 getScaRedirectFlow()));
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
                LinkParameters linkParameters = LinkParameters.builder()
                    .httpUrl(getHttpUrl())
                    .isAuthorisationConfirmationRequestMandated(isAuthorisationConfirmationRequestMandated())
                    .instanceId(requestProviderService.getInstanceId())
                    .build();
                body.setLinks(new CreateAisAuthorisationLinks(linkParameters, body, scaApproachResolver, redirectLinkBuilder,
                                                              redirectIdService, getScaRedirectFlow()));
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

            LinkParameters linkParameters = LinkParameters.builder()
                .httpUrl(getHttpUrl())
                .isExplicitMethod(explicitMethod)
                .isSigningBasketModeActive(signingBasketModeActive)
                .isAuthorisationConfirmationRequestMandated(isAuthorisationConfirmationRequestMandated())
                .instanceId(requestProviderService.getInstanceId())
                .build();

            body.setLinks(new CreatePiisConsentLinks(linkParameters, scaApproachResolver, body, redirectLinkBuilder,
                                                     redirectIdService,
                                                     getScaRedirectFlow()));
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
                LinkParameters linkParameters = LinkParameters.builder()
                    .httpUrl(getHttpUrl())
                    .isAuthorisationConfirmationRequestMandated(isAuthorisationConfirmationRequestMandated())
                    .instanceId(requestProviderService.getInstanceId())
                    .build();
                body.setLinks(new CreatePiisAuthorisationLinks(linkParameters, body, scaApproachResolver, redirectLinkBuilder,
                                                               redirectIdService, getScaRedirectFlow()));
            }
        }
        return result;
    }
}
