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

package de.adorsys.aspsp.xs2a.service.consent;

import de.adorsys.aspsp.xs2a.config.rest.consent.PisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.GetPisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreatePisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PisConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisConsentRemoteUrls remotePisConsentUrls;
    private final Xs2aPisConsentMapper pisConsentMapper;
    private final PaymentSpi paymentSpi;

    /**
     * Sends a POST request to CMS to store created PIS consent for single payment
     *
     * @param createPisConsentData Provides transporting data when creating an consent
     * @param paymentId            String identifier of the payment
     * @return CreatePisConsentResponse, which contains String identifier of the payment and String identifier of created PIS consent
     */
    public CreatePisConsentResponse createPisConsentForSinglePayment(CreatePisConsentData createPisConsentData, String paymentId) {
        PisConsentRequest cmsPisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForSinglePayment(createPisConsentData, paymentId);
        CreatePisConsentResponse createPisConsentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), cmsPisConsentRequest, CreatePisConsentResponse.class).getBody();

        return Optional.ofNullable(createPisConsentResponse)
                   .orElse(null);
    }

    /**
     * Sends a POST request to CMS to store created PIS consent for bulk payment
     *
     * @param createPisConsentData Provides transporting data when creating an consent
     * @return CreatePisConsentResponse, which contains String identifier of the payment and String identifier of created PIS consent
     */
    public CreatePisConsentResponse createPisConsentForBulkPayment(CreatePisConsentData createPisConsentData) {
        PisConsentRequest pisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForBulkPayment(createPisConsentData);
        CreatePisConsentResponse createPisConsentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), pisConsentRequest, CreatePisConsentResponse.class).getBody();

        return Optional.ofNullable(createPisConsentResponse)
                   .orElse(null);
    }

    /**
     * Sends a POST request to CMS to store created PIS consent for periodic payment
     *
     * @param createPisConsentData Provides transporting data when creating an consent
     * @param paymentId            String identifier of the payment
     * @return CreatePisConsentResponse, which contains String identifier of the payment and String identifier of created PIS consent
     */
    public CreatePisConsentResponse createPisConsentForPeriodicPayment(CreatePisConsentData createPisConsentData, String paymentId) {
        PisConsentRequest pisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForPeriodicPayment(createPisConsentData, paymentId);
        CreatePisConsentResponse createPisConsentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), pisConsentRequest, CreatePisConsentResponse.class).getBody();

        return Optional.ofNullable(createPisConsentResponse)
                   .orElse(null);
    }

    /**
     * Sends a POST request to CMS to store created consent authorization
     *
     * @param paymentId String representation of identifier of stored consent
     * @return long representation of identifier of stored consent authorization
     */
    public SpiCreatePisConsentAuthorizationResponse createPisConsentAuthorization(String paymentId) {
        return consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsentAuthorization(),
            null, SpiCreatePisConsentAuthorizationResponse.class, paymentId)
                   .getBody();
    }

    public UpdatePisConsentPsuDataResponse updatePisConsentAuthorization(UpdatePisConsentPsuDataRequest request) {
        GetPisConsentAuthorizationResponse authorizationResponse = consentRestTemplate.exchange(remotePisConsentUrls.getPisConsentAuthorizationById(), HttpMethod.GET, new HttpEntity<>(request), GetPisConsentAuthorizationResponse.class, request.getAuthorizationId())
                                                                       .getBody();
        if (CmsScaStatus.STARTED == authorizationResponse.getScaStatus()) {
            String token = paymentSpi.authorisePsu(request.getPsuId(), request.getPassword()).getPayload();
            if (StringUtils.isEmpty(token)) {
                return new UpdatePisConsentPsuDataResponse(CmsScaStatus.FAILED);
            }
            List<SpiScaMethod> spiScaMethods = paymentSpi.readAvailableScaMethod(token).getPayload();

            if (CollectionUtils.isEmpty(spiScaMethods)) {
                String executionPaymentId = paymentSpi.executePayment(authorizationResponse.getPaymentType(), authorizationResponse.getPayments()).getPayload();
                request.setExecutionPaymentId(executionPaymentId);
                request.setScaStatus(CmsScaStatus.FINALISED);

                return consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentAuthorization(), HttpMethod.PUT, new HttpEntity<>(request),
                    UpdatePisConsentPsuDataResponse.class, request.getAuthorizationId()).getBody();

            } else if (isSingleScaMethod(spiScaMethods)) {
                paymentSpi.generateConfirmationCode();
                request.setScaStatus(CmsScaStatus.SCAMETHODSELECTED);

                return consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentAuthorization(), HttpMethod.PUT, new HttpEntity<>(request),
                    UpdatePisConsentPsuDataResponse.class, request.getAuthorizationId()).getBody();

            } else if (isMultipleScaMethod(spiScaMethods)) {
                // TODO will be implemented in the next MR
                return null;
            }
        }
        return new UpdatePisConsentPsuDataResponse();
    }

    private boolean isSingleScaMethod(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.size() == 1;
    }

    private boolean isMultipleScaMethod(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.size() > 1;
    }
}
