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

package de.adorsys.aspsp.xs2a.web12;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentModelMapper;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentModelMapper;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.api.PaymentApi;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

@RestController
@AllArgsConstructor
public class PaymentController12 implements PaymentApi {
    private final PaymentService xs2aPaymentService;
    private final ResponseMapper responseMapper;
    private final PaymentModelMapper paymentModelMapper;
    private final ConsentService consentService;

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

        return responseMapper.ok(response, PaymentModelMapper::mapToStatusResponse12);
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
                   : responseMapper.ok(ResponseObject.builder().body(paymentModelMapper.mapToGetPaymentResponse12(response.getBody(), PaymentType.getByValue(paymentService).get(), PaymentProduct.SCT)).build());
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
        Optional<PaymentProduct> product = PaymentProduct.getByCode(paymentProduct);
        Optional<PaymentType> paymentType = PaymentType.getByValue(paymentService);
        String cert = new String(Optional.ofNullable(tpPSignatureCertificate).orElse(new byte[]{}), StandardCharsets.UTF_8);
        ResponseObject serviceResponse =
            xs2aPaymentService.createPayment(paymentModelMapper.mapToXs2aPayment(body, paymentType.get(), product.get()), paymentType.get(), product.get(), cert);

        return serviceResponse.hasError()
                   ? responseMapper.created(serviceResponse)
                   : responseMapper.created(ResponseObject.builder().body(paymentModelMapper.mapToPaymentInitiationResponse12(serviceResponse.getBody(), paymentType.get(), product.get())).build());
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
        return responseMapper.ok(consentService.createPisConsentAuthorization(PSU_ID, paymentId), ConsentModelMapper::mapToStartScaProcessResponse);
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
        return null; //TODO implement
    }
}
