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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.service.PisConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.config.factory.PisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.pis.stage.PisScaStage;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.config.factory.PisScaStageAuthorisationFactory.CANCELLATION_SERVICE_PREFIX;
import static de.adorsys.psd2.xs2a.config.factory.PisScaStageAuthorisationFactory.SERVICE_PREFIX;

@Service
@RequiredArgsConstructor
// TODO this class takes low-level communication to Consent-management-system. Should be migrated to consent-services package. All XS2A business-logic should be removed from here to XS2A services. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
public class PisAuthorisationService {
    private final PisConsentServiceEncrypted pisConsentService;
    private final PisScaStageAuthorisationFactory pisScaStageAuthorisationFactory;
    private final Xs2aPisConsentMapper pisConsentMapper;

    /**
     * Sends a POST request to CMS to store created consent authorization
     *
     * @param paymentId String representation of identifier of stored consent
     * @param psuData   PsuIdData container of authorisation data about PSU
     * @return long representation of identifier of stored consent authorization
     */
    public CreatePisConsentAuthorisationResponse createPisConsentAuthorisation(String paymentId, PsuIdData psuData) {
        return pisConsentService.createAuthorization(paymentId, CmsAuthorisationType.CREATED, psuData)
                   .orElse(null);
    }

    /**
     * Updates PIS consent authorization according to psu's sca methods
     *
     * @param request Provides transporting data when updating consent authorization
     * @return update consent authorization response, which contains payment id, authorization id, sca status, psu message and links
     */
    public Xs2aUpdatePisConsentPsuDataResponse updatePisConsentAuthorisation(Xs2aUpdatePisConsentPsuDataRequest request) {
        GetPisConsentAuthorisationResponse response = pisConsentService.getPisConsentAuthorisationById(request.getAuthorizationId())
                                                          .orElse(null);

        PisScaStage<Xs2aUpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, Xs2aUpdatePisConsentPsuDataResponse> service = pisScaStageAuthorisationFactory.getService(SERVICE_PREFIX + response.getScaStatus().name());
        Xs2aUpdatePisConsentPsuDataResponse stageResponse = service.apply(request, response);

        if (!stageResponse.hasError()) {
            doUpdatePisConsentAuthorisation(pisConsentMapper.mapToCmsUpdateConsentPsuDataReq(request, stageResponse));
        }

        return stageResponse;
    }

    /**
     * Updates PIS consent cancellation authorisation according to psu's sca methods
     *
     * @param request Provides transporting data when updating consent cancellation authorisation
     * @return update consent authorisation response, which contains payment id, authorisation id, sca status, psu message and links
     */
    public Xs2aUpdatePisConsentPsuDataResponse updatePisConsentCancellationAuthorisation(Xs2aUpdatePisConsentPsuDataRequest request) {
        GetPisConsentAuthorisationResponse response = pisConsentService.getPisConsentCancellationAuthorisationById(request.getAuthorizationId())
                                                          .orElse(null);

        PisScaStage<Xs2aUpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, Xs2aUpdatePisConsentPsuDataResponse> service = pisScaStageAuthorisationFactory.getService(CANCELLATION_SERVICE_PREFIX + response.getScaStatus().name());
        Xs2aUpdatePisConsentPsuDataResponse stageResponse = service.apply(request, response);

        if (!stageResponse.hasError()) {
            doUpdatePisConsentCancellationAuthorisation(pisConsentMapper.mapToCmsUpdateConsentPsuDataReq(request, stageResponse));
        }

        return stageResponse;
    }

    public void doUpdatePisConsentAuthorisation(UpdatePisConsentPsuDataRequest request) {
        pisConsentService.updateConsentAuthorisation(request.getAuthorizationId(), request);
    }

    public void doUpdatePisConsentCancellationAuthorisation(UpdatePisConsentPsuDataRequest request) {
        pisConsentService.updateConsentCancellationAuthorisation(request.getAuthorizationId(), request);
    }

    /**
     * Sends a POST request to CMS to store created consent authorization cancellation
     *
     * @param paymentId String representation of identifier of payment ID
     * @param psuData   PsuIdData container of authorisation data about PSU
     * @return long representation of identifier of stored consent authorization cancellation
     */
    public CreatePisConsentAuthorisationResponse createPisConsentAuthorisationCancellation(String paymentId, PsuIdData psuData) {
        return pisConsentService.createAuthorizationCancellation(paymentId, CmsAuthorisationType.CANCELLED, psuData)
                   .orElse(null);
    }

    /**
     * Sends a GET request to CMS to get cancellation authorisation sub resources
     *
     * @param paymentId String representation of identifier of payment ID
     * @return list of consent authorisation IDs
     */
    public Optional<List<String>> getCancellationAuthorisationSubResources(String paymentId) {
        return pisConsentService.getAuthorisationsByPaymentId(paymentId, CmsAuthorisationType.CANCELLED);
    }

    /**
     * Sends a GET request to CMS to get authorisation sub resources
     *
     * @param paymentId String representation of identifier of payment ID
     * @return list of consent authorisation IDs
     */
    public Optional<List<String>> getAuthorisationSubResources(String paymentId) {
        return pisConsentService.getAuthorisationsByPaymentId(paymentId, CmsAuthorisationType.CREATED);
    }

    /**
     * Gets SCA status of the authorisation
     *
     * @param paymentId       String representation of the payment identifier
     * @param authorisationId String representation of the authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId) {
        return pisConsentService.getAuthorisationScaStatus(paymentId, authorisationId, CmsAuthorisationType.CREATED);
    }

    /**
     * Gets SCA status of the cancellation authorisation
     *
     * @param paymentId      String representation of the payment identifier
     * @param cancellationId String representation of the cancellation authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String cancellationId) {
        return pisConsentService.getAuthorisationScaStatus(paymentId, cancellationId, CmsAuthorisationType.CANCELLED);
    }
}
