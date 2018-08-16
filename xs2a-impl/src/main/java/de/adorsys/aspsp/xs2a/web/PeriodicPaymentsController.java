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
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.service.AccountReferenceValidationService;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/periodic-payments/{payment-product}")
@Api(value = "api/v1/periodic-payments/{payment-product}", tags = "PISP, Periodic Payments", description = "Orders for periodic payments")
public class PeriodicPaymentsController {
    private final static String TPP_INFO = "eyJuYXRpb25hbENvbXBldGVudEF1dGhvcml0eSI6ICJOYXRpb25hbCBjb21wZXRlbnQgYXV0aG9yaXR5IiwKICAgICJub2tSZWRpcmVjdFVyaSI6I" +
                                               "CJOb2sgcmVkaXJlY3QgVVJJIiwKICAgICJyZWRpcmVjdFVyaSI6ICJSZWRpcmVjdCBVUkkiLAogICAgInJlZ2lzdHJhdGlvbk51bWJlciI6IC" +
                                               "IxMjM0X3JlZ2lzdHJhdGlvbk51bWJlciIsCiAgICAidHBwTmFtZSI6ICJUcHAgY29tcGFueSIsCiAgICAidHBwUm9sZSI6ICJUcHAgcm9sZSIKICB9";
    private final PaymentService paymentService;
    private final ResponseMapper responseMapper;
    private final AccountReferenceValidationService referenceValidationService;

    @ApiOperation(value = "The TPP can submit a recurring payment initiation where the starting date, frequency and conditionally an end date is provided. Once authorised by the PSU, the payment then will be executed by the ASPSP, if possible, following this “standing order” as submitted by the TPP.", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 400, message = "Bad request")})
    @PostMapping
    @ApiImplicitParams({
        @ApiImplicitParam(name = "psu-ip-address", value = "192.168.0.26", required = true, paramType = "header"), //NOPMD value is correct according to specification
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "date", value = "Sun, 11 Aug 2019 15:02:37 GMT", required = true, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-redirect-uri", value = "http://example.com", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", defaultValue = TPP_INFO, dataType = "String", paramType = "header")})
    public ResponseEntity<PaymentInitialisationResponse> createPeriodicPayment(
        @ApiParam(name = "payment-product", value = "The addressed payment product endpoint for periodic payments e.g. for a periodic SEPA Credit Transfers", allowableValues = "sepa-credit-transfers, target-2-payments,instant-sepa-credit-transfers, cross-border-credit-transfers", required = true)
        @PathVariable("payment-product") String paymentProduct,
        @RequestHeader(name = "tpp-signature-certificate", required = false) String tppSignatureCertificate,
        @ApiParam(name = "Periodic Payment", value = "All data relevant for the corresponding payment product and necessary for execution of the standing order.", required = true)
        @RequestBody @Valid PeriodicPayment periodicPayment) {
        ResponseObject accountReferenceValidationResponse = referenceValidationService.validateAccountReferences(periodicPayment.getAccountReferences());
        ResponseObject<PaymentInitialisationResponse> response = accountReferenceValidationResponse.hasError()
                                                                     ? ResponseObject.<PaymentInitialisationResponse>builder().fail(accountReferenceValidationResponse.getError()).build()
                                                                     : paymentService.initiatePeriodicPayment(periodicPayment, tppSignatureCertificate, paymentProduct);
        return responseMapper.created(response);
    }
}
