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
import de.adorsys.aspsp.xs2a.consent.api.CmsScaMethod;
import de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreatePisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus.FINALISED;
import static de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus.SCAMETHODSELECTED;

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
    public SpiCreatePisConsentAuthorizationResponse createPisConsentAuthorisation(String paymentId) {
        return consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsentAuthorisation(),
            null, SpiCreatePisConsentAuthorizationResponse.class, paymentId)
                   .getBody();
    }

    public UpdatePisConsentPsuDataResponse updatePisConsentAuthorisation(UpdatePisConsentPsuDataRequest request) {
        GetPisConsentAuthorisationResponse authorizationResponse = consentRestTemplate.exchange(remotePisConsentUrls.getPisConsentAuthorisationById(), HttpMethod.GET, new HttpEntity<>(request), GetPisConsentAuthorisationResponse.class, request.getAuthorizationId())
                                                                       .getBody();
        if (CmsScaStatus.STARTED == authorizationResponse.getScaStatus()) {
            SpiAuthorisationStatus authorisationStatus = paymentSpi.authorisePsu(request.getPsuId(), request.getPassword(), new AspspConsentData()).getPayload();
            if (SpiAuthorisationStatus.FAILURE == authorisationStatus) {
                return new UpdatePisConsentPsuDataResponse(CmsScaStatus.FAILED);
            }
            List<SpiScaMethod> spiScaMethods = paymentSpi.readAvailableScaMethod(new AspspConsentData()).getPayload();

            if (CollectionUtils.isEmpty(spiScaMethods)) {
                paymentSpi.executePayment(authorizationResponse.getPaymentType(), authorizationResponse.getPayments(), new AspspConsentData());
                request.setScaStatus(FINALISED);

                return consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentAuthorisation(), HttpMethod.PUT, new HttpEntity<>(request),
                    UpdatePisConsentPsuDataResponse.class, request.getAuthorizationId()).getBody();

            } else if (isSingleScaMethod(spiScaMethods)) {
                paymentSpi.performStrongUserAuthorisation(new AspspConsentData());
                request.setScaStatus(SCAMETHODSELECTED);
                request.setChosenScaMethod(CmsScaMethod.valueOf(spiScaMethods.get(0).name()));

                return consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentAuthorisation(), HttpMethod.PUT, new HttpEntity<>(request),
                    UpdatePisConsentPsuDataResponse.class, request.getAuthorizationId()).getBody();

            } else if (isMultipleScaMethods(spiScaMethods)) {
                // TODO will be implemented in the next MR https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/296
                return null;
            }
        } else if (SCAMETHODSELECTED == authorizationResponse.getScaStatus()) {
            paymentSpi.authorisePsu(authorizationResponse.getPsuId(), authorizationResponse.getPassword(), new AspspConsentData());
            paymentSpi.applyStrongUserAuthorisation(buildSpiPaymentConfirmation(request, authorizationResponse), new AspspConsentData());
            paymentSpi.executePayment(authorizationResponse.getPaymentType(), authorizationResponse.getPayments(), new AspspConsentData());
            request.setScaStatus(FINALISED);

            UpdatePisConsentPsuDataResponse response = consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentAuthorisation(), HttpMethod.PUT, new HttpEntity<>(request),
                UpdatePisConsentPsuDataResponse.class, request.getAuthorizationId()).getBody();
            response.setChosenScaMethod(response.getChosenScaMethod());
            return response;
        }
        return new UpdatePisConsentPsuDataResponse(null);
    }

    private SpiPaymentConfirmation buildSpiPaymentConfirmation(UpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse authorizationResponse) {
        SpiPaymentConfirmation paymentConfirmation = new SpiPaymentConfirmation();
        paymentConfirmation.setTanNumber(request.getPassword());
        paymentConfirmation.setPaymentId(request.getPaymentId());
        paymentConfirmation.setConsentId(authorizationResponse.getConsentId());
        return paymentConfirmation;
    }

    private boolean isSingleScaMethod(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.size() == 1;
    }

    private boolean isMultipleScaMethods(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.size() > 1;
    }
}
