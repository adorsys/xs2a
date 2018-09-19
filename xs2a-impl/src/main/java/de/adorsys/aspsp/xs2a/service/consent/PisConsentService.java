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
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
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

import static de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus.*;

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

    /**
     * Updates PIS consent authorization according to psu's sca methods
     *
     * @param request Provides transporting data when updating consent authorization
     * @return sca status
     */
    public UpdatePisConsentPsuDataResponse updatePisConsentAuthorisation(UpdatePisConsentPsuDataRequest request) {
        GetPisConsentAuthorisationResponse pisConsentAuthorisation = consentRestTemplate.exchange(remotePisConsentUrls.getPisConsentAuthorisationById(), HttpMethod.GET, new HttpEntity<>(request), GetPisConsentAuthorisationResponse.class, request.getAuthorizationId())
                                                                         .getBody();

        if (STARTED == pisConsentAuthorisation.getScaStatus()) {
            SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = paymentSpi.authorisePsu(request.getPsuId(), request.getPassword());

            if (SpiAuthorisationStatus.FAILURE == authorisationStatusSpiResponse.getPayload()) {
                return new UpdatePisConsentPsuDataResponse(FAILED);
            }
            request.setAspspData(authorisationStatusSpiResponse.getAspspConsentData().getBody());
            List<SpiScaMethod> spiScaMethods = paymentSpi.readAvailableScaMethod(request.getPsuId(), authorisationStatusSpiResponse.getAspspConsentData()).getPayload();

            if (CollectionUtils.isEmpty(spiScaMethods)) {
                request.setScaStatus(FINALISED);
                paymentSpi.executePayment(pisConsentAuthorisation.getPaymentType(), pisConsentAuthorisation.getPayments(), authorisationStatusSpiResponse.getAspspConsentData());
                return doUpdatePisConsentAuthorisation(request);

            } else if (isSingleScaMethod(spiScaMethods)) {
                request.setScaStatus(SCAMETHODSELECTED);
                request.setAuthenticationMethodId(spiScaMethods.get(0).name());
                paymentSpi.performStrongUserAuthorisation(request.getPsuId(), new AspspConsentData());
                return doUpdatePisConsentAuthorisation(request);

            } else if (isMultipleScaMethods(spiScaMethods)) {
                request.setScaStatus(PSUAUTHENTICATED);
                UpdatePisConsentPsuDataResponse response = doUpdatePisConsentAuthorisation(request);
                response.setAvailableScaMethods(pisConsentMapper.mapToCmsScaMethods(spiScaMethods));
                return response;

            }
        } else if (SCAMETHODSELECTED == pisConsentAuthorisation.getScaStatus()) {
            request.setScaStatus(FINALISED);
            paymentSpi.applyStrongUserAuthorisation(buildSpiPaymentConfirmation(request, pisConsentAuthorisation.getConsentId()), new AspspConsentData());
            paymentSpi.executePayment(pisConsentAuthorisation.getPaymentType(), pisConsentAuthorisation.getPayments(), new AspspConsentData());
            return doUpdatePisConsentAuthorisation(request);

        } else if (PSUAUTHENTICATED == pisConsentAuthorisation.getScaStatus()) {
            request.setScaStatus(SCAMETHODSELECTED);
            paymentSpi.performStrongUserAuthorisation(request.getPsuId(), new AspspConsentData());
            return doUpdatePisConsentAuthorisation(request);
        }
        return new UpdatePisConsentPsuDataResponse(null);
    }

    private UpdatePisConsentPsuDataResponse doUpdatePisConsentAuthorisation(UpdatePisConsentPsuDataRequest request) {
        return consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentAuthorisation(), HttpMethod.PUT, new HttpEntity<>(request),
            UpdatePisConsentPsuDataResponse.class, request.getAuthorizationId()).getBody();
    }

    private SpiPaymentConfirmation buildSpiPaymentConfirmation(UpdatePisConsentPsuDataRequest request, String consentId) {
        SpiPaymentConfirmation paymentConfirmation = new SpiPaymentConfirmation();
        paymentConfirmation.setTanNumber(request.getPassword());
        paymentConfirmation.setPaymentId(request.getPaymentId());
        paymentConfirmation.setConsentId(consentId);
        paymentConfirmation.setPsuId(request.getPsuId());
        return paymentConfirmation;
    }

    private boolean isSingleScaMethod(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.size() == 1;
    }

    private boolean isMultipleScaMethods(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.size() > 1;
    }
}
