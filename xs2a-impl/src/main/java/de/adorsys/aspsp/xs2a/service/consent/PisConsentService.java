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
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RCVD;
import static de.adorsys.aspsp.xs2a.domain.consent.Xs2aAuthorisationStartType.IMPLICIT;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.*;

@Service
@RequiredArgsConstructor
public class PisConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisConsentRemoteUrls remotePisConsentUrls;
    private final Xs2aPisConsentMapper pisConsentMapper;
    private final AspspProfileServiceWrapper profileService;
    private final PisScaAuthorisationService pisScaAuthorisationService;

    public ResponseObject createPisConsent(Object payment, Object xs2aResponse, PaymentRequestParameters requestParameters, TppInfo tppInfo) {
        CreatePisConsentData consentData = getPisConsentData(payment, xs2aResponse, tppInfo, requestParameters, new AspspConsentData());

        PisConsentRequest pisConsentRequest;
        if (requestParameters.getPaymentType() == SINGLE) {
            pisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForSinglePayment(consentData);
        } else if (requestParameters.getPaymentType() == PERIODIC) {
            pisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForPeriodicPayment(consentData);
        } else {
            pisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForBulkPayment(consentData);
        }
        CreatePisConsentResponse consentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), pisConsentRequest, CreatePisConsentResponse.class).getBody();

        return ResponseObject.builder().body(extendPaymentResponseFields(xs2aResponse, consentResponse.getConsentId(), requestParameters.getPaymentType())).build();
    }

    private <T> Object extendPaymentResponseFields(T response, String consentId, PaymentType paymentType) {
        Object extendedResponse = EnumSet.of(SINGLE, PERIODIC).contains(paymentType)
                                      ? extendPaymentResponseFieldsSimple((PaymentInitialisationResponse) response, consentId, paymentType)
                                      : extendPaymentResponseFieldsBulk((List<PaymentInitialisationResponse>) response, consentId);

        return IMPLICIT == profileService.getAuthorisationStartType()
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

    private <T> Object createPisAuthorisationForImplicitApproach(T response, PaymentType paymentType) {
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

    private CreatePisConsentData getPisConsentData(Object payment, Object xs2aResponse, TppInfo tppInfo, PaymentRequestParameters requestParameters, AspspConsentData aspspConsentData) {
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
