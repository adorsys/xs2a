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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentRequestParameters;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentModelMapper;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentModelMapperPsd2;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentModelMapperXs2a;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.api.PaymentApi;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.UUID;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

@RestController
@AllArgsConstructor
@Api(value = "v1", description = "Provides access to the payment initiation", tags = {"Payment Initiation Service (PIS)"})
public class PaymentController implements PaymentApi {
    private final PaymentService xs2aPaymentService;
    private final ResponseMapper responseMapper;
    private final PaymentModelMapperPsd2 paymentModelMapperPsd2;
    private final PaymentModelMapperXs2a paymentModelMapperXs2a;
    private final ConsentService consentService;
    private final ConsentModelMapper consentModelMapper;

    @Override
    public ResponseEntity<?> getPaymentInitiationStatus(String paymentService, String paymentId, UUID xRequestID, String digest,
                                                        String signature, byte[] tpPSignatureCertificate, String psUIPAddress,
                                                        Object psUIPPort, String psUAccept, String psUAcceptCharset,
                                                        String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent,
                                                        String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        ResponseObject<Xs2aTransactionStatus> response = PaymentType.getByValue(paymentService)
                                                             .map(pt -> xs2aPaymentService.getPaymentStatusById(paymentId, pt))
                                                             .orElseGet(() -> ResponseObject.<Xs2aTransactionStatus>builder()
                                                                                  .fail(new MessageError(FORMAT_ERROR)).build());

        return responseMapper.ok(response, PaymentModelMapperPsd2::mapToStatusResponse12);
    }

    @Override
    public ResponseEntity<?> getPaymentInformation(String paymentService, String paymentId, UUID xRequestID, String digest,
                                                   String signature, byte[] tpPSignatureCertificate, String psUIPAddress,
                                                   Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                   String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                                   UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<?> response = PaymentType.getByValue(paymentService)
                                         .map(pt -> xs2aPaymentService.getPaymentById(pt, paymentId))
                                         .orElseGet(() -> ResponseObject.builder()
                                                              .fail(new MessageError(FORMAT_ERROR)).build());

        return response.hasError()
                   ? responseMapper.ok(response)
                   : responseMapper.ok(ResponseObject.builder().body(paymentModelMapperPsd2.mapToGetPaymentResponse12(response.getBody(), PaymentType.getByValue(paymentService).get(), PaymentProduct.SCT)).build());
    }

    @Override
    public ResponseEntity<?> initiatePayment(Object body, String paymentService, String paymentProduct, UUID xRequestID,
                                             String psUIPAddress, String digest, String signature, byte[] tpPSignatureCertificate,
                                             String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType,
                                             String consentID, Boolean tpPRedirectPreferred, String tpPRedirectURI,
                                             String tpPNokRedirectURI, Boolean tpPExplicitAuthorisationPreferred,
                                             Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                             String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID,
                                             String psUGeoLocation) {
        PaymentRequestParameters requestParams = paymentModelMapperPsd2.mapToPaymentRequestParameters(paymentProduct, paymentService, tpPSignatureCertificate, tpPRedirectURI, tpPNokRedirectURI, BooleanUtils.isTrue(tpPExplicitAuthorisationPreferred));
        ResponseObject serviceResponse =
            xs2aPaymentService.createPayment(paymentModelMapperXs2a.mapToXs2aPayment(body, requestParams), requestParams, PSU_ID);

        return serviceResponse.hasError()
                   ? responseMapper.created(serviceResponse)
                   : responseMapper.created(ResponseObject
                                                .builder()
                                                .body(paymentModelMapperPsd2.mapToPaymentInitiationResponse12(serviceResponse.getBody(), requestParams))
                                                .build());
    }

    @Override
    public ResponseEntity<?> cancelPayment(String paymentService, String paymentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null; //TODO implement
    }

    @Override
    public ResponseEntity<?> getPaymentCancellationScaStatus(String paymentService, String paymentId, String cancellationId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null; //TODO implement
    }

    @Override
    public ResponseEntity<?> getPaymentInitiationAuthorisation(String paymentService, String paymentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null; //TODO implement
    }

    @Override
    public ResponseEntity<?> getPaymentInitiationCancellationAuthorisationInformation(String paymentService, String paymentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null; //TODO implement
    }

    @Override
    public ResponseEntity<?> getPaymentInitiationScaStatus(String paymentService, String paymentId, String authorisationId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null; //TODO implement
    }

    @Override
    public ResponseEntity<?> startPaymentAuthorisation(String paymentService, String paymentId, UUID xRequestID, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return responseMapper.created(consentService.createPisConsentAuthorization(paymentId, PaymentType.getByValue(paymentService).get()), consentModelMapper::mapToStartScaProcessResponse);
    }

    @Override
    public ResponseEntity<?> startPaymentInitiationCancellationAuthorisation(String paymentService, String paymentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null; //TODO implement
    }

    @Override
    public ResponseEntity<?> updatePaymentCancellationPsuData(String paymentService, String paymentId, String cancellationId, UUID xRequestID, Object body, String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null; //TODO implement
    }

    @Override
    public ResponseEntity<?> updatePaymentPsuData(String paymentService, String paymentId, String authorisationId, UUID xRequestID, Object body, String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return responseMapper.ok(consentService.updatePisConsentPsuData(consentModelMapper.mapToPisUpdatePsuData(PSU_ID, paymentId, authorisationId, paymentService, (HashMap) body)), consentModelMapper::mapToUpdatePsuAuthenticationResponse);
    }
}
