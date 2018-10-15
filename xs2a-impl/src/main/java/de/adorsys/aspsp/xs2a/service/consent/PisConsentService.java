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
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.consent.api.pis.PisPaymentProduct;
import de.adorsys.psd2.consent.api.pis.PisPaymentType;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RCVD;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.*;

@Service
@RequiredArgsConstructor
public class PisConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisConsentRemoteUrls remotePisConsentUrls;
    private final Xs2aPisConsentMapper pisConsentMapper;
    private final AuthorisationMethodService authorisationMethodService;
    private final PisScaAuthorisationService pisScaAuthorisationService;

    /**
     * Creates PIS consent
     *
     * @param parameters Payment request parameters to get needed payment info
     * @return String consentId
     */
    // TODO rename method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/409
    public CreatePisConsentResponse createPisConsent(PaymentInitiationParameters parameters) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPaymentProduct(PisPaymentProduct.getByCode(parameters.getPaymentProduct().getCode()).orElse(null));
        request.setPaymentType(PisPaymentType.valueOf(parameters.getPaymentType().name()));
        return consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), request, CreatePisConsentResponse.class).getBody();
    }

    public void updatePaymentInPisConsent(SinglePayment singlePayment, PaymentInitiationParameters paymentInitiationParameters, String consentId) {
        PisConsentRequest pisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequest(singlePayment, paymentInitiationParameters.getPaymentProduct());
        consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentPayment(), HttpMethod.PUT, new HttpEntity<>(pisConsentRequest), Void.class, consentId);
    }

    /**
     * @deprecated since 1.8. Will be removed in 1.10
     * {@link de.adorsys.aspsp.xs2a.service.consent.PisConsentService#createPisConsent(PaymentInitiationParameters)}
     */
    @Deprecated
    public ResponseObject createPisConsent(Object payment, Object xs2aResponse, PaymentInitiationParameters requestParameters, TppInfo tppInfo) {
        CreatePisConsentData consentData = getPisConsentData(payment, xs2aResponse, tppInfo, requestParameters, new AspspConsentData());

        PisConsentRequest pisConsentRequest;
        if (requestParameters.getPaymentType() == PERIODIC) {
            pisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForPeriodicPayment(consentData);
        } else {
            pisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForBulkPayment(consentData);
        }
        CreatePisConsentResponse consentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), pisConsentRequest, CreatePisConsentResponse.class).getBody();

        return ResponseObject.builder().body(extendPaymentResponseFields(xs2aResponse, consentResponse.getConsentId(), requestParameters.getPaymentType(), requestParameters.isTppExplicitAuthorisationPreferred())).build();
    }

    private <T> Object extendPaymentResponseFields(T response, String consentId, PaymentType paymentType, boolean tppExplicitAuthorisationPreferred) {
        Object extendedResponse = EnumSet.of(SINGLE, PERIODIC).contains(paymentType)
                                      ? extendPaymentResponseFieldsSimple((PaymentInitialisationResponse) response, consentId, paymentType)
                                      : extendPaymentResponseFieldsBulk((List<PaymentInitialisationResponse>) response, consentId);

        return authorisationMethodService.isImplicitMethod(tppExplicitAuthorisationPreferred)
                   ? createPisAuthorisationForImplicitApproach(extendedResponse, paymentType)
                   : extendedResponse;
    }

    private List<PaymentInitialisationResponse> extendPaymentResponseFieldsBulk(List<PaymentInitialisationResponse> responses, String consentId) {
        return responses.stream()
                   .map(resp -> extendPaymentResponseFieldsSimple(resp, consentId, BULK))
                   .collect(Collectors.toList());
    }

    private PaymentInitialisationResponse extendPaymentResponseFieldsSimple(PaymentInitialisationResponse response, String consentId, PaymentType paymentType) {
        if (StringUtils.isNotBlank(consentId)) {
            response.setTransactionStatus(RCVD);
            response.setPisConsentId(consentId);
            response.setPaymentType(paymentType.name());
        }
        return response;
    }

    private Object createPisAuthorisationForImplicitApproach(Object response, PaymentType paymentType) {
        if (EnumSet.of(SINGLE, PERIODIC).contains(paymentType)) {
            PaymentInitialisationResponse resp = (PaymentInitialisationResponse) response;
            return pisScaAuthorisationService.createConsentAuthorisation(resp.getPaymentId(), paymentType)
                       .map(r -> extendResponseFieldsWithAuthData(r, resp))
                       .orElseGet(() -> resp);
        } else {
            List<PaymentInitialisationResponse> responses = (List<PaymentInitialisationResponse>) response;
            return pisScaAuthorisationService.createConsentAuthorisation(responses.get(0).getPaymentId(), paymentType)
                       .map(r -> responses.stream()
                                     .map(pr -> extendResponseFieldsWithAuthData(r, pr))
                                     .collect(Collectors.toList()))
                       .orElseGet(() -> responses);
        }
    }

    private PaymentInitialisationResponse extendResponseFieldsWithAuthData(Xsa2CreatePisConsentAuthorisationResponse authorisationResponse, PaymentInitialisationResponse response) {
        response.setAuthorizationId(authorisationResponse.getAuthorizationId());
        response.setScaStatus(authorisationResponse.getScaStatus());
        return response;
    }

    private CreatePisConsentData getPisConsentData(Object payment, Object xs2aResponse, TppInfo tppInfo, PaymentInitiationParameters requestParameters, AspspConsentData aspspConsentData) {
        CreatePisConsentData pisConsentData;
        if (requestParameters.getPaymentType() == SINGLE) {
            SinglePayment singlePayment = (SinglePayment) payment;
            PaymentInitialisationResponse response = (PaymentInitialisationResponse) xs2aResponse;
            singlePayment.setPaymentId(response.getPaymentId());
            pisConsentData = new CreatePisConsentData(singlePayment, tppInfo, requestParameters.getPaymentProduct().getCode(), aspspConsentData);
        } else if (requestParameters.getPaymentType() == PERIODIC) {
            PeriodicPayment periodicPayment = (PeriodicPayment) payment;
            PaymentInitialisationResponse response = (PaymentInitialisationResponse) xs2aResponse;
            periodicPayment.setPaymentId(response.getPaymentId());
            pisConsentData = new CreatePisConsentData(periodicPayment, tppInfo, requestParameters.getPaymentProduct().getCode(), aspspConsentData);
        } else {
            BulkPayment payments = (BulkPayment) payment;
            List<PaymentInitialisationResponse> responses = (List<PaymentInitialisationResponse>) xs2aResponse;

            Map<SinglePayment, PaymentInitialisationResponse> paymentMap = IntStream.range(0, payments.getPayments().size())
                                                                               .boxed()
                                                                               .collect(Collectors.toMap(payments.getPayments()::get, responses::get));
            paymentMap.forEach((k, v) -> k.setPaymentId(v.getPaymentId()));
            pisConsentData = new CreatePisConsentData(paymentMap, tppInfo, requestParameters.getPaymentProduct().getCode(), aspspConsentData);
        }
        return pisConsentData;
    }
}
