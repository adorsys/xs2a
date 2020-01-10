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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.service.PisAuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisCancellationAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
// TODO this class takes low-level communication to Consent-management-system. Should be migrated to consent-services package. All XS2A business-logic should be removed from here to XS2A services. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
public class PisAuthorisationService {
    private final PisAuthorisationServiceEncrypted pisAuthorisationServiceEncrypted;
    private final Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    private final ScaApproachResolver scaApproachResolver;
    private final RequestProviderService requestProviderService;
    private final TppRedirectUriMapper tppRedirectUriMapper;
    private final AuthorisationChainResponsibilityService authorisationChainResponsibilityService;

    /**
     * Sends a POST request to CMS to store created pis authorisation
     *
     * @param paymentId String representation of identifier of stored payment
     * @param psuData   PsuIdData container of authorisation data about PSU
     * @return a response object containing authorisation id
     */
    public CreatePisAuthorisationResponse createPisAuthorisation(String paymentId, PsuIdData psuData) {
        TppRedirectUri redirectURIs = tppRedirectUriMapper.mapToTppRedirectUri(requestProviderService.getTppRedirectURI(), requestProviderService.getTppNokRedirectURI());

        CreatePisAuthorisationRequest request = new CreatePisAuthorisationRequest(PaymentAuthorisationType.CREATED, psuData, scaApproachResolver.resolveScaApproach(), redirectURIs);
        CmsResponse<CreatePisAuthorisationResponse> cmsResponse = pisAuthorisationServiceEncrypted.createAuthorization(paymentId, request);

        if (cmsResponse.hasError()) {
            log.info("Payment-ID [{}]. Create PIS authorisation has failed: can't save authorisation to cms DB",
                     paymentId);
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
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisAuthorisation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, ScaApproach scaApproach) {
        String authorisationId = request.getAuthorisationId();
        CmsResponse<GetPisAuthorisationResponse> pisAuthorisationResponse = pisAuthorisationServiceEncrypted.getPisAuthorisationById(authorisationId);
        if (pisAuthorisationResponse.hasError()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_404)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_AUTHORISATION))
                                          .build();
            log.info("Payment-ID [{}], Authorisation-ID [{}]. Updating PIS authorisation PSU Data has failed: authorisation is not found by id.",
                     request.getPaymentId(), request.getAuthorisationId());
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), authorisationId, request.getPsuData());

        }

        GetPisAuthorisationResponse response = pisAuthorisationResponse.getPayload();

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
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisCancellationAuthorisation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, ScaApproach scaApproach) {
        String authorisationId = request.getAuthorisationId();
        CmsResponse<GetPisAuthorisationResponse> pisCancellationAuthorisationResponse = pisAuthorisationServiceEncrypted.getPisCancellationAuthorisationById(request.getAuthorisationId());
        if (pisCancellationAuthorisationResponse.hasError()) {
            log.warn("Payment-ID [{}], Authorisation-ID [{}]. Updating PIS Payment Cancellation authorisation PSU Data has failed: authorisation is not found by id.",
                     request.getPaymentId(), request.getAuthorisationId());

            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_404)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_CANC_AUTHORISATION))
                                          .build();
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), authorisationId, request.getPsuData());
        }

        GetPisAuthorisationResponse response = pisCancellationAuthorisationResponse.getPayload();

        return (Xs2aUpdatePisCommonPaymentPsuDataResponse) authorisationChainResponsibilityService.apply(
            new PisCancellationAuthorisationProcessorRequest(scaApproach,
                                                             response.getScaStatus(),
                                                             request,
                                                             response));
    }

    /**
     * Sends a POST request to CMS to store created pis authorisation cancellation
     *
     * @param paymentId String representation of identifier of payment ID
     * @param psuData   PsuIdData container of authorisation data about PSU
     * @return long representation of identifier of stored pis authorisation cancellation
     */
    public CreatePisAuthorisationResponse createPisAuthorisationCancellation(String paymentId, PsuIdData psuData) {
        TppRedirectUri redirectURIs = tppRedirectUriMapper.mapToTppRedirectUri(requestProviderService.getTppRedirectURI(), requestProviderService.getTppNokRedirectURI());

        CreatePisAuthorisationRequest request = new CreatePisAuthorisationRequest(PaymentAuthorisationType.CANCELLED, psuData, scaApproachResolver.resolveScaApproach(), redirectURIs);
        CmsResponse<CreatePisAuthorisationResponse> cmsResponse = pisAuthorisationServiceEncrypted.createAuthorizationCancellation(paymentId, request);

        if (cmsResponse.hasError()) {
            log.info("Payment-ID [{}]. Create PIS Payment Cancellation Authorisation has failed. Can't find Payment Data by id or Payment is Finalised.",
                     paymentId);
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
        CmsResponse<List<String>> cmsResponse = pisAuthorisationServiceEncrypted.getAuthorisationsByPaymentId(paymentId, PaymentAuthorisationType.CANCELLED);

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
        CmsResponse<List<String>> cmsResponse = pisAuthorisationServiceEncrypted.getAuthorisationsByPaymentId(paymentId, PaymentAuthorisationType.CREATED);

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
        CmsResponse<ScaStatus> cmsResponse = pisAuthorisationServiceEncrypted.getAuthorisationScaStatus(paymentId, authorisationId, PaymentAuthorisationType.CREATED);

        if (cmsResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(cmsResponse.getPayload());
    }

    /**
     * Gets SCA status of the cancellation authorisation
     *
     * @param paymentId      String representation of the payment identifier
     * @param cancellationId String representation of the cancellation authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String cancellationId) {
        CmsResponse<ScaStatus> cmsResponse = pisAuthorisationServiceEncrypted.getAuthorisationScaStatus(paymentId, cancellationId, PaymentAuthorisationType.CANCELLED);

        if (cmsResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(cmsResponse.getPayload());
    }

    /**
     * Gets SCA approach of the authorisation by its id and type
     *
     * @param authorisationId   String representation of the authorisation identifier
     * @param authorisationType Type of authorisation
     * @return SCA approach of the authorisation
     */
    public Optional<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId, PaymentAuthorisationType authorisationType) {
        CmsResponse<AuthorisationScaApproachResponse> cmsResponse = pisAuthorisationServiceEncrypted.getAuthorisationScaApproach(authorisationId, authorisationType);

        if (cmsResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(cmsResponse.getPayload());
    }

    public void updateAuthorisation(UpdateAuthorisationRequest request,
                                    AuthorisationProcessorResponse response) {
        if (response.hasError()) {
            log.warn("Payment-ID [{}], Authorisation-ID [{}]. Updating PIS authorisation PSU Data has failed. Error msg: [{}]",
                     request.getBusinessObjectId(), request.getAuthorisationId(), response.getErrorHolder());
        } else {
            doUpdatePisAuthorisation(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(response));
        }
    }

    public void updateCancellationAuthorisation(UpdateAuthorisationRequest request,
                                                AuthorisationProcessorResponse response) {
        if (response.hasError()) {
            log.warn("Payment-ID [{}], Authorisation-ID [{}]. Updating PIS Payment Cancellation authorisation PSU Data has failed:. Error msg: [{}]",
                     request.getBusinessObjectId(), request.getAuthorisationId(), response.getErrorHolder());
        } else {
            doUpdatePisCancellationAuthorisation(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(response));
        }
    }

    private void doUpdatePisAuthorisation(UpdatePisCommonPaymentPsuDataRequest request) {
        pisAuthorisationServiceEncrypted.updatePisAuthorisation(request.getAuthorizationId(), request);
    }

    private void doUpdatePisCancellationAuthorisation(UpdatePisCommonPaymentPsuDataRequest request) {
        pisAuthorisationServiceEncrypted.updatePisCancellationAuthorisation(request.getAuthorizationId(), request);
    }
}
