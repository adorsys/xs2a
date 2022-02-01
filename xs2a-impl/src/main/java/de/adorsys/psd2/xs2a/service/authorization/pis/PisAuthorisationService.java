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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.PisAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.PisCancellationAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisCancellationAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PisAuthorisationService {
    private final AuthorisationServiceEncrypted authorisationServiceEncrypted;
    private final Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    private final RequestProviderService requestProviderService;
    private final TppRedirectUriMapper tppRedirectUriMapper;
    private final AuthorisationChainResponsibilityService authorisationChainResponsibilityService;

    /**
     * Sends a POST request to CMS to store created pis authorisation
     *
     * @param xs2aCreateAuthorisationRequest xs2a create authorisation request
     * @return a response object containing authorisation id
     */
    public CreateAuthorisationResponse createPisAuthorisation(Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest) {
        TppRedirectUri redirectURIs = tppRedirectUriMapper.mapToTppRedirectUri(requestProviderService.getTppRedirectURI(), requestProviderService.getTppNokRedirectURI());

        CreateAuthorisationRequest request = new CreateAuthorisationRequest(xs2aCreateAuthorisationRequest.getAuthorisationId(),
                                                                            xs2aCreateAuthorisationRequest.getPsuData(),
                                                                            xs2aCreateAuthorisationRequest.getScaApproach(),
                                                                            xs2aCreateAuthorisationRequest.getScaStatus(),
                                                                            redirectURIs);
        CmsResponse<CreateAuthorisationResponse> cmsResponse =
            authorisationServiceEncrypted.createAuthorisation(new PisAuthorisationParentHolder(xs2aCreateAuthorisationRequest.getPaymentId()), request);

        if (cmsResponse.hasError()) {
            log.info("Payment-ID [{}]. Create PIS authorisation has failed: can't save authorisation to cms DB",
                     xs2aCreateAuthorisationRequest.getPaymentId());
            return null;
        }

        return cmsResponse.getPayload();
    }

    /**
     * Updates PIS authorisation according to psu's sca methods with embedded and decoupled SCA approach
     *
     * @param request     Provides transporting data when updating pis authorisation
     * @param scaApproach current SCA approach, preferred by the server
     * @return update pis authorisation response, which contains payment id, authorisation id, sca status, psu message and links
     */
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisAuthorisation(PaymentAuthorisationParameters request, ScaApproach scaApproach) {
        String authorisationId = request.getAuthorisationId();
        CmsResponse<Authorisation> pisAuthorisationResponse = authorisationServiceEncrypted.getAuthorisationById(authorisationId);
        if (pisAuthorisationResponse.hasError()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_404)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_AUTHORISATION))
                                          .build();
            log.info("Payment-ID [{}], Authorisation-ID [{}]. Updating PIS authorisation PSU Data has failed: authorisation is not found by id.",
                     request.getPaymentId(), request.getAuthorisationId());
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), authorisationId, request.getPsuData());

        }

        Authorisation response = pisAuthorisationResponse.getPayload();

        return (Xs2aUpdatePisCommonPaymentPsuDataResponse) authorisationChainResponsibilityService.apply(
            new PisAuthorisationProcessorRequest(scaApproach,
                                                 response.getScaStatus(),
                                                 request,
                                                 response));
    }

    /**
     * Updates PIS cancellation authorisation according to psu's sca methods with embedded and decoupled SCA approach
     *
     * @param request     Provides transporting data when updating pis cancellation authorisation
     * @param scaApproach current SCA approach, preferred by the server
     * @return update pis authorisation response, which contains payment id, authorisation id, sca status, psu message and links
     */
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisCancellationAuthorisation(PaymentAuthorisationParameters request, ScaApproach scaApproach) {
        String authorisationId = request.getAuthorisationId();
        CmsResponse<Authorisation> pisCancellationAuthorisationResponse = authorisationServiceEncrypted.getAuthorisationById(request.getAuthorisationId());
        if (pisCancellationAuthorisationResponse.hasError()) {
            log.warn("Payment-ID [{}], Authorisation-ID [{}]. Updating PIS Payment Cancellation authorisation PSU Data has failed: authorisation is not found by id.",
                     request.getPaymentId(), request.getAuthorisationId());

            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_404)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_CANC_AUTHORISATION))
                                          .build();
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), authorisationId, request.getPsuData());
        }

        Authorisation response = pisCancellationAuthorisationResponse.getPayload();

        return (Xs2aUpdatePisCommonPaymentPsuDataResponse) authorisationChainResponsibilityService.apply(
            new PisCancellationAuthorisationProcessorRequest(scaApproach,
                                                             response.getScaStatus(),
                                                             request,
                                                             response));
    }

    /**
     * Sends a POST request to CMS to store created pis authorisation cancellation
     *
     * @param xs2aCreateAuthorisationRequest create Authorisation Request
     * @return long representation of identifier of stored pis authorisation cancellation
     */
    public CreateAuthorisationResponse createPisAuthorisationCancellation(Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest) {
        TppRedirectUri redirectURIs = tppRedirectUriMapper.mapToTppRedirectUri(requestProviderService.getTppRedirectURI(), requestProviderService.getTppNokRedirectURI());

        CreateAuthorisationRequest request = new CreateAuthorisationRequest(xs2aCreateAuthorisationRequest.getAuthorisationId(),
                                                                            xs2aCreateAuthorisationRequest.getPsuData(),
                                                                            xs2aCreateAuthorisationRequest.getScaApproach(),
                                                                            xs2aCreateAuthorisationRequest.getScaStatus(),
                                                                            redirectURIs);
        CmsResponse<CreateAuthorisationResponse> cmsResponse = authorisationServiceEncrypted.createAuthorisation(new PisCancellationAuthorisationParentHolder(xs2aCreateAuthorisationRequest.getPaymentId()), request);

        if (cmsResponse.hasError()) {
            log.info("Payment-ID [{}]. Create PIS Payment Cancellation Authorisation has failed. Can't find Payment Data by id or Payment is Finalised.",
                     xs2aCreateAuthorisationRequest.getPaymentId());
            return null;
        }

        return cmsResponse.getPayload();
    }

    /**
     * Sends a GET request to CMS to get cancellation authorisation sub resources
     *
     * @param paymentId String representation of identifier of payment ID
     * @return list of pis authorisation IDs
     */
    public Optional<List<String>> getCancellationAuthorisationSubResources(String paymentId) {
        CmsResponse<List<String>> cmsResponse = authorisationServiceEncrypted.getAuthorisationsByParentId(new PisCancellationAuthorisationParentHolder(paymentId));

        if (cmsResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(cmsResponse.getPayload());
    }

    /**
     * Sends a GET request to CMS to get authorisation sub resources
     *
     * @param paymentId String representation of identifier of payment ID
     * @return list of pis authorisation IDs
     */
    public Optional<List<String>> getAuthorisationSubResources(String paymentId) {
        CmsResponse<List<String>> cmsResponse = authorisationServiceEncrypted.getAuthorisationsByParentId(new PisAuthorisationParentHolder(paymentId));

        if (cmsResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(cmsResponse.getPayload());
    }

    /**
     * Gets SCA status of the authorisation
     *
     * @param paymentId       String representation of the payment identifier
     * @param authorisationId String representation of the authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId) {
        CmsResponse<ScaStatus> cmsResponse = authorisationServiceEncrypted.getAuthorisationScaStatus(authorisationId, new PisAuthorisationParentHolder(paymentId));

        if (cmsResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(cmsResponse.getPayload());
    }

    /**
     * Gets SCA status of the cancellation authorisation
     *
     * @param paymentId       String representation of the payment identifier
     * @param authorisationId String representation of the authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String authorisationId) {
        CmsResponse<ScaStatus> cmsResponse = authorisationServiceEncrypted.getAuthorisationScaStatus(authorisationId, new PisCancellationAuthorisationParentHolder(paymentId));

        if (cmsResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(cmsResponse.getPayload());
    }

    /**
     * Gets SCA approach of the authorisation by its id and type
     *
     * @param authorisationId String representation of the authorisation identifier
     * @return SCA approach of the authorisation
     */
    public Optional<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        CmsResponse<AuthorisationScaApproachResponse> cmsResponse = authorisationServiceEncrypted.getAuthorisationScaApproach(authorisationId);

        if (cmsResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(cmsResponse.getPayload());
    }

    public void updateAuthorisation(CommonAuthorisationParameters request,
                                    AuthorisationProcessorResponse response) {
        if (response.hasError()) {
            log.warn("Payment-ID [{}], Authorisation-ID [{}]. Updating PIS authorisation PSU Data has failed. Error msg: [{}]",
                     request.getBusinessObjectId(), request.getAuthorisationId(), response.getErrorHolder());
        } else {
            authorisationServiceEncrypted.updateAuthorisation(request.getAuthorisationId(),
                                                              pisCommonPaymentMapper.mapToUpdateAuthorisationRequest(response, AuthorisationType.PIS_CREATION));
        }
    }

    public void updateCancellationAuthorisation(CommonAuthorisationParameters request,
                                                AuthorisationProcessorResponse response) {
        if (response.hasError()) {
            log.warn("Payment-ID [{}], Authorisation-ID [{}]. Updating PIS Payment Cancellation authorisation PSU Data has failed:. Error msg: [{}]",
                     request.getBusinessObjectId(), request.getAuthorisationId(), response.getErrorHolder());
        } else {
            authorisationServiceEncrypted.updateAuthorisation(request.getAuthorisationId(),
                                                              pisCommonPaymentMapper.mapToUpdateAuthorisationRequest(response, AuthorisationType.PIS_CANCELLATION));
        }
    }
}
