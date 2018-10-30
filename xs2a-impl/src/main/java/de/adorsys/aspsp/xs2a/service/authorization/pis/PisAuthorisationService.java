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

package de.adorsys.aspsp.xs2a.service.authorization.pis;

import de.adorsys.aspsp.xs2a.config.factory.PisScaStageAuthorisationFactory;
import de.adorsys.aspsp.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.authorization.pis.stage.PisScaStage;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.aspsp.xs2a.config.factory.PisScaStageAuthorisationFactory.SERVICE_PREFIX;

@Service
@RequiredArgsConstructor
// TODO this class takes low-level communication to Consent-management-system. Should be migrated to consent-services package. All XS2A business-logic should be removed from here to XS2A services. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
public class PisAuthorisationService {
    private final PisConsentService pisConsentService;
    private final PisScaStageAuthorisationFactory pisScaStageAuthorisationFactory;
    private final Xs2aPisConsentMapper pisConsentMapper;

    /**
     * Sends a POST request to CMS to store created consent authorization
     *
     * @param paymentId String representation of identifier of stored consent
     * @return long representation of identifier of stored consent authorization
     */
    public CreatePisConsentAuthorisationResponse createPisConsentAuthorisation(String paymentId) {
        return pisConsentService.createAuthorization(paymentId, CmsAuthorisationType.CREATED)
                                                                       .orElse(null);
    }

    /**
     * Updates PIS consent authorization according to psu's sca methods
     *
     * @param request Provides transporting data when updating consent authorization
     * @return sca status
     */
    public Xs2aUpdatePisConsentPsuDataResponse updatePisConsentAuthorisation(UpdatePisConsentPsuDataRequest request) {
        GetPisConsentAuthorisationResponse response = pisConsentService.getPisConsentAuthorizationById(request.getAuthorizationId(), CmsAuthorisationType.CREATED)
                                                          .orElse(null);

        PisScaStage<UpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, Xs2aUpdatePisConsentPsuDataResponse> service = pisScaStageAuthorisationFactory.getService(SERVICE_PREFIX + response.getScaStatus().name());
        Xs2aUpdatePisConsentPsuDataResponse stageResponse = service.apply(request, response);

        if (!stageResponse.hasError()) {
            doUpdatePisConsentAuthorisation(pisConsentMapper.mapToSpiUpdateConsentPsuDataReq(request, stageResponse));
        }

        return stageResponse;
    }

    public void doUpdatePisConsentAuthorisation(UpdatePisConsentPsuDataRequest request) {
        pisConsentService.updateConsentAuthorization(request.getAuthorizationId(), request, CmsAuthorisationType.CREATED);
    }

    /**
     * Sends a POST request to CMS to store created consent authorization cancellation
     *
     * @param paymentId String representation of identifier of payment ID
     * @return long representation of identifier of stored consent authorization cancellation
     */
    public CreatePisConsentAuthorisationResponse createPisConsentAuthorisationCancellation(String paymentId) {
        return pisConsentService.createAuthorization(paymentId, CmsAuthorisationType.CANCELLED)
            .orElse(null);
    }

    /**
     * Sends a GET request to CMS to get authorization sub resources
     *
     * @param paymentId String representation of identifier of payment ID
     * @return long representation of identifier of stored consent authorization cancellation
     */
    public String getCancellationAuthorisationSubResources(String paymentId) {
        return pisConsentService.getAuthorisationByPaymentId(paymentId, CmsAuthorisationType.CANCELLED)
                   .orElse(null);
    }
}
