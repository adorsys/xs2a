/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentDataStatusResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/pis/common-payments")
@Api(value = "api/v1/pis/common-payments", tags = "PIS, Common Payment", description = "Provides access to common payment system for PIS")
public class PisCommonPaymentController {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    @PostMapping(path = "/")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CreatePisCommonPaymentResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<CreatePisCommonPaymentResponse> createCommonPayment(@RequestBody PisPaymentInfo request) {
        return pisCommonPaymentServiceEncrypted.createCommonPayment(request)
                   .map(c -> new ResponseEntity<>(c, HttpStatus.CREATED))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping(path = "/{payment-id}/status")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PisCommonPaymentDataStatusResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<PisCommonPaymentDataStatusResponse> getPisCommonPaymentStatusById(
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId) {
        return pisCommonPaymentServiceEncrypted.getPisCommonPaymentStatusById(paymentId)
                   .map(status -> new ResponseEntity<>(new PisCommonPaymentDataStatusResponse(status), HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping(path = "/{payment-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PisCommonPaymentResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<PisCommonPaymentResponse> getCommonPaymentById(
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId) {
        return pisCommonPaymentServiceEncrypted.getCommonPaymentById(paymentId)
                   .map(pc -> new ResponseEntity<>(pc, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping(path = "/{payment-id}/status/{status}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateCommonPaymentStatus(
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP by more values.", allowableValues = "RECEIVED,  REJECTED, VALID, REVOKED_BY_PSU,  EXPIRED,  TERMINATED_BY_TPP")
        @PathVariable("status") String status) {
        return pisCommonPaymentServiceEncrypted.updateCommonPaymentStatusById(paymentId, TransactionStatus.getByValue(status))
                   .map(updated -> new ResponseEntity<Void>(HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PostMapping(path = "/{payment-id}/authorizations")
    @ApiOperation(value = "Create authorization for given id.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CreatePisAuthorisationResponse> createAuthorization(
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId,
        @RequestBody PsuIdData psuData) {
        return pisCommonPaymentServiceEncrypted.createAuthorization(paymentId, CmsAuthorisationType.CREATED, psuData)
                   .map(authorization -> new ResponseEntity<>(authorization, HttpStatus.CREATED))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(path = "/{payment-id}/cancellation-authorisations")
    @ApiOperation(value = "Create payment authorization cancellation for given payment id.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CreatePisAuthorisationResponse> createAuthorizationCancellation(
        @ApiParam(name = "payment-id", value = "The payment identification of the related payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId,
        @RequestBody PsuIdData psuData) {
        return pisCommonPaymentServiceEncrypted.createAuthorization(paymentId, CmsAuthorisationType.CANCELLED, psuData)
                   .map(authorization -> new ResponseEntity<>(authorization, HttpStatus.CREATED))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/authorizations/{authorization-id}")
    @ApiOperation(value = "Update pis authorization.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<UpdatePisCommonPaymentPsuDataResponse> updateAuthorization(
        @ApiParam(name = "authorization-id", value = "The authorization identification assigned to the created authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorization-id") String authorizationId,
        @RequestBody UpdatePisCommonPaymentPsuDataRequest request) {
        return pisCommonPaymentServiceEncrypted.updatePisAuthorisation(authorizationId, request)
                   .map(updated -> new ResponseEntity<>(updated, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/authorizations/{authorization-id}")
    @ApiOperation(value = "Getting pis authorization.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<GetPisAuthorisationResponse> getAuthorization(
        @ApiParam(name = "authorization-id", value = "The authorization identification assigned to the created authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorization-id") String authorizationId) {
        return pisCommonPaymentServiceEncrypted.getPisAuthorisationById(authorizationId)
                   .map(resp -> new ResponseEntity<>(resp, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/{payment-id}/authorisations/{authorisation-id}/status")
    @ApiOperation(value = "Gets SCA status of pis consent authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<ScaStatus> getAuthorisationScaStatus(
        @ApiParam(name = "payment-id", value = "Identification of the payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId,
        @ApiParam(name = "authorisation-id", value = "The consent authorisation identification", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorisation-id") String authorisationId) {
        return pisCommonPaymentServiceEncrypted.getAuthorisationScaStatus(paymentId, authorisationId, CmsAuthorisationType.CREATED)
                   .map(resp -> new ResponseEntity<>(resp, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/cancellation-authorisations/{cancellation-id}")
    @ApiOperation(value = "Update pis cancellation authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<UpdatePisCommonPaymentPsuDataResponse> updateCancellationAuthorization(
        @ApiParam(name = "cancellation-id", value = "The cancellation authorisation identification assigned to the created cancellation authorisation.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("cancellation-id") String cancellationId,
        @RequestBody UpdatePisCommonPaymentPsuDataRequest request) {
        return pisCommonPaymentServiceEncrypted.updatePisCancellationAuthorisation(cancellationId, request)
                   .map(updated -> new ResponseEntity<>(updated, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/cancellation-authorisations/{cancellation-id}")
    @ApiOperation(value = "Getting pis cancellation authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<GetPisAuthorisationResponse> getAuthorizationCancellation(
        @ApiParam(name = "cancellation-id", value = "The cancellation authorisation identification assigned to the created cancellation authorisation.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("cancellation-id") String cancellationId) {
        return pisCommonPaymentServiceEncrypted.getPisCancellationAuthorisationById(cancellationId)
                   .map(resp -> new ResponseEntity<>(resp, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/{payment-id}/cancellation-authorisations")
    @ApiOperation(value = "Gets list of payment cancellation authorisation IDs by payment ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<String>> getAuthorisationCancellation(
        @ApiParam(name = "payment-id", value = "The payment identification of the related payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId) {
        return pisCommonPaymentServiceEncrypted.getAuthorisationsByPaymentId(paymentId, CmsAuthorisationType.CANCELLED)
                   .map(authorisation -> new ResponseEntity<>(authorisation, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/{payment-id}/cancellation-authorisations/{cancellation-id}/status")
    @ApiOperation(value = "Gets SCA status of pis consent cancellation authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<ScaStatus> getCancellationAuthorisationScaStatus(
        @ApiParam(name = "payment-id", value = "Identification of the payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId,
        @ApiParam(name = "cancellation-id", value = "Identification of the consent cancellation authorisation", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("cancellation-id") String authorisationId) {
        return pisCommonPaymentServiceEncrypted.getAuthorisationScaStatus(paymentId, authorisationId, CmsAuthorisationType.CANCELLED)
                   .map(resp -> new ResponseEntity<>(resp, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/{payment-id}/authorisations")
    @ApiOperation(value = "Gets list of payment authorisation IDs by payment ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<String>> getAuthorisation(
        @ApiParam(name = "payment-id", value = "The payment identification of the related payment.", example = "vOHy6fj2f5IgxHk-kTlhw6sZdTXbRE3bWsu2obq54beYOChP5NvRmfh06nrwumc2R01HygQenchEcdGOlU-U0A==_=_iR74m2PdNyE")
        @PathVariable("payment-id") String paymentId) {
        return pisCommonPaymentServiceEncrypted.getAuthorisationsByPaymentId(paymentId, CmsAuthorisationType.CREATED)
                   .map(authorisation -> new ResponseEntity<>(authorisation, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // TODO return correct error code in case was not found https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/408
    @PutMapping(path = "/{payment-id}/payment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updatePaymentCommonPaymentData(@RequestBody PisCommonPaymentRequest request, @PathVariable("payment-id") String paymentId) {
        pisCommonPaymentServiceEncrypted.updateCommonPayment(request, paymentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
