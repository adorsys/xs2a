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
import de.adorsys.aspsp.xs2a.domain.TransactionStatusResponse;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.service.AccountReferenceValidationService;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/payments/{payment-product}")
@Api(value = "api/v1/payments/{payment-product}", tags = "PISP, Payments", description = "Provides access to the PIS")
public class PaymentInitiationController {
    private final ResponseMapper responseMapper;
    private final PaymentService paymentService;
    private final AccountReferenceValidationService referenceValidationService;

    @ApiOperation(value = "Initialises a new payment ", notes = "debtor account, creditor accout, creditor name, remittance information unstructured")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 400, message = "Bad request")})
    @PostMapping
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "psu-ip-address", value = "192.168.8.78", required = true, dataType = "String", paramType = "header"), //NOPMD value is correct according to specification
        @ApiImplicitParam(name = "date", value = "Sun, 11 Aug 2019 15:02:37 GMT", required = true, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-id", value = "12312324", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-id-type", value = "Type of the PSU-ID", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-corporate-id", value = "Might be mandated in the ASPSP’s documentation", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-corporate-id-type", value = "Might be mandated in the ASPSP’s documentation", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-consent-id", value = "This data element may be contained, if the payment initiation transaction is part of a session, i.e. combined AIS/PIS service", required = false, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-agent", value = "The forwarded Agent header field of the http request between PSU and TPP.", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-geo-location", value = "GEO:52.506931,13.1445588", dataType = "Geo Location", paramType = "header"),
        @ApiImplicitParam(name = "tpp-redirect-uri", value = "Uri of TPP", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "A signature of the request by TPP", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-qwac-certificate", value = "qwac certificate", required = true, dataType = "String", paramType = "header")})
    public ResponseEntity<PaymentInitialisationResponse> createPaymentInitiation(
        @ApiParam(name = "payment-product", value = "The addressed payment product endpoint for payments e.g. for a SEPA Credit Transfers", allowableValues = "sepa-credit-transfers, target-2-payments,instant-sepa-credit-transfers, cross-border-credit-transfers")
        @PathVariable("payment-product") String paymentProduct,
        @RequestHeader(name = "tpp-signature-certificate", required = false) String tppSignatureCertificate,
        @ApiParam(name = "singlePayment", required = true)
        @RequestBody @Valid SinglePayment singlePayment) {
        ResponseObject accountReferenceValidationResponse = referenceValidationService.validateAccountReferences(singlePayment.getAccountReferences());
        ResponseObject<PaymentInitialisationResponse> response = accountReferenceValidationResponse.hasError()
                                                                     ? ResponseObject.<PaymentInitialisationResponse>builder().fail(accountReferenceValidationResponse.getError()).build()
                                                                     : paymentService.createPaymentInitiation(singlePayment, tppSignatureCertificate, paymentProduct);
        return responseMapper.created(response);
    }

    @ApiOperation(value = "Get information  about the status of a payment initialisation ")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Xs2aTransactionStatus.class),
        @ApiResponse(code = 403, message = "Not found")})
    @GetMapping(path = "/{paymentId}/status")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "date", value = "Sun, 11 Aug 2019 15:02:37 GMT", required = true, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-qwac-certificate", value = "qwac certificate", dataType = "String", paramType = "header")})
    public ResponseEntity<TransactionStatusResponse> getPaymentInitiationStatusById(
        @ApiParam(name = "payment-product", value = "The addressed payment product endpoint for payments e.g. for a SEPA Credit Transfers", allowableValues = "sepa-credit-transfers, target-2-payments,instant-sepa-credit-transfers, cross-border-credit-transfers")
        @PathVariable("payment-product") String paymentProduct,
        @ApiParam(name = "paymentId", value = "529e0507-7539-4a65-9b74-bdf87061e99b")
        @PathVariable("paymentId") String paymentId) {
        ResponseObject<Xs2aTransactionStatus> status = paymentService.getPaymentStatusById(paymentId, PaymentType.SINGLE);

        return responseMapper.ok(status.hasError()
                                     ? ResponseObject.<TransactionStatusResponse>builder().fail(status.getError()).build()
                                     : ResponseObject.<TransactionStatusResponse>builder().body(new TransactionStatusResponse(status.getBody())).build());
    }
}
